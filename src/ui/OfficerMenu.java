package ui;

import enums.DriveStatus;
import exception.CompanyNotFoundException;
import exception.InvalidInputException;
import exception.JobNotFoundException;
import file.ReportFileHandler;
import model.Company;
import model.PlacementDrive;
import model.PlacementOfficer;
import service.ApplicationService;
import service.CompanyService;
import service.InterviewService;
import service.JobService;
import service.PlacementDriveService;
import service.PlacementService;
import service.ReportService;
import service.ResumeService;
import service.StudentService;
import util.InputUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * The placement officer's dashboard. It opens one screen per area and keeps
 * the placement drive screens itself.
 */
public class OfficerMenu extends ConsoleMenu {

    private final InputUtil input;
    private final CompanyService companyService;
    private final PlacementDriveService driveService;

    private final CompanyMenu companyMenu;
    private final JobMenu jobMenu;
    private final StudentProfileMenu studentProfileMenu;
    private final ApplicationMenu applicationMenu;
    private final InterviewMenu interviewMenu;
    private final PlacementMenu placementMenu;
    private final ReportMenu reportMenu;

    public OfficerMenu(InputUtil input, CompanyService companyService, JobService jobService,
                       PlacementDriveService driveService, StudentService studentService,
                       ResumeService resumeService, ApplicationService applicationService,
                       InterviewService interviewService, PlacementService placementService,
                       ReportService reportService, ReportFileHandler fileHandler) {
        super(64);
        this.input = input;
        this.companyService = companyService;
        this.driveService = driveService;

        this.companyMenu = new CompanyMenu(input, companyService);
        this.jobMenu = new JobMenu(input, jobService, companyService);
        this.studentProfileMenu = new StudentProfileMenu(input, studentService, resumeService);
        this.applicationMenu = new ApplicationMenu(input, applicationService, studentService, jobService);
        this.interviewMenu = new InterviewMenu(input, interviewService);
        this.placementMenu = new PlacementMenu(input, placementService, studentService, companyService);
        this.reportMenu = new ReportMenu(input, reportService, fileHandler);
    }

    /** Shows the officer dashboard until the officer logs out. */
    public void run(PlacementOfficer officer) {
        boolean loggedIn = true;
        while (loggedIn) {
            printOptions("OFFICER DASHBOARD", "Manage Companies", "Manage Jobs",
                    "Manage Placement Drives", "Manage Students", "Review Applications",
                    "Manage Interviews", "Selection and Placements", "Reports", "Logout");
            int choice = input.readMenuChoice("Choose an option: ", 1, 9);

            switch (choice) {
                case 1: companyMenu.run(); break;
                case 2: jobMenu.run(); break;
                case 3: driveMenu(); break;
                case 4: studentProfileMenu.run(); break;
                case 5: applicationMenu.run(); break;
                case 6: interviewMenu.run(); break;
                case 7: placementMenu.run(); break;
                case 8: reportMenu.run(); break;
                default:
                    loggedIn = false;
                    System.out.println("\nYou have been logged out.");
                    break;
            }
        }
    }

    // ---------- Placement drives ----------

    private void driveMenu() {
        boolean back = false;
        while (!back) {
            printOptions("MANAGE PLACEMENT DRIVES", "Create drive", "Update drive",
                    "Change drive status", "View all drives", "Back");
            int choice = input.readMenuChoice("Choose an option: ", 1, 5);
            switch (choice) {
                case 1: runSafely(() -> createDrive()); break;
                case 2: runSafely(() -> updateDrive()); break;
                case 3: runSafely(() -> changeDriveStatus()); break;
                case 4: runSafely(() -> viewDrives()); break;
                default: back = true; break;
            }
        }
    }

    private void createDrive() throws InvalidInputException, CompanyNotFoundException,
            JobNotFoundException, SQLException {
        printTitle("CREATE PLACEMENT DRIVE");
        int companyId = input.readInt("Company ID: ");
        LocalDate driveDate = input.readDate("Drive date (yyyy-mm-dd): ");
        LocalDate registrationDeadline = input.readDate("Registration deadline (yyyy-mm-dd): ");
        String venue = input.readRequiredString("Venue: ");
        List<Integer> jobIds = parseJobIds(input.readRequiredString("Job IDs to offer (e.g. 1, 2): "));

        PlacementDrive drive = new PlacementDrive(companyId, driveDate, registrationDeadline, venue);
        for (int jobId : jobIds) {
            drive.addJob(jobId);
        }
        driveService.createDrive(drive);
        System.out.println("\nDrive created with ID " + drive.getDriveId()
                + ", status " + drive.getStatus() + ". Change the status to OPEN to start registration.");
    }

