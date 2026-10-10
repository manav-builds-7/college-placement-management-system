package exception;

/** Thrown when no student matches the ID or email that was searched for. */
public class StudentNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public StudentNotFoundException(String message) {
        super(message);
    }
}