package ui;

import exception.CompanyNotFoundException;
import exception.DuplicateApplicationException;
import exception.IneligibleStudentException;
import exception.InvalidInputException;
import exception.JobNotFoundException;
import exception.StudentNotFoundException;
import file.ReportFileHandler;
import model.Application;
import model.Company;
import model.Interview;
import model.Job;
import model.Placement;
import model.PlacementDrive;
import model.Resume;
import model.Student;
import service.ApplicationService;
import service.CompanyService;
import service.InterviewService;
import service.JobService;
import service.PlacementDriveService;
import service.PlacementService;
import service.ResumeService;
import service.StudentService;
import util.InputUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The student portal. Every option calls a service and prints the result.
 * No business rules live here.
 */
public class StudentMenu {

    private static final int WIDTH = 64;

    private final InputUtil input;
    private final StudentService studentService;
    private final ResumeService resumeService;
    private final JobService jobService;
    private final CompanyService companyService;
    private final PlacementDriveService driveService;
    private final ApplicationService applicationService;
    private final InterviewService interviewService;
    private final PlacementService placementService;
    private final ReportFileHandler fileHandler;

    public StudentMenu(InputUtil input, StudentService studentService, ResumeService resumeService,
                       JobService jobService, CompanyService companyService,
                       PlacementDriveService driveService, ApplicationService applicationService,
                       InterviewService interviewService, PlacementService placementService,
                       ReportFileHandler fileHandler) {
        this.input = input;
        this.studentService = studentService;
        this.resumeService = resumeService;
        this.jobService = jobService;
        this.companyService = companyService;
        this.driveService = driveService;
        this.applicationService = applicationService;
        this.interviewService = interviewService;
        this.placementService = placementService;
        this.fileHandler = fileHandler;
    }

    /** Shows the dashboard until the student logs out. */
    public void run(Student student) {
        int studentId = student.getStudentId();
        boolean loggedIn = true;

        while (loggedIn) {
            printOptions("STUDENT DASHBOARD",
                    "My Profile", "My Resume", "Placement Drives", "View Jobs", "Apply for Job",
                    "My Applications", "My Interviews", "Placement Status", "Change Password", "Logout");
            int choice = input.readMenuChoice("Choose an option: ", 1, 10);

            switch (choice) {
                case 1: profileMenu(studentId); break;
                case 2: resumeMenu(studentId); break;
                case 3: runSafely(() -> viewOpenDrives()); break;
                case 4: runSafely(() -> viewJobs(studentId)); break;
                case 5: runSafely(() -> applyForJob(studentId)); break;
                case 6: runSafely(() -> myApplications(studentId)); break;
                case 7: runSafely(() -> myInterviews(studentId)); break;
                case 8: runSafely(() -> placementStatus(studentId)); break;
                case 9: runSafely(() -> changePassword(studentId)); break;
                default:
                    loggedIn = false;
                    System.out.println("\nYou have been logged out.");
                    break;
            }
        }
    }

    // ---------- Profile submenu ----------

    private void profileMenu(int studentId) {
        boolean back = false;
        while (!back) {
            printOptions("MY PROFILE", "View profile", "Update profile", "Back");
            int choice = input.readMenuChoice("Choose an option: ", 1, 3);
            switch (choice) {
                case 1: runSafely(() -> viewProfile(studentId)); break;
                case 2: runSafely(() -> updateProfile(studentId)); break;
                default: back = true; break;
            }
        }
    }

    private void viewProfile(int studentId) throws InvalidInputException, StudentNotFoundException, SQLException {
        Student student = studentService.findStudent(studentId);
        printTitle("MY PROFILE");
        student.displayProfile();
        System.out.println("Address           : " + student.getAddress());
        System.out.println("Skills            : " + student.getSkills());
        System.out.println("10th / 12th       : " + student.getTenthPercentage() + "% / "
                + student.getTwelfthPercentage() + "%");
        System.out.println("Profile complete  : "
                + String.format("%.0f%%", studentService.getProfileCompletionPercentage(student)));
    }

