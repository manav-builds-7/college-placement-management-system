package dao;

import enums.ApplicationStatus;
import model.Application;
import util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Database operations for applications.
 * Stores and fetches rows. The duplicate and eligibility rules are in ApplicationService.
 */
public class ApplicationDAO {

    private static final String BASE_SELECT =
            "SELECT application_id, student_id, job_id, application_date, status "
          + "FROM applications ";

    // ---------- CREATE ----------

    /** Inserts an application and returns the generated application_id. */
    public int addApplication(Application application) throws SQLException {
        String sql = "INSERT INTO applications (student_id, job_id, application_date, status) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, application.getStudentId());
            ps.setInt(2, application.getJobId());
            ps.setDate(3, Date.valueOf(application.getApplicationDate()));
            ps.setString(4, application.getStatus().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    application.setApplicationId(newId);
                    return newId;
                }
            }
        }
        throw new SQLException("Database did not return an ID for the new application.");
    }

    // ---------- READ ----------

    /** Returns the application with this ID, or null. */
    public Application getApplicationById(int applicationId) throws SQLException {
        String sql = BASE_SELECT + "WHERE application_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            List<Application> found = readApplications(ps);
            return found.isEmpty() ? null : found.get(0);
        }
    }

    /**
     * Returns the application this student made to this job, or null.
     * Used for the duplicate check.
     */
    public Application findByStudentAndJob(int studentId, int jobId) throws SQLException {
        String sql = BASE_SELECT + "WHERE student_id = ? AND job_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, jobId);
            List<Application> found = readApplications(ps);
            return found.isEmpty() ? null : found.get(0);
        }
    }

    /** Returns one student's applications, newest first. */
    public List<Application> getApplicationsByStudent(int studentId) throws SQLException {
        String sql = BASE_SELECT + "WHERE student_id = ? ORDER BY application_date DESC, application_id DESC";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            return readApplications(ps);
        }
    }

    /** Returns every application made to one job. */
    public List<Application> getApplicationsByJob(int jobId) throws SQLException {
        String sql = BASE_SELECT + "WHERE job_id = ? ORDER BY application_id";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, jobId);
            return readApplications(ps);
        }
    }

    /** Returns applications in one status, such as all SHORTLISTED ones. */
    public List<Application> getApplicationsByStatus(ApplicationStatus status) throws SQLException {
        String sql = BASE_SELECT + "WHERE status = ? ORDER BY application_id";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            return readApplications(ps);
        }
    }

    /** Returns every application. */
    public List<Application> getAllApplications() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY application_id";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            return readApplications(ps);
        }
    }

    // ---------- UPDATE ----------

    /** Changes the status of one application. The service checks the move first. */
    public boolean updateStatus(int applicationId, ApplicationStatus status) throws SQLException {
        String sql = "UPDATE applications SET status = ? WHERE application_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, applicationId);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------- Private helpers ----------

    private List<Application> readApplications(PreparedStatement ps) throws SQLException {
        List<Application> applications = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                applications.add(mapRow(rs));
            }
        }
        return applications;
    }

    /** The only place that maps application columns to the Application object. */
    private Application mapRow(ResultSet rs) throws SQLException {
        return new Application(
                rs.getInt("application_id"),
                rs.getInt("student_id"),
                rs.getInt("job_id"),
                rs.getDate("application_date").toLocalDate(),
                ApplicationStatus.valueOf(rs.getString("status")));
    }
}