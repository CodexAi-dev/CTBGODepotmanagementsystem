package lk.bustracking.depotmanagementsystem.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Dashboard Data Model - Holds all dashboard-related information
 */
public class DashboardData {

    // Bus Statistics
    private int totalBuses;
    private int activeBuses;
    private int busesInMaintenance;
    private int busesOffline;

    // Employee Statistics
    private int totalEmployees;
    private int employeesOnDuty;
    private int driversAvailable;
    private int employeesOnLeave;

    // Route Statistics
    private int totalRoutes;
    private int activeRoutes;
    private int completedTripsToday;
    private int pendingTrips;

    // System Status
    private SystemStatus systemStatus;
    private LocalDateTime lastUpdated;

    // Recent Activities
    private List<ActivityLog> recentActivities;

    // Performance Metrics
    private double fuelEfficiency;
    private double onTimePerformance;
    private int passengersToday;
    private double revenue;

    public DashboardData() {
        this.recentActivities = new ArrayList<>();
        this.systemStatus = new SystemStatus();
        this.lastUpdated = LocalDateTime.now();
        // Initialize with zeros instead of demo data
        initializeWithZeros();
    }

    /**
     * Initialize with zeros for real data loading
     */
    private void initializeWithZeros() {
        // Bus statistics
        this.totalBuses = 0;
        this.activeBuses = 0;
        this.busesInMaintenance = 0;
        this.busesOffline = 0;

        // Employee statistics
        this.totalEmployees = 0;
        this.employeesOnDuty = 0;
        this.driversAvailable = 0;
        this.employeesOnLeave = 0;

        // Route statistics
        this.totalRoutes = 0;
        this.activeRoutes = 0;
        this.completedTripsToday = 0;
        this.pendingTrips = 0;

        // Performance metrics
        this.fuelEfficiency = 0.0;
        this.onTimePerformance = 0.0;
        this.passengersToday = 0;
        this.revenue = 0.0;

        // Sample recent activities (empty for now)
        // recentActivities will be populated from database

        // System status
        this.systemStatus.setGpsSystemOnline(false);
        this.systemStatus.setDatabaseConnected(false);
        this.systemStatus.setApiServiceOnline(false);
        this.systemStatus.setMaintenanceMode(false);
    }

    // Getters and Setters for Bus Statistics
    public int getTotalBuses() {
        return totalBuses;
    }

    public void setTotalBuses(int totalBuses) {
        this.totalBuses = totalBuses;
    }

    public int getActiveBuses() {
        return activeBuses;
    }

    public void setActiveBuses(int activeBuses) {
        this.activeBuses = activeBuses;
    }

    public int getBusesInMaintenance() {
        return busesInMaintenance;
    }

    public void setBusesInMaintenance(int busesInMaintenance) {
        this.busesInMaintenance = busesInMaintenance;
    }

    public int getBusesOffline() {
        return busesOffline;
    }

    public void setBusesOffline(int busesOffline) {
        this.busesOffline = busesOffline;
    }

    // Getters and Setters for Employee Statistics
    public int getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(int totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public int getEmployeesOnDuty() {
        return employeesOnDuty;
    }

    public void setEmployeesOnDuty(int employeesOnDuty) {
        this.employeesOnDuty = employeesOnDuty;
    }

    public int getDriversAvailable() {
        return driversAvailable;
    }

    public void setDriversAvailable(int driversAvailable) {
        this.driversAvailable = driversAvailable;
    }

    public int getEmployeesOnLeave() {
        return employeesOnLeave;
    }

    public void setEmployeesOnLeave(int employeesOnLeave) {
        this.employeesOnLeave = employeesOnLeave;
    }

    // Getters and Setters for Route Statistics
    public int getTotalRoutes() {
        return totalRoutes;
    }

    public void setTotalRoutes(int totalRoutes) {
        this.totalRoutes = totalRoutes;
    }

    public int getActiveRoutes() {
        return activeRoutes;
    }

    public void setActiveRoutes(int activeRoutes) {
        this.activeRoutes = activeRoutes;
    }

    public int getCompletedTripsToday() {
        return completedTripsToday;
    }

    public void setCompletedTripsToday(int completedTripsToday) {
        this.completedTripsToday = completedTripsToday;
    }

    public int getPendingTrips() {
        return pendingTrips;
    }

    public void setPendingTrips(int pendingTrips) {
        this.pendingTrips = pendingTrips;
    }

    // Getters and Setters for Performance Metrics
    public double getFuelEfficiency() {
        return fuelEfficiency;
    }

    public void setFuelEfficiency(double fuelEfficiency) {
        this.fuelEfficiency = fuelEfficiency;
    }

    public double getOnTimePerformance() {
        return onTimePerformance;
    }

    public void setOnTimePerformance(double onTimePerformance) {
        this.onTimePerformance = onTimePerformance;
    }

    public int getPassengersToday() {
        return passengersToday;
    }

    public void setPassengersToday(int passengersToday) {
        this.passengersToday = passengersToday;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    // System Status
    public SystemStatus getSystemStatus() {
        return systemStatus;
    }

    public void setSystemStatus(SystemStatus systemStatus) {
        this.systemStatus = systemStatus;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // Recent Activities
    public List<ActivityLog> getRecentActivities() {
        return recentActivities;
    }

    public void setRecentActivities(List<ActivityLog> recentActivities) {
        this.recentActivities = recentActivities;
    }

    public void addActivity(ActivityLog activity) {
        this.recentActivities.add(0, activity); // Add to the beginning
        // Keep only the last 50 activities
        if (this.recentActivities.size() > 50) {
            this.recentActivities = this.recentActivities.subList(0, 50);
        }
    }

    /**
     * Calculate dashboard summary statistics
     */
    public double getBusUtilizationRate() {
        return totalBuses > 0 ? (double) activeBuses / totalBuses * 100 : 0;
    }

    public double getEmployeeUtilizationRate() {
        return totalEmployees > 0 ? (double) employeesOnDuty / totalEmployees * 100 : 0;
    }

    public double getRouteCompletionRate() {
        int totalTrips = completedTripsToday + pendingTrips;
        return totalTrips > 0 ? (double) completedTripsToday / totalTrips * 100 : 0;
    }

    /**
     * Update timestamp when data is refreshed
     */
    public void updateTimestamp() {
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Get formatted summary for display
     */
    public String getFormattedSummary() {
        return String.format(
                "Buses: %d/%d active | Employees: %d/%d on duty | Routes: %d active | Performance: %.1f%%",
                activeBuses, totalBuses, employeesOnDuty, totalEmployees, activeRoutes, onTimePerformance);
    }

    @Override
    public String toString() {
        return "DashboardData{" +
                "totalBuses=" + totalBuses +
                ", activeBuses=" + activeBuses +
                ", totalEmployees=" + totalEmployees +
                ", employeesOnDuty=" + employeesOnDuty +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}