    /** Each prompt shows the current value. Pressing Enter keeps it. */
    private void updateProfile(int studentId) throws InvalidInputException, StudentNotFoundException, SQLException {
        Student student = studentService.findStudent(studentId);
        printTitle("UPDATE PROFILE");
        System.out.println("Press Enter to keep the value shown in brackets.\n");

        student.setName(input.readOptionalString("Name", student.getName()));
        student.setPhone(input.readOptionalString("Phone", student.getPhone()));
        student.setDepartment(input.readOptionalString("Department", student.getDepartment()));
        student.setCourse(input.readOptionalString("Course", student.getCourse()));
        student.setGraduationYear(parseInt(input.readOptionalString("Graduation year",
                String.valueOf(student.getGraduationYear())), "Graduation year"));
        student.setCgpa(parseDouble(input.readOptionalString("CGPA",
                String.valueOf(student.getCgpa())), "CGPA"));
        student.setBacklogs(parseInt(input.readOptionalString("Backlogs",
                String.valueOf(student.getBacklogs())), "Backlogs"));
        student.setTenthPercentage(parseDouble(input.readOptionalString("10th percentage",
                String.valueOf(student.getTenthPercentage())), "10th percentage"));
        student.setTwelfthPercentage(parseDouble(input.readOptionalString("12th percentage",
                String.valueOf(student.getTwelfthPercentage())), "12th percentage"));
        student.setAddress(input.readOptionalString("Address", student.getAddress()));
        student.setSkills(input.readOptionalString("Skills (comma separated)", student.getSkills()));

        studentService.updateProfile(student);
        System.out.println("\nProfile updated.");
    }

    // ---------- Resume submenu ----------

    private void resumeMenu(int studentId) {
        boolean back = false;
        while (!back) {
            printOptions("MY RESUME", "View / print resume", "Create resume", "Update resume",
                    "Export resume to TXT", "Back");
            int choice = input.readMenuChoice("Choose an option: ", 1, 5);
            switch (choice) {
                case 1: runSafely(() -> viewResume(studentId)); break;
                case 2: runSafely(() -> createResume(studentId)); break;
                case 3: runSafely(() -> updateResume(studentId)); break;
                case 4: runSafely(() -> exportResume(studentId)); break;
                default: back = true; break;
            }
        }
    }

    private void viewResume(int studentId) throws InvalidInputException, StudentNotFoundException, SQLException {
        Student student = studentService.findStudent(studentId);
        Resume resume = resumeService.getResume(studentId);
        if (resume == null) {
            System.out.println("\nYou have not created a resume yet. Choose 'Create resume' first.");
            return;
        }
        System.out.println();
        System.out.println(resumeService.buildResumeText(student, resume));
    }

    private void createResume(int studentId) throws InvalidInputException, StudentNotFoundException, SQLException {
        resumeService.createResume(studentId);
        System.out.println("\nResume created. Choose 'Update resume' to add your details.");
    }

    private void updateResume(int studentId) throws InvalidInputException, StudentNotFoundException, SQLException {
        Resume resume = resumeService.getResume(studentId);
        if (resume == null) {
            System.out.println("\nCreate a resume first.");
            return;
        }
        printTitle("UPDATE RESUME");
        System.out.println("Press Enter to keep the value shown in brackets.\n");

        resume.setEducation(input.readOptionalString("Education", resume.getEducation()));
        resume.setSkills(input.readOptionalString("Skills", resume.getSkills()));
        resume.setProgrammingLanguages(input.readOptionalString("Programming languages",
                resume.getProgrammingLanguages()));
        resume.setProjects(input.readOptionalString("Projects", resume.getProjects()));
        resume.setInternships(input.readOptionalString("Internships", resume.getInternships()));
        resume.setCertifications(input.readOptionalString("Certifications", resume.getCertifications()));
        resume.setAchievements(input.readOptionalString("Achievements", resume.getAchievements()));
        resume.setLinkedin(input.readOptionalString("LinkedIn URL", resume.getLinkedin()));
        resume.setGithub(input.readOptionalString("GitHub URL", resume.getGithub()));
        resume.setPortfolio(input.readOptionalString("Portfolio URL", resume.getPortfolio()));

        resumeService.updateResume(resume);
        System.out.println("\nResume saved.");
    }

    private void exportResume(int studentId)
            throws InvalidInputException, StudentNotFoundException, SQLException, IOException {
        Student student = studentService.findStudent(studentId);
        Resume resume = resumeService.getResume(studentId);
        if (resume == null) {
            System.out.println("\nCreate a resume before exporting it.");
            return;
        }
        Path saved = fileHandler.saveReport("resume_" + studentId + ".txt",
                resumeService.buildResumeText(student, resume));
        System.out.println("\nResume exported to " + saved.toAbsolutePath());
    }

