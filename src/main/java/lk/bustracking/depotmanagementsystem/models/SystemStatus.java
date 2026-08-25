package lk.bustracking.depotmanagementsystem.models;

import java.time.LocalDateTime;

/**
 * System Status Model - Represents overall system health and status
 */
public class SystemStatus {
    
    private boolean gpsSystemOnline;
    private boolean databaseConnected;
    private boolean apiServiceOnline;
    private boolean maintenanceMode;
    private LocalDateTime lastHealthCheck;
    private String version;
    private int activeConnections;
    private double systemLoad; // CPU usage percentage
    private double memoryUsage; // Memory usage percentage
    private String serverStatus;
    
    public SystemStatus() {
        this.lastHealthCheck = LocalDateTime.now();
        this.version = "3.0.0";
        this.serverStatus = "Online";
        this.systemLoad = 25.5;
        this.memoryUsage = 68.2;
        this.activeConnections = 12;
    }
    
    // Getters and Setters
    public boolean isGpsSystemOnline() { return gpsSystemOnline; }
    public void setGpsSystemOnline(boolean gpsSystemOnline) { this.gpsSystemOnline = gpsSystemOnline; }
    
    public boolean isDatabaseConnected() { return databaseConnected; }
    public void setDatabaseConnected(boolean databaseConnected) { this.databaseConnected = databaseConnected; }
    
    public boolean isApiServiceOnline() { return apiServiceOnline; }
    public void setApiServiceOnline(boolean apiServiceOnline) { this.apiServiceOnline = apiServiceOnline; }
    
    public boolean isMaintenanceMode() { return maintenanceMode; }
    public void setMaintenanceMode(boolean maintenanceMode) { this.maintenanceMode = maintenanceMode; }
    
    public LocalDateTime getLastHealthCheck() { return lastHealthCheck; }
    public void setLastHealthCheck(LocalDateTime lastHealthCheck) { this.lastHealthCheck = lastHealthCheck; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public int getActiveConnections() { return activeConnections; }
    public void setActiveConnections(int activeConnections) { this.activeConnections = activeConnections; }
    
    public double getSystemLoad() { return systemLoad; }
    public void setSystemLoad(double systemLoad) { this.systemLoad = systemLoad; }
    
    public double getMemoryUsage() { return memoryUsage; }
    public void setMemoryUsage(double memoryUsage) { this.memoryUsage = memoryUsage; }
    
    public String getServerStatus() { return serverStatus; }
    public void setServerStatus(String serverStatus) { this.serverStatus = serverStatus; }
    
    /**
     * Check if all critical systems are operational
     */
    public boolean isSystemHealthy() {
        return gpsSystemOnline && databaseConnected && apiServiceOnline && !maintenanceMode;
    }
    
    /**
     * Get overall system status message
     */
    public String getOverallStatus() {
        if (maintenanceMode) {
            return "Maintenance Mode";
        } else if (isSystemHealthy()) {
            return "All Systems Operational";
        } else {
            return "System Issues Detected";
        }
    }
    
    /**
     * Get system health percentage
     */
    public double getHealthPercentage() {
        int healthyComponents = 0;
        int totalComponents = 4;
        
        if (gpsSystemOnline) healthyComponents++;
        if (databaseConnected) healthyComponents++;
        if (apiServiceOnline) healthyComponents++;
        if (!maintenanceMode) healthyComponents++;
        
        return (double) healthyComponents / totalComponents * 100;
    }
    
    /**
     * Update health check timestamp
     */
    public void updateHealthCheck() {
        this.lastHealthCheck = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "SystemStatus{" +
                "gpsSystemOnline=" + gpsSystemOnline +
                ", databaseConnected=" + databaseConnected +
                ", apiServiceOnline=" + apiServiceOnline +
                ", maintenanceMode=" + maintenanceMode +
                ", overallStatus='" + getOverallStatus() + '\'' +
                '}';
    }

    public void setSystemHealthy(boolean healthy) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}