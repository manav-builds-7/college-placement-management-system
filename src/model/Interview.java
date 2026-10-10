package model;

import enums.InterviewResult;
import enums.InterviewRound;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * One interview round for one application.
 * An application can have several Interview records, one per round.
 */
public class Interview {

    private int interviewId;
    private int applicationId;
    private InterviewRound round;
    private LocalDate date;
    private LocalTime time;
    private String mode;          // e.g. "Online" or "On-campus"
    private String venue;
    private String interviewer;
    private InterviewResult result;
    private String remarks;

    /**
     * Creates a newly scheduled interview.
     * The result starts as PENDING and remarks start empty,
     * because the interview has not happened yet.
     */
    public Interview(int applicationId, InterviewRound round, LocalDate date, LocalTime time,
                     String mode, String venue, String interviewer) {
        this(0, applicationId, round, date, time, mode, venue, interviewer,
             InterviewResult.PENDING, "");
    }

    /** Creates an interview with all fields, typically loaded from the database. */
    public Interview(int interviewId, int applicationId, InterviewRound round, LocalDate date,
                     LocalTime time, String mode, String venue, String interviewer,
                     InterviewResult result, String remarks) {
        this.interviewId = interviewId;
        this.applicationId = applicationId;
        this.round = round;
        this.date = date;
        this.time = time;
        this.mode = mode;
        this.venue = venue;
        this.interviewer = interviewer;
        this.result = result;
        this.remarks = remarks;
    }

    public int getInterviewId() { return interviewId; }
    public void setInterviewId(int interviewId) { this.interviewId = interviewId; }

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public InterviewRound getRound() { return round; }
    public void setRound(InterviewRound round) { this.round = round; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getInterviewer() { return interviewer; }
    public void setInterviewer(String interviewer) { this.interviewer = interviewer; }

    public InterviewResult getResult() { return result; }
    public void setResult(InterviewResult result) { this.result = result; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}