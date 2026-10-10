package model;

import enums.ApplicationStatus;

import java.time.LocalDate;

/**
 * One student's application to one job.
 * The status moves through the placement process over time.
 */
public class Application {

    private int applicationId;
    private int studentId;
    private int jobId;
    private LocalDate applicationDate;
    private ApplicationStatus status;

    /**
     * Creates a new application that has not been saved yet.
     * The status starts as APPLIED and the date is today.
     * (The id is 0 until the database assigns one.)
     */
    public Application(int studentId, int jobId) {
        this(0, studentId, jobId, LocalDate.now(), ApplicationStatus.APPLIED);
    }

    /** Creates an application with all fields, typically loaded from the database. */
    public Application(int applicationId, int studentId, int jobId,
                       LocalDate applicationDate, ApplicationStatus status) {
        this.applicationId = applicationId;
        this.studentId = studentId;
        this.jobId = jobId;
        this.applicationDate = applicationDate;
        this.status = status;
    }

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getJobId() { return jobId; }
    public void setJobId(int jobId) { this.jobId = jobId; }

    public LocalDate getApplicationDate() { return applicationDate; }
    public void setApplicationDate(LocalDate applicationDate) { this.applicationDate = applicationDate; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
}