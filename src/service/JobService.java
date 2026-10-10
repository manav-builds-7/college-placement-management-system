package service;

import dao.JobDAO;
import exception.CompanyNotFoundException;
import exception.InvalidInputException;
import exception.JobNotFoundException;
import model.Job;
import util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Business rules for jobs: valid eligibility criteria, deadline rules, and no
 * deletion of jobs that students have already applied to.
 */
public class JobService {

    private final JobDAO jobDAO;
    private final CompanyService companyService;

    public JobService(JobDAO jobDAO, CompanyService companyService) {
        this.jobDAO = jobDAO;
        this.companyService = companyService;
    }

    /**
     * Adds a job. The company must exist, and the deadline cannot already be
     * past.
     *
     * @return the saved job, with its new ID filled in
     */
    public Job addJob(Job job)
            throws InvalidInputException, CompanyNotFoundException, SQLException {
        companyService.findCompany(job.getCompanyId());
        validateJob(job);

        if (job.getApplicationDeadline().isBefore(LocalDate.now())) {
            throw new InvalidInputException("Application deadline cannot be in the past.");
        }

        jobDAO.addJob(job);
        return job;
    }

    /**
     * Saves changes to a job. A past deadline is rejected only when the officer
     * changes the deadline to one. Other edits to an old job are still allowed.
     */
    public void updateJob(Job job)
            throws InvalidInputException, CompanyNotFoundException, JobNotFoundException, SQLException {
        Job existing = findJob(job.getJobId());
        companyService.findCompany(job.getCompanyId());
        validateJob(job);

        boolean deadlineChanged = !existing.getApplicationDeadline().equals(job.getApplicationDeadline());
        if (deadlineChanged && job.getApplicationDeadline().isBefore(LocalDate.now())) {
            throw new InvalidInputException("A new application deadline cannot be in the past.");
        }

        jobDAO.updateJob(job);
    }

    /**
     * Deletes a job only when no student has applied to it. Deleting it would
     * otherwise erase application history.
     */
    public void deleteJob(int jobId) throws InvalidInputException, JobNotFoundException, SQLException {
        Job job = findJob(jobId);

        int applicationCount = jobDAO.countApplicationsForJob(jobId);
        if (applicationCount > 0) {
            throw new InvalidInputException("The job '" + job.getTitle() + "' has "
                    + applicationCount + " application(s) and cannot be deleted.");
        }

        jobDAO.deleteJob(jobId);
    }

    /**
     * Finds a job by ID, or throws if the ID is invalid or unknown.
     */
    public Job findJob(int jobId) throws InvalidInputException, JobNotFoundException, SQLException {
        ValidationUtil.requirePositiveId(jobId, "Job ID");
        Job job = jobDAO.getJobById(jobId);
        if (job == null) {
            throw new JobNotFoundException("No job found with ID " + jobId + ".");
        }
        return job;
    }

    public List<Job> getAllJobs() throws SQLException {
        return jobDAO.getAllJobs();
    }

    public List<Job> getJobsForCompany(int companyId)
            throws InvalidInputException, CompanyNotFoundException, SQLException {
        companyService.findCompany(companyId);
        return jobDAO.getJobsByCompany(companyId);
    }

    public List<Job> searchJobs(String keyword) throws SQLException {
        return jobDAO.searchJobs(keyword == null ? "" : keyword.trim());
    }

    /**
     * Returns only the jobs that students can still apply to today.
     */
    public List<Job> getOpenJobs() throws SQLException {
        List<Job> open = new ArrayList<>();
        for (Job job : jobDAO.getAllJobs()) {
            if (!isDeadlinePassed(job)) {
                open.add(job);
            }
        }
        return open;
    }

    /**
     * True when today is after the job's deadline. A job whose deadline is
     * today is still open. The application rules in Phase 9 use this same
     * method.
     */
    public boolean isDeadlinePassed(Job job) {
        return LocalDate.now().isAfter(job.getApplicationDeadline());
    }

    // ---------- Private helpers ----------
    /**
     * Shared rules for add and update, including the eligibility criteria.
     */
    /**
     * Shared rules for add and update, including the eligibility criteria.
     */
    private void validateJob(Job job) throws InvalidInputException {
        ValidationUtil.requireNotBlank(job.getTitle(), "Job title");
        ValidationUtil.requireNotBlank(job.getLocation(), "Location");
        ValidationUtil.requireNotBlank(job.getEligibleBranches(), "Eligible branches");
        ValidationUtil.requireDate(job.getApplicationDeadline(), "Application deadline");

        if (job.getJobType() == null) {
            throw new InvalidInputException("Job type is required.");
        }
        if (job.getCtc() <= 0) {
            throw new InvalidInputException("CTC must be greater than 0 LPA.");
        }

        // Eligibility criteria. A value of 0 means "no requirement", which is allowed.
        ValidationUtil.requireCgpa(job.getMinimumCgpa(), "Minimum CGPA");
        ValidationUtil.requirePercentage(job.getMinimumTenth(), "Minimum 10th percentage");
        ValidationUtil.requirePercentage(job.getMinimumTwelfth(), "Minimum 12th percentage");
        ValidationUtil.requireNonNegative(job.getMaximumBacklogs(), "Maximum backlogs");
        ValidationUtil.requireGraduationYear(job.getGraduationYear(), "Graduation year");
    }
}
