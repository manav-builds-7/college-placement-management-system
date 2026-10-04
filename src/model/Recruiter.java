package model;

public class Recruiter extends User{
    private String companyName;
    private String companyAddress;

    public Recruiter(String email, String username, String password, String companyName, String companyAddress){
        super(email, username, password);
        this.companyName = companyName;
        this.companyAddress = companyAddress;
    }

    public String getCompanyName(){
        return companyName;
    }

    public String getCompanyAddress(){
        return companyAddress;
    }

    public void setCompanyName(String companyName){
        this.companyName = companyName;
    }

    public void setCompanyAddress(String companyAddress){
        this.companyAddress = companyAddress;
    }
}