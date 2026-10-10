package dao;

import model.Resume;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database operations for resumes.
 * Each student has at most one resume, identified by student_id.
 */
public class ResumeDAO {

    private static final String BASE_SELECT =
            "SELECT resume_id, student_id, education, skills, projects, certifications, "
          + "       achievements, internships, programming_languages, linkedin, github, portfolio "
          + "FROM resumes ";

    /** Inserts a resume and returns the generated resume_id. Also sets it on the object. */
    public int addResume(Resume resume) throws SQLException {
        String sql = "INSERT INTO resumes (student_id, education, skills, projects, certifications, "
                   + "achievements, internships, programming_languages, linkedin, github, portfolio) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindResume(ps, resume);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    resume.setResumeId(newId);
                    return newId;
                }
            }
        }
        throw new SQLException("Database did not return an ID for the new resume.");
    }

    /** Returns the resume belonging to this student, or null if none exists yet. */
    public Resume getResumeByStudentId(int studentId) throws SQLException {
        String sql = BASE_SELECT + "WHERE student_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /** Saves changes to a student's resume. Returns true if a row was updated. */
    public boolean updateResume(Resume resume) throws SQLException {
        String sql = "UPDATE resumes SET education = ?, skills = ?, projects = ?, certifications = ?, "
                   + "achievements = ?, internships = ?, programming_languages = ?, linkedin = ?, "
                   + "github = ?, portfolio = ? WHERE student_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, resume.getEducation());
            ps.setString(2, resume.getSkills());
            ps.setString(3, resume.getProjects());
            ps.setString(4, resume.getCertifications());
            ps.setString(5, resume.getAchievements());
            ps.setString(6, resume.getInternships());
            ps.setString(7, resume.getProgrammingLanguages());
            ps.setString(8, resume.getLinkedin());
            ps.setString(9, resume.getGithub());
            ps.setString(10, resume.getPortfolio());
            ps.setInt(11, resume.getStudentId());
            return ps.executeUpdate() > 0;
        }
    }

    // ---------- Private helpers ----------

    /**
     * Sets the 11 INSERT parameters from a resume.
     * Shared by addResume so the column order is written in one place.
     */
    private void bindResume(PreparedStatement ps, Resume resume) throws SQLException {
        ps.setInt(1, resume.getStudentId());
        ps.setString(2, resume.getEducation());
        ps.setString(3, resume.getSkills());
        ps.setString(4, resume.getProjects());
        ps.setString(5, resume.getCertifications());
        ps.setString(6, resume.getAchievements());
        ps.setString(7, resume.getInternships());
        ps.setString(8, resume.getProgrammingLanguages());
        ps.setString(9, resume.getLinkedin());
        ps.setString(10, resume.getGithub());
        ps.setString(11, resume.getPortfolio());
    }

    /** The only place that maps resume columns to the Resume object. */
    private Resume mapRow(ResultSet rs) throws SQLException {
        return new Resume(
                rs.getInt("resume_id"),
                rs.getInt("student_id"),
                valueOrEmpty(rs.getString("education")),
                valueOrEmpty(rs.getString("skills")),
                valueOrEmpty(rs.getString("projects")),
                valueOrEmpty(rs.getString("certifications")),
                valueOrEmpty(rs.getString("achievements")),
                valueOrEmpty(rs.getString("internships")),
                valueOrEmpty(rs.getString("programming_languages")),
                valueOrEmpty(rs.getString("linkedin")),
                valueOrEmpty(rs.getString("github")),
                valueOrEmpty(rs.getString("portfolio")));
    }

    /** Nullable columns come back as null. Convert them to "" so nothing prints as "null". */
    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}