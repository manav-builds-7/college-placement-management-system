package ui;

import exception.InvalidInputException;
import model.PlacementOfficer;
import model.Student;
import service.AuthService;
import service.StudentService;
import util.InputUtil;

import java.sql.SQLException;

/**
 * The first screen the user sees. Routes to the student portal or the officer
 * dashboard after a successful login. Returns here when that session ends.
 */
public class LoginMenu extends ConsoleMenu {

    private final InputUtil input;
    private final AuthService authService;
    private final StudentService studentService;
    private final StudentMenu studentMenu;
    private final OfficerMenu officerMenu;

    public LoginMenu(InputUtil input, AuthService authService, StudentService studentService,
                     StudentMenu studentMenu, OfficerMenu officerMenu) {
        super(64);
        this.input = input;
        this.authService = authService;
        this.studentService = studentService;
        this.studentMenu = studentMenu;
        this.officerMenu = officerMenu;
    }

    /** Shows the login screen until the user chooses Exit. */
    public void run() {
        printTitle("COLLEGE PLACEMENT MANAGEMENT SYSTEM");
        boolean running = true;

        while (running) {
            try {
                printOptions("MAIN MENU", "Student Login", "Student Registration",
                        "Placement Officer Login", "Exit");
                int choice = input.readMenuChoice("Choose an option: ", 1, 4);

                switch (choice) {
                    case 1: runSafely(() -> studentLogin()); break;
                    case 2: runSafely(() -> registerStudent()); break;
                    case 3: runSafely(() -> officerLogin()); break;
                    default: running = false; break;
                }
            } catch (IllegalStateException e) {
                running = false;
                System.out.println("\nInput stream closed. Exiting the application.");
            }
        }
        System.out.println("\nThank you for using the College Placement Management System.");
    }

    private void studentLogin() throws InvalidInputException, SQLException {
        String email = input.readString("Email: ");
        String password = input.readString("Password: ");

        Student student = authService.loginStudent(email, password);
        System.out.println("\nWelcome, " + student.getName() + ".");
        studentMenu.run(student);
    }

    private void officerLogin() throws InvalidInputException, SQLException {
        String email = input.readString("Email: ");
        String password = input.readString("Password: ");

        PlacementOfficer officer = authService.loginOfficer(email, password);
        System.out.println("\nWelcome, " + officer.getName() + ".");
        officerMenu.run(officer);
    }

    private void registerStudent() throws InvalidInputException, SQLException {
        printTitle("STUDENT REGISTRATION");
        String name = input.readRequiredString("Full name: ");
        String email = input.readRequiredString("Email: ");
        String phone = input.readRequiredString("Phone (10 digits, starts with 6-9): ");
        String password = input.readRequiredString("Password (at least 6 characters): ");
        String confirm = input.readRequiredString("Confirm password: ");

        if (!password.equals(confirm)) {
            throw new InvalidInputException("The passwords do not match.");
        }
        Student student = studentService.registerStudent(name, email, phone, password);
        System.out.println("\nRegistration successful. Your student ID is "
                + student.getStudentId() + ". Please log in.");
    }

    // ---------- Shared helpers ----------

    /** One menu option. Only the exceptions these screens can receive are listed. */
    private interface Step {
        void run() throws InvalidInputException, SQLException;
    }

    /** Runs one option and prints a short message if it fails. */
    private void runSafely(Step step) {
        try {
            step.run();
        } catch (InvalidInputException e) {
            System.out.println("\n[!] " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("\n[!] Database error. Check that MySQL is running and try again.");
        }
    }

}