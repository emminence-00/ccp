package models;
import exceptions.*;


import java.time.LocalDate;

public class User implements java.io.Serializable {
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dob;
    private String nationalId;
    private String passwordHash;
    private Role role;
    private int failedAttempts;
    private boolean isLocked;

    public User(String fullName, String email, String phone, LocalDate dob, String nationalId, String passwordHash, Role role) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.dob = dob;
        this.nationalId = nationalId;
        this.passwordHash = passwordHash;
        this.role = role;
        this.failedAttempts = 0;
        this.isLocked = false;
    }

    // Getters and Setters
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public LocalDate getDob() { return dob; }
    public String getNationalId() { return nationalId; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public int getFailedAttempts() { return failedAttempts; }
    public boolean isLocked() { return isLocked; }

    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }
    public void setLocked(boolean locked) { isLocked = locked; }
    
    public void incrementFailedAttempts() {
        this.failedAttempts++;
        if (this.failedAttempts >= 5) {
            this.isLocked = true;
        }
    }

    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.isLocked = false;
    }

    @Override
    public String toString() {
        return "User{" +
                "fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", isLocked=" + isLocked +
                '}';
    }
}
