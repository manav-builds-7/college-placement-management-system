package exception;

/** Thrown when a value fails validation, such as a bad email or a CGPA above 10. */
public class InvalidInputException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidInputException(String message) {
        super(message);
    }
}