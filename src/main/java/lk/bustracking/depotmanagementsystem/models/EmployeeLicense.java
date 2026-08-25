package lk.bustracking.depotmanagementsystem.models;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Employee License Model Class
 * Represents driver licenses and certifications
 */
public class EmployeeLicense {
    
    private final IntegerProperty licenseId = new SimpleIntegerProperty();
    private final IntegerProperty employeeId = new SimpleIntegerProperty();
    private final StringProperty licenseNumber = new SimpleStringProperty();
    private final StringProperty licenseType = new SimpleStringProperty();
    private final StringProperty licenseClass = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> issueDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> expiryDate = new SimpleObjectProperty<>();
    private final StringProperty issuingAuthority = new SimpleStringProperty();
    private final StringProperty licenseStatus = new SimpleStringProperty();
    private final BooleanProperty renewalReminderSent = new SimpleBooleanProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();
    
    // Employee details (for display purposes)
    private final StringProperty employeeName = new SimpleStringProperty();
    private final StringProperty employeeCode = new SimpleStringProperty();
    
    // Constructors
    public EmployeeLicense() {
        this.licenseStatus.set("Valid");
        this.renewalReminderSent.set(false);
        this.createdAt.set(LocalDateTime.now());
        this.updatedAt.set(LocalDateTime.now());
    }
    
    public EmployeeLicense(int employeeId, String licenseNumber, String licenseType, 
                          String licenseClass, LocalDate issueDate, LocalDate expiryDate) {
        this();
        this.employeeId.set(employeeId);
        this.licenseNumber.set(licenseNumber);
        this.licenseType.set(licenseType);
        this.licenseClass.set(licenseClass);
        this.issueDate.set(issueDate);
        this.expiryDate.set(expiryDate);
    }
    
    // License ID
    public int getLicenseId() { return licenseId.get(); }
    public void setLicenseId(int licenseId) { this.licenseId.set(licenseId); }
    public IntegerProperty licenseIdProperty() { return licenseId; }
    
    // Employee ID
    public int getEmployeeId() { return employeeId.get(); }
    public void setEmployeeId(int employeeId) { this.employeeId.set(employeeId); }
    public IntegerProperty employeeIdProperty() { return employeeId; }
    
    // License Number
    public String getLicenseNumber() { return licenseNumber.get(); }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber.set(licenseNumber); }
    public StringProperty licenseNumberProperty() { return licenseNumber; }
    
    // License Type
    public String getLicenseType() { return licenseType.get(); }
    public void setLicenseType(String licenseType) { this.licenseType.set(licenseType); }
    public StringProperty licenseTypeProperty() { return licenseType; }
    
    // License Class
    public String getLicenseClass() { return licenseClass.get(); }
    public void setLicenseClass(String licenseClass) { this.licenseClass.set(licenseClass); }
    public StringProperty licenseClassProperty() { return licenseClass; }
    
    // Issue Date
    public LocalDate getIssueDate() { return issueDate.get(); }
    public void setIssueDate(LocalDate issueDate) { this.issueDate.set(issueDate); }
    public ObjectProperty<LocalDate> issueDateProperty() { return issueDate; }
    
    // Expiry Date
    public LocalDate getExpiryDate() { return expiryDate.get(); }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate.set(expiryDate); }
    public ObjectProperty<LocalDate> expiryDateProperty() { return expiryDate; }
    
    // Issuing Authority
    public String getIssuingAuthority() { return issuingAuthority.get(); }
    public void setIssuingAuthority(String issuingAuthority) { this.issuingAuthority.set(issuingAuthority); }
    public StringProperty issuingAuthorityProperty() { return issuingAuthority; }
    
    // License Status
    public String getLicenseStatus() { return licenseStatus.get(); }
    public void setLicenseStatus(String licenseStatus) { this.licenseStatus.set(licenseStatus); }
    public StringProperty licenseStatusProperty() { return licenseStatus; }
    
    // Renewal Reminder Sent
    public boolean isRenewalReminderSent() { return renewalReminderSent.get(); }
    public void setRenewalReminderSent(boolean renewalReminderSent) { this.renewalReminderSent.set(renewalReminderSent); }
    public BooleanProperty renewalReminderSentProperty() { return renewalReminderSent; }
    
    // Created At
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    
    // Updated At
    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt.set(updatedAt); }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }
    
    // Employee Name (for display)
    public String getEmployeeName() { return employeeName.get(); }
    public void setEmployeeName(String employeeName) { this.employeeName.set(employeeName); }
    public StringProperty employeeNameProperty() { return employeeName; }
    
    // Employee Code (for display)
    public String getEmployeeCode() { return employeeCode.get(); }
    public void setEmployeeCode(String employeeCode) { this.employeeCode.set(employeeCode); }
    public StringProperty employeeCodeProperty() { return employeeCode; }
    
    // Utility methods
    public boolean isValid() {
        return "Valid".equals(getLicenseStatus()) && !isExpired();
    }
    
    public boolean isExpired() {
        return expiryDate.get() != null && expiryDate.get().isBefore(LocalDate.now());
    }
    
    public boolean isExpiringSoon() {
        if (expiryDate.get() == null) return false;
        long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate.get());
        return daysUntilExpiry <= 90 && daysUntilExpiry >= 0; // 90 days warning
    }
    
    public long getDaysUntilExpiry() {
        if (expiryDate.get() == null) return -1;
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate.get());
    }
    
    public String getStatusDescription() {
        if (isExpired()) {
            return "Expired";
        } else if (isExpiringSoon()) {
            return "Expiring Soon";
        } else if (isValid()) {
            return "Valid";
        } else {
            return getLicenseStatus();
        }
    }
    
    public String getStatusColor() {
        if (isExpired()) {
            return "#e74c3c"; // Red
        } else if (isExpiringSoon()) {
            return "#f39c12"; // Orange
        } else if (isValid()) {
            return "#27ae60"; // Green
        } else {
            return "#95a5a6"; // Gray
        }
    }
    
    // Static methods
    public static String[] getLicenseTypes() {
        return new String[]{"Light Vehicle", "Heavy Vehicle", "Bus", "Motorcycle"};
    }
    
    public static String[] getLicenseClasses() {
        return new String[]{"Class A", "Class B", "Class C", "Class D", "Class E"};
    }
    
    public static String[] getLicenseStatuses() {
        return new String[]{"Valid", "Expired", "Suspended", "Revoked"};
    }
    
    @Override
    public String toString() {
        return String.format("EmployeeLicense{id=%d, employeeId=%d, number='%s', type='%s', status='%s'}", 
                getLicenseId(), getEmployeeId(), getLicenseNumber(), getLicenseType(), getLicenseStatus());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EmployeeLicense license = (EmployeeLicense) obj;
        return getLicenseId() == license.getLicenseId();
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(getLicenseId());
    }
}