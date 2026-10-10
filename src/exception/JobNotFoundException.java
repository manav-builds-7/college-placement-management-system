package exception;

/** Thrown when no job matches the ID that was searched for. */
public class JobNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public JobNotFoundException(String message) {
        super(message);
    }
}