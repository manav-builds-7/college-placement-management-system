package service;

import dao.ResumeDAO;
import exception.InvalidInputException;
import exception.StudentNotFoundException;
import model.Resume;
import model.Student;
import util.ValidationUtil;

import java.sql.SQLException;

/**
 * Business rules for resumes: creating one per student, validating links,
 * and building the printable text used for the terminal and TXT export.
 */
public class ResumeService {

    private static final int REPORT_WIDTH = 60;

    private final ResumeDAO resumeDAO;
    private final StudentService studentService;

    public ResumeService(ResumeDAO resumeDAO, StudentService studentService) {
        this.resumeDAO = resumeDAO;
        this.studentService = studentService;
    }

    /**
     * Creates an empty resume for a student. Each student may have only one.
     *
     * @return the new, blank resume
     */
    public Resume createResume(int studentId)
            throws InvalidInputException, StudentNotFoundException, SQLException {
        studentService.findStudent(studentId);   // confirms the student exists

        if (resumeDAO.getResumeByStudentId(studentId) != null) {
            throw new InvalidInputException("You already have a resume. Use update instead.");
        }

        Resume resume = new Resume(studentId);
        resumeDAO.addResume(resume);
        return resume;
    }

    /**
     * Returns the student's resume, or null if they have not created one yet.
     * A missing resume is a normal state, so it is not treated as an error.
     */
    public Resume getResume(int studentId)
            throws InvalidInputException, StudentNotFoundException, SQLException {
        studentService.findStudent(studentId);
        return resumeDAO.getResumeByStudentId(studentId);
    }

    /**
     * Saves changes to an existing resume.
     * Education and skills are required. Links are optional, but must be full URLs if given.
     */
    public void updateResume(Resume resume)
            throws InvalidInputException, StudentNotFoundException, SQLException {
        studentService.findStudent(resume.getStudentId());

        if (resumeDAO.getResumeByStudentId(resume.getStudentId()) == null) {
            throw new InvalidInputException("Create a resume before updating it.");
        }

        ValidationUtil.requireNotBlank(resume.getEducation(), "Education");
        ValidationUtil.requireNotBlank(resume.getSkills(), "Skills");
        validateOptionalLink(resume.getLinkedin(), "LinkedIn");
        validateOptionalLink(resume.getGithub(), "GitHub");
        validateOptionalLink(resume.getPortfolio(), "Portfolio");

        resumeDAO.updateResume(resume);
    }

    /**
     * Builds the resume as plain text, ready to print in the terminal or save to a file.
     * Empty sections are left out, so the output stays clean.
     */
    public String buildResumeText(Student student, Resume resume) {
        StringBuilder text = new StringBuilder();
        String divider = "=".repeat(REPORT_WIDTH) + "\n";

        text.append(divider);
        text.append(student.getName().toUpperCase()).append("\n");
        text.append(student.getEmail()).append("  |  ").append(student.getPhone()).append("\n");
        text.append(divider);

        appendSection(text, "Academic Details", buildAcademicSummary(student));
        appendSection(text, "Education", resume.getEducation());
        appendSection(text, "Skills", resume.getSkills());
        appendSection(text, "Programming Languages", resume.getProgrammingLanguages());
        appendSection(text, "Projects", resume.getProjects());
        appendSection(text, "Internships", resume.getInternships());
        appendSection(text, "Certifications", resume.getCertifications());
        appendSection(text, "Achievements", resume.getAchievements());
        appendSection(text, "Links", buildLinks(resume));

        text.append(divider);
        return text.toString();
    }

    // ---------- Private helpers ----------

    /** A link is optional. If one is entered, it must start with http:// or https://. */
    private void validateOptionalLink(String link, String label) throws InvalidInputException {
        if (ValidationUtil.isBlank(link)) {
            return;
        }
        String trimmed = link.trim().toLowerCase();
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            throw new InvalidInputException(label + " must start with http:// or https://.");
        }
    }

    private String buildAcademicSummary(Student student) {
        return "Department : " + student.getDepartment() + "\n"
             + "Course     : " + student.getCourse() + "\n"
             + "Graduation : " + student.getGraduationYear() + "\n"
             + "CGPA       : " + student.getCgpa();
    }

    private String buildLinks(Resume resume) {
        StringBuilder links = new StringBuilder();
        appendLine(links, "LinkedIn", resume.getLinkedin());
        appendLine(links, "GitHub", resume.getGithub());
        appendLine(links, "Portfolio", resume.getPortfolio());
        return links.toString();
    }

    private void appendLine(StringBuilder builder, String label, String value) {
        if (!ValidationUtil.isBlank(value)) {
            builder.append(label).append(": ").append(value.trim()).append("\n");
        }
    }

    /** Adds a titled section, skipped entirely when its content is blank. */
    private void appendSection(StringBuilder text, String title, String content) {
        if (ValidationUtil.isBlank(content)) {
            return;
        }
        text.append(title.toUpperCase()).append("\n");
        text.append(content.trim()).append("\n\n");
    }
}