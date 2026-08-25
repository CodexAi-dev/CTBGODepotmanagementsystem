package lk.bustracking.depotmanagementsystem.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * Bus Model - Represents a physical bus with all its properties
 */
public class Bus {
    
    // Primary identification
    private int busId;
    private String busNumber;        // e.g., "CTB-245"
    private String registrationNumber; // Government registration
    
    // Physical specifications
    private String make;             // Manufacturer
    private String model;            // Model name
    private int yearManufactured;
    private int seatingCapacity;
    private BigDecimal fuelTankCapacity;
    
    // Operational status
    private OperationalStatus operationalStatus;
    private ConditionRating conditionRating;
    
    // GPS device information
    private String gpsDeviceId;      // Physical GPS device identifier
    private GpsStatus gpsDeviceStatus;
    private LocalDateTime lastGpsUpdate;
    
    // Current assignments
    private int currentRouteId;
    private String currentRouteName;  // For display purposes
    private String currentRouteNumber; // For display purposes
    private int assignedDriverId;
    private String assignedDriverName; // For display purposes
    
    // Maintenance information
    private LocalDate lastServiceDate;
    private LocalDate nextServiceDueDate;
    private BigDecimal totalMileageKm;
    
    // Administrative
    private LocalDate purchaseDate;
    private BigDecimal purchaseCost;
    private LocalDate insuranceExpiry;
    private LocalDate fitnessCertificateExpiry;
    
    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int createdBy;
    
    // Real-time data (from GPS)
    private BigDecimal currentLatitude;
    private BigDecimal currentLongitude;
    private BigDecimal currentSpeed;
    private BigDecimal currentFuelLevel;
    private LocalDateTime lastLocationUpdate;
    
    // Calculated fields
    private String maintenanceStatus; // OVERDUE, DUE_SOON, OK
    private boolean isOnline;         // Based on GPS updates
    
    // Enums for status fields (DECLARED ONLY ONCE)
    public enum OperationalStatus {
        ACTIVE("Active"),
        MAINTENANCE("In Maintenance"),
        RETIRED("Retired"),
        OUT_OF_SERVICE("Out of Service");
        
        private final String displayName;
        
        OperationalStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum ConditionRating {
        EXCELLENT("Excellent"),
        GOOD("Good"),
        FAIR("Fair"),
        POOR("Poor");
        
        private final String displayName;
        
        ConditionRating(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum GpsStatus {
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        FAULTY("Faulty");
        
        private final String displayName;
        
        GpsStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructors
    public Bus() {
        this.operationalStatus = OperationalStatus.ACTIVE;
        this.conditionRating = ConditionRating.GOOD;
        this.gpsDeviceStatus = GpsStatus.ACTIVE;
        this.totalMileageKm = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Bus(String busNumber, String registrationNumber, String make, String model, 
               int seatingCapacity, String gpsDeviceId) {
        this();
        this.busNumber = busNumber;
        this.registrationNumber = registrationNumber;
        this.make = make;
        this.model = model;
        this.seatingCapacity = seatingCapacity;
        this.gpsDeviceId = gpsDeviceId;
    }
    
    // Getters and Setters
    public int getBusId() { return busId; }
    public void setBusId(int busId) { this.busId = busId; }
    
    public String getBusNumber() { return busNumber; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }
    
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public int getYearManufactured() { return yearManufactured; }
    public void setYearManufactured(int yearManufactured) { this.yearManufactured = yearManufactured; }
    
    public int getSeatingCapacity() { return seatingCapacity; }
    public void setSeatingCapacity(int seatingCapacity) { this.seatingCapacity = seatingCapacity; }
    
    public BigDecimal getFuelTankCapacity() { return fuelTankCapacity; }
    public void setFuelTankCapacity(BigDecimal fuelTankCapacity) { this.fuelTankCapacity = fuelTankCapacity; }
    
    public OperationalStatus getOperationalStatus() { return operationalStatus; }
    public void setOperationalStatus(OperationalStatus operationalStatus) { this.operationalStatus = operationalStatus; }
    
    public ConditionRating getConditionRating() { return conditionRating; }
    public void setConditionRating(ConditionRating conditionRating) { this.conditionRating = conditionRating; }
    
    public String getGpsDeviceId() { return gpsDeviceId; }
    public void setGpsDeviceId(String gpsDeviceId) { this.gpsDeviceId = gpsDeviceId; }
    
    public GpsStatus getGpsDeviceStatus() { return gpsDeviceStatus; }
    public void setGpsDeviceStatus(GpsStatus gpsDeviceStatus) { this.gpsDeviceStatus = gpsDeviceStatus; }
    
    public LocalDateTime getLastGpsUpdate() { return lastGpsUpdate; }
    public void setLastGpsUpdate(LocalDateTime lastGpsUpdate) { this.lastGpsUpdate = lastGpsUpdate; }
    
    public int getCurrentRouteId() { return currentRouteId; }
    public void setCurrentRouteId(int currentRouteId) { this.currentRouteId = currentRouteId; }
    
    public String getCurrentRouteName() { return currentRouteName; }
    public void setCurrentRouteName(String currentRouteName) { this.currentRouteName = currentRouteName; }
    
    public String getCurrentRouteNumber() { return currentRouteNumber; }
    public void setCurrentRouteNumber(String currentRouteNumber) { this.currentRouteNumber = currentRouteNumber; }
    
    public int getAssignedDriverId() { return assignedDriverId; }
    public void setAssignedDriverId(int assignedDriverId) { this.assignedDriverId = assignedDriverId; }
    
    public String getAssignedDriverName() { return assignedDriverName; }
    public void setAssignedDriverName(String assignedDriverName) { this.assignedDriverName = assignedDriverName; }
    
    public LocalDate getLastServiceDate() { return lastServiceDate; }
    public void setLastServiceDate(LocalDate lastServiceDate) { this.lastServiceDate = lastServiceDate; }
    
    public LocalDate getNextServiceDueDate() { return nextServiceDueDate; }
    public void setNextServiceDueDate(LocalDate nextServiceDueDate) { this.nextServiceDueDate = nextServiceDueDate; }
    
    public BigDecimal getTotalMileageKm() { return totalMileageKm; }
    public void setTotalMileageKm(BigDecimal totalMileageKm) { this.totalMileageKm = totalMileageKm; }
    
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    
    public BigDecimal getPurchaseCost() { return purchaseCost; }
    public void setPurchaseCost(BigDecimal purchaseCost) { this.purchaseCost = purchaseCost; }
    
    public LocalDate getInsuranceExpiry() { return insuranceExpiry; }
    public void setInsuranceExpiry(LocalDate insuranceExpiry) { this.insuranceExpiry = insuranceExpiry; }
    
    public LocalDate getFitnessCertificateExpiry() { return fitnessCertificateExpiry; }
    public void setFitnessCertificateExpiry(LocalDate fitnessCertificateExpiry) { this.fitnessCertificateExpiry = fitnessCertificateExpiry; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    
    // Real-time data getters and setters
    public BigDecimal getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(BigDecimal currentLatitude) { this.currentLatitude = currentLatitude; }
    
    public BigDecimal getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(BigDecimal currentLongitude) { this.currentLongitude = currentLongitude; }
    
    public BigDecimal getCurrentSpeed() { return currentSpeed; }
    public void setCurrentSpeed(BigDecimal currentSpeed) { this.currentSpeed = currentSpeed; }
    
    public BigDecimal getCurrentFuelLevel() { return currentFuelLevel; }
    public void setCurrentFuelLevel(BigDecimal currentFuelLevel) { this.currentFuelLevel = currentFuelLevel; }
    
    public LocalDateTime getLastLocationUpdate() { return lastLocationUpdate; }
    public void setLastLocationUpdate(LocalDateTime lastLocationUpdate) { this.lastLocationUpdate = lastLocationUpdate; }
    
    public String getMaintenanceStatus() { return maintenanceStatus; }
    public void setMaintenanceStatus(String maintenanceStatus) { this.maintenanceStatus = maintenanceStatus; }
    
    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }
    
    // Utility methods
    
    /**
     * Get current route display text
     */
    public String getCurrentRouteDisplay() {
        if (currentRouteNumber != null && currentRouteName != null) {
            return currentRouteNumber + " - " + currentRouteName;
        } else if (currentRouteNumber != null) {
            return currentRouteNumber;
        }
        return "Not Assigned";
    }
    
    /**
     * Check if bus needs attention (maintenance due, GPS issues, etc.)
     */
    public boolean needsAttention() {
        return getOperationalStatus() == OperationalStatus.MAINTENANCE ||
               getConditionRating() == ConditionRating.POOR ||
               (getGpsDeviceStatus() != null && getGpsDeviceStatus() != GpsStatus.ACTIVE) ||
               isMaintenanceDue();
    }
    
    /**
     * Get display name for the bus (combination of number and registration)
     */
    public String getDisplayName() {
        return busNumber + " (" + (registrationNumber != null ? registrationNumber : "No Reg") + ")";
    }
    
    /**
     * Check if bus is currently assigned to a route
     */
    public boolean isAssignedToRoute() {
        return currentRouteId > 0;
    }
    
    /**
     * Check if maintenance is due or overdue
     */
    public boolean isMaintenanceDue() {
        if (nextServiceDueDate == null) return false;
        return !nextServiceDueDate.isAfter(LocalDate.now());
    }
    
    /**
     * Get days until next service
     */
    public long getDaysUntilService() {
        if (nextServiceDueDate == null) return -1;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), nextServiceDueDate);
    }
    
    /**
     * Check if GPS is active (last update within 30 minutes)
     */
    public boolean isGpsActive() {
        if (lastGpsUpdate == null) return false;
        return lastGpsUpdate.isAfter(LocalDateTime.now().minusMinutes(30));
    }
    
    /**
     * Get age of the bus in years
     */
    public int getAgeInYears() {
        if (yearManufactured == 0) return 0;
        return LocalDate.now().getYear() - yearManufactured;
    }
    
    /**
     * Get status color for UI display
     */
    public String getStatusColor() {
        return switch (operationalStatus) {
            case ACTIVE -> "#10b981";      // Green
            case MAINTENANCE -> "#f59e0b"; // Yellow
            case OUT_OF_SERVICE -> "#ef4444"; // Red
            case RETIRED -> "#6b7280";     // Gray
        };
    }
    
    /**
     * Get condition color for UI display
     */
    public String getConditionColor() {
        return switch (conditionRating) {
            case EXCELLENT -> "#10b981";   // Green
            case GOOD -> "#3b82f6";        // Blue
            case FAIR -> "#f59e0b";        // Yellow
            case POOR -> "#ef4444";        // Red
        };
    }
    
    /**
     * Get GPS status color
     */
    public String getGpsStatusColor() {
        return switch (gpsDeviceStatus) {
            case ACTIVE -> "#10b981";      // Green
            case INACTIVE -> "#f59e0b";    // Yellow
            case FAULTY -> "#ef4444";      // Red
        };
    }
    
    /**
     * Format fuel tank capacity for display
     */
    public String getFormattedFuelCapacity() {
        if (fuelTankCapacity == null) return "N/A";
        return fuelTankCapacity.toString() + " L";
    }
    
    /**
     * Format total mileage for display
     */
    public String getFormattedMileage() {
        if (totalMileageKm == null) return "0 km";
        return String.format("%,.0f km", totalMileageKm);
    }
    
    /**
     * Format current speed for display
     */
    public String getFormattedCurrentSpeed() {
        if (currentSpeed == null) return "N/A";
        return String.format("%.1f km/h", currentSpeed);
    }
    
    /**
     * Format current fuel level for display
     */
    public String getFormattedFuelLevel() {
        if (currentFuelLevel == null) return "N/A";
        return String.format("%.1f%%", currentFuelLevel);
    }
    
    /**
     * Get comprehensive status summary
     */
    public String getStatusSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Status: ").append(operationalStatus.getDisplayName());
        summary.append(", Condition: ").append(conditionRating.getDisplayName());
        summary.append(", GPS: ").append(gpsDeviceStatus.getDisplayName());
        
        if (isAssignedToRoute()) {
            summary.append(", Route: ").append(getCurrentRouteDisplay());
        }
        
        return summary.toString();
    }
    
    /**
     * Get attention reasons
     */
    public String getAttentionReasons() {
        if (!needsAttention()) return "No issues";
        
        StringBuilder reasons = new StringBuilder();
        
        if (isMaintenanceDue()) {
            reasons.append("Maintenance overdue; ");
        }
        
        if (gpsDeviceStatus != GpsStatus.ACTIVE) {
            reasons.append("GPS issues; ");
        }
        
        if (operationalStatus == OperationalStatus.MAINTENANCE) {
            reasons.append("Currently in maintenance; ");
        }
        
        if (conditionRating == ConditionRating.POOR) {
            reasons.append("Poor condition rating; ");
        }
        
        // Remove trailing semicolon and space
        if (reasons.length() > 0) {
            reasons.setLength(reasons.length() - 2);
        }
        
        return reasons.toString();
    }
    
    @Override
    public String toString() {
        return "Bus{" +
                "busId=" + busId +
                ", busNumber='" + busNumber + '\'' +
                ", registrationNumber='" + registrationNumber + '\'' +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", operationalStatus=" + operationalStatus +
                ", currentRouteId=" + currentRouteId +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Bus bus = (Bus) obj;
        return busId == bus.busId;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(busId);
    }
}