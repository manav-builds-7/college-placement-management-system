package service;

import dao.PlacementOfficerDAO;
import exception.InvalidInputException;
import model.PlacementOfficer;
import model.Student;
import util.PasswordUtil;
import util.ValidationUtil;

import java.sql.SQLException;

/**
 * The single entry point for logging in. Students and officers have separate
 * accounts, so each login checks the matching table. Both return the same
 * message for a wrong email and a wrong password.
 */
public class AuthService {

    private final StudentService studentService;
    private final PlacementOfficerDAO officerDAO;

    public AuthService(StudentService studentService, PlacementOfficerDAO officerDAO) {
        this.studentService = studentService;
        this.officerDAO = officerDAO;
    }

    /** Student login. The rules live in StudentService, so they are not repeated here. */
    public Student loginStudent(String email, String password) throws InvalidInputException, SQLException {
        return studentService.login(email, password);
    }

    /** Officer login. Checks the placement_officers table and the stored password hash. */
    public PlacementOfficer loginOfficer(String email, String password)
            throws InvalidInputException, SQLException {
        if (ValidationUtil.isBlank(email) || ValidationUtil.isBlank(password)) {
            throw new InvalidInputException("Email and password are required.");
        }

        PlacementOfficer officer = officerDAO.getOfficerByEmail(email.trim());
        if (officer == null || !PasswordUtil.verifyPassword(password, officer.getPasswordHash())) {
            throw new InvalidInputException("Invalid email or password.");
        }
        return officer;
    }
}