package service;

import dao.ApplicationDAO;
import enums.ApplicationStatus;
import exception.DuplicateApplicationException;
import exception.IneligibleStudentException;
import exception.InvalidInputException;
import exception.JobNotFoundException;
import exception.StudentNotFoundException;
import model.Application;
import model.Job;
import model.Student;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Business rules for applications: the apply checks, the eligibility engine,
 * and the status moves through the placement process.
 */
public class ApplicationService {

    private final ApplicationDAO applicationDAO;
    private final StudentService studentService;
    private final JobService jobService;

    public ApplicationService(ApplicationDAO applicationDAO, StudentService studentService,
            JobService jobService) {
        this.applicationDAO = applicationDAO;
        this.studentService = studentService;
        this.jobService = jobService;
    }

    // ---------- Applying ----------
    /**
     * Applies a student to a job. The checks run in the order the spec lists
     * them, so the first failure is the reason the student sees.
     *
     * @return the new application, with status APPLIED
     */
    public Application applyForJob(int studentId, int jobId)
            throws InvalidInputException, StudentNotFoundException, JobNotFoundException,
            IneligibleStudentException, DuplicateApplicationException, SQLException {
        Student student = studentService.findStudent(studentId);
        Job job = jobService.findJob(jobId);

        if (student.isPlaced()) {
            throw new InvalidInputException("You have already been placed, so you cannot apply to new jobs.");
        }
        if (jobService.isDeadlinePassed(job)) {
            throw new InvalidInputException("Applications for " + job.getTitle()
                    + " closed on " + job.getApplicationDeadline() + ".");
        }

        checkEligibility(student, job);

        if (applicationDAO.findByStudentAndJob(studentId, jobId) != null) {
            throw new DuplicateApplicationException("You have already applied for " + job.getTitle() + ".");
        }

        Application application = new Application(studentId, jobId);
        applicationDAO.addApplication(application);
        return application;
    }

    /**
     * Lets a student withdraw their own application.
     */
    public void withdraw(int studentId, int applicationId)
            throws InvalidInputException, StudentNotFoundException, SQLException {
        studentService.findStudent(studentId);
        Application application = findApplication(applicationId);

        if (application.getStudentId() != studentId) {
            throw new InvalidInputException("This application does not belong to you.");
        }
        moveTo(applicationId, ApplicationStatus.WITHDRAWN);
    }

    // ---------- Eligibility engine ----------
    /**
     * Returns every reason the student is not eligible. An empty list means the
     * student meets all the criteria. This method does not use the database, so
     * it can be tested on its own.
     */
    /**
     * Returns every reason the student is not eligible. An empty list means the
     * student meets all the criteria. This method does not use the database, so
     * it can be tested on its own.
     */
    public List<String> findEligibilityIssues(Student student, Job job) {
        List<String> issues = new ArrayList<>();

        if (student.getCgpa() == 0) {
            issues.add("Not eligible: your CGPA is not entered in your profile.");
        } else if (student.getCgpa() < job.getMinimumCgpa()) {
            issues.add("Not eligible: minimum CGPA required is " + job.getMinimumCgpa()
                    + ", student's CGPA is " + student.getCgpa() + ".");
        }

        if (job.getMinimumTenth() > 0) {
            if (student.getTenthPercentage() == 0) {
                issues.add("Not eligible: your 10th percentage is not entered in your profile.");
            } else if (student.getTenthPercentage() < job.getMinimumTenth()) {
                issues.add("Not eligible: minimum 10th percentage required is " + job.getMinimumTenth()
                        + ", student's 10th percentage is " + student.getTenthPercentage() + ".");
            }
        }

        if (job.getMinimumTwelfth() > 0) {
            if (student.getTwelfthPercentage() == 0) {
                issues.add("Not eligible: your 12th percentage is not entered in your profile.");
            } else if (student.getTwelfthPercentage() < job.getMinimumTwelfth()) {
                issues.add("Not eligible: minimum 12th percentage required is " + job.getMinimumTwelfth()
                        + ", student's 12th percentage is " + student.getTwelfthPercentage() + ".");
            }
        }

        if (student.getBacklogs() > job.getMaximumBacklogs()) {
            issues.add("Not eligible: maximum backlogs allowed is " + job.getMaximumBacklogs()
                    + ", student has " + student.getBacklogs() + ".");
        }

        if (!isBranchEligible(student.getDepartment(), job.getEligibleBranches())) {
            issues.add("Not eligible: your branch " + student.getDepartment()
                    + " is not in the eligible branches (" + job.getEligibleBranches() + ").");
        }

        if (student.getGraduationYear() != job.getGraduationYear()) {
            issues.add("Not eligible: this job is for the " + job.getGraduationYear()
                    + " batch, but your graduation year is " + student.getGraduationYear() + ".");
        }

        return issues;
    }

    /**
     * True when the student meets every criterion.
     */
    public boolean isEligible(Student student, Job job) {
        return findEligibilityIssues(student, job).isEmpty();
    }

