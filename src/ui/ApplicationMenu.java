package ui;

import enums.ApplicationStatus;
import exception.InvalidInputException;
import exception.JobNotFoundException;
import exception.StudentNotFoundException;
import model.Application;
import model.Job;
import model.Student;
import service.ApplicationService;
import service.JobService;
import service.StudentService;
import util.InputUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * The officer's application review. Shortlisting and rejecting go through
 * ApplicationService, which enforces the status rules.
 */
public class ApplicationMenu {

    private static final int WIDTH = 72;

    private final InputUtil input;
    private final ApplicationService applicationService;
    private final StudentService studentService;
    private final JobService jobService;

    public ApplicationMenu(InputUtil input, ApplicationService applicationService,
                           StudentService studentService, JobService jobService) {
        this.input = input;
        this.applicationService = applicationService;
        this.studentService = studentService;
        this.jobService = jobService;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            printOptions("REVIEW APPLICATIONS", "View all applications", "Filter by status",
                    "View applications for a job", "Shortlist an application",
                    "Reject an application", "Back");
            int choice = input.readMenuChoice("Choose an option: ", 1, 6);

            switch (choice) {
                case 1: runSafely(() -> showApplications(applicationService.getAllApplications(), "ALL APPLICATIONS")); break;
                case 2: runSafely(() -> filterByStatus()); break;
                case 3: runSafely(() -> applicationsForJob()); break;
                case 4: runSafely(() -> shortlist()); break;
                case 5: runSafely(() -> reject()); break;
                default: back = true; break;
            }
        }
    }

    private void filterByStatus() throws InvalidInputException, StudentNotFoundException,
            JobNotFoundException, SQLException {
        ApplicationStatus status = parseStatus(input.readRequiredString(
                "Status (e.g. APPLIED, SHORTLISTED, SELECTED): "));
        showApplications(applicationService.getApplicationsByStatus(status), "APPLICATIONS WITH STATUS " + status);
    }

    private void applicationsForJob() throws InvalidInputException, StudentNotFoundException,
            JobNotFoundException, SQLException {
        int jobId = input.readInt("Job ID: ");
        Job job = jobService.findJob(jobId);
        showApplications(applicationService.getApplicationsForJob(jobId),
                "APPLICATIONS FOR " + job.getTitle().toUpperCase());
    }

    private void shortlist() throws InvalidInputException, SQLException {
        int applicationId = input.readInt("Application ID to shortlist: ");
        applicationService.changeStatus(applicationId, ApplicationStatus.SHORTLISTED);
        System.out.println("\nApplication " + applicationId + " shortlisted.");
    }

    private void reject() throws InvalidInputException, SQLException {
        int applicationId = input.readInt("Application ID to reject: ");
        if (!input.readYesNo("Reject application " + applicationId + "? (y/n): ")) {
            System.out.println("Rejection cancelled.");
            return;
        }
        applicationService.changeStatus(applicationId, ApplicationStatus.REJECTED);
        System.out.println("\nApplication " + applicationId + " rejected.");
    }

    /** Prints applications with the student's name and the job title, not just their IDs. */
    private void showApplications(List<Application> applications, String title) throws InvalidInputException,
            StudentNotFoundException, JobNotFoundException, SQLException {
        printTitle(title);
        if (applications.isEmpty()) {
            System.out.println("No applications found.");
            return;
        }
        System.out.println(String.format("%-7s %-22s %-24s %-11s %s",
                "App ID", "Student", "Job", "Applied", "Status"));
        System.out.println("-".repeat(WIDTH));
        for (Application application : applications) {
            Student student = studentService.findStudent(application.getStudentId());
            Job job = jobService.findJob(application.getJobId());
            System.out.println(String.format("%-7d %-22s %-24s %-11s %s",
                    application.getApplicationId(), student.getName(), job.getTitle(),
                    application.getApplicationDate(), application.getStatus()));
        }
    }

    private ApplicationStatus parseStatus(String text) throws InvalidInputException {
        try {
            return ApplicationStatus.valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Unknown status '" + text.trim() + "'.");
        }
    }

    // ---------- Shared helpers ----------

    private interface Step {
        void run() throws InvalidInputException, StudentNotFoundException, JobNotFoundException, SQLException;
    }

    private void runSafely(Step step) {
        try {
            step.run();
        } catch (InvalidInputException | StudentNotFoundException | JobNotFoundException e) {
            System.out.println("\n[!] " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("\n[!] Database error. Check that MySQL is running and try again.");
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