package service;

import dao.PlacementDAO;
import enums.ApplicationStatus;
import enums.InterviewResult;
import enums.OfferStatus;
import enums.PlacementStatus;
import exception.InvalidInputException;
import exception.JobNotFoundException;
import exception.StudentNotFoundException;
import model.Application;
import model.Interview;
import model.Job;
import model.Placement;
import model.Student;
import util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Business rules for the selection flow: selecting a student, recording the offer
 * response, recording the joining date, and listing placed and unplaced students.
 */
public class PlacementService {

    private final PlacementDAO placementDAO;
    private final ApplicationService applicationService;
    private final InterviewService interviewService;
    private final StudentService studentService;
    private final JobService jobService;

    public PlacementService(PlacementDAO placementDAO, ApplicationService applicationService,
                            InterviewService interviewService, StudentService studentService,
                            JobService jobService) {
        this.placementDAO = placementDAO;
        this.applicationService = applicationService;
        this.interviewService = interviewService;
        this.studentService = studentService;
        this.jobService = jobService;
    }

    /**
     * Selects the student for the job in this application.
     * Creates the placement record, marks the application SELECTED,
     * and sets the student's status to PLACED.
     *
     * @return the new placement record
     */
    public Placement selectStudent(int applicationId)
            throws InvalidInputException, StudentNotFoundException, JobNotFoundException, SQLException {
        // 1. Checks that need no writes
        Application application = applicationService.findApplication(applicationId);
        if (!applicationService.canMoveTo(application, ApplicationStatus.SELECTED)) {
            throw new InvalidInputException("Cannot select an application that is "
                    + application.getStatus() + ".");
        }
        checkInterviewsPassed(applicationId);

        Student student = studentService.findStudent(application.getStudentId());
        if (student.isPlaced() || placementDAO.getPlacementByStudent(student.getStudentId()) != null) {
            throw new InvalidInputException(student.getName() + " is already placed.");
        }
        Job job = jobService.findJob(application.getJobId());

        // 2. Writes, with a compensating delete if a later step fails
        Placement placement = new Placement(student.getStudentId(), job.getCompanyId(),
                job.getJobId(), job.getCtc(), LocalDate.now());
        placement.setApplicationId(applicationId);
        placementDAO.addPlacement(placement);

        try {
            applicationService.markSelected(applicationId);
            studentService.setPlacementStatus(student.getStudentId(), PlacementStatus.PLACED);
        } catch (InvalidInputException | StudentNotFoundException | SQLException e) {
            placementDAO.deletePlacement(placement.getPlacementId());
            throw e;
        }
        return placement;
    }

    /**
     * Records the student's response to the offer.
     * Only ACCEPTED is supported, because a student may hold just one placement record.
     */
    public void recordOfferResponse(int placementId, OfferStatus response)
            throws InvalidInputException, SQLException {
        Placement placement = findPlacement(placementId);

        if (placement.getOfferStatus() != OfferStatus.OFFERED) {
            throw new InvalidInputException("This offer has already been " + placement.getOfferStatus() + ".");
        }
        if (response != OfferStatus.ACCEPTED) {
            throw new InvalidInputException("Only an accepted offer can be recorded. Declined offers are not "
                    + "supported, because each student holds one placement record.");
        }

        placementDAO.updateOfferStatus(placementId, OfferStatus.ACCEPTED);
    }

    /** Records when an accepted offer's student joins the company. */
    public void recordJoiningDate(int placementId, LocalDate joiningDate)
            throws InvalidInputException, SQLException {
        Placement placement = findPlacement(placementId);
        ValidationUtil.requireDate(joiningDate, "Joining date");

        if (placement.getOfferStatus() != OfferStatus.ACCEPTED) {
            throw new InvalidInputException("The offer must be accepted before a joining date is recorded.");
        }
        if (joiningDate.isBefore(placement.getPlacementDate())) {
            throw new InvalidInputException("Joining date cannot be before the placement date ("
                    + placement.getPlacementDate() + ").");
        }

        placementDAO.updateJoiningDate(placementId, joiningDate);
    }

    /** Finds a placement by ID, or throws if the ID is invalid or unknown. */
    public Placement findPlacement(int placementId) throws InvalidInputException, SQLException {
        ValidationUtil.requirePositiveId(placementId, "Placement ID");
        Placement placement = placementDAO.getPlacementById(placementId);
        if (placement == null) {
            throw new InvalidInputException("No placement found with ID " + placementId + ".");
        }
        return placement;
    }

    public Placement getPlacementForStudent(int studentId)
            throws InvalidInputException, StudentNotFoundException, SQLException {
        studentService.findStudent(studentId);
        return placementDAO.getPlacementByStudent(studentId);
    }

    /** Every placement record, most recent first. These are the placed students. */
    public List<Placement> getAllPlacements() throws SQLException {
        return placementDAO.getAllPlacements();
    }

    /** Students whose status is still NOT_PLACED. */
    public List<Student> getUnplacedStudents() throws SQLException {
        List<Student> unplaced = new ArrayList<>();
        for (Student student : studentService.getAllStudents()) {
            if (!student.isPlaced()) {
                unplaced.add(student);
            }
        }
        return unplaced;
    }

    // ---------- Private helpers ----------

    /**
     * Every interview must have a PASSED result before selection.
     * At least one interview must exist.
     */
    private void checkInterviewsPassed(int applicationId) throws InvalidInputException, SQLException {
        List<Interview> interviews = interviewService.getInterviewsForApplication(applicationId);
        if (interviews.isEmpty()) {
            throw new InvalidInputException("Schedule and record at least one interview before selection.");
        }
        for (Interview interview : interviews) {
            if (interview.getResult() == InterviewResult.PENDING) {
                throw new InvalidInputException("Record the result of the " + interview.getRound()
                        + " round before selection.");
            }
            if (interview.getResult() == InterviewResult.FAILED) {
                throw new InvalidInputException("The " + interview.getRound()
                        + " round was failed, so the student cannot be selected.");
            }
        }
    }
}