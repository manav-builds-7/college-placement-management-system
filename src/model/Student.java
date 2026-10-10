package model;

import enums.PlacementStatus;

/**
 * A student who can log in, maintain a profile, and apply for jobs.
 * Extends User, so it inherits identity and login details.
 */
public class Student extends User {

    // ---------- Student-specific fields ----------
    private String department;
    private String course;
    private int graduationYear;
    private double cgpa;
    private int backlogs;
    private double tenthPercentage;
    private double twelfthPercentage;
    private String address;
    private String skills;               // comma-separated, e.g. "Java, SQL, React"
    private PlacementStatus placementStatus;

    // ---------- Constructor 1: used at registration ----------
    /**
     * Creates a new student who has just registered.
     * Academic details are left empty and filled in later from the profile screen.
     * The userId is 0 until the database assigns a real one.
     */
    public Student(String name, String email, String password, String phone) {
        super(0, name, email, password, phone);
        this.placementStatus = PlacementStatus.NOT_PLACED;
        this.department = "";
        this.course = "";
        this.address = "";
        this.skills = "";
    }

    // ---------- Constructor 2: used when loading a student from the database ----------
    /**
     * Creates a fully populated student, typically from a database row.
     * This is an overloaded constructor: same name, different parameter list.
     */
    public Student(int userId, String name, String email, String password, String phone,
                   String department, String course, int graduationYear,
                   double cgpa, int backlogs,
                   double tenthPercentage, double twelfthPercentage,
                   String address, String skills, PlacementStatus placementStatus) {
        super(userId, name, email, password, phone);
        this.department = department;
        this.course = course;
        this.graduationYear = graduationYear;
        this.cgpa = cgpa;
        this.backlogs = backlogs;
        this.tenthPercentage = tenthPercentage;
        this.twelfthPercentage = twelfthPercentage;
        this.address = address;
        this.skills = skills;
        this.placementStatus = placementStatus;
    }

    // ---------- Identity ----------

    /** studentId and userId are the same value. See the design note above. */
    public int getStudentId() {
        return getUserId();
    }

    // ---------- Getters and setters ----------

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public int getGraduationYear() { return graduationYear; }
    public void setGraduationYear(int graduationYear) { this.graduationYear = graduationYear; }

    public double getCgpa() { return cgpa; }
    public void setCgpa(double cgpa) { this.cgpa = cgpa; }

    public int getBacklogs() { return backlogs; }
    public void setBacklogs(int backlogs) { this.backlogs = backlogs; }

    public double getTenthPercentage() { return tenthPercentage; }
    public void setTenthPercentage(double tenthPercentage) { this.tenthPercentage = tenthPercentage; }

    public double getTwelfthPercentage() { return twelfthPercentage; }
    public void setTwelfthPercentage(double twelfthPercentage) { this.twelfthPercentage = twelfthPercentage; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public PlacementStatus getPlacementStatus() { return placementStatus; }
    public void setPlacementStatus(PlacementStatus placementStatus) { this.placementStatus = placementStatus; }

    // ---------- Convenience method ----------

    /** True when the student has been placed. Used in the application rules later. */
    public boolean isPlaced() {
        return placementStatus == PlacementStatus.PLACED;
    }

    // ---------- Overridden methods ----------

    /** Role name required by the Authenticatable interface. */
    @Override
    public String getRoleName() {
        return "Student";
    }

    /** Overrides User.displayProfile() to add student details. */
    @Override
    public void displayProfile() {
        super.displayProfile();                       // print the common part first
        System.out.println("Department : " + department);
        System.out.println("Course     : " + course);
        System.out.println("Grad Year  : " + graduationYear);
        System.out.println("CGPA       : " + cgpa);
        System.out.println("Backlogs   : " + backlogs);
        System.out.println("Placement  : " + placementStatus);
    }
}