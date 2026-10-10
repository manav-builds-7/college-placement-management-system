package exception;

/** Thrown when a student tries to apply to a job they have already applied to. */
public class DuplicateApplicationException extends Exception {

    private static final long serialVersionUID = 1L;

    public DuplicateApplicationException(String message) {
        super(message);
    }
}