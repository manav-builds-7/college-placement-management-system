package model;

public class PlacementDrive {
    private String driveId;
    private String companyName;
    private String date;
    private String time;
    private String location;
    private String description;
    private String eligibilityCriteria;
    private String status;

    public PlacementDrive(String driveId, String companyName, String date, String time, String location,
                          String description, String eligibilityCriteria, String status) {
        this.driveId = driveId;
        this.companyName = companyName;
        this.date = date;
        this.time = time;
        this.location = location;
        this.description = description;
        this.eligibilityCriteria = eligibilityCriteria;
        this.status = status;
    }

    public String getDriveId() {
        return driveId;
    }

    public void setDriveId(String driveId) {
        this.driveId = driveId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEligibilityCriteria() {
        return eligibilityCriteria;
    }

    public void setEligibilityCriteria(String eligibilityCriteria) {
        this.eligibilityCriteria = eligibilityCriteria;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}