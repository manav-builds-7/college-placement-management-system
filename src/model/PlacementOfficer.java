package model;

/**
 * A placement cell staff member who manages companies, jobs, drives,
 * applications, and reports. Extends User.
 */
public class PlacementOfficer extends User {

    private String employeeId;
    private String designation;

    /**
     * Creates a placement officer.
     * The userId is the value from the users table.
     */
    public PlacementOfficer(int userId, String name, String email, String password, String phone,
                            String employeeId, String designation) {
        super(userId, name, email, password, phone);
        this.employeeId = employeeId;
        this.designation = designation;
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    /** Role name required by the Authenticatable interface. */
    @Override
    public String getRoleName() {
        return "Placement Officer";
    }

    /** Overrides User.displayProfile() to add officer details. */
    @Override
    public void displayProfile() {
        super.displayProfile();
        System.out.println("Employee ID : " + employeeId);
        System.out.println("Designation : " + designation);
    }
}