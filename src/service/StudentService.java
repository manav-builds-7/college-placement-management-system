package service;

import dao.StudentDAO;
import exception.InvalidInputException;
import exception.StudentNotFoundException;
import model.Student;
import util.PasswordUtil;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;
import enums.PlacementStatus;

/**
 * Business rules for students: registration, login, profile, password, and
 * completion. No SQL here (that is the DAO's job) and no printing here (that is
 * the UI's job).
 */
public class StudentService {

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MIN_GRADUATION_YEAR = 2000;
    private static final int MAX_GRADUATION_YEAR = 2100;

    // The eight fields counted toward profile completion
    private static final int COMPLETION_FIELD_COUNT = 8;

    private final StudentDAO studentDAO;

    public StudentService(StudentDAO studentDAO) {
        this.studentDAO = studentDAO;
    }

    // ---------- Registration and login ----------
    /**
     * Registers a new student with basic details. Academic details are added
     * later from the profile screen.
     *
     * @return the saved student, with its new ID filled in
     */
    public Student registerStudent(String name, String email, String phone, String plainPassword)
            throws InvalidInputException, SQLException {
        ValidationUtil.requireNotBlank(name, "Name");
        ValidationUtil.requireEmail(email, "Email");
        ValidationUtil.requirePhone(phone, "Phone");
        validateNewPassword(plainPassword);

        String cleanEmail = email.trim();
        if (studentDAO.getStudentByEmail(cleanEmail) != null) {
            throw new InvalidInputException("This email is already registered.");
        }

        // Hash the password here, so the DAO and the database only ever see the hash
        String hash = PasswordUtil.hashPassword(plainPassword);
        Student student = new Student(name.trim(), cleanEmail, hash, phone.trim());
        studentDAO.addStudent(student);
        return student;
    }

    /**
     * Sets a student's placement status. Called by PlacementService during
     * selection, not directly from a menu.
     */
    public void setPlacementStatus(int studentId, PlacementStatus status)
            throws InvalidInputException, StudentNotFoundException, SQLException {
        findStudent(studentId);
        studentDAO.updatePlacementStatus(studentId, status);
    }

    /**
     * Checks an email and password at login. Both wrong-email and
     * wrong-password cases give the same message.
     *
     * @return the logged-in student
     */
    public Student login(String email, String plainPassword)
            throws InvalidInputException, SQLException {
        if (ValidationUtil.isBlank(email) || ValidationUtil.isBlank(plainPassword)) {
            throw new InvalidInputException("Email and password are required.");
        }

        Student student = studentDAO.getStudentByEmail(email.trim());
        if (student == null || !PasswordUtil.verifyPassword(plainPassword, student.getPasswordHash())) {
            throw new InvalidInputException("Invalid email or password.");
        }
        return student;
    }

    // ---------- Lookup ----------
    /**
     * Finds a student by ID, or throws if the ID is invalid or unknown. Other
     * services call this instead of talking to the DAO directly, so the "not
     * found" message is the same everywhere.
     */
    public Student findStudent(int studentId)
            throws InvalidInputException, StudentNotFoundException, SQLException {
        ValidationUtil.requirePositiveId(studentId, "Student ID");
        Student student = studentDAO.getStudentById(studentId);
        if (student == null) {
            throw new StudentNotFoundException("No student found with ID " + studentId + ".");
        }
        return student;
    }

    public List<Student> getAllStudents() throws SQLException {
        return studentDAO.getAllStudents();
    }

    public List<Student> searchStudents(String keyword) throws SQLException {
        return studentDAO.searchStudents(keyword == null ? "" : keyword.trim());
    }

    public List<Student> getStudentsByDepartment(String department) throws SQLException {
        return studentDAO.getStudentsByDepartment(department);
    }

    // ---------- Profile ----------
    /**
     * Saves a student's profile after checking every field. The menu sets
     * values on the Student object first, then calls this method. Email is not
     * changed here.
     */
    public void updateProfile(Student student)
            throws InvalidInputException, StudentNotFoundException, SQLException {
        ValidationUtil.requireNotBlank(student.getName(), "Name");
        ValidationUtil.requirePhone(student.getPhone(), "Phone");
        validateAcademicFields(student);

        boolean updated = studentDAO.updateStudent(student);
        if (!updated) {
            throw new StudentNotFoundException("No student found with ID " + student.getStudentId() + ".");
        }
    }

    /**
     * Checks academic fields. A value of 0 or a blank string means "not entered
     * yet", so those are allowed. Any value that is entered must be in range.
     */
    private void validateAcademicFields(Student student) throws InvalidInputException {
        if (student.getGraduationYear() != 0) {
            checkYearRange(student.getGraduationYear());
        }
        if (student.getCgpa() != 0) {
            ValidationUtil.requireCgpa(student.getCgpa(), "CGPA");
        }
        ValidationUtil.requireNonNegative(student.getBacklogs(), "Backlogs");
        if (student.getTenthPercentage() != 0) {
            ValidationUtil.requirePercentage(student.getTenthPercentage(), "10th percentage");
        }
        if (student.getTwelfthPercentage() != 0) {
            ValidationUtil.requirePercentage(student.getTwelfthPercentage(), "12th percentage");
        }
    }

    private void checkYearRange(int year) throws InvalidInputException {
        if (year < MIN_GRADUATION_YEAR || year > MAX_GRADUATION_YEAR) {
            throw new InvalidInputException("Graduation year must be between "
                    + MIN_GRADUATION_YEAR + " and " + MAX_GRADUATION_YEAR + ".");
        }
    }

    /**
     * Returns how much of the profile is filled in, from 0.0 to 100.0. A field
     * counts as filled when it is not blank or not zero.
     */
    public double getProfileCompletionPercentage(Student student) {
        int filled = 0;
        if (!ValidationUtil.isBlank(student.getDepartment())) {
            filled++;
        }
        if (!ValidationUtil.isBlank(student.getCourse())) {
            filled++;
        }
        if (student.getGraduationYear() != 0) {
            filled++;
        }
        if (student.getCgpa() != 0) {
            filled++;
        }
        if (student.getTenthPercentage() != 0) {
            filled++;
        }
        if (student.getTwelfthPercentage() != 0) {
            filled++;
        }
        if (!ValidationUtil.isBlank(student.getAddress())) {
            filled++;
        }
        if (!ValidationUtil.isBlank(student.getSkills())) {
            filled++;
        }
        return filled * 100.0 / COMPLETION_FIELD_COUNT;
    }

    // ---------- Password ----------
    /**
     * Changes a student's password. The old password must be correct, and the
     * new one must be long enough and different from the old one.
     */
    public void changePassword(int studentId, String oldPassword, String newPassword)
            throws InvalidInputException, StudentNotFoundException, SQLException {
        Student student = findStudent(studentId);

        if (!PasswordUtil.verifyPassword(oldPassword, student.getPasswordHash())) {
            throw new InvalidInputException("Current password is incorrect.");
        }
        validateNewPassword(newPassword);
        if (PasswordUtil.verifyPassword(newPassword, student.getPasswordHash())) {
            throw new InvalidInputException("New password must be different from the current one.");
        }

        studentDAO.updatePasswordHash(studentId, PasswordUtil.hashPassword(newPassword));
    }

    /**
     * Shared password rule used by registration and password change.
     */
    private void validateNewPassword(String password) throws InvalidInputException {
        if (ValidationUtil.isBlank(password)) {
            throw new InvalidInputException("Password is required.");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidInputException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
    }
}