    /**
     * Throws IneligibleStudentException with the first reason, if any. The
     * message matches the spec's example, for example: "Not eligible: minimum
     * CGPA required is 7.5, student's CGPA is 6.9."
     */
    public void checkEligibility(Student student, Job job) throws IneligibleStudentException {
        List<String> issues = findEligibilityIssues(student, job);
        if (!issues.isEmpty()) {
            throw new IneligibleStudentException(issues.get(0));
        }
    }

    // ---------- Status moves ----------
    /**
     * Moves an application to a new status, as the officer shortlists or
     * rejects it. SELECTED is refused here, because selection must also create
     * a placement record.
     */
    public void changeStatus(int applicationId, ApplicationStatus newStatus)
            throws InvalidInputException, SQLException {
        if (newStatus == ApplicationStatus.SELECTED) {
            throw new InvalidInputException("Use the selection step so the placement record is created.");
        }
        moveTo(applicationId, newStatus);
    }

    /**
     * Marks an application as SELECTED. Called by PlacementService in Phase 11,
     * which also creates the placement record. The same transition rules apply.
     */
    public void markSelected(int applicationId) throws InvalidInputException, SQLException {
        moveTo(applicationId, ApplicationStatus.SELECTED);
    }

    // ---------- Lookup ----------
    /**
     * Finds an application by ID, or throws if the ID is invalid or unknown.
     */
    public Application findApplication(int applicationId) throws InvalidInputException, SQLException {
        ValidationUtil.requirePositiveId(applicationId, "Application ID");
        Application application = applicationDAO.getApplicationById(applicationId);
        if (application == null) {
            throw new InvalidInputException("No application found with ID " + applicationId + ".");
        }
        return application;
    }

    public List<Application> getApplicationsForStudent(int studentId)
            throws InvalidInputException, StudentNotFoundException, SQLException {
        studentService.findStudent(studentId);
        return applicationDAO.getApplicationsByStudent(studentId);
    }

    public List<Application> getApplicationsForJob(int jobId)
            throws InvalidInputException, JobNotFoundException, SQLException {
        jobService.findJob(jobId);
        return applicationDAO.getApplicationsByJob(jobId);
    }

    public List<Application> getApplicationsByStatus(ApplicationStatus status) throws SQLException {
        return applicationDAO.getApplicationsByStatus(status);
    }

    public List<Application> getAllApplications() throws SQLException {
        return applicationDAO.getAllApplications();
    }

    // ---------- Private helpers ----------
    /**
     * Checks that the move is allowed, then saves it. Every status change goes
     * through here.
     */
    private void moveTo(int applicationId, ApplicationStatus newStatus)
            throws InvalidInputException, SQLException {
        Application application = findApplication(applicationId);
        ApplicationStatus current = application.getStatus();

        if (!isAllowedTransition(current, newStatus)) {
            throw new InvalidInputException("Cannot move an application from " + current + " to " + newStatus + ".");
        }
        applicationDAO.updateStatus(applicationId, newStatus);
    }

    /**
     * The status table for the placement process. SELECTED, REJECTED, and
     * WITHDRAWN are final.
     */
    private boolean isAllowedTransition(ApplicationStatus from, ApplicationStatus to) {
        switch (from) {
            case APPLIED:
                return to == ApplicationStatus.SHORTLISTED
                        || to == ApplicationStatus.REJECTED
                        || to == ApplicationStatus.WITHDRAWN;
            case SHORTLISTED:
                return to == ApplicationStatus.APTITUDE_TEST
                        || to == ApplicationStatus.TECHNICAL_INTERVIEW
                        || to == ApplicationStatus.HR_INTERVIEW
                        || to == ApplicationStatus.REJECTED
                        || to == ApplicationStatus.WITHDRAWN;
            case APTITUDE_TEST:
                return to == ApplicationStatus.TECHNICAL_INTERVIEW
                        || to == ApplicationStatus.HR_INTERVIEW
                        || to == ApplicationStatus.REJECTED
                        || to == ApplicationStatus.WITHDRAWN;
            case TECHNICAL_INTERVIEW:
                return to == ApplicationStatus.HR_INTERVIEW
                        || to == ApplicationStatus.SELECTED
                        || to == ApplicationStatus.REJECTED
                        || to == ApplicationStatus.WITHDRAWN;
            case HR_INTERVIEW:
                return to == ApplicationStatus.SELECTED
                        || to == ApplicationStatus.REJECTED
                        || to == ApplicationStatus.WITHDRAWN;
            default:
                return false;   // SELECTED, REJECTED, and WITHDRAWN cannot change
        }
    }

    /**
     * Uses the same comparison as the placement-branch check, ignoring case and
     * spaces. "CSE, IT" is split into "CSE" and "IT" before comparing.
     */
    private boolean isBranchEligible(String studentBranch, String eligibleBranches) {
        if (ValidationUtil.isBlank(studentBranch) || ValidationUtil.isBlank(eligibleBranches)) {
            return false;
        }
        for (String branch : eligibleBranches.split(",")) {
            if (branch.trim().equalsIgnoreCase(studentBranch.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * True when the application may move to the target status. Lets other
     * services check a move before they save related records.
     */
    public boolean canMoveTo(Application application, ApplicationStatus target) {
        return isAllowedTransition(application.getStatus(), target);
    }
}
