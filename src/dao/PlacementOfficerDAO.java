package dao;

import model.PlacementOfficer;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database operations for placement officers.
 * An officer is stored in users (login details) and placement_officers (officer details).
 */
public class PlacementOfficerDAO {

    private static final String BASE_SELECT =
            "SELECT u.user_id, u.name, u.email, u.password_hash, u.phone, "
          + "       po.employee_id, po.designation "
          + "FROM placement_officers po "
          + "JOIN users u ON po.officer_id = u.user_id ";

    /**
     * Inserts an officer into users and placement_officers in one transaction.
     * Same pattern as StudentDAO.addStudent.
     *
     * @return the generated user ID, which is also the officer ID
     */
    public int addOfficer(PlacementOfficer officer) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int newId = insertUser(connection, officer);
                insertOfficerDetails(connection, newId, officer);
                connection.commit();
                officer.setUserId(newId);
                return newId;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    /** Returns the officer with this ID, or null. */
    public PlacementOfficer getOfficerById(int officerId) throws SQLException {
        String sql = BASE_SELECT + "WHERE po.officer_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, officerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /** Returns the officer with this email, or null. Used for login. */
    public PlacementOfficer getOfficerByEmail(String email) throws SQLException {
        String sql = BASE_SELECT + "WHERE u.email = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // ---------- Private helpers ----------

    private int insertUser(Connection connection, PlacementOfficer officer) throws SQLException {
        String sql = "INSERT INTO users (name, email, password_hash, phone, role) "
                   + "VALUES (?, ?, ?, ?, 'PLACEMENT_OFFICER')";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, officer.getName());
            ps.setString(2, officer.getEmail());
            ps.setString(3, officer.getPasswordHash());
            ps.setString(4, officer.getPhone());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Database did not return an ID for the new officer.");
    }

    private void insertOfficerDetails(Connection connection, int officerId, PlacementOfficer officer)
            throws SQLException {
        String sql = "INSERT INTO placement_officers (officer_id, employee_id, designation) "
                   + "VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, officerId);
            ps.setString(2, officer.getEmployeeId());
            ps.setString(3, officer.getDesignation());
            ps.executeUpdate();
        }
    }

    private PlacementOfficer mapRow(ResultSet rs) throws SQLException {
        return new PlacementOfficer(
                rs.getInt("user_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("phone"),
                rs.getString("employee_id"),
                rs.getString("designation"));
    }
}