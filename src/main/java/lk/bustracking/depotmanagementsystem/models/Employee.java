package lk.bustracking.depotmanagementsystem.models;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Employee Model Class
 * Represents an employee in the bus depot management system
 */
public class Employee {
    
    // Primary fields
    private final IntegerProperty employeeId = new SimpleIntegerProperty();
    private final StringProperty employeeCode = new SimpleStringProperty();
    private final StringProperty firstName = new SimpleStringProperty();
    private final StringProperty lastName = new SimpleStringProperty();
    private final StringProperty fullName = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty phone = new SimpleStringProperty();
    private final StringProperty nationalId = new SimpleStringProperty();
    private final StringProperty address = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dateOfBirth = new SimpleObjectProperty<>();
    private final StringProperty gender = new SimpleStringProperty();
    private final StringProperty employeeType = new SimpleStringProperty();
    private final StringProperty department = new SimpleStringProperty();
    private final StringProperty position = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> hireDate = new SimpleObjectProperty<>();
    private final DoubleProperty salary = new SimpleDoubleProperty();
    private final StringProperty employmentStatus = new SimpleStringProperty();
    private final StringProperty emergencyContactName = new SimpleStringProperty();
    private final StringProperty emergencyContactPhone = new SimpleStringProperty();
    private final StringProperty photoPath = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();
    
    // Constructors
    public Employee() {
        this.employmentStatus.set("Active");
        this.hireDate.set(LocalDate.now());
        this.createdAt.set(LocalDateTime.now());
        this.updatedAt.set(LocalDateTime.now());
    }
    
    public Employee(String employeeCode, String firstName, String lastName, 
                   String email, String phone, String nationalId, String employeeType) {
        this();
        this.employeeCode.set(employeeCode);
        this.firstName.set(firstName);
        this.lastName.set(lastName);
        this.fullName.set(firstName + " " + lastName);
        this.email.set(email);
        this.phone.set(phone);
        this.nationalId.set(nationalId);
        this.employeeType.set(employeeType);
    }
    
    // Employee ID
    public int getEmployeeId() { return employeeId.get(); }
    public void setEmployeeId(int employeeId) { this.employeeId.set(employeeId); }
    public IntegerProperty employeeIdProperty() { return employeeId; }
    
    // Employee Code
    public String getEmployeeCode() { return employeeCode.get(); }
    public void setEmployeeCode(String employeeCode) { this.employeeCode.set(employeeCode); }
    public StringProperty employeeCodeProperty() { return employeeCode; }
    
    // First Name
    public String getFirstName() { return firstName.get(); }
    public void setFirstName(String firstName) { 
        this.firstName.set(firstName); 
        updateFullName();
    }
    public StringProperty firstNameProperty() { return firstName; }
    
    // Last Name
    public String getLastName() { return lastName.get(); }
    public void setLastName(String lastName) { 
        this.lastName.set(lastName); 
        updateFullName();
    }
    public StringProperty lastNameProperty() { return lastName; }
    
    // Full Name
    public String getFullName() { return fullName.get(); }
    public StringProperty fullNameProperty() { return fullName; }
    
    private void updateFullName() {
        String first = firstName.get() != null ? firstName.get() : "";
        String last = lastName.get() != null ? lastName.get() : "";
        fullName.set((first + " " + last).trim());
    }
    
