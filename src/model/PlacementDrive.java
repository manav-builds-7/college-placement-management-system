package model;

import enums.DriveStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A campus visit by one company, which may offer several jobs.
 * The jobs are stored as a list of job IDs, mirroring the drive_jobs table.
 */
public class PlacementDrive {

    private int driveId;
    private int companyId;
    private LocalDate driveDate;
    private LocalDate registrationDeadline;
    private String venue;
    private DriveStatus status;

    // Collection of job IDs offered in this drive. Private, so only addJob() changes it.
    private final List<Integer> jobIds = new ArrayList<>();

    /**
     * Creates a new drive that has not been saved yet.
     * Its status starts as UPCOMING.
     */
    public PlacementDrive(int companyId, LocalDate driveDate,
                          LocalDate registrationDeadline, String venue) {
        this(0, companyId, driveDate, registrationDeadline, venue, DriveStatus.UPCOMING);
    }

    /** Creates a drive with all fields, typically loaded from the database. */
    public PlacementDrive(int driveId, int companyId, LocalDate driveDate,
                          LocalDate registrationDeadline, String venue, DriveStatus status) {
        this.driveId = driveId;
        this.companyId = companyId;
        this.driveDate = driveDate;
        this.registrationDeadline = registrationDeadline;
        this.venue = venue;
        this.status = status;
    }

    /** Adds a job to this drive. Duplicate job IDs are ignored. */
    public void addJob(int jobId) {
        if (!jobIds.contains(jobId)) {
            jobIds.add(jobId);
        }
    }

    /**
     * Returns the job IDs as a read-only view.
     * Callers can read the list but cannot change it directly.
     */
    public List<Integer> getJobIds() {
        return Collections.unmodifiableList(jobIds);
    }

    public int getDriveId() { return driveId; }
    public void setDriveId(int driveId) { this.driveId = driveId; }

    public int getCompanyId() { return companyId; }
    public void setCompanyId(int companyId) { this.companyId = companyId; }

    public LocalDate getDriveDate() { return driveDate; }
    public void setDriveDate(LocalDate driveDate) { this.driveDate = driveDate; }

    public LocalDate getRegistrationDeadline() { return registrationDeadline; }
    public void setRegistrationDeadline(LocalDate registrationDeadline) { this.registrationDeadline = registrationDeadline; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public DriveStatus getStatus() { return status; }
    public void setStatus(DriveStatus status) { this.status = status; }
}