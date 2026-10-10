package service;

import dao.PlacementDriveDAO;
import enums.DriveStatus;
import exception.CompanyNotFoundException;
import exception.InvalidInputException;
import exception.JobNotFoundException;
import model.Job;
import model.PlacementDrive;
import util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Business rules for placement drives: valid dates, jobs that belong to the company,
 * the status lifecycle, and which drives are open for student registration.
 */
public class PlacementDriveService {

    private final PlacementDriveDAO driveDAO;
    private final CompanyService companyService;
    private final JobService jobService;

    public PlacementDriveService(PlacementDriveDAO driveDAO, CompanyService companyService,
                                 JobService jobService) {
        this.driveDAO = driveDAO;
        this.companyService = companyService;
        this.jobService = jobService;
    }

    /**
     * Creates a drive. The company must exist, the dates must make sense,
     * and every job must exist and belong to that company.
     */
    public PlacementDrive createDrive(PlacementDrive drive)
            throws InvalidInputException, CompanyNotFoundException, JobNotFoundException, SQLException {
        companyService.findCompany(drive.getCompanyId());
        validateDrive(drive);
        validateDriveJobs(drive);

        LocalDate today = LocalDate.now();
        if (drive.getDriveDate().isBefore(today)) {
            throw new InvalidInputException("Drive date cannot be in the past.");
        }
        if (drive.getRegistrationDeadline().isBefore(today)) {
            throw new InvalidInputException("Registration deadline cannot be in the past.");
        }

        driveDAO.addDrive(drive);
        return drive;
    }

    /**
     * Saves changes to a drive's details and jobs.
     * The status is kept as stored, so status changes must go through changeStatus.
     */
    public void updateDrive(PlacementDrive drive)
            throws InvalidInputException, CompanyNotFoundException, JobNotFoundException, SQLException {
        PlacementDrive existing = findDrive(drive.getDriveId());
        if (existing.getStatus() == DriveStatus.COMPLETED || existing.getStatus() == DriveStatus.CANCELLED) {
            throw new InvalidInputException("A " + existing.getStatus().name().toLowerCase()
                    + " drive cannot be edited.");
        }

        companyService.findCompany(drive.getCompanyId());
        validateDrive(drive);
        validateDriveJobs(drive);

        boolean deadlineChanged = !existing.getRegistrationDeadline().equals(drive.getRegistrationDeadline());
        if (deadlineChanged && drive.getRegistrationDeadline().isBefore(LocalDate.now())) {
            throw new InvalidInputException("A new registration deadline cannot be in the past.");
        }

        drive.setStatus(existing.getStatus());
        driveDAO.updateDrive(drive);
    }

    /**
     * Moves a drive to a new status, but only along an allowed path.
     * For example, a drive cannot jump from UPCOMING straight to COMPLETED.
     */
    public void changeStatus(int driveId, DriveStatus newStatus)
            throws InvalidInputException, SQLException {
        PlacementDrive drive = findDriveOrThrow(driveId);
        DriveStatus current = drive.getStatus();

        if (!isAllowedTransition(current, newStatus)) {
            throw new InvalidInputException("A drive cannot move from " + current + " to " + newStatus + ".");
        }
        driveDAO.updateStatus(driveId, newStatus);
    }

    /** Finds a drive by ID, or throws if the ID is invalid or unknown. */
    public PlacementDrive findDrive(int driveId) throws InvalidInputException, SQLException {
        ValidationUtil.requirePositiveId(driveId, "Drive ID");
        return findDriveOrThrow(driveId);
    }

    public List<PlacementDrive> getAllDrives() throws SQLException {
        return driveDAO.getAllDrives();
    }

    public List<PlacementDrive> getDrivesForCompany(int companyId)
            throws InvalidInputException, CompanyNotFoundException, SQLException {
        companyService.findCompany(companyId);
        return driveDAO.getDrivesByCompany(companyId);
    }

    /** Returns the drives that students can register for today. */
    public List<PlacementDrive> getOpenDrives() throws SQLException {
        List<PlacementDrive> open = new ArrayList<>();
        for (PlacementDrive drive : driveDAO.getAllDrives()) {
            if (isRegistrationOpen(drive)) {
                open.add(drive);
            }
        }
        return open;
    }

    /**
     * True when the drive is OPEN and today is not after its registration deadline.
     * The deadline day itself still counts as open, matching the job deadline rule.
     */
    public boolean isRegistrationOpen(PlacementDrive drive) {
        return drive.getStatus() == DriveStatus.OPEN
                && !LocalDate.now().isAfter(drive.getRegistrationDeadline());
    }

    // ---------- Private helpers ----------

    private PlacementDrive findDriveOrThrow(int driveId) throws InvalidInputException, SQLException {
        PlacementDrive drive = driveDAO.getDriveById(driveId);
        if (drive == null) {
            throw new InvalidInputException("No placement drive found with ID " + driveId + ".");
        }
        return drive;
    }

    /**
     * The allowed status moves:
     * UPCOMING -> OPEN or CANCELLED, OPEN -> CLOSED or CANCELLED,
     * CLOSED -> COMPLETED or CANCELLED. COMPLETED and CANCELLED are final.
     */
    private boolean isAllowedTransition(DriveStatus from, DriveStatus to) {
        switch (from) {
            case UPCOMING:
                return to == DriveStatus.OPEN || to == DriveStatus.CANCELLED;
            case OPEN:
                return to == DriveStatus.CLOSED || to == DriveStatus.CANCELLED;
            case CLOSED:
                return to == DriveStatus.COMPLETED || to == DriveStatus.CANCELLED;
            default:
                return false;   // COMPLETED and CANCELLED cannot change
        }
    }

    /** Shared rules for create and update that do not depend on jobs. */
    private void validateDrive(PlacementDrive drive) throws InvalidInputException {
        ValidationUtil.requireDate(drive.getDriveDate(), "Drive date");
        ValidationUtil.requireDate(drive.getRegistrationDeadline(), "Registration deadline");
        ValidationUtil.requireNotBlank(drive.getVenue(), "Venue");

        if (drive.getRegistrationDeadline().isAfter(drive.getDriveDate())) {
            throw new InvalidInputException("Registration deadline must be on or before the drive date.");
        }
    }

    /** A drive must offer at least one job, and every job must belong to the drive's company. */
    private void validateDriveJobs(PlacementDrive drive)
            throws InvalidInputException, JobNotFoundException, SQLException {
        if (drive.getJobIds().isEmpty()) {
            throw new InvalidInputException("A drive must offer at least one job.");
        }
        for (int jobId : drive.getJobIds()) {
            Job job = jobService.findJob(jobId);
            if (job.getCompanyId() != drive.getCompanyId()) {
                throw new InvalidInputException("Job " + job.getTitle()
                        + " belongs to a different company and cannot be added to this drive.");
            }
        }
    }
}