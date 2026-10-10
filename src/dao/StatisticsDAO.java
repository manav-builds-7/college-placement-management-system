package dao;

import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SQL-based statistics for the placement report.
 * Every query is a simple COUNT, MAX, MIN, AVG, or GROUP BY, as the spec asks.
 * None of these queries use user input, so they have no parameters.
 */
public class StatisticsDAO {

    // ---------- Counts ----------

    public int countStudents() throws SQLException {
        return countQuery("SELECT COUNT(*) FROM students");
    }

    public int countCompanies() throws SQLException {
        return countQuery("SELECT COUNT(*) FROM companies");
    }

    public int countJobs() throws SQLException {
        return countQuery("SELECT COUNT(*) FROM jobs");
    }

    public int countApplications() throws SQLException {
        return countQuery("SELECT COUNT(*) FROM applications");
    }

    /**
     * Applications that passed shortlisting: still in the process, or selected.
     * Rejected and withdrawn applications are not counted.
     */
    public int countShortlisted() throws SQLException {
        return countQuery("SELECT COUNT(*) FROM applications WHERE status IN "
                + "('SHORTLISTED', 'APTITUDE_TEST', 'TECHNICAL_INTERVIEW', 'HR_INTERVIEW', 'SELECTED')");
    }

    public int countPlacedStudents() throws SQLException {
        return countQuery("SELECT COUNT(*) FROM students WHERE placement_status = 'PLACED'");
    }

    // ---------- Package figures (in LPA) ----------

    public double getHighestPackage() throws SQLException {
        return singleDouble("SELECT MAX(ctc) FROM placements");
    }

    public double getLowestPackage() throws SQLException {
        return singleDouble("SELECT MIN(ctc) FROM placements");
    }

    public double getAveragePackage() throws SQLException {
        return singleDouble("SELECT AVG(ctc) FROM placements");
    }

    // ---------- Group totals ----------

    /** Number of students in each department. */
    public Map<String, Integer> getStudentCountByDepartment() throws SQLException {
        return groupCount("SELECT department, COUNT(*) FROM students "
                + "GROUP BY department ORDER BY department");
    }

    /** Number of placed students in each department. */
    public Map<String, Integer> getPlacedCountByDepartment() throws SQLException {
        return groupCount("SELECT s.department, COUNT(*) FROM placements p "
                + "JOIN students s ON p.student_id = s.student_id "
                + "GROUP BY s.department ORDER BY s.department");
    }

    /** Number of placements made by each company. */
    public Map<String, Integer> getPlacedCountByCompany() throws SQLException {
        return groupCount("SELECT c.company_name, COUNT(*) FROM placements p "
                + "JOIN companies c ON p.company_id = c.company_id "
                + "GROUP BY c.company_name ORDER BY c.company_name");
    }

    // ---------- Private helpers ----------

    /** Runs a query that returns one whole number, such as COUNT(*). */
    private int countQuery(String sql) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * Runs a query that returns one decimal number, such as MAX(ctc).
     * MAX, MIN, and AVG return NULL when there are no rows. That becomes 0.0 here.
     */
    private double singleDouble(String sql) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            double value = rs.getDouble(1);
            return rs.wasNull() ? 0.0 : value;
        }
    }

    /**
     * Runs a GROUP BY query and returns label-to-count pairs.
     * LinkedHashMap keeps the order from ORDER BY, so reports list groups alphabetically.
     */
    private Map<String, Integer> groupCount(String sql) throws SQLException {
        Map<String, Integer> result = new LinkedHashMap<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString(1), rs.getInt(2));
            }
        }
        return result;
    }
}