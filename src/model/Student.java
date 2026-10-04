package model;

public class Student extends User {
    private String studentId;
    private double cgpa;
    private Resume resume;

    public Student(String email, String username, String password, String studentId, double cgpa) {
        super(email, username, password);
        this.studentId = studentId;
        this.cgpa = validateCgpa(cgpa);
    }

    public String getStudentId() {
        return studentId;
    }

    public double getCgpa() {
        return cgpa;
    }

    public Resume getResume() {
        return resume;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setCgpa(double cgpa) {
        this.cgpa = validateCgpa(cgpa);
    }

    public void setResume(Resume resume) {
        this.resume = resume;
    }

    private static double validateCgpa(double cgpa) {
        if (!Double.isFinite(cgpa) || cgpa < 0 || cgpa > 10) {
            throw new IllegalArgumentException("CGPA must be between 0 and 10.");
        }
        return cgpa;
    }

}