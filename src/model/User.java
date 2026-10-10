package model;

import interfaces.Authenticatable;

/**
 * Common base class for every person who can log into the system.
 * Abstract, so that a plain "User" object cannot be created.
 * Subclasses (Student, PlacementOfficer) must supply their own role name.
 */
public abstract class User implements Authenticatable {

    // ---------- Fields (private = encapsulation) ----------
    private int userId;
    private String name;
    private String email;
    private String password;   // stores a HASH, never plain text
    private String phone;

    // ---------- Constructor ----------
    public User(int userId, String name, String email, String password, String phone) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }

    // ---------- Getters and setters ----------
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // ---------- Methods required by the Authenticatable interface ----------

    /** The login ID is the email address. */
    @Override
    public String getLoginId() {
        return email;
    }

    /** Returns the stored hash so that the login service can compare it. */
    @Override
    public String getPasswordHash() {
        return password;
    }

    // getRoleName() is NOT implemented here. It comes from the interface,
    // and each subclass must implement it. This is abstraction in action.

    // ---------- Shared behaviour that subclasses can override ----------

    /**
     * Prints the details common to every user.
     * Subclasses override this method and call super.displayProfile()
     * before printing their own extra details.
     */
    public void displayProfile() {
        System.out.println("Role   : " + getRoleName());
        System.out.println("Name   : " + name);
        System.out.println("Email  : " + email);
        System.out.println("Phone  : " + phone);
    }
}