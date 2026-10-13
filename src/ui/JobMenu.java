package ui;

import enums.JobType;
import exception.CompanyNotFoundException;
import exception.InvalidInputException;
import exception.JobNotFoundException;
import model.Company;
import model.Job;
import service.CompanyService;
import service.JobService;
import util.InputUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/** The officer's job management screens: add, update, delete, view, and search. */
public class JobMenu extends ConsoleMenu {

    private final InputUtil input;
    private final JobService jobService;
    private final CompanyService companyService;

    public JobMenu(InputUtil input, JobService jobService, CompanyService companyService) {
        super(64);
        this.input = input;
        this.jobService = jobService;
        this.companyService = companyService;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            printOptions("MANAGE JOBS", "Add job", "Update job", "Delete job",
                    "View all jobs", "Search jobs", "Back");
            int choice = input.readMenuChoice("Choose an option: ", 1, 6);
            switch (choice) {
                case 1: runSafely(() -> addJob()); break;
                case 2: runSafely(() -> updateJob()); break;
                case 3: runSafely(() -> deleteJob()); break;
                case 4: runSafely(() -> viewJobs(jobService.getAllJobs(), "ALL JOBS")); break;
                case 5: runSafely(() -> searchJobs()); break;
                default: back = true; break;
            }
        }
    }

    private void addJob() throws InvalidInputException, CompanyNotFoundException, JobNotFoundException, SQLException {
        printTitle("ADD JOB");
        int companyId = input.readInt("Company ID: ");
        String title = input.readRequiredString("Job title: ");
        String description = input.readString("Description (optional, press Enter to skip): ").trim();
        JobType type = chooseJobType();
        String location = input.readRequiredString("Location: ");
        double ctc = input.readDouble("CTC in LPA: ");
        double minimumCgpa = input.readDouble("Minimum CGPA (0 if no requirement): ");
        double minimumTenth = input.readDouble("Minimum 10th percentage (0 if no requirement): ");
        double minimumTwelfth = input.readDouble("Minimum 12th percentage (0 if no requirement): ");
        int maximumBacklogs = input.readInt("Maximum backlogs allowed: ");
        String branches = input.readRequiredString("Eligible branches (comma separated, e.g. CSE, IT): ");
        int graduationYear = input.readInt("Graduation year of the batch: ");
        LocalDate deadline = input.readDate("Application deadline (yyyy-mm-dd): ");

        Job job = new Job(companyId, title, description, type, location, ctc, minimumCgpa,
                maximumBacklogs, branches, graduationYear, deadline);
        job.setMinimumTenth(minimumTenth);
        job.setMinimumTwelfth(minimumTwelfth);

        jobService.addJob(job);
        System.out.println("\nJob added with ID " + job.getJobId() + ".");
    }

    private void updateJob() throws InvalidInputException, CompanyNotFoundException, JobNotFoundException, SQLException {
        int jobId = input.readInt("Job ID to update: ");
        Job job = jobService.findJob(jobId);

        printTitle("UPDATE JOB");
        System.out.println("Press Enter to keep the value shown in brackets.\n");
        job.setCompanyId(parseInt(input.readOptionalString("Company ID",
                String.valueOf(job.getCompanyId())), "Company ID"));
        job.setTitle(input.readOptionalString("Job title", job.getTitle()));
        job.setDescription(input.readOptionalString("Description", blankIfNull(job.getDescription())));
        if (input.readYesNo("Change job type (currently " + job.getJobType() + ")? (y/n): ")) {
            job.setJobType(chooseJobType());
        }
        job.setLocation(input.readOptionalString("Location", job.getLocation()));
        job.setCtc(parseDouble(input.readOptionalString("CTC in LPA",
                String.valueOf(job.getCtc())), "CTC"));
        job.setMinimumCgpa(parseDouble(input.readOptionalString("Minimum CGPA",
                String.valueOf(job.getMinimumCgpa())), "Minimum CGPA"));
        job.setMinimumTenth(parseDouble(input.readOptionalString("Minimum 10th percentage",
                String.valueOf(job.getMinimumTenth())), "Minimum 10th percentage"));
        job.setMinimumTwelfth(parseDouble(input.readOptionalString("Minimum 12th percentage",
                String.valueOf(job.getMinimumTwelfth())), "Minimum 12th percentage"));
        job.setMaximumBacklogs(parseInt(input.readOptionalString("Maximum backlogs",
                String.valueOf(job.getMaximumBacklogs())), "Maximum backlogs"));
        job.setEligibleBranches(input.readOptionalString("Eligible branches", job.getEligibleBranches()));
        job.setGraduationYear(parseInt(input.readOptionalString("Graduation year",
                String.valueOf(job.getGraduationYear())), "Graduation year"));
        job.setApplicationDeadline(parseDate(input.readOptionalString("Application deadline",
                job.getApplicationDeadline().toString()), "Application deadline"));

        jobService.updateJob(job);
        System.out.println("\nJob updated.");
    }

    private void deleteJob() throws InvalidInputException, JobNotFoundException, SQLException {
        int jobId = input.readInt("Job ID to delete: ");
        Job job = jobService.findJob(jobId);

        if (!input.readYesNo("Delete '" + job.getTitle() + "'? (y/n): ")) {
            System.out.println("Deletion cancelled.");
            return;
        }
        jobService.deleteJob(jobId);
        System.out.println("\nJob deleted.");
    }

    private void searchJobs() throws CompanyNotFoundException, JobNotFoundException, InvalidInputException, SQLException {
        String keyword = input.readRequiredString("Search by title or location: ");
        viewJobs(jobService.searchJobs(keyword), "SEARCH RESULTS");
    }

    /** Prints each job in a short block, with its open or closed status from the deadline. */
    private void viewJobs(List<Job> jobs, String title)
            throws CompanyNotFoundException, InvalidInputException, SQLException {
        printTitle(title);
        if (jobs.isEmpty()) {
            System.out.println("No jobs found.");
            return;
        }
        for (Job job : jobs) {
            Company company = companyService.findCompany(job.getCompanyId());
            String status = jobService.isDeadlinePassed(job) ? "closed" : "open";
            System.out.println("Job " + job.getJobId() + " | " + job.getTitle()
                    + " | " + company.getCompanyName()
                    + " | " + job.getJobType() + " | " + job.getCtc() + " LPA");
            System.out.println("    Min CGPA " + job.getMinimumCgpa()
                    + ", min 10th " + job.getMinimumTenth() + "%"
                    + ", min 12th " + job.getMinimumTwelfth() + "%"
                    + ", backlogs up to " + job.getMaximumBacklogs()
                    + ", branches " + job.getEligibleBranches()
                    + ", batch " + job.getGraduationYear()
                    + ", deadline " + job.getApplicationDeadline() + " (" + status + ")");
        }
    }

    private JobType chooseJobType() {
        System.out.println("Job type:");
        System.out.println("  1. FULL_TIME");
        System.out.println("  2. INTERNSHIP");
        System.out.println("  3. INTERNSHIP_TO_FULL_TIME");
        int choice = input.readMenuChoice("Choose job type: ", 1, 3);
        switch (choice) {
            case 1: return JobType.FULL_TIME;
            case 2: return JobType.INTERNSHIP;
            default: return JobType.INTERNSHIP_TO_FULL_TIME;
        }
    }

    // ---------- Parsing helpers ----------

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

    private LocalDate parseDate(String text, String label) throws InvalidInputException {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new InvalidInputException(label + " must be a date in yyyy-mm-dd format.");
        }
    }

    private String blankIfNull(String value) {
        return value == null ? "" : value;
    }

    // ---------- Shared helpers ----------

    private interface Step {
        void run() throws InvalidInputException, CompanyNotFoundException, JobNotFoundException, SQLException;
    }

    private void runSafely(Step step) {
        try {
            step.run();
        } catch (InvalidInputException | CompanyNotFoundException | JobNotFoundException e) {
            System.out.println("\n[!] " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("\n[!] Database error. Check that MySQL is running and try again.");
        }
    }

}