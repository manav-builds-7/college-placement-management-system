package enums;

/**
 * Lifecycle states of a student's job application.
 * A status change should always follow the placement process
 * (APPLIED -> SHORTLISTED -> ... -> SELECTED).
 */
public enum ApplicationStatus {
    APPLIED,
    SHORTLISTED,
    APTITUDE_TEST,
    TECHNICAL_INTERVIEW,
    HR_INTERVIEW,
    SELECTED,
    REJECTED,
    WITHDRAWN
}