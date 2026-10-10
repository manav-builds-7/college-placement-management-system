package ui;

import enums.OfferStatus;
import exception.CompanyNotFoundException;
import exception.InvalidInputException;
import exception.JobNotFoundException;
import exception.StudentNotFoundException;
import model.Company;
import model.Placement;
import model.Student;
import service.CompanyService;
import service.PlacementService;
import service.StudentService;
import util.InputUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * The selection stage: select students, record offers and joining dates, and list results.
 * PlacementService checks every rule before it writes anything.
 */
public class PlacementMenu {

    private static final int WIDTH = 72;

    private final InputUtil input;
    private final PlacementService placementService;
    private final StudentService studentService;
    private final CompanyService companyService;

    public PlacementMenu(InputUtil input, PlacementService placementService,
                         StudentService studentService, CompanyService companyService) {
        this.input = input;
        this.placementService = placementService;
        this.studentService = studentService;
        this.companyService = companyService;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            printOptions("SELECTION AND PLACEMENTS", "Select a student", "Record offer accepted",
                    "Record joining date", "View placed students", "View unplaced students", "Back");
            int choice = input.readMenuChoice("Choose an option: ", 1, 6);

            switch (choice) {
                case 1: runSafely(() -> selectStudent()); break;
                case 2: runSafely(() -> recordOfferAccepted()); break;
                case 3: runSafely(() -> recordJoiningDate()); break;
                case 4: runSafely(() -> viewPlaced()); break;
                case 5: runSafely(() -> viewUnplaced()); break;
                default: back = true; break;
            }
        }
    }

    private void selectStudent() throws InvalidInputException, StudentNotFoundException,
            JobNotFoundException, CompanyNotFoundException, SQLException {
        int applicationId = input.readInt("Application ID of the student to select: ");
        if (!input.readYesNo("Select this student? (y/n): ")) {
            System.out.println("Selection cancelled.");
            return;
        }

        Placement placement = placementService.selectStudent(applicationId);
        Student student = studentService.findStudent(placement.getStudentId());
        Company company = companyService.findCompany(placement.getCompanyId());
        System.out.println("\n" + student.getName() + " selected by " + company.getCompanyName()
                + " for " + placement.getCtc() + " LPA.");
        System.out.println("Placement ID " + placement.getPlacementId()
                + ", offer " + placement.getOfferStatus() + ".");
    }

    private void recordOfferAccepted() throws InvalidInputException, SQLException {
        int placementId = input.readInt("Placement ID: ");
        if (!input.readYesNo("Record that the student accepted the offer? (y/n): ")) {
            System.out.println("Cancelled.");
            return;
        }
        placementService.recordOfferResponse(placementId, OfferStatus.ACCEPTED);
        System.out.println("\nOffer recorded as ACCEPTED.");
    }

    private void recordJoiningDate() throws InvalidInputException, SQLException {
        int placementId = input.readInt("Placement ID: ");
        LocalDate joiningDate = input.readDate("Joining date (yyyy-mm-dd): ");
        placementService.recordJoiningDate(placementId, joiningDate);
        System.out.println("\nJoining date recorded.");
    }

    private void viewPlaced() throws InvalidInputException, StudentNotFoundException,
            CompanyNotFoundException, SQLException {
        List<Placement> placements = placementService.getAllPlacements();
        printTitle("PLACED STUDENTS");
        if (placements.isEmpty()) {
            System.out.println("No students have been placed yet.");
            return;
        }
        for (Placement placement : placements) {
            Student student = studentService.findStudent(placement.getStudentId());
            Company company = companyService.findCompany(placement.getCompanyId());
            String joined = placement.getJoiningDate() == null
                    ? "not yet recorded" : placement.getJoiningDate().toString();
            System.out.println("Placement " + placement.getPlacementId()
                    + " | " + student.getName()
                    + " | " + company.getCompanyName()
                    + " | " + placement.getCtc() + " LPA"
                    + " | placed " + placement.getPlacementDate()
                    + " | offer " + placement.getOfferStatus()
                    + " | joined " + joined);
        }
    }

    private void viewUnplaced() throws SQLException {
        List<Student> unplaced = placementService.getUnplacedStudents();
        printTitle("UNPLACED STUDENTS");
        if (unplaced.isEmpty()) {
            System.out.println("Every student has been placed.");
            return;
        }
        System.out.println(String.format("%-5s %-22s %-8s %6s %9s", "ID", "Name", "Dept", "CGPA", "Backlogs"));
        System.out.println("-".repeat(WIDTH));
        for (Student student : unplaced) {
            System.out.println(String.format("%-5d %-22s %-8s %6.2f %9d",
                    student.getStudentId(), student.getName(), student.getDepartment(),
                    student.getCgpa(), student.getBacklogs()));
        }
    }

    // ---------- Shared helpers ----------

    private interface Step {
        void run() throws InvalidInputException, StudentNotFoundException, JobNotFoundException,
                CompanyNotFoundException, SQLException;
    }

    private void runSafely(Step step) {
        try {
            step.run();
        } catch (InvalidInputException | StudentNotFoundException | JobNotFoundException
                | CompanyNotFoundException e) {
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