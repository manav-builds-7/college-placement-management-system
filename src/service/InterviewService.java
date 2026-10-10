package service;

import dao.InterviewDAO;
import enums.ApplicationStatus;
import enums.InterviewResult;
import enums.InterviewRound;
import exception.InvalidInputException;
import exception.StudentNotFoundException;
import model.Application;
import model.Interview;
import model.Student;
import util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Business rules for interviews: which applications can be interviewed,
 * how rounds move the application through the placement process,
 * and when a result may be recorded.
 */
public class InterviewService {

    private final InterviewDAO interviewDAO;
    private final ApplicationService applicationService;

    public InterviewService(InterviewDAO interviewDAO, ApplicationService applicationService) {
        this.interviewDAO = interviewDAO;
        this.applicationService = applicationService;
    }

    /**
     * Schedules one interview round for a shortlisted application.
     * The application's status moves to match the round when a matching status exists.
     *
     * @return the saved interview, with its new ID filled in
     */
    public Interview scheduleInterview(Interview interview) throws InvalidInputException, SQLException {
        Application application = applicationService.findApplication(interview.getApplicationId());
        ApplicationStatus current = application.getStatus();

        if (!isOpenForInterviews(current)) {
            throw new InvalidInputException("Only shortlisted applications can have interviews. This one is "
                    + current + ".");
        }

        validateSchedule(interview);

        if (interviewDAO.findByApplicationAndRound(interview.getApplicationId(), interview.getRound()) != null) {
            throw new InvalidInputException("The " + interview.getRound() + " round is already scheduled for this application.");
        }

        // Check the status move before saving anything, so a refused move leaves no interview behind
        ApplicationStatus target = statusForRound(interview.getRound());
        if (target != null && target != current && !applicationService.canMoveTo(application, target)) {
            throw new InvalidInputException("Cannot schedule a " + interview.getRound()
                    + " interview while the application is " + current + ".");
        }

        interviewDAO.addInterview(interview);

        if (target != null && target != current) {
            applicationService.changeStatus(application.getApplicationId(), target);
        }
        return interview;
    }

    /**
     * Records the result of a round that has already taken place.
     * A result can be recorded only once, on or after the interview date,
     * and only while the application is still active.
     */
    public void recordResult(int interviewId, InterviewResult result, String remarks)
            throws InvalidInputException, SQLException {
        Interview interview = findInterview(interviewId);

        if (interview.getResult() != InterviewResult.PENDING) {
            throw new InvalidInputException("A result has already been recorded for this interview.");
        }
        if (result == null || result == InterviewResult.PENDING) {
            throw new InvalidInputException("The result must be PASSED or FAILED.");
        }
        if (interview.getDate().isAfter(LocalDate.now())) {
            throw new InvalidInputException("Results can be recorded only on or after the interview date ("
                    + interview.getDate() + ").");
        }

        Application application = applicationService.findApplication(interview.getApplicationId());
        if (isFinal(application.getStatus())) {
            throw new InvalidInputException("The application is " + application.getStatus()
                    + ", so results cannot be recorded.");
        }

        String cleanRemarks = remarks == null ? "" : remarks.trim();
        interviewDAO.updateResult(interviewId, result, cleanRemarks);
    }

    /** Finds an interview by ID, or throws if the ID is invalid or unknown. */
    public Interview findInterview(int interviewId) throws InvalidInputException, SQLException {
        ValidationUtil.requirePositiveId(interviewId, "Interview ID");
        Interview interview = interviewDAO.getInterviewById(interviewId);
        if (interview == null) {
            throw new InvalidInputException("No interview found with ID " + interviewId + ".");
        }
        return interview;
    }

    public List<Interview> getInterviewsForApplication(int applicationId)
            throws InvalidInputException, SQLException {
        applicationService.findApplication(applicationId);
        return interviewDAO.getInterviewsByApplication(applicationId);
    }

    /** Collects the interviews of every application a student has made. */
    public List<Interview> getInterviewsForStudent(int studentId)
            throws InvalidInputException, StudentNotFoundException, SQLException {
        List<Interview> all = new ArrayList<>();
        for (Application application : applicationService.getApplicationsForStudent(studentId)) {
            all.addAll(interviewDAO.getInterviewsByApplication(application.getApplicationId()));
        }
        return all;
    }

    public List<Interview> getAllInterviews() throws SQLException {
        return interviewDAO.getAllInterviews();
    }

    // ---------- Private helpers ----------

    /** Only shortlisted applications, or ones already in an interview stage, can be scheduled. */
    private boolean isOpenForInterviews(ApplicationStatus status) {
        return status == ApplicationStatus.SHORTLISTED
            || status == ApplicationStatus.APTITUDE_TEST
            || status == ApplicationStatus.TECHNICAL_INTERVIEW
            || status == ApplicationStatus.HR_INTERVIEW;
    }

    /** Statuses in which no result or move is allowed any more. */
    private boolean isFinal(ApplicationStatus status) {
        return status == ApplicationStatus.SELECTED
            || status == ApplicationStatus.REJECTED
            || status == ApplicationStatus.WITHDRAWN;
    }

    /**
     * The application status that matches a round.
     * MANAGERIAL has no matching status, so it returns null and the status stays as it is.
     */
    private ApplicationStatus statusForRound(InterviewRound round) {
        switch (round) {
            case APTITUDE:
                return ApplicationStatus.APTITUDE_TEST;
            case TECHNICAL:
                return ApplicationStatus.TECHNICAL_INTERVIEW;
            case HR:
                return ApplicationStatus.HR_INTERVIEW;
            default:
                return null;
        }
    }

    private void validateSchedule(Interview interview) throws InvalidInputException {
        if (interview.getRound() == null) {
            throw new InvalidInputException("Interview round is required.");
        }
        ValidationUtil.requireDate(interview.getDate(), "Interview date");
        if (interview.getTime() == null) {
            throw new InvalidInputException("Interview time is required.");
        }
        ValidationUtil.requireNotBlank(interview.getMode(), "Mode");
        ValidationUtil.requireNotBlank(interview.getVenue(), "Venue");
        ValidationUtil.requireNotBlank(interview.getInterviewer(), "Interviewer");

        if (interview.getDate().isBefore(LocalDate.now())) {
            throw new InvalidInputException("Interview date cannot be in the past.");
        }
    }
}