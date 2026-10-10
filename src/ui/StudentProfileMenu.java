package ui;

import enums.PlacementStatus;
import exception.InvalidInputException;
import exception.StudentNotFoundException;
import model.Resume;
import model.Student;
import service.ResumeService;
import service.StudentService;
import util.InputUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The officer's view of students: list, search, filter, and inspect profiles and resumes.
 * Read-only: officers cannot change student records from this screen.
 */
public class StudentProfileMenu {

    private static final int WIDTH = 64;

    private final InputUtil input;
    private final StudentService studentService;
    private final ResumeService resumeService;

    public StudentProfileMenu(InputUtil input, StudentService studentService, ResumeService resumeService) {
        this.input = input;
        this.studentService = studentService;
        this.resumeService = resumeService;
    }

    /** Shows the student options until the officer goes back. */
    public void run() {
        boolean back = false;
        while (!back) {
            printOptions("MANAGE STUDENTS", "View all students", "Search students",
                    "Filter by department", "Filter by placement status",
                    "View a student's profile", "View a student's resume", "Back");
            int choice = input.readMenuChoice("Choose an option: ", 1, 7);

            switch (choice) {
                case 1: runSafely(() -> showStudents(studentService.getAllStudents(), "ALL STUDENTS")); break;
                case 2: runSafely(() -> searchStudents()); break;
                case 3: runSafely(() -> filterByDepartment()); break;
                case 4: runSafely(() -> filterByPlacementStatus()); break;
                case 5: runSafely(() -> viewProfile()); break;
                case 6: runSafely(() -> viewResume()); break;
                default: back = true; break;
            }
        }
    }

    private void searchStudents() throws SQLException {
        String keyword = input.readRequiredString("Search by name, email or department: ");
        showStudents(studentService.searchStudents(keyword), "SEARCH RESULTS");
    }

    private void filterByDepartment() throws SQLException {
        String department = input.readRequiredString("Department (e.g. CSE): ");
        showStudents(studentService.getStudentsByDepartment(department),
                "STUDENTS IN " + department.trim().toUpperCase());
    }

    private void filterByPlacementStatus() throws InvalidInputException, SQLException {
        PlacementStatus status = parsePlacementStatus(
                input.readRequiredString("Status (PLACED or NOT_PLACED): "));

        List<Student> matching = new ArrayList<>();
        for (Student student : studentService.getAllStudents()) {
            if (student.getPlacementStatus() == status) {
                matching.add(student);
            }
        }
        showStudents(matching, "STUDENTS WITH STATUS " + status);
    }

    private void viewProfile() throws InvalidInputException, StudentNotFoundException, SQLException {
        int studentId = input.readInt("Student ID: ");
        Student student = studentService.findStudent(studentId);

        printTitle("STUDENT PROFILE");
        student.displayProfile();
        System.out.println("Address           : " + student.getAddress());
        System.out.println("Skills            : " + student.getSkills());
        System.out.println("10th / 12th       : " + student.getTenthPercentage() + "% / "
                + student.getTwelfthPercentage() + "%");
        System.out.println("Profile complete  : "
                + String.format("%.0f%%", studentService.getProfileCompletionPercentage(student)));
    }

    private void viewResume() throws InvalidInputException, StudentNotFoundException, SQLException {
        int studentId = input.readInt("Student ID: ");
        Student student = studentService.findStudent(studentId);
        Resume resume = resumeService.getResume(studentId);

        if (resume == null) {
            System.out.println("\n" + student.getName() + " has not created a resume yet.");
            return;
        }
        System.out.println();
        System.out.println(resumeService.buildResumeText(student, resume));
    }

    /** Prints students in a table. Used by every list option. */
    private void showStudents(List<Student> students, String title) {
        printTitle(title);
        if (students.isEmpty()) {
            System.out.println("No students found.");
            return;
        }
        System.out.println(String.format("%-5s %-22s %-8s %6s %9s  %s",
                "ID", "Name", "Dept", "CGPA", "Backlogs", "Status"));
        System.out.println("-".repeat(WIDTH));
        for (Student student : students) {
            System.out.println(String.format("%-5d %-22s %-8s %6.2f %9d  %s",
                    student.getStudentId(), student.getName(), student.getDepartment(),
                    student.getCgpa(), student.getBacklogs(), student.getPlacementStatus()));
        }
    }

    private PlacementStatus parsePlacementStatus(String text) throws InvalidInputException {
        try {
            return PlacementStatus.valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Status must be PLACED or NOT_PLACED.");
        }
    }

    // ---------- Shared helpers ----------

    private interface Step {
        void run() throws InvalidInputException, StudentNotFoundException, SQLException;
    }

    private void runSafely(Step step) {
        try {
            step.run();
        } catch (InvalidInputException | StudentNotFoundException e) {
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