    private void updateDrive() throws InvalidInputException, CompanyNotFoundException,
            JobNotFoundException, SQLException {
        int driveId = input.readInt("Drive ID to update: ");
        PlacementDrive current = driveService.findDrive(driveId);

        printTitle("UPDATE PLACEMENT DRIVE");
        System.out.println("Press Enter to keep the value shown in brackets.\n");
        int companyId = parseInt(input.readOptionalString("Company ID",
                String.valueOf(current.getCompanyId())), "Company ID");
        LocalDate driveDate = parseDate(input.readOptionalString("Drive date",
                current.getDriveDate().toString()), "Drive date");
        LocalDate registrationDeadline = parseDate(input.readOptionalString("Registration deadline",
                current.getRegistrationDeadline().toString()), "Registration deadline");
        String venue = input.readOptionalString("Venue", current.getVenue());
        String jobsText = input.readOptionalString("Job IDs", formatIds(current.getJobIds()));

        // The job list has no setter, so build a new drive with the same status and add the jobs
        PlacementDrive updated = new PlacementDrive(driveId, companyId, driveDate, registrationDeadline,
                venue, current.getStatus());
        for (int jobId : parseJobIds(jobsText)) {
            updated.addJob(jobId);
        }
        driveService.updateDrive(updated);
        System.out.println("\nDrive updated. Its status is unchanged.");
    }

    private void changeDriveStatus() throws InvalidInputException, SQLException {
        int driveId = input.readInt("Drive ID: ");
        PlacementDrive drive = driveService.findDrive(driveId);
        System.out.println("Current status: " + drive.getStatus());
        System.out.println("Allowed moves: UPCOMING to OPEN or CANCELLED; OPEN to CLOSED or CANCELLED;"
                + " CLOSED to COMPLETED or CANCELLED.");

        DriveStatus newStatus = parseDriveStatus(input.readRequiredString(
                "New status (OPEN, CLOSED, COMPLETED, CANCELLED): "));
        driveService.changeStatus(driveId, newStatus);
        System.out.println("\nDrive status changed to " + newStatus + ".");
    }

    private void viewDrives()
            throws CompanyNotFoundException, JobNotFoundException, InvalidInputException, SQLException {
        List<PlacementDrive> drives = driveService.getAllDrives();
        printTitle("ALL PLACEMENT DRIVES");
        if (drives.isEmpty()) {
            System.out.println("No placement drives yet.");
            return;
        }
        for (PlacementDrive drive : drives) {
            Company company = companyService.findCompany(drive.getCompanyId());
            String registration = driveService.isRegistrationOpen(drive)
                    ? "registration open" : "registration closed";
            System.out.println("Drive " + drive.getDriveId() + " | " + company.getCompanyName()
                    + " | " + drive.getStatus() + " | " + registration);
            System.out.println("    Drive date " + drive.getDriveDate()
                    + ", registration until " + drive.getRegistrationDeadline()
                    + ", venue " + drive.getVenue()
                    + ", jobs " + formatIds(drive.getJobIds()));
        }
    }

    // ---------- Parsing helpers ----------

    /** Turns "1, 2, 3" into a list of job IDs. Rejects blanks and non-numbers. */
    private List<Integer> parseJobIds(String text) throws InvalidInputException {
        List<Integer> ids = new ArrayList<>();
        for (String part : text.split(",")) {
            if (!part.trim().isEmpty()) {
                ids.add(parseInt(part, "Job ID"));
            }
        }
        if (ids.isEmpty()) {
            throw new InvalidInputException("Enter at least one job ID.");
        }
        return ids;
    }

    private String formatIds(List<Integer> ids) {
        StringBuilder text = new StringBuilder();
        for (int id : ids) {
            if (text.length() > 0) {
                text.append(", ");
            }
            text.append(id);
        }
        return text.toString();
    }

    private DriveStatus parseDriveStatus(String text) throws InvalidInputException {
        try {
            return DriveStatus.valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Unknown drive status '" + text.trim() + "'.");
        }
    }

    private int parseInt(String text, String label) throws InvalidInputException {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException(label + " must be a whole number.");
        }
    }

    private LocalDate parseDate(String text, String label) throws InvalidInputException {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new InvalidInputException(label + " must be a date in yyyy-mm-dd format.");
        }
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