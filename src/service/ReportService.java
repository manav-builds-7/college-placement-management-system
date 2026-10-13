package service;

import dao.StatisticsDAO;
import interfaces.Reportable;
import model.Application;
import model.Company;
import model.Interview;
import model.Job;
import model.Placement;
import model.Student;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Builds every placement report as text. Nothing is printed or saved here:
 * the UI prints the returned text, and ReportFileHandler saves it.
 */
public class ReportService {

    private static final int WIDTH = 72;
    private static final String DOUBLE_LINE = "=".repeat(WIDTH);
    private static final String SINGLE_LINE = "-".repeat(WIDTH);

    private final StatisticsDAO statisticsDAO;
    private final StudentService studentService;
    private final CompanyService companyService;
    private final JobService jobService;
    private final PlacementDriveService driveService;
    private final ApplicationService applicationService;
    private final InterviewService interviewService;
    private final PlacementService placementService;

    public ReportService(StatisticsDAO statisticsDAO, StudentService studentService,
                         CompanyService companyService, JobService jobService,
                         PlacementDriveService driveService, ApplicationService applicationService,
                         InterviewService interviewService, PlacementService placementService) {
        this.statisticsDAO = statisticsDAO;
        this.studentService = studentService;
        this.companyService = companyService;
        this.jobService = jobService;
        this.driveService = driveService;
        this.applicationService = applicationService;
        this.interviewService = interviewService;
        this.placementService = placementService;
    }

    // ---------- Listing reports ----------

    public String studentReport() throws SQLException {
        StringBuilder sb = new StringBuilder();
        appendTitle(sb, "STUDENT REPORT");
        sb.append(String.format(Locale.US, "%-22s %-8s %6s %9s  %-14s%n",
                "Name", "Dept", "CGPA", "Backlogs", "Status"));
        sb.append(SINGLE_LINE).append("\n");

        List<Student> students = studentService.getAllStudents();
        for (Student student : students) {
            sb.append(String.format(Locale.US, "%-22s %-8s %6.2f %9d  %-14s%n",
                    student.getName(), student.getDepartment(), student.getCgpa(),
                    student.getBacklogs(), student.getPlacementStatus()));
        }
        sb.append(SINGLE_LINE).append("\n");
        sb.append(row("Total students", students.size()));
        return sb.toString();
    }

    public String companyReport() throws SQLException {
        StringBuilder sb = new StringBuilder();
        appendTitle(sb, "COMPANY REPORT");
        List<Company> companies = companyService.getAllCompanies();
        for (Company company : companies) {
            appendReportable(sb, company);
        }
        sb.append(row("Total companies", companies.size()));
        return sb.toString();
    }

    public String jobReport() throws SQLException {
        StringBuilder sb = new StringBuilder();
        appendTitle(sb, "JOB REPORT");
        Map<Integer, String> companyNames = companyNamesById();
        List<Job> jobs = jobService.getAllJobs();
        for (Job job : jobs) {
            sb.append("Company    : ").append(companyNames.get(job.getCompanyId())).append("\n");
            appendReportable(sb, job);
        }
        sb.append(SINGLE_LINE).append("\n");
        sb.append(row("Total jobs", jobs.size()));
        return sb.toString();
    }

    private void appendReportable(StringBuilder sb, Reportable reportable) {
        sb.append(reportable.generateReport()).append("\n").append(SINGLE_LINE).append("\n");
    }

    public String applicationReport() throws SQLException {
        StringBuilder sb = new StringBuilder();
        appendTitle(sb, "APPLICATION REPORT");
        Map<Integer, String> studentNames = studentNamesById();
        Map<Integer, String> jobTitles = jobTitlesById();
        List<Application> applications = applicationService.getAllApplications();
        for (Application application : applications) {
            sb.append(String.format(Locale.US, "%-22s %-26s %-10s %s%n",
                    studentNames.get(application.getStudentId()),
                    jobTitles.get(application.getJobId()),
                    application.getApplicationDate(), application.getStatus()));
        }
        sb.append(SINGLE_LINE).append("\n");
        sb.append(row("Total applications", applications.size()));
        return sb.toString();
    }

