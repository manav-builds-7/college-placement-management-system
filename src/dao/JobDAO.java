package dao;

import enums.JobType;
import model.Job;
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
 * Database operations for jobs.
 * Stores and retrieves job rows. Eligibility and deadline rules are in the service layer.
 */
public class JobDAO {

    private static final String BASE_SELECT =
            "SELECT job_id, company_id, title, description, job_type, location, ctc, "
          + "       minimum_cgpa, maximum_backlogs, eligible_branches, graduation_year, "
          + "       application_deadline, minimum_tenth, minimum_twelfth "
          + "FROM jobs ";

    // ---------- CREATE ----------

    /** Inserts a job and returns the generated job_id. Also sets it on the object. */
    public int addJob(Job job) throws SQLException {
        String sql = "INSERT INTO jobs (company_id, title, description, job_type, location, ctc, "
                   + "minimum_cgpa, maximum_backlogs, eligible_branches, graduation_year, "
                   + "application_deadline, minimum_tenth, minimum_twelfth) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindJob(ps, job);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    job.setJobId(newId);
                    return newId;
                }
            }
        }
        throw new SQLException("Database did not return an ID for the new job.");
    }

    // ---------- READ ----------

    /** Returns the job with this ID, or null if none exists. */
    public Job getJobById(int jobId) throws SQLException {
        String sql = BASE_SELECT + "WHERE job_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /** Returns every job, ordered by title. */
    public List<Job> getAllJobs() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY title";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            return readJobs(ps);
        }
    }

    /** Returns the jobs offered by one company. */
    public List<Job> getJobsByCompany(int companyId) throws SQLException {
        String sql = BASE_SELECT + "WHERE company_id = ? ORDER BY title";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, companyId);
            return readJobs(ps);
        }
    }

    /** Returns jobs whose title or location contains the keyword. */
    public List<Job> searchJobs(String keyword) throws SQLException {
        String sql = BASE_SELECT + "WHERE title LIKE ? OR location LIKE ? ORDER BY title";
        String pattern = "%" + keyword + "%";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            return readJobs(ps);
        }
    }

    /** Counts applications made to a job. The service checks this before deleting. */
    public int countApplicationsForJob(int jobId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM applications WHERE job_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    // ---------- UPDATE ----------

    /** Saves changes to an existing job. Returns true if a row was updated. */
    public boolean updateJob(Job job) throws SQLException {
        String sql = "UPDATE jobs SET company_id = ?, title = ?, description = ?, job_type = ?, "
                   + "location = ?, ctc = ?, minimum_cgpa = ?, maximum_backlogs = ?, "
                   + "eligible_branches = ?, graduation_year = ?, application_deadline = ?, "
                   + "minimum_tenth = ?, minimum_twelfth = ? "
                   + "WHERE job_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bindJob(ps, job);
            ps.setInt(14, job.getJobId());
            return ps.executeUpdate() > 0;
        }
    }

    // ---------- DELETE ----------

    /** Deletes a job. Fails with a foreign-key error if applications exist. The service checks first. */
    public boolean deleteJob(int jobId) throws SQLException {
        String sql = "DELETE FROM jobs WHERE job_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, jobId);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------- Private helpers ----------

    /** Sets the 13 shared parameters used by both INSERT and UPDATE, in the same order. */
    private void bindJob(PreparedStatement ps, Job job) throws SQLException {
        ps.setInt(1, job.getCompanyId());
        ps.setString(2, job.getTitle());
        ps.setString(3, job.getDescription());
        ps.setString(4, job.getJobType().name());
        ps.setString(5, job.getLocation());
        ps.setDouble(6, job.getCtc());
        ps.setDouble(7, job.getMinimumCgpa());
        ps.setInt(8, job.getMaximumBacklogs());
        ps.setString(9, job.getEligibleBranches());
        ps.setInt(10, job.getGraduationYear());
        ps.setDate(11, Date.valueOf(job.getApplicationDeadline()));
        ps.setDouble(12, job.getMinimumTenth());
        ps.setDouble(13, job.getMinimumTwelfth());
    }

    private List<Job> readJobs(PreparedStatement ps) throws SQLException {
        List<Job> jobs = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                jobs.add(mapRow(rs));
            }
        }
        return jobs;
    }

    /** The only place that maps job columns to the Job object. */
    private Job mapRow(ResultSet rs) throws SQLException {
        Job job = new Job(
                rs.getInt("job_id"),
                rs.getInt("company_id"),
                rs.getString("title"),
                rs.getString("description"),
                JobType.valueOf(rs.getString("job_type")),
                rs.getString("location"),
                rs.getDouble("ctc"),
                rs.getDouble("minimum_cgpa"),
                rs.getInt("maximum_backlogs"),
                rs.getString("eligible_branches"),
                rs.getInt("graduation_year"),
                rs.getDate("application_deadline").toLocalDate());
        job.setMinimumTenth(rs.getDouble("minimum_tenth"));
        job.setMinimumTwelfth(rs.getDouble("minimum_twelfth"));
        return job;
    }
}