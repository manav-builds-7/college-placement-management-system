package exception;

/**
 * Thrown when a student does not meet a job's eligibility rules.
 * The message holds the exact reason, for example the CGPA shortfall.
 */
public class IneligibleStudentException extends Exception {

    private static final long serialVersionUID = 1L;

    public IneligibleStudentException(String message) {
        super(message);
    }
}