package lk.bustracking.depotmanagementsystem.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced Route Model - Complete route representation with all necessary properties
 */
public class Route {
    
    // Core properties
    private int routeId;
    private String routeNumber;
    private String routeName;
    private String startLocation;
    private String endLocation;
    private BigDecimal totalDistanceKm;
    private Integer estimatedDurationMinutes;
    private RouteType routeType;
    private BigDecimal farePrice;
    private boolean isActive;
    private LocalTime operatingHoursStart;
    private LocalTime operatingHoursEnd;
    private Integer frequencyMinutes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional properties for BusDAO compatibility
    private List<RouteStop> stops;
    private String routeDirection;
    
    // Performance metrics (populated by DAO)
    private int busesAssigned;
    private int totalTrips;
    private BigDecimal avgDelayMinutes;
    private int totalPassengers;
    private BigDecimal totalRevenue;
    private BigDecimal avgFuelPerTrip;
    private BigDecimal onTimePercentage;
    
    public enum RouteType {
        REGULAR("Regular Service"),
        EXPRESS("Express Service"),
        NIGHT_SERVICE("Night Service");
        
        private final String displayName;
        
        RouteType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructors
    public Route() {
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.stops = new ArrayList<>();
    }
    
    public Route(String routeNumber, String routeName, String startLocation, 
                String endLocation, RouteType routeType) {
        this();
        this.routeNumber = routeNumber;
        this.routeName = routeName;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.routeType = routeType;
    }
    
    // Business methods
    public String getRouteDisplay() {
        return String.format("Route %s: %s", routeNumber, routeName);
    }
    
    public String getRouteDirection() {
        if (routeDirection != null && !routeDirection.isEmpty()) {
            return routeDirection;
        }
        return String.format("%s → %s", startLocation, endLocation);
    }
    
    public String getOperatingHoursDisplay() {
        if (operatingHoursStart != null && operatingHoursEnd != null) {
            return String.format("%s - %s", operatingHoursStart, operatingHoursEnd);
        }
        return "Not set";
    }
    
    public int getEstimatedTripsPerDay() {
        if (operatingHoursStart != null && operatingHoursEnd != null && frequencyMinutes != null && frequencyMinutes > 0) {
            int operatingMinutes = (int) java.time.Duration.between(operatingHoursStart, operatingHoursEnd).toMinutes();
            if (operatingMinutes < 0) { // Handle overnight routes
                operatingMinutes += 24 * 60;
            }
            return Math.max(1, operatingMinutes / frequencyMinutes);
        }
        return 0;
    }
    
    public String getStatusDisplay() {
        return isActive ? "Active" : "Inactive";
    }
    
    /**
     * Get detailed information about the route (for details dialog)
     */
    public String getDetailedInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Route Information\n");
        info.append("================\n\n");
        
        info.append("Route Number: ").append(routeNumber).append("\n");
        info.append("Route Name: ").append(routeName).append("\n");
        info.append("Type: ").append(routeType != null ? routeType.getDisplayName() : "N/A").append("\n");
        info.append("Status: ").append(getStatusDisplay()).append("\n\n");
        
        info.append("Route Details\n");
        info.append("-------------\n");
        info.append("Start Location: ").append(startLocation).append("\n");
        info.append("End Location: ").append(endLocation).append("\n");
        info.append("Total Distance: ").append(totalDistanceKm != null ? 
            String.format("%.1f km", totalDistanceKm.doubleValue()) : "N/A").append("\n");
        info.append("Estimated Duration: ").append(estimatedDurationMinutes != null ? 
            estimatedDurationMinutes + " minutes" : "N/A").append("\n");
        info.append("Fare Price: ").append(farePrice != null ? 
            String.format("Rs. %.2f", farePrice.doubleValue()) : "N/A").append("\n\n");
        
        info.append("Schedule Information\n");
        info.append("-------------------\n");
        info.append("Operating Hours: ").append(getOperatingHoursDisplay()).append("\n");
        info.append("Service Frequency: ").append(frequencyMinutes != null ? 
            frequencyMinutes + " minutes" : "N/A").append("\n");
        info.append("Estimated Trips/Day: ").append(getEstimatedTripsPerDay()).append("\n\n");
        
        if (busesAssigned > 0 || totalTrips > 0) {
            info.append("Performance Metrics\n");
            info.append("------------------\n");
            info.append("Buses Assigned: ").append(busesAssigned).append("\n");
            info.append("Total Trips (30 days): ").append(totalTrips).append("\n");
            info.append("Avg Delay: ").append(avgDelayMinutes != null ? 
                String.format("%.1f minutes", avgDelayMinutes.doubleValue()) : "N/A").append("\n");
            info.append("Total Passengers: ").append(totalPassengers).append("\n");
            info.append("Total Revenue: ").append(totalRevenue != null ? 
                String.format("Rs. %.2f", totalRevenue.doubleValue()) : "N/A").append("\n");
            info.append("On-Time Performance: ").append(onTimePercentage != null ? 
                String.format("%.1f%%", onTimePercentage.doubleValue()) : "N/A").append("\n");
        }
        
        if (stops != null && !stops.isEmpty()) {
            info.append("\nRoute Stops (").append(stops.size()).append(" stops)\n");
            info.append("------------------------\n");
            for (RouteStop stop : stops) {
                info.append(String.format("%d. %s", stop.getStopOrder(), stop.getStopName()));
                if (stop.getDistanceFromStartKm() != null) {
                    info.append(String.format(" (%.1f km)", stop.getDistanceFromStartKm().doubleValue()));
                }
                info.append("\n");
            }
        }
        
        return info.toString();
    }
    
    /**
     * Validate if route has all required fields
     */
    public boolean isValidRoute() {
        return routeNumber != null && !routeNumber.trim().isEmpty() &&
               routeName != null && !routeName.trim().isEmpty() &&
               startLocation != null && !startLocation.trim().isEmpty() &&
               endLocation != null && !endLocation.trim().isEmpty() &&
               routeType != null;
    }
    
    /**
     * Add a stop to this route
     */
    public void addStop(RouteStop stop) {
        if (stops == null) {
            stops = new ArrayList<>();
        }
        stop.setRouteId(this.routeId);
        stops.add(stop);
    }
    
    /**
     * Remove a stop from this route
     */
    public void removeStop(RouteStop stop) {
        if (stops != null) {
            stops.remove(stop);
        }
    }
    
    /**
     * Get stop count
     */
    public int getStopCount() {
        return stops != null ? stops.size() : 0;
    }
    
    // Getters and Setters
    public int getRouteId() { return routeId; }
    public void setRouteId(int routeId) { this.routeId = routeId; }
    
    public String getRouteNumber() { return routeNumber; }
    public void setRouteNumber(String routeNumber) { this.routeNumber = routeNumber; }
    
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    
    public String getStartLocation() { return startLocation; }
    public void setStartLocation(String startLocation) { this.startLocation = startLocation; }
    
    public String getEndLocation() { return endLocation; }
    public void setEndLocation(String endLocation) { this.endLocation = endLocation; }
    
    public BigDecimal getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(BigDecimal totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }
    
    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { 
        this.estimatedDurationMinutes = estimatedDurationMinutes; 
    }
    
    public RouteType getRouteType() { return routeType; }
    public void setRouteType(RouteType routeType) { this.routeType = routeType; }
    
    public BigDecimal getFarePrice() { return farePrice; }
    public void setFarePrice(BigDecimal farePrice) { this.farePrice = farePrice; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    
    public LocalTime getOperatingHoursStart() { return operatingHoursStart; }
    public void setOperatingHoursStart(LocalTime operatingHoursStart) { 
        this.operatingHoursStart = operatingHoursStart; 
    }
    
    public LocalTime getOperatingHoursEnd() { return operatingHoursEnd; }
    public void setOperatingHoursEnd(LocalTime operatingHoursEnd) { 
        this.operatingHoursEnd = operatingHoursEnd; 
    }
    
    public Integer getFrequencyMinutes() { return frequencyMinutes; }
    public void setFrequencyMinutes(Integer frequencyMinutes) { this.frequencyMinutes = frequencyMinutes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<RouteStop> getStops() { return stops; }
    public void setStops(List<RouteStop> stops) { this.stops = stops; }
    
    public String getRouteDirectionField() { return routeDirection; }
    public void setRouteDirection(String routeDirection) { this.routeDirection = routeDirection; }
    
    // Performance metrics getters and setters
    public int getBusesAssigned() { return busesAssigned; }
    public void setBusesAssigned(int busesAssigned) { this.busesAssigned = busesAssigned; }
    
    public int getTotalTrips() { return totalTrips; }
    public void setTotalTrips(int totalTrips) { this.totalTrips = totalTrips; }
    
    public BigDecimal getAvgDelayMinutes() { return avgDelayMinutes; }
    public void setAvgDelayMinutes(BigDecimal avgDelayMinutes) { this.avgDelayMinutes = avgDelayMinutes; }
    
    public int getTotalPassengers() { return totalPassengers; }
    public void setTotalPassengers(int totalPassengers) { this.totalPassengers = totalPassengers; }
    
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    
    public BigDecimal getAvgFuelPerTrip() { return avgFuelPerTrip; }
    public void setAvgFuelPerTrip(BigDecimal avgFuelPerTrip) { this.avgFuelPerTrip = avgFuelPerTrip; }
    
    public BigDecimal getOnTimePercentage() { return onTimePercentage; }
    public void setOnTimePercentage(BigDecimal onTimePercentage) { this.onTimePercentage = onTimePercentage; }
    
    @Override
    public String toString() {
        return getRouteDisplay();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Route route = (Route) obj;
        return routeId == route.routeId;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(routeId);
    }
}