    // ---------- Drives and jobs ----------

    private void viewOpenDrives() throws InvalidInputException, CompanyNotFoundException,
            JobNotFoundException, SQLException {
        List<PlacementDrive> drives = driveService.getOpenDrives();
        printTitle("OPEN PLACEMENT DRIVES");
        if (drives.isEmpty()) {
            System.out.println("No drives are open for registration right now.");
            return;
        }
        for (PlacementDrive drive : drives) {
            Company company = companyService.findCompany(drive.getCompanyId());
            System.out.println("Drive ID " + drive.getDriveId() + " | " + company.getCompanyName());
            System.out.println("  Drive date          : " + drive.getDriveDate());
            System.out.println("  Registration until  : " + drive.getRegistrationDeadline());
            System.out.println("  Venue               : " + drive.getVenue());
            System.out.println("  Jobs offered        : " + jobTitles(drive.getJobIds()));
            System.out.println();
        }
    }

    private void viewJobs(int studentId) throws InvalidInputException, StudentNotFoundException,
            JobNotFoundException, CompanyNotFoundException, SQLException {
        List<Job> jobs = jobService.getOpenJobs();
        printTitle("OPEN JOBS");
        if (jobs.isEmpty()) {
            System.out.println("There are no open jobs right now.");
            return;
        }
        System.out.println(String.format("%-5s %-24s %-18s %6s %8s  %s",
                "ID", "Title", "Company", "CTC", "Min CGPA", "Deadline"));
        System.out.println("-".repeat(WIDTH));
        for (Job job : jobs) {
            Company company = companyService.findCompany(job.getCompanyId());
            System.out.println(String.format("%-5d %-24s %-18s %6.2f %8.2f  %s",
                    job.getJobId(), job.getTitle(), company.getCompanyName(),
                    job.getCtc(), job.getMinimumCgpa(), job.getApplicationDeadline()));
        }

        int jobId = input.readInt("\nEnter a job ID to check your eligibility (0 to skip): ");
        if (jobId == 0) {
            return;
        }
        Job job = jobService.findJob(jobId);
        Student student = studentService.findStudent(studentId);
        List<String> issues = applicationService.findEligibilityIssues(student, job);
        if (issues.isEmpty()) {
            System.out.println("Student is eligible for this job.");
        } else {
            for (String issue : issues) {
                System.out.println(issue);
            }
        }
    }

    // ---------- Applications and interviews ----------

    private void applyForJob(int studentId) throws InvalidInputException, StudentNotFoundException,
            JobNotFoundException, IneligibleStudentException, DuplicateApplicationException, SQLException {
        int jobId = input.readInt("Enter the job ID you want to apply for: ");
        Job job = jobService.findJob(jobId);

        if (!input.readYesNo("Apply for '" + job.getTitle() + "'? (y/n): ")) {
            System.out.println("Application cancelled.");
            return;
        }
        Application application = applicationService.applyForJob(studentId, jobId);
        System.out.println("\nApplication submitted. Application ID " + application.getApplicationId()
                + ", status " + application.getStatus() + ".");
    }

    private void myApplications(int studentId) throws InvalidInputException, StudentNotFoundException,
            JobNotFoundException, SQLException {
        List<Application> applications = applicationService.getApplicationsForStudent(studentId);
        printTitle("MY APPLICATIONS");
        if (applications.isEmpty()) {
            System.out.println("You have not applied to any job yet.");
            return;
        }
        for (Application application : applications) {
            Job job = jobService.findJob(application.getJobId());
            System.out.println("Application ID " + application.getApplicationId()
                    + " | " + job.getTitle()
                    + " | applied " + application.getApplicationDate()
                    + " | status " + application.getStatus());
        }

        if (input.readYesNo("\nDo you want to withdraw an application? (y/n): ")) {
            int applicationId = input.readInt("Enter the application ID to withdraw: ");
            applicationService.withdraw(studentId, applicationId);
            System.out.println("Application withdrawn.");
        }
    }

