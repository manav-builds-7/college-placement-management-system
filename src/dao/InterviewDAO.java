package dao;

import enums.InterviewResult;
import enums.InterviewRound;
import model.Interview;
import util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * Database operations for interview rounds.
 * The rule that one round per application can be scheduled only once
 * is enforced by the UNIQUE constraint and checked in InterviewService.
 */
public class InterviewDAO {

    private static final String BASE_SELECT =
            "SELECT interview_id, application_id, round, interview_date, interview_time, "
          + "       mode, venue, interviewer, result, remarks "
          + "FROM interviews ";

    // ---------- CREATE ----------

    /** Inserts an interview and returns the generated interview_id. */
    public int addInterview(Interview interview) throws SQLException {
        String sql = "INSERT INTO interviews (application_id, round, interview_date, interview_time, "
                   + "mode, venue, interviewer, result, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, interview.getApplicationId());
            ps.setString(2, interview.getRound().name());
            ps.setDate(3, Date.valueOf(interview.getDate()));
            ps.setTime(4, Time.valueOf(interview.getTime()));
            ps.setString(5, interview.getMode());
            ps.setString(6, interview.getVenue());
            ps.setString(7, interview.getInterviewer());
            ps.setString(8, interview.getResult().name());
            ps.setString(9, interview.getRemarks());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    interview.setInterviewId(newId);
                    return newId;
                }
            }
        }
        throw new SQLException("Database did not return an ID for the new interview.");
    }

    // ---------- READ ----------

    /** Returns the interview with this ID, or null. */
    public Interview getInterviewById(int interviewId) throws SQLException {
        String sql = BASE_SELECT + "WHERE interview_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, interviewId);
            List<Interview> found = readInterviews(ps);
            return found.isEmpty() ? null : found.get(0);
        }
    }

    /**
     * Returns the interview for one round of one application, or null.
     * Used to stop the same round from being scheduled twice.
     */
    public Interview findByApplicationAndRound(int applicationId, InterviewRound round) throws SQLException {
        String sql = BASE_SELECT + "WHERE application_id = ? AND round = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            ps.setString(2, round.name());
            List<Interview> found = readInterviews(ps);
            return found.isEmpty() ? null : found.get(0);
        }
    }

    /** Returns every interview of one application, in date and time order. */
    public List<Interview> getInterviewsByApplication(int applicationId) throws SQLException {
        String sql = BASE_SELECT + "WHERE application_id = ? ORDER BY interview_date, interview_time, interview_id";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            return readInterviews(ps);
        }
    }

    /** Returns every interview. Used by reports later. */
    public List<Interview> getAllInterviews() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY interview_date, interview_time, interview_id";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            return readInterviews(ps);
        }
    }

    // ---------- UPDATE ----------

    /** Saves the result and remarks for one interview. */
    public boolean updateResult(int interviewId, InterviewResult result, String remarks) throws SQLException {
        String sql = "UPDATE interviews SET result = ?, remarks = ? WHERE interview_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, result.name());
            ps.setString(2, remarks);
            ps.setInt(3, interviewId);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------- Private helpers ----------

    private List<Interview> readInterviews(PreparedStatement ps) throws SQLException {
        List<Interview> interviews = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                interviews.add(mapRow(rs));
            }
        }
        return interviews;
    }

    /** The only place that maps interview columns to the Interview object. */
    private Interview mapRow(ResultSet rs) throws SQLException {
        return new Interview(
                rs.getInt("interview_id"),
                rs.getInt("application_id"),
                InterviewRound.valueOf(rs.getString("round")),
                rs.getDate("interview_date").toLocalDate(),
                rs.getTime("interview_time").toLocalTime(),
                rs.getString("mode"),
                rs.getString("venue"),
                rs.getString("interviewer"),
                InterviewResult.valueOf(rs.getString("result")),
                valueOrEmpty(rs.getString("remarks")));
    }

    /** Remarks are nullable in the schema. Convert null to "" so nothing prints as "null". */
    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}