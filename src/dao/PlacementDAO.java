package dao;

import enums.OfferStatus;
import model.Placement;
import util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Database operations for placement records.
 * The student_id column is UNIQUE, so one student can hold only one placement.
 */
public class PlacementDAO {

    private static final String BASE_SELECT =
            "SELECT placement_id, application_id, student_id, company_id, job_id, ctc, "
          + "       placement_date, offer_status, joining_date "
          + "FROM placements ";

    // ---------- CREATE ----------

    /** Inserts a placement record and returns the generated placement_id. */
    public int addPlacement(Placement placement) throws SQLException {
        String sql = "INSERT INTO placements (application_id, student_id, company_id, job_id, ctc, "
                   + "placement_date, offer_status, joining_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, placement.getApplicationId());
            ps.setInt(2, placement.getStudentId());
            ps.setInt(3, placement.getCompanyId());
            ps.setInt(4, placement.getJobId());
            ps.setDouble(5, placement.getCtc());
            ps.setDate(6, Date.valueOf(placement.getPlacementDate()));
            ps.setString(7, placement.getOfferStatus().name());
            if (placement.getJoiningDate() == null) {
                ps.setNull(8, Types.DATE);
            } else {
                ps.setDate(8, Date.valueOf(placement.getJoiningDate()));
            }
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    placement.setPlacementId(newId);
                    return newId;
                }
            }
        }
        throw new SQLException("Database did not return an ID for the new placement.");
    }

    // ---------- READ ----------

    /** Returns the placement with this ID, or null. */
    public Placement getPlacementById(int placementId) throws SQLException {
        String sql = BASE_SELECT + "WHERE placement_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, placementId);
            List<Placement> found = readPlacements(ps);
            return found.isEmpty() ? null : found.get(0);
        }
    }

    /** Returns a student's placement, or null if they have none. */
    public Placement getPlacementByStudent(int studentId) throws SQLException {
        String sql = BASE_SELECT + "WHERE student_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            List<Placement> found = readPlacements(ps);
            return found.isEmpty() ? null : found.get(0);
        }
    }

    /** Returns every placement, most recent first. */
    public List<Placement> getAllPlacements() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY placement_date DESC, placement_id DESC";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            return readPlacements(ps);
        }
    }

    // ---------- UPDATE ----------

    /** Records the student's response to the offer. */
    public boolean updateOfferStatus(int placementId, OfferStatus offerStatus) throws SQLException {
        String sql = "UPDATE placements SET offer_status = ? WHERE placement_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, offerStatus.name());
            ps.setInt(2, placementId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Records when the student joins the company. */
    public boolean updateJoiningDate(int placementId, LocalDate joiningDate) throws SQLException {
        String sql = "UPDATE placements SET joining_date = ? WHERE placement_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(joiningDate));
            ps.setInt(2, placementId);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------- DELETE ----------

    /**
     * Removes a placement record. Used only to undo a selection that failed part way through.
     */
    public boolean deletePlacement(int placementId) throws SQLException {
        String sql = "DELETE FROM placements WHERE placement_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, placementId);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------- Private helpers ----------

    private List<Placement> readPlacements(PreparedStatement ps) throws SQLException {
        List<Placement> placements = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                placements.add(mapRow(rs));
            }
        }
        return placements;
    }

    /** The only place that maps placement columns to the Placement object. */
    private Placement mapRow(ResultSet rs) throws SQLException {
        Date joining = rs.getDate("joining_date");   // null until the student joins

        Placement placement = new Placement(
                rs.getInt("placement_id"),
                rs.getInt("student_id"),
                rs.getInt("company_id"),
                rs.getInt("job_id"),
                rs.getDouble("ctc"),
                rs.getDate("placement_date").toLocalDate(),
                OfferStatus.valueOf(rs.getString("offer_status")),
                joining == null ? null : joining.toLocalDate());
        placement.setApplicationId(rs.getInt("application_id"));
        return placement;
    }
}