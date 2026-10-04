package model;

public class Admin extends User{
    private String adminId;

    public Admin(String email, String username, String password, String adminId){
        super(email, username, password);
        this.adminId = adminId;
    }

    public String getAdminId(){
        return adminId;
    }

    public void setAdminId(String adminId){
        this.adminId = adminId;
    }
}