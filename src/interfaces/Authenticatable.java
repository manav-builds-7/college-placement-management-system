package interfaces;

/**
 * Any user type that can log into the system.
 * Student and PlacementOfficer both implement this, so the login
 * screen can treat them in the same way (runtime polymorphism).
 */
public interface Authenticatable {

    /** Returns the value used to log in (the email address). */
    String getLoginId();

    /** Returns the stored (hashed) password for comparison. */
    String getPasswordHash();

    /** Returns the role name shown on the dashboard, e.g. "Student". */
    String getRoleName();
}