    // Email
    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }
    public StringProperty emailProperty() { return email; }
    
    // Phone
    public String getPhone() { return phone.get(); }
    public void setPhone(String phone) { this.phone.set(phone); }
    public StringProperty phoneProperty() { return phone; }
    
    // National ID
    public String getNationalId() { return nationalId.get(); }
    public void setNationalId(String nationalId) { this.nationalId.set(nationalId); }
    public StringProperty nationalIdProperty() { return nationalId; }
    
    // Address
    public String getAddress() { return address.get(); }
    public void setAddress(String address) { this.address.set(address); }
    public StringProperty addressProperty() { return address; }
    
    // Date of Birth
    public LocalDate getDateOfBirth() { return dateOfBirth.get(); }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth.set(dateOfBirth); }
    public ObjectProperty<LocalDate> dateOfBirthProperty() { return dateOfBirth; }
    
    // Gender
    public String getGender() { return gender.get(); }
    public void setGender(String gender) { this.gender.set(gender); }
    public StringProperty genderProperty() { return gender; }
    
    // Employee Type
    public String getEmployeeType() { return employeeType.get(); }
    public void setEmployeeType(String employeeType) { this.employeeType.set(employeeType); }
    public StringProperty employeeTypeProperty() { return employeeType; }
    
    // Department
    public String getDepartment() { return department.get(); }
    public void setDepartment(String department) { this.department.set(department); }
    public StringProperty departmentProperty() { return department; }
    
    // Position
    public String getPosition() { return position.get(); }
    public void setPosition(String position) { this.position.set(position); }
    public StringProperty positionProperty() { return position; }
    
    // Hire Date
    public LocalDate getHireDate() { return hireDate.get(); }
    public void setHireDate(LocalDate hireDate) { this.hireDate.set(hireDate); }
    public ObjectProperty<LocalDate> hireDateProperty() { return hireDate; }
    
    // Salary
    public double getSalary() { return salary.get(); }
    public void setSalary(double salary) { this.salary.set(salary); }
    public DoubleProperty salaryProperty() { return salary; }
    
    // Employment Status
    public String getEmploymentStatus() { return employmentStatus.get(); }
    public void setEmploymentStatus(String employmentStatus) { this.employmentStatus.set(employmentStatus); }
    public StringProperty employmentStatusProperty() { return employmentStatus; }
    
    // Emergency Contact Name
    public String getEmergencyContactName() { return emergencyContactName.get(); }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName.set(emergencyContactName); }
    public StringProperty emergencyContactNameProperty() { return emergencyContactName; }
    
    // Emergency Contact Phone
    public String getEmergencyContactPhone() { return emergencyContactPhone.get(); }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone.set(emergencyContactPhone); }
    public StringProperty emergencyContactPhoneProperty() { return emergencyContactPhone; }
    
    // Photo Path
    public String getPhotoPath() { return photoPath.get(); }
    public void setPhotoPath(String photoPath) { this.photoPath.set(photoPath); }
    public StringProperty photoPathProperty() { return photoPath; }
    
    // Created At
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    
    // Updated At
    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt.set(updatedAt); }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }
    
    // Utility methods
    public boolean isDriver() {
        return "Driver".equals(getEmployeeType());
    }
    
    public boolean isConductor() {
        return "Conductor".equals(getEmployeeType());
    }
    
    public boolean isMechanic() {
        return "Mechanic".equals(getEmployeeType());
    }
    
    public boolean isActive() {
        return "Active".equals(getEmploymentStatus());
    }
    
    public int getAge() {
        if (dateOfBirth.get() != null) {
            return LocalDate.now().getYear() - dateOfBirth.get().getYear();
        }
        return 0;
    }
    
    public int getYearsOfService() {
        if (hireDate.get() != null) {
            return LocalDate.now().getYear() - hireDate.get().getYear();
        }
        return 0;
    }
    
    // Static methods for employee types
    public static String[] getEmployeeTypes() {
        return new String[]{"Driver", "Conductor", "Mechanic", "Supervisor", "Office Staff"};
    }
    
    public static String[] getEmploymentStatuses() {
        return new String[]{"Active", "Inactive", "Terminated", "Suspended"};
    }
    
    public static String[] getGenders() {
        return new String[]{"Male", "Female", "Other"};
    }
    
    @Override
    public String toString() {
        return String.format("Employee{id=%d, code='%s', name='%s', type='%s', status='%s'}", 
                getEmployeeId(), getEmployeeCode(), getFullName(), getEmployeeType(), getEmploymentStatus());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Employee employee = (Employee) obj;
        return getEmployeeId() == employee.getEmployeeId();
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(getEmployeeId());
    }
}