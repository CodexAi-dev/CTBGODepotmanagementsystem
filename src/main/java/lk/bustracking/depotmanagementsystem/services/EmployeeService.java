package lk.bustracking.depotmanagementsystem.services;

import lk.bustracking.depotmanagementsystem.models.Employee;
import lk.bustracking.depotmanagementsystem.models.EmployeeLicense;
import lk.bustracking.depotmanagementsystem.db.Database;
import lk.bustracking.depotmanagementsystem.utils.AppLogger;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Employee Service Class
 * Handles all employee-related business logic and database operations
 */
public class EmployeeService {
    
    private static final Logger LOGGER = AppLogger.getLogger(EmployeeService.class);
    private static EmployeeService instance;
    
    private EmployeeService() {}
    
    public static synchronized EmployeeService getInstance() {
        if (instance == null) {
            instance = new EmployeeService();
        }
        return instance;
    }
    
    // ====================================
    // EMPLOYEE CRUD OPERATIONS
    // ====================================
    
    /**
     * Get all employees
     */
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String query = """
            SELECT 
                employee_id, employee_code, first_name, last_name, 
                email, phone, national_id, address, date_of_birth, 
                gender, employee_type, department, position, 
                hire_date, salary, employment_status, 
                emergency_contact_name, emergency_contact_phone, 
                photo_path, created_at, updated_at
            FROM Employees 
            ORDER BY employee_code
            """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Employee employee = mapResultSetToEmployee(rs);
                employees.add(employee);
            }
            
            LOGGER.info("Retrieved " + employees.size() + " employees from database");
            
        } catch (SQLException e) {
            LOGGER.severe("Error retrieving employees: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve employees", e);
        }
        
        return employees;
    }
    
    /**
     * Get employee by ID
     */
    public Employee getEmployeeById(int employeeId) {
        String query = """
            SELECT 
                employee_id, employee_code, first_name, last_name, 
                email, phone, national_id, address, date_of_birth, 
                gender, employee_type, department, position, 
                hire_date, salary, employment_status, 
                emergency_contact_name, emergency_contact_phone, 
                photo_path, created_at, updated_at
            FROM Employees 
            WHERE employee_id = ?
            """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, employeeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                }
            }
            
        } catch (SQLException e) {
            LOGGER.severe("Error retrieving employee by ID: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve employee", e);
        }
        
        return null;
    }
    
    /**
     * Get employees by type
     */
    public List<Employee> getEmployeesByType(String employeeType) {
        List<Employee> employees = new ArrayList<>();
        String query = """
            SELECT 
                employee_id, employee_code, first_name, last_name, 
                email, phone, national_id, address, date_of_birth, 
                gender, employee_type, department, position, 
                hire_date, salary, employment_status, 
                emergency_contact_name, emergency_contact_phone, 
                photo_path, created_at, updated_at
            FROM Employees 
            WHERE employee_type = ? AND employment_status = 'Active'
            ORDER BY employee_code
            """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, employeeType);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Employee employee = mapResultSetToEmployee(rs);
                    employees.add(employee);
                }
            }
            
            LOGGER.info("Retrieved " + employees.size() + " " + employeeType + " employees");
            
        } catch (SQLException e) {
            LOGGER.severe("Error retrieving employees by type: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve employees by type", e);
        }
        
        return employees;
    }
    
    /**
     * Search employees
     */
    public List<Employee> searchEmployees(String searchTerm) {
        List<Employee> employees = new ArrayList<>();
        String query = """
            SELECT 
                employee_id, employee_code, first_name, last_name, 
                email, phone, national_id, address, date_of_birth, 
                gender, employee_type, department, position, 
                hire_date, salary, employment_status, 
                emergency_contact_name, emergency_contact_phone, 
                photo_path, created_at, updated_at
            FROM Employees 
            WHERE (employee_code LIKE ? OR first_name LIKE ? OR last_name LIKE ? 
                   OR email LIKE ? OR phone LIKE ? OR national_id LIKE ?)
            ORDER BY employee_code
            """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            String searchPattern = "%" + searchTerm + "%";
            for (int i = 1; i <= 6; i++) {
                stmt.setString(i, searchPattern);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Employee employee = mapResultSetToEmployee(rs);
                    employees.add(employee);
                }
            }
            
            LOGGER.info("Found " + employees.size() + " employees matching search term: " + searchTerm);
            
        } catch (SQLException e) {
            LOGGER.severe("Error searching employees: " + e.getMessage());
            throw new RuntimeException("Failed to search employees", e);
        }
        
        return employees;
    }
    
    /**
     * Add new employee
     */
    public boolean addEmployee(Employee employee) {
        String query = """
            INSERT INTO Employees (
                employee_code, first_name, last_name, email, phone, national_id, 
                address, date_of_birth, gender, employee_type, department, position, 
                hire_date, salary, employment_status, emergency_contact_name, 
                emergency_contact_phone, photo_path
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, employee.getEmployeeCode());
            stmt.setString(2, employee.getFirstName());
            stmt.setString(3, employee.getLastName());
            stmt.setString(4, employee.getEmail());
            stmt.setString(5, employee.getPhone());
            stmt.setString(6, employee.getNationalId());
            stmt.setString(7, employee.getAddress());
            stmt.setDate(8, employee.getDateOfBirth() != null ? Date.valueOf(employee.getDateOfBirth()) : null);
            stmt.setString(9, employee.getGender());
            stmt.setString(10, employee.getEmployeeType());
            stmt.setString(11, employee.getDepartment());
            stmt.setString(12, employee.getPosition());
            stmt.setDate(13, employee.getHireDate() != null ? Date.valueOf(employee.getHireDate()) : Date.valueOf(LocalDate.now()));
            stmt.setDouble(14, employee.getSalary());
            stmt.setString(15, employee.getEmploymentStatus());
            stmt.setString(16, employee.getEmergencyContactName());
            stmt.setString(17, employee.getEmergencyContactPhone());
            stmt.setString(18, employee.getPhotoPath());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        employee.setEmployeeId(generatedKeys.getInt(1));
                    }
                }
                LOGGER.info("Employee added successfully: " + employee.getEmployeeCode());
                return true;
            }
            
        } catch (SQLException e) {
            LOGGER.severe("Error adding employee: " + e.getMessage());
            throw new RuntimeException("Failed to add employee", e);
        }
        
        return false;
    }
    
    /**
     * Update employee
     */
    public boolean updateEmployee(Employee employee) {
        String query = """
            UPDATE Employees SET 
                employee_code = ?, first_name = ?, last_name = ?, email = ?, phone = ?, 
                national_id = ?, address = ?, date_of_birth = ?, gender = ?, 
                employee_type = ?, department = ?, position = ?, hire_date = ?, 
                salary = ?, employment_status = ?, emergency_contact_name = ?, 
                emergency_contact_phone = ?, photo_path = ?, updated_at = GETDATE()
            WHERE employee_id = ?
            """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, employee.getEmployeeCode());
            stmt.setString(2, employee.getFirstName());
            stmt.setString(3, employee.getLastName());
            stmt.setString(4, employee.getEmail());
            stmt.setString(5, employee.getPhone());
            stmt.setString(6, employee.getNationalId());
            stmt.setString(7, employee.getAddress());
            stmt.setDate(8, employee.getDateOfBirth() != null ? Date.valueOf(employee.getDateOfBirth()) : null);
            stmt.setString(9, employee.getGender());
            stmt.setString(10, employee.getEmployeeType());
            stmt.setString(11, employee.getDepartment());
            stmt.setString(12, employee.getPosition());
            stmt.setDate(13, employee.getHireDate() != null ? Date.valueOf(employee.getHireDate()) : null);
            stmt.setDouble(14, employee.getSalary());
            stmt.setString(15, employee.getEmploymentStatus());
            stmt.setString(16, employee.getEmergencyContactName());
            stmt.setString(17, employee.getEmergencyContactPhone());
            stmt.setString(18, employee.getPhotoPath());
            stmt.setInt(19, employee.getEmployeeId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                LOGGER.info("Employee updated successfully: " + employee.getEmployeeCode());
                return true;
            }
            
        } catch (SQLException e) {
            LOGGER.severe("Error updating employee: " + e.getMessage());
            throw new RuntimeException("Failed to update employee", e);
        }
        
        return false;
    }
    
    /**
     * Delete employee
     */
    public boolean deleteEmployee(int employeeId) {
        String query = "DELETE FROM Employees WHERE employee_id = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, employeeId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                LOGGER.info("Employee deleted successfully: ID " + employeeId);
                return true;
            }
            
        } catch (SQLException e) {
            LOGGER.severe("Error deleting employee: " + e.getMessage());
            throw new RuntimeException("Failed to delete employee", e);
        }
        
        return false;
    }
    
    // ====================================
    // LICENSE MANAGEMENT
    // ====================================
    
    /**
     * Get licenses for employee
     */
    public List<EmployeeLicense> getEmployeeLicenses(int employeeId) {
        List<EmployeeLicense> licenses = new ArrayList<>();
        String query = """
            SELECT 
                el.license_id, el.employee_id, el.license_number, el.license_type, 
                el.license_class, el.issue_date, el.expiry_date, el.issuing_authority, 
                el.license_status, el.renewal_reminder_sent, el.created_at, el.updated_at,
                e.first_name + ' ' + e.last_name as employee_name, e.employee_code
            FROM Employee_Licenses el
            LEFT JOIN Employees e ON el.employee_id = e.employee_id
            WHERE el.employee_id = ?
            ORDER BY el.expiry_date DESC
            """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, employeeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    EmployeeLicense license = mapResultSetToLicense(rs);
                    licenses.add(license);
                }
            }
            
        } catch (SQLException e) {
            LOGGER.severe("Error retrieving employee licenses: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve employee licenses", e);
        }
        
        return licenses;
    }
    
    /**
     * Get all licenses
     */
    public List<EmployeeLicense> getAllLicenses() {
        List<EmployeeLicense> licenses = new ArrayList<>();
        String query = """
            SELECT 
                el.license_id, el.employee_id, el.license_number, el.license_type, 
                el.license_class, el.issue_date, el.expiry_date, el.issuing_authority, 
                el.license_status, el.renewal_reminder_sent, el.created_at, el.updated_at,
                e.first_name + ' ' + e.last_name as employee_name, e.employee_code
            FROM Employee_Licenses el
            LEFT JOIN Employees e ON el.employee_id = e.employee_id
            ORDER BY el.expiry_date ASC
            """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                EmployeeLicense license = mapResultSetToLicense(rs);
                licenses.add(license);
            }
            
        } catch (SQLException e) {
            LOGGER.severe("Error retrieving all licenses: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve licenses", e);
        }
        
        return licenses;
    }
    
    /**
     * Get expiring licenses
     */
    public List<EmployeeLicense> getExpiringLicenses(int daysAhead) {
        List<EmployeeLicense> licenses = new ArrayList<>();
        String query = """
            SELECT 
                el.license_id, el.employee_id, el.license_number, el.license_type, 
                el.license_class, el.issue_date, el.expiry_date, el.issuing_authority, 
                el.license_status, el.renewal_reminder_sent, el.created_at, el.updated_at,
                e.first_name + ' ' + e.last_name as employee_name, e.employee_code
            FROM Employee_Licenses el
            LEFT JOIN Employees e ON el.employee_id = e.employee_id
            WHERE el.expiry_date BETWEEN GETDATE() AND DATEADD(day, ?, GETDATE())
                AND el.license_status = 'Valid'
            ORDER BY el.expiry_date ASC
            """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, daysAhead);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    EmployeeLicense license = mapResultSetToLicense(rs);
                    licenses.add(license);
                }
            }
            
        } catch (SQLException e) {
            LOGGER.severe("Error retrieving expiring licenses: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve expiring licenses", e);
        }
        
        return licenses;
    }
    
    /**
     * Add new employee license
     */
    public boolean addEmployeeLicense(EmployeeLicense license) {
        String query = """
            INSERT INTO Employee_Licenses (
                employee_id, license_number, license_type, license_class, 
                issue_date, expiry_date, issuing_authority, license_status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, license.getEmployeeId());
            stmt.setString(2, license.getLicenseNumber());
            stmt.setString(3, license.getLicenseType());
            stmt.setString(4, license.getLicenseClass());
            stmt.setDate(5, license.getIssueDate() != null ? Date.valueOf(license.getIssueDate()) : null);
            stmt.setDate(6, license.getExpiryDate() != null ? Date.valueOf(license.getExpiryDate()) : null);
            stmt.setString(7, license.getIssuingAuthority());
            stmt.setString(8, license.getLicenseStatus());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        license.setLicenseId(generatedKeys.getInt(1));
                    }
                }
                LOGGER.info("Employee license added successfully: " + license.getLicenseNumber());
                return true;
            }
            
        } catch (SQLException e) {
            LOGGER.severe("Error adding employee license: " + e.getMessage());
            throw new RuntimeException("Failed to add employee license", e);
        }
        
        return false;
    }
    
    /**
     * Update employee license
     */
    public boolean updateEmployeeLicense(EmployeeLicense license) {
        String query = """
            UPDATE Employee_Licenses SET 
                license_number = ?, license_type = ?, license_class = ?, 
                issue_date = ?, expiry_date = ?, issuing_authority = ?, 
                license_status = ?, updated_at = GETDATE()
            WHERE license_id = ?
            """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, license.getLicenseNumber());
            stmt.setString(2, license.getLicenseType());
            stmt.setString(3, license.getLicenseClass());
            stmt.setDate(4, license.getIssueDate() != null ? Date.valueOf(license.getIssueDate()) : null);
            stmt.setDate(5, license.getExpiryDate() != null ? Date.valueOf(license.getExpiryDate()) : null);
            stmt.setString(6, license.getIssuingAuthority());
            stmt.setString(7, license.getLicenseStatus());
            stmt.setInt(8, license.getLicenseId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                LOGGER.info("Employee license updated successfully: " + license.getLicenseNumber());
                return true;
            }
            
        } catch (SQLException e) {
            LOGGER.severe("Error updating employee license: " + e.getMessage());
            throw new RuntimeException("Failed to update employee license", e);
        }
        
        return false;
    }
    
    /**
     * Delete employee license
     */
    public boolean deleteEmployeeLicense(int licenseId) {
        String query = "DELETE FROM Employee_Licenses WHERE license_id = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, licenseId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                LOGGER.info("Employee license deleted successfully: ID " + licenseId);
                return true;
            }
            
        } catch (SQLException e) {
            LOGGER.severe("Error deleting employee license: " + e.getMessage());
            throw new RuntimeException("Failed to delete employee license", e);
        }
        
        return false;
    }
    
    /**
     * Generate next employee code
     */
    public String generateEmployeeCode() {
        String query = "SELECT MAX(CAST(SUBSTRING(employee_code, 4, LEN(employee_code)) AS INT)) FROM Employees WHERE employee_code LIKE 'EMP%'";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                int nextNumber = rs.getInt(1) + 1;
                return String.format("EMP%03d", nextNumber);
            } else {
                return "EMP001";
            }
            
        } catch (SQLException e) {
            LOGGER.severe("Error generating employee code: " + e.getMessage());
            return "EMP001";
        }
    }
    
    /**
     * Check if employee code exists
     */
    public boolean employeeCodeExists(String employeeCode) {
        String query = "SELECT COUNT(*) FROM Employees WHERE employee_code = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, employeeCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            LOGGER.severe("Error checking employee code existence: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get employee statistics
     */
    public java.util.Map<String, Integer> getEmployeeStatistics() {
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        String query = """
            SELECT 
                employee_type,
                COUNT(*) as count
            FROM Employees 
            WHERE employment_status = 'Active'
            GROUP BY employee_type
            """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                stats.put(rs.getString("employee_type"), rs.getInt("count"));
            }
            
        } catch (SQLException e) {
            LOGGER.severe("Error retrieving employee statistics: " + e.getMessage());
        }
        
        return stats;
    }
    
    // ====================================
    // PRIVATE HELPER METHODS
    // ====================================
    
    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        
        employee.setEmployeeId(rs.getInt("employee_id"));
        employee.setEmployeeCode(rs.getString("employee_code"));
        employee.setFirstName(rs.getString("first_name"));
        employee.setLastName(rs.getString("last_name"));
        employee.setEmail(rs.getString("email"));
        employee.setPhone(rs.getString("phone"));
        employee.setNationalId(rs.getString("national_id"));
        employee.setAddress(rs.getString("address"));
        employee.setGender(rs.getString("gender"));
        employee.setEmployeeType(rs.getString("employee_type"));
        employee.setDepartment(rs.getString("department"));
        employee.setPosition(rs.getString("position"));
        employee.setSalary(rs.getDouble("salary"));
        employee.setEmploymentStatus(rs.getString("employment_status"));
        employee.setEmergencyContactName(rs.getString("emergency_contact_name"));
        employee.setEmergencyContactPhone(rs.getString("emergency_contact_phone"));
        employee.setPhotoPath(rs.getString("photo_path"));
        
        // Handle dates
        Date dateOfBirth = rs.getDate("date_of_birth");
        if (dateOfBirth != null) {
            employee.setDateOfBirth(dateOfBirth.toLocalDate());
        }
        
        Date hireDate = rs.getDate("hire_date");
        if (hireDate != null) {
            employee.setHireDate(hireDate.toLocalDate());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            employee.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            employee.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return employee;
    }
    
    private EmployeeLicense mapResultSetToLicense(ResultSet rs) throws SQLException {
        EmployeeLicense license = new EmployeeLicense();
        
        license.setLicenseId(rs.getInt("license_id"));
        license.setEmployeeId(rs.getInt("employee_id"));
        license.setLicenseNumber(rs.getString("license_number"));
        license.setLicenseType(rs.getString("license_type"));
        license.setLicenseClass(rs.getString("license_class"));
        license.setIssuingAuthority(rs.getString("issuing_authority"));
        license.setLicenseStatus(rs.getString("license_status"));
        license.setRenewalReminderSent(rs.getBoolean("renewal_reminder_sent"));
        license.setEmployeeName(rs.getString("employee_name"));
        license.setEmployeeCode(rs.getString("employee_code"));
        
        // Handle dates
        Date issueDate = rs.getDate("issue_date");
        if (issueDate != null) {
            license.setIssueDate(issueDate.toLocalDate());
        }
        
        Date expiryDate = rs.getDate("expiry_date");
        if (expiryDate != null) {
            license.setExpiryDate(expiryDate.toLocalDate());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            license.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            license.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return license;
    }
}