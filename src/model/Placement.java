package model;

import enums.OfferStatus;

import java.time.LocalDate;

/**
 * The final record created when a student is selected. Statistics such as
 * average package and placement rate read from here.
 */
public class Placement {

    private int placementId;
    private int studentId;
    private int companyId;
    private int jobId;
    private double ctc;                  // package in LPA
    private LocalDate placementDate;
    private OfferStatus offerStatus;
    private LocalDate joiningDate;       // null until the student joins
    // Add with the other fields
    private int applicationId;

    // Add with the other getters and setters
    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Creates a new placement record right after selection. The offer starts as
     * OFFERED, and joiningDate is unknown (null) for now.
     */
    public Placement(int studentId, int companyId, int jobId, double ctc, LocalDate placementDate) {
        this(0, studentId, companyId, jobId, ctc, placementDate, OfferStatus.OFFERED, null);
    }

    /**
     * Creates a placement with all fields, typically loaded from the database.
     */
    public Placement(int placementId, int studentId, int companyId, int jobId, double ctc,
            LocalDate placementDate, OfferStatus offerStatus, LocalDate joiningDate) {
        this.placementId = placementId;
        this.studentId = studentId;
        this.companyId = companyId;
        this.jobId = jobId;
        this.ctc = ctc;
        this.placementDate = placementDate;
        this.offerStatus = offerStatus;
        this.joiningDate = joiningDate;
    }

    public int getPlacementId() {
        return placementId;
    }

    public void setPlacementId(int placementId) {
        this.placementId = placementId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public double getCtc() {
        return ctc;
    }

    public void setCtc(double ctc) {
        this.ctc = ctc;
    }

    public LocalDate getPlacementDate() {
        return placementDate;
    }

    public void setPlacementDate(LocalDate placementDate) {
        this.placementDate = placementDate;
    }

    public OfferStatus getOfferStatus() {
        return offerStatus;
    }

    public void setOfferStatus(OfferStatus offerStatus) {
        this.offerStatus = offerStatus;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }
}