    private void myInterviews(int studentId) throws InvalidInputException, StudentNotFoundException, SQLException {
        List<Interview> interviews = interviewService.getInterviewsForStudent(studentId);
        printTitle("MY INTERVIEWS");
        if (interviews.isEmpty()) {
            System.out.println("No interviews have been scheduled for you yet.");
            return;
        }
        for (Interview interview : interviews) {
            System.out.println("Application " + interview.getApplicationId()
                    + " | " + interview.getRound()
                    + " | " + interview.getDate() + " " + interview.getTime()
                    + " | " + interview.getMode() + ", " + interview.getVenue()
                    + " | " + interview.getInterviewer()
                    + " | Result: " + interview.getResult());
            if (!interview.getRemarks().isEmpty()) {
                System.out.println("    Remarks: " + interview.getRemarks());
            }
        }
    }

    // ---------- Placement status and password ----------

    private void placementStatus(int studentId) throws InvalidInputException, StudentNotFoundException,
            CompanyNotFoundException, SQLException {
        Student student = studentService.findStudent(studentId);
        printTitle("PLACEMENT STATUS");
        System.out.println("Status: " + student.getPlacementStatus());

        Placement placement = placementService.getPlacementForStudent(studentId);
        if (placement == null) {
            System.out.println("You have not been placed yet.");
            return;
        }
        Company company = companyService.findCompany(placement.getCompanyId());
        System.out.println("Company        : " + company.getCompanyName());
        System.out.println("Package        : " + placement.getCtc() + " LPA");
        System.out.println("Offer status   : " + placement.getOfferStatus());
        System.out.println("Placed on      : " + placement.getPlacementDate());
        System.out.println("Joining date   : "
                + (placement.getJoiningDate() == null ? "Not yet recorded" : placement.getJoiningDate()));
    }

    private void changePassword(int studentId) throws InvalidInputException, StudentNotFoundException, SQLException {
        printTitle("CHANGE PASSWORD");
        String oldPassword = input.readRequiredString("Current password: ");
        String newPassword = input.readRequiredString("New password: ");
        String confirm = input.readRequiredString("Confirm new password: ");
        if (!newPassword.equals(confirm)) {
            throw new InvalidInputException("The new passwords do not match.");
        }
        studentService.changePassword(studentId, oldPassword, newPassword);
        System.out.println("\nPassword changed.");
    }

    // ---------- Shared helpers ----------

    /**
     * Runs one option and turns each expected error into a short message.
     * Only the exception types this menu can receive are caught.
     */
    private void runSafely(Step step) {
        try {
            step.run();
        } catch (InvalidInputException | StudentNotFoundException | JobNotFoundException
                | CompanyNotFoundException | IneligibleStudentException
                | DuplicateApplicationException e) {
            System.out.println("\n[!] " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("\n[!] Database error. Check that MySQL is running and try again.");
        } catch (IOException e) {
            System.out.println("\n[!] Could not save the file: " + e.getMessage());
        }
    }

    /** One menu option. Lambdas can throw any of the exceptions listed here. */
    private interface Step {
        void run() throws InvalidInputException, StudentNotFoundException, JobNotFoundException,
                CompanyNotFoundException, IneligibleStudentException, DuplicateApplicationException,
                SQLException, IOException;
    }

    /** Turns a list of job IDs into a readable list of titles. */
    private String jobTitles(List<Integer> jobIds) throws InvalidInputException, JobNotFoundException, SQLException {
        List<String> titles = new ArrayList<>();
        for (int jobId : jobIds) {
            titles.add(jobService.findJob(jobId).getTitle());
        }
        return String.join(", ", titles);
    }

    private int parseInt(String text, String label) throws InvalidInputException {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException(label + " must be a whole number.");
        }
    }

    private double parseDouble(String text, String label) throws InvalidInputException {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException(label + " must be a number.");
        }
    }

    private void printOptions(String title, String... options) {
        System.out.println();
        printTitle(title);
        for (int i = 0; i < options.length; i++) {
            System.out.println("  " + (i + 1) + ". " + options[i]);
        }
        System.out.println("=".repeat(WIDTH));
    }

    private void printTitle(String title) {
        System.out.println("=".repeat(WIDTH));
        int padding = Math.max(0, (WIDTH - title.length()) / 2);
        System.out.println(" ".repeat(padding) + title);
        System.out.println("=".repeat(WIDTH));
    }
}