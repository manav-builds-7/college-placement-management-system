package exception;

/** Thrown when no company matches the ID that was searched for. */
public class CompanyNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public CompanyNotFoundException(String message) {
        super(message);
    }
}