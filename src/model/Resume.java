package model;

public class Resume extends Student {
    private String skills;
    private String projects;
    private String internships;
    private String achievements;
    private String certifications;
    private String languages;

    public Resume(String skills, String projects, String internships, String achievements,
                  String certifications, String languages, String email, String username, String password, String studentId, double cgpa) {
        super(email, username, password, studentId, cgpa);
        this.skills = skills;
        this.projects = projects;
        this.internships = internships;
        this.achievements = achievements;
        this.certifications = certifications;
        this.languages = languages;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getProjects() {
        return projects;
    }

    public void setProjects(String projects) {
        this.projects = projects;
    }

    public String getInternships() {
        return internships;
    }

    public void setInternships(String internships) {
        this.internships = internships;
    }

    public String getAchievements() {
        return achievements;
    }

    public void setAchievements(String achievements) {
        this.achievements = achievements;
    }

    public String getCertifications() {
        return certifications;
    }

    public void setCertifications(String certifications) {
        this.certifications = certifications;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

}    