    public String interviewReport() throws SQLException {
        StringBuilder sb = new StringBuilder();
        appendTitle(sb, "INTERVIEW REPORT");
        List<Interview> interviews = interviewService.getAllInterviews();
        for (Interview interview : interviews) {
            sb.append(String.format(Locale.US, "Application %-5d %-10s %s %s  %-8s  %s%n",
                    interview.getApplicationId(), interview.getRound(), interview.getDate(),
                    interview.getTime(), interview.getResult(), interview.getInterviewer()));
        }
        sb.append(SINGLE_LINE).append("\n");
        sb.append(row("Total interviews", interviews.size()));
        return sb.toString();
    }

    public String placementReport() throws SQLException {
        StringBuilder sb = new StringBuilder();
        appendTitle(sb, "PLACEMENT REPORT");
        Map<Integer, String> studentNames = studentNamesById();
        Map<Integer, String> companyNames = companyNamesById();
        List<Placement> placements = placementService.getAllPlacements();
        for (Placement placement : placements) {
            sb.append(String.format(Locale.US, "%-22s %-20s %6.2f LPA  %-10s %s%n",
                    studentNames.get(placement.getStudentId()),
                    companyNames.get(placement.getCompanyId()),
                    placement.getCtc(), placement.getPlacementDate(), placement.getOfferStatus()));
        }
        sb.append(SINGLE_LINE).append("\n");
        sb.append(row("Total placements", placements.size()));
        return sb.toString();
    }

    // ---------- Grouped and statistical reports ----------

    public String departmentReport() throws SQLException {
        StringBuilder sb = new StringBuilder();
        appendTitle(sb, "DEPARTMENT-WISE PLACEMENT");
        sb.append(String.format(Locale.US, "%-12s %9s %8s %10s%n", "Department", "Students", "Placed", "Rate"));
        sb.append(SINGLE_LINE).append("\n");

        Map<String, Integer> totals = statisticsDAO.getStudentCountByDepartment();
        Map<String, Integer> placed = statisticsDAO.getPlacedCountByDepartment();
        for (Map.Entry<String, Integer> entry : totals.entrySet()) {
            String department = entry.getKey();
            int total = entry.getValue();
            int placedCount = placed.getOrDefault(department, 0);
            sb.append(String.format(Locale.US, "%-12s %9d %8d %9.2f%%%n",
                    department, total, placedCount, percentage(placedCount, total)));
        }
        return sb.toString();
    }

    public String companyWiseReport() throws SQLException {
        StringBuilder sb = new StringBuilder();
        appendTitle(sb, "COMPANY-WISE PLACEMENT");
        sb.append(String.format(Locale.US, "%-28s %8s%n", "Company", "Placed"));
        sb.append(SINGLE_LINE).append("\n");

        Map<String, Integer> placed = statisticsDAO.getPlacedCountByCompany();
        for (Company company : companyService.getAllCompanies()) {
            sb.append(String.format(Locale.US, "%-28s %8d%n",
                    company.getCompanyName(), placed.getOrDefault(company.getCompanyName(), 0)));
        }
        return sb.toString();
    }

    public String packageStatistics() throws SQLException {
        StringBuilder sb = new StringBuilder();
        appendTitle(sb, "PACKAGE STATISTICS");
        if (statisticsDAO.countPlacedStudents() == 0) {
            sb.append("No placements yet.\n");
            return sb.toString();
        }
        sb.append(row("Highest package", lpa(statisticsDAO.getHighestPackage())));
        sb.append(row("Lowest package", lpa(statisticsDAO.getLowestPackage())));
        sb.append(row("Average package", lpa(statisticsDAO.getAveragePackage())));
        return sb.toString();
    }

    /** The main report from the spec, with the summary block printed in the terminal. */
    public String completePlacementReport() throws SQLException {
        int totalStudents = statisticsDAO.countStudents();
        int placed = statisticsDAO.countPlacedStudents();

        StringBuilder sb = new StringBuilder();
        sb.append(DOUBLE_LINE).append("\n");
        sb.append(center("COLLEGE PLACEMENT REPORT")).append("\n");
        sb.append(DOUBLE_LINE).append("\n");
        sb.append(row("Academic Year", academicYear()));
        sb.append(row("Total Students", totalStudents));
        sb.append(row("Eligible Students", countEligibleStudents()));
        sb.append(row("Total Companies", statisticsDAO.countCompanies()));
        sb.append(row("Total Jobs", statisticsDAO.countJobs()));
        sb.append(row("Total Placement Drives", driveService.getAllDrives().size()));
        sb.append(row("Total Applications", statisticsDAO.countApplications()));
        sb.append(row("Total Shortlisted", statisticsDAO.countShortlisted()));
        sb.append(row("Total Students Placed", placed));
        sb.append(row("Placement Rate", String.format(Locale.US, "%.2f%%", percentage(placed, totalStudents))));

        if (placed > 0) {
            sb.append(row("Highest Package", lpa(statisticsDAO.getHighestPackage())));
            sb.append(row("Lowest Package", lpa(statisticsDAO.getLowestPackage())));
            sb.append(row("Average Package", lpa(statisticsDAO.getAveragePackage())));
        } else {
            sb.append(row("Package Details", "No placements yet"));
        }
        sb.append(DOUBLE_LINE).append("\n");
        return sb.toString();
    }

    // ---------- Row formatting (overloaded on purpose) ----------

    /** One label-and-value line with a text value, for example "Academic Year : 2026-27". */
    private String row(String label, String value) {
        return String.format(Locale.US, "%-24s: %s%n", label, value);
    }

    /** One label-and-value line with a whole-number value. */
    private String row(String label, int value) {
        return row(label, String.valueOf(value));
    }

    // ---------- Private helpers ----------

    private void appendTitle(StringBuilder sb, String title) {
        sb.append(DOUBLE_LINE).append("\n");
        sb.append(center(title)).append("\n");
        sb.append(DOUBLE_LINE).append("\n");
    }

    private String center(String text) {
        int padding = Math.max(0, (WIDTH - text.length()) / 2);
        return " ".repeat(padding) + text;
    }

    /** The academic year starts in June, so June 2026 to May 2027 is "2026-27". */
    private String academicYear() {
        LocalDate today = LocalDate.now();
        int startYear = today.getMonthValue() >= 6 ? today.getYear() : today.getYear() - 1;
        int endYear = (startYear + 1) % 100;
        return startYear + "-" + String.format("%02d", endYear);
    }

    /**
     * Counts students who are eligible for at least one job that is still open.
     * Each student is counted once, even if several jobs match.
     */
    private int countEligibleStudents() throws SQLException {
        List<Job> openJobs = jobService.getOpenJobs();
        int count = 0;
        for (Student student : studentService.getAllStudents()) {
            for (Job job : openJobs) {
                if (applicationService.isEligible(student, job)) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    private double percentage(int part, int whole) {
        return whole == 0 ? 0.0 : part * 100.0 / whole;
    }

    private String lpa(double value) {
        return String.format(Locale.US, "%.2f LPA", value);
    }

    private Map<Integer, String> studentNamesById() throws SQLException {
        Map<Integer, String> names = new HashMap<>();
        for (Student student : studentService.getAllStudents()) {
            names.put(student.getStudentId(), student.getName());
        }
        return names;
    }

    private Map<Integer, String> companyNamesById() throws SQLException {
        Map<Integer, String> names = new HashMap<>();
        for (Company company : companyService.getAllCompanies()) {
            names.put(company.getCompanyId(), company.getCompanyName());
        }
        return names;
    }

    private Map<Integer, String> jobTitlesById() throws SQLException {
        Map<Integer, String> titles = new HashMap<>();
        for (Job job : jobService.getAllJobs()) {
            titles.put(job.getJobId(), job.getTitle());
        }
        return titles;
    }
}