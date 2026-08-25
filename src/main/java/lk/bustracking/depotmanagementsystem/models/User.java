package lk.bustracking.depotmanagementsystem.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * User Model compatible with existing database schema
 */
public class User {
    
    // Primary fields (matching your database)
    private int userId;
    private String username;
    private String password; // Keep for internal use, never expose
    private String role;
    
    // Enhanced fields (optional - can be added to database later)
    private String email;
    private String fullName;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    
    // Additional profile fields
    private String phoneNumber;
    private String department;
    private String employeeId;
    
    // Security fields
    private boolean firstLogin;
    private boolean passwordChangeRequired;
    private int failedLoginAttempts;
    private LocalDateTime accountLockedUntil;
    
    // Default constructor
    public User() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.firstLogin = true;
        this.failedLoginAttempts = 0;
    }
    
    // Constructor for your existing UserDAO
    public User(int userId, String username, String role) {
        this();
        this.userId = userId;
        this.username = username;
        this.role = role;
    }
    
    // Extended constructor for future use
    public User(int userId, String username, String role, String email, String fullName) {
        this(userId, username, role);
        this.email = email;
        this.fullName = fullName;
    }
    
    // Getters and Setters for database fields
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    // For compatibility with enhanced controller
    public String getId() { return String.valueOf(userId); }
    public void setId(String id) { 
        try {
            this.userId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            this.userId = 0;
        }
    }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    // Enhanced fields getters and setters
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    
    public boolean isFirstLogin() { return firstLogin; }
    public void setFirstLogin(boolean firstLogin) { this.firstLogin = firstLogin; }
    
    public boolean isPasswordChangeRequired() { return passwordChangeRequired; }
    public void setPasswordChangeRequired(boolean passwordChangeRequired) { this.passwordChangeRequired = passwordChangeRequired; }
    
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }
    
    public LocalDateTime getAccountLockedUntil() { return accountLockedUntil; }
    public void setAccountLockedUntil(LocalDateTime accountLockedUntil) { this.accountLockedUntil = accountLockedUntil; }
    
    // Utility methods
    
    public boolean isAccountLocked() {
        if (accountLockedUntil == null) return false;
        return LocalDateTime.now().isBefore(accountLockedUntil);
    }
    
    public void lockAccount(int durationInMinutes) {
        this.accountLockedUntil = LocalDateTime.now().plusMinutes(durationInMinutes);
    }
    
    public void unlockAccount() {
        this.accountLockedUntil = null;
        this.failedLoginAttempts = 0;
    }
    
    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            lockAccount(30); // Lock for 30 minutes
        }
    }
    
    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
    }
    
    public String getDisplayName() {
        return (fullName != null && !fullName.trim().isEmpty()) ? fullName : username;
    }
    
    public String getFormattedLastLogin() {
        if (lastLogin == null) return "Never";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        return lastLogin.format(formatter);
    }
    
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
    
    public boolean isManager() {
        return "MANAGER".equalsIgnoreCase(role) || isAdmin();
    }
    
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
        this.firstLogin = false;
        resetFailedAttempts();
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", fullName='" + fullName + '\'' +
                ", active=" + active +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return userId == user.userId;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(userId);
    }
}