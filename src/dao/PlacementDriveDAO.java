package dao;

import enums.DriveStatus;
import model.PlacementDrive;
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
 * Database operations for placement drives and their job links.
 * Drive rows and drive_jobs rows are always written together, in one transaction.
 */
public class PlacementDriveDAO {

    private static final String BASE_SELECT =
            "SELECT drive_id, company_id, drive_date, registration_deadline, venue, status "
          + "FROM placement_drives ";

    // ---------- CREATE ----------

    /**
     * Saves a drive and its job links in one transaction.
     * If any part fails, nothing is saved.
     *
     * @return the new drive_id
     */
    public int addDrive(PlacementDrive drive) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int newId = insertDrive(connection, drive);
                insertDriveJobs(connection, newId, drive.getJobIds());
                connection.commit();
                drive.setDriveId(newId);
                return newId;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    // ---------- READ ----------

    /** Returns the drive with this ID, including its job IDs, or null. */
    public PlacementDrive getDriveById(int driveId) throws SQLException {
        String sql = BASE_SELECT + "WHERE drive_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, driveId);
            List<PlacementDrive> found = readDrives(connection, ps);
            return found.isEmpty() ? null : found.get(0);
        }
    }

    /** Returns every drive, earliest first. */
    public List<PlacementDrive> getAllDrives() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY drive_date";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            return readDrives(connection, ps);
        }
    }

    /** Returns the drives of one company. */
    public List<PlacementDrive> getDrivesByCompany(int companyId) throws SQLException {
        String sql = BASE_SELECT + "WHERE company_id = ? ORDER BY drive_date";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, companyId);
            return readDrives(connection, ps);
        }
    }

    // ---------- UPDATE ----------

    /**
     * Saves changes to a drive's details and replaces its job links.
     * The status column is not changed here. Use updateStatus for that.
     */
    public boolean updateDrive(PlacementDrive drive) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int rows = updateDriveRow(connection, drive);
                deleteDriveJobs(connection, drive.getDriveId());
                insertDriveJobs(connection, drive.getDriveId(), drive.getJobIds());
                connection.commit();
                return rows > 0;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    /** Changes only the status column. The service checks that the move is allowed. */
    public boolean updateStatus(int driveId, DriveStatus status) throws SQLException {
        String sql = "UPDATE placement_drives SET status = ? WHERE drive_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, driveId);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------- COUNT ----------

    /** Counts drives belonging to a company. Used by the delete rules in CompanyService. */
    public int countDrivesForCompany(int companyId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM placement_drives WHERE company_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    // ---------- Private helpers ----------

    private int insertDrive(Connection connection, PlacementDrive drive) throws SQLException {
        String sql = "INSERT INTO placement_drives (company_id, drive_date, registration_deadline, "
                   + "venue, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, drive.getCompanyId());
            ps.setDate(2, Date.valueOf(drive.getDriveDate()));
            ps.setDate(3, Date.valueOf(drive.getRegistrationDeadline()));
            ps.setString(4, drive.getVenue());
            ps.setString(5, drive.getStatus().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Database did not return an ID for the new drive.");
    }

    private int updateDriveRow(Connection connection, PlacementDrive drive) throws SQLException {
        String sql = "UPDATE placement_drives SET company_id = ?, drive_date = ?, "
                   + "registration_deadline = ?, venue = ? WHERE drive_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, drive.getCompanyId());
            ps.setDate(2, Date.valueOf(drive.getDriveDate()));
            ps.setDate(3, Date.valueOf(drive.getRegistrationDeadline()));
            ps.setString(4, drive.getVenue());
            ps.setInt(5, drive.getDriveId());
            return ps.executeUpdate();
        }
    }

    /** Inserts one drive_jobs row per job ID. An empty list inserts nothing. */
    private void insertDriveJobs(Connection connection, int driveId, List<Integer> jobIds)
            throws SQLException {
        String sql = "INSERT INTO drive_jobs (drive_id, job_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int jobId : jobIds) {
                ps.setInt(1, driveId);
                ps.setInt(2, jobId);
                ps.executeUpdate();
            }
        }
    }

    private void deleteDriveJobs(Connection connection, int driveId) throws SQLException {
        String sql = "DELETE FROM drive_jobs WHERE drive_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, driveId);
            ps.executeUpdate();
        }
    }

    /**
     * Converts result rows to drives and loads each drive's job IDs.
     * It reuses the caller's connection, so the work stays within one connection.
     */
    private List<PlacementDrive> readDrives(Connection connection, PreparedStatement ps)
            throws SQLException {
        List<PlacementDrive> drives = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PlacementDrive drive = mapRow(rs);
                loadJobIds(connection, drive);
                drives.add(drive);
            }
        }
        return drives;
    }

    /** Fills the drive's job list from drive_jobs. */
    private void loadJobIds(Connection connection, PlacementDrive drive) throws SQLException {
        String sql = "SELECT job_id FROM drive_jobs WHERE drive_id = ? ORDER BY job_id";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, drive.getDriveId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    drive.addJob(rs.getInt("job_id"));
                }
            }
        }
    }

    /** The only place that maps drive columns to the PlacementDrive object. */
    private PlacementDrive mapRow(ResultSet rs) throws SQLException {
        return new PlacementDrive(
                rs.getInt("drive_id"),
                rs.getInt("company_id"),
                rs.getDate("drive_date").toLocalDate(),
                rs.getDate("registration_deadline").toLocalDate(),
                rs.getString("venue"),
                DriveStatus.valueOf(rs.getString("status")));
    }
}