package lk.bustracking.depotmanagementsystem.models;

/**
 * Activity Type Enum - Defines different types of system activities
 */
public enum ActivityType {
    // Bus related activities
    BUS_STARTED("Bus Started"),
    BUS_STOPPED("Bus Stopped"),
    BUS_MAINTENANCE("Bus Maintenance"),
    BUS_REGISTERED("Bus Registered"),
    BUS_DECOMMISSIONED("Bus Decommissioned"),
    
    // Employee related activities
    EMPLOYEE_CHECKIN("Employee Check-in"),
    EMPLOYEE_CHECKOUT("Employee Check-out"),
    EMPLOYEE_REGISTERED("Employee Registered"),
    EMPLOYEE_UPDATED("Employee Updated"),
    DRIVER_ASSIGNED("Driver Assigned"),
    
    // Route related activities
    ROUTE_STARTED("Route Started"),
    ROUTE_COMPLETED("Route Completed"),
    ROUTE_CANCELLED("Route Cancelled"),
    ROUTE_DELAYED("Route Delayed"),
    
    // System alerts
    GPS_ALERT("GPS Alert"),
    MAINTENANCE_ALERT("Maintenance Alert"),
    FUEL_ALERT("Fuel Alert"),
    EMERGENCY_STOP("Emergency Stop"),
    
    // Administrative activities
    REPORT_GENERATED("Report Generated"),
    BACKUP_CREATED("Backup Created"),
    SYSTEM_UPDATE("System Update"),
    USER_LOGIN("User Login"),
    USER_LOGOUT("User Logout"),
    
    // Generic activity
    SYSTEM_NOTIFICATION("System Notification");
    
    private final String displayName;
    
    ActivityType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}