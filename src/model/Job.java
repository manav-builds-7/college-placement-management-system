package model;

import enums.JobType;
import interfaces.Reportable;

import java.time.LocalDate;

/**
 * A job or internship offered by a company. Holds the eligibility criteria used
 * by the eligibility engine.
 */
public class Job implements Reportable {

    private int jobId;
    private int companyId;
    private String title;
    private String description;
    private JobType jobType;
    private String location;
    private double ctc;                  // in LPA (lakhs per annum)
    private double minimumCgpa;
    private int maximumBacklogs;
    private String eligibleBranches;     // comma-separated, e.g. "CSE, IT, ECE"
    private int graduationYear;
    private LocalDate applicationDeadline;
    private double minimumTenth;
    private double minimumTwelfth;

    /**
     * Creates a new job that has not been saved yet. The jobId is 0 until the
     * database assigns one.
     */
    public Job(int companyId, String title, String description, JobType jobType,
            String location, double ctc, double minimumCgpa, int maximumBacklogs,
            String eligibleBranches, int graduationYear, LocalDate applicationDeadline) {
        this(0, companyId, title, description, jobType, location, ctc, minimumCgpa,
                maximumBacklogs, eligibleBranches, graduationYear, applicationDeadline);
    }

    public double getMinimumTenth() {
        return minimumTenth;
    }

    public void setMinimumTenth(double minimumTenth) {
        this.minimumTenth = minimumTenth;
    }

    public double getMinimumTwelfth() {
        return minimumTwelfth;
    }

    public void setMinimumTwelfth(double minimumTwelfth) {
        this.minimumTwelfth = minimumTwelfth;
    }

    /**
     * Creates a job with all fields, typically loaded from the database.
     */
    public Job(int jobId, int companyId, String title, String description, JobType jobType,
            String location, double ctc, double minimumCgpa, int maximumBacklogs,
            String eligibleBranches, int graduationYear, LocalDate applicationDeadline) {
        this.jobId = jobId;
        this.companyId = companyId;
        this.title = title;
        this.description = description;
        this.jobType = jobType;
        this.location = location;
        this.ctc = ctc;
        this.minimumCgpa = minimumCgpa;
        this.maximumBacklogs = maximumBacklogs;
        this.eligibleBranches = eligibleBranches;
        this.graduationYear = graduationYear;
        this.applicationDeadline = applicationDeadline;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getCtc() {
        return ctc;
    }

    public void setCtc(double ctc) {
        this.ctc = ctc;
    }

    public double getMinimumCgpa() {
        return minimumCgpa;
    }

    public void setMinimumCgpa(double minimumCgpa) {
        this.minimumCgpa = minimumCgpa;
    }

    public int getMaximumBacklogs() {
        return maximumBacklogs;
    }

    public void setMaximumBacklogs(int maximumBacklogs) {
        this.maximumBacklogs = maximumBacklogs;
    }

    public String getEligibleBranches() {
        return eligibleBranches;
    }

    public void setEligibleBranches(String eligibleBranches) {
        this.eligibleBranches = eligibleBranches;
    }

    public int getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(int graduationYear) {
        this.graduationYear = graduationYear;
    }

    public LocalDate getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(LocalDate applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }

    @Override
    public String generateReport() {
        return "Job Title    : " + title + "\n"
                + "Job Type     : " + jobType + "\n"
                + "Location     : " + location + "\n"
                + "CTC          : " + ctc + " LPA\n"
                + "Min CGPA     : " + minimumCgpa + "\n"
                + "Min 10th %   : " + minimumTenth + "\n"
                + "Min 12th %   : " + minimumTwelfth + "\n"
                + "Max Backlogs : " + maximumBacklogs + "\n"
                + "Branches     : " + eligibleBranches + "\n"
                + "Grad Year    : " + graduationYear + "\n"
                + "Deadline     : " + applicationDeadline;
    }
}
