package model;

import interfaces.Reportable;

/**
 * A company that recruits students through the placement cell.
 * Implements Reportable so it can be included in company reports.
 */
public class Company implements Reportable {

    private int companyId;
    private String companyName;
    private String industry;
    private String location;
    private String website;
    private String hrName;
    private String hrEmail;
    private String hrPhone;

    /**
     * Creates a new company that has not been saved yet.
     * The companyId is 0 until the database assigns one.
     */
    public Company(String companyName, String industry, String location, String website,
                   String hrName, String hrEmail, String hrPhone) {
        this(0, companyName, industry, location, website, hrName, hrEmail, hrPhone);
    }

    /** Creates a company with all fields, typically loaded from the database. */
    public Company(int companyId, String companyName, String industry, String location,
                   String website, String hrName, String hrEmail, String hrPhone) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.industry = industry;
        this.location = location;
        this.website = website;
        this.hrName = hrName;
        this.hrEmail = hrEmail;
        this.hrPhone = hrPhone;
    }

    public int getCompanyId() { return companyId; }
    public void setCompanyId(int companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getHrName() { return hrName; }
    public void setHrName(String hrName) { this.hrName = hrName; }

    public String getHrEmail() { return hrEmail; }
    public void setHrEmail(String hrEmail) { this.hrEmail = hrEmail; }

    public String getHrPhone() { return hrPhone; }
    public void setHrPhone(String hrPhone) { this.hrPhone = hrPhone; }

    /** Returns the company summary as text. Required by Reportable. */
    @Override
    public String generateReport() {
        return "Company  : " + companyName + "\n"
             + "Industry : " + industry + "\n"
             + "Location : " + location + "\n"
             + "HR       : " + hrName + " (" + hrEmail + ", " + hrPhone + ")";
    }
}