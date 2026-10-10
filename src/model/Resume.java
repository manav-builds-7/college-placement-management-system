package model;

/**
 * A student's resume. Each student has one resume, linked by studentId.
 * All sections are free text, so a student can write several entries
 * in one field (for example, two projects separated by a semicolon).
 */
public class Resume {

    private int resumeId;
    private int studentId;
    private String education;
    private String skills;
    private String projects;
    private String certifications;
    private String achievements;
    private String internships;
    private String programmingLanguages;
    private String linkedin;
    private String github;
    private String portfolio;

    /**
     * Creates a blank resume for a student who has just started.
     * All text sections start as empty strings, never null, so printing
     * and saving never produce "null" on the screen.
     */
    public Resume(int studentId) {
        this(0, studentId, "", "", "", "", "", "", "", "", "", "");
    }

    /** Creates a full resume, typically loaded from the database. */
    public Resume(int resumeId, int studentId, String education, String skills,
                  String projects, String certifications, String achievements,
                  String internships, String programmingLanguages,
                  String linkedin, String github, String portfolio) {
        this.resumeId = resumeId;
        this.studentId = studentId;
        this.education = education;
        this.skills = skills;
        this.projects = projects;
        this.certifications = certifications;
        this.achievements = achievements;
        this.internships = internships;
        this.programmingLanguages = programmingLanguages;
        this.linkedin = linkedin;
        this.github = github;
        this.portfolio = portfolio;
    }

    public int getResumeId() { return resumeId; }
    public void setResumeId(int resumeId) { this.resumeId = resumeId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getProjects() { return projects; }
    public void setProjects(String projects) { this.projects = projects; }

    public String getCertifications() { return certifications; }
    public void setCertifications(String certifications) { this.certifications = certifications; }

    public String getAchievements() { return achievements; }
    public void setAchievements(String achievements) { this.achievements = achievements; }

    public String getInternships() { return internships; }
    public void setInternships(String internships) { this.internships = internships; }

    public String getProgrammingLanguages() { return programmingLanguages; }
    public void setProgrammingLanguages(String programmingLanguages) { this.programmingLanguages = programmingLanguages; }

    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin; }

    public String getGithub() { return github; }
    public void setGithub(String github) { this.github = github; }

    public String getPortfolio() { return portfolio; }
    public void setPortfolio(String portfolio) { this.portfolio = portfolio; }
}