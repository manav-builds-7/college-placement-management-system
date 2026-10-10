package util;

import exception.InvalidInputException;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Input checks shared by all services. "isValidX" methods return true or false.
 * "requireX" methods throw InvalidInputException with a readable message.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN
            = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Indian mobile numbers: 10 digits, starting with 6, 7, 8, or 9
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");

    private static final double MIN_CGPA = 0.0;
    private static final double MAX_CGPA = 10.0;
    private static final double MIN_PERCENTAGE = 0.0;
    private static final double MAX_PERCENTAGE = 100.0;
    private static final int MIN_GRADUATION_YEAR = 2000;
    private static final int MAX_GRADUATION_YEAR = 2100;

    private ValidationUtil() {
    }

    // ---------- Boolean checks ----------
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // ---------- Requirement checks (throw on failure) ----------
    /**
     * Fails if the value is null or only spaces. fieldName is used in the
     * message.
     */
    public static void requireNotBlank(String value, String fieldName) throws InvalidInputException {
        if (isBlank(value)) {
            throw new InvalidInputException(fieldName + " is required.");
        }
    }

    public static void requireEmail(String email, String fieldName) throws InvalidInputException {
        if (!isValidEmail(email)) {
            throw new InvalidInputException(fieldName + " must be a valid email address (e.g. name@mail.com).");
        }
    }

    public static void requirePhone(String phone, String fieldName) throws InvalidInputException {
        if (!isValidPhone(phone)) {
            throw new InvalidInputException(fieldName + " must be a 10-digit mobile number starting with 6-9.");
        }
    }

    public static void requireCgpa(double cgpa, String fieldName) throws InvalidInputException {
        if (cgpa < MIN_CGPA || cgpa > MAX_CGPA) {
            throw new InvalidInputException(fieldName + " must be between 0 and 10. You entered " + cgpa + ".");
        }
    }

    public static void requirePercentage(double percentage, String fieldName) throws InvalidInputException {
        if (percentage < MIN_PERCENTAGE || percentage > MAX_PERCENTAGE) {
            throw new InvalidInputException(fieldName + " must be between 0 and 100. You entered " + percentage + ".");
        }
    }

    /**
     * IDs come from the database with AUTO_INCREMENT, so they must be positive.
     */
    public static void requirePositiveId(int id, String fieldName) throws InvalidInputException {
        if (id <= 0) {
            throw new InvalidInputException(fieldName + " must be a positive number.");
        }
    }

    public static void requireNonNegative(int value, String fieldName) throws InvalidInputException {
        if (value < 0) {
            throw new InvalidInputException(fieldName + " cannot be negative.");
        }
    }

    public static void requireDate(LocalDate date, String fieldName) throws InvalidInputException {
        if (date == null) {
            throw new InvalidInputException(fieldName + " is required.");
        }
    }

    // Add above the final closing brace of the class
    /**
     * A link is optional. If one is entered, it must start with http:// or
     * https://.
     */
    public static void requireOptionalUrl(String url, String fieldName) throws InvalidInputException {
        if (isBlank(url)) {
            return;
        }
        String lower = url.trim().toLowerCase();
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            throw new InvalidInputException(fieldName + " must start with http:// or https://.");
        }
    }

    /**
     * Graduation years are checked against a fixed range to catch typing
     * mistakes.
     */
    public static void requireGraduationYear(int year, String fieldName) throws InvalidInputException {
        if (year < MIN_GRADUATION_YEAR || year > MAX_GRADUATION_YEAR) {
            throw new InvalidInputException(fieldName + " must be between "
                    + MIN_GRADUATION_YEAR + " and " + MAX_GRADUATION_YEAR + ".");
        }
    }
}
