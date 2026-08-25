package lk.bustracking.depotmanagementsystem.models;

import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * RouteStop Model - Represents a stop along a bus route
 */
public class RouteStop {
    
    private int stopId;
    private int routeId;
    private String stopName;
    private int stopOrder;               // Order of stop in the route (1, 2, 3...)
    private BigDecimal distanceFromStartKm;
    private int estimatedTimeMinutes;    // Time from start to this stop
    
    // GPS coordinates
    private BigDecimal latitude;
    private BigDecimal longitude;
    
    private LocalDateTime createdAt;
    
    // Constructors
    public RouteStop() {
        this.createdAt = LocalDateTime.now();
    }
    
    public RouteStop(int routeId, String stopName, int stopOrder) {
        this();
        this.routeId = routeId;
        this.stopName = stopName;
        this.stopOrder = stopOrder;
    }
    
    // Getters and Setters
    public int getStopId() { return stopId; }
    public void setStopId(int stopId) { this.stopId = stopId; }
    
    public int getRouteId() { return routeId; }
    public void setRouteId(int routeId) { this.routeId = routeId; }
    
    public String getStopName() { return stopName; }
    public void setStopName(String stopName) { this.stopName = stopName; }
    
    public int getStopOrder() { return stopOrder; }
    public void setStopOrder(int stopOrder) { this.stopOrder = stopOrder; }
    
    public BigDecimal getDistanceFromStartKm() { return distanceFromStartKm; }
    public void setDistanceFromStartKm(BigDecimal distanceFromStartKm) { this.distanceFromStartKm = distanceFromStartKm; }
    
    public int getEstimatedTimeMinutes() { return estimatedTimeMinutes; }
    public void setEstimatedTimeMinutes(int estimatedTimeMinutes) { this.estimatedTimeMinutes = estimatedTimeMinutes; }
    
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Utility methods
    
    public String getFormattedDistance() {
        if (distanceFromStartKm == null) return "N/A";
        return String.format("%.1f km", distanceFromStartKm);
    }
    
    public String getFormattedTime() {
        if (estimatedTimeMinutes <= 0) return "N/A";
        int hours = estimatedTimeMinutes / 60;
        int minutes = estimatedTimeMinutes % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }
    
    public boolean hasGpsCoordinates() {
        return latitude != null && longitude != null;
    }
    
    @Override
    public String toString() {
        return "RouteStop{" +
                "stopId=" + stopId +
                ", stopName='" + stopName + '\'' +
                ", stopOrder=" + stopOrder +
                '}';
    }
}