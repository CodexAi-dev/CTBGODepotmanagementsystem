// ================================================================
// File: src/main/java/lk/bustracking/model/FuelEfficiencyRecord.java
// ================================================================
package lk.bustracking.depotmanagementsystem.models;

/**
 * Model class for fuel efficiency records
 */
public class FuelEfficiencyRecord {
    private String busNumber;
    private String busId;
    private String routeName;
    private String routeId;
    private Double distanceTraveled; // km
    private Double fuelUsed; // litres
    private Double efficiency; // km per litre
    private String period;
    private String status;
    
    // Constructors
    public FuelEfficiencyRecord() {}
    
    public FuelEfficiencyRecord(String busNumber, String routeName, 
                               Double distanceTraveled, Double fuelUsed) {
        this.busNumber = busNumber;
        this.routeName = routeName;
        this.distanceTraveled = distanceTraveled;
        this.fuelUsed = fuelUsed;
        calculateEfficiency();
        determineStatus();
    }
    
    // Business methods
    public void calculateEfficiency() {
        if (distanceTraveled != null && fuelUsed != null && fuelUsed > 0) {
            this.efficiency = distanceTraveled / fuelUsed;
        }
    }
    
    public void determineStatus() {
        if (efficiency == null) {
            status = "UNKNOWN";
        } else if (efficiency >= 5.0) {
            status = "GOOD";
        } else if (efficiency >= 3.5) {
            status = "AVERAGE";
        } else {
            status = "POOR";
        }
    }
    
    // Getters and Setters
    public String getBusNumber() { return busNumber; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }
    
    public String getBusId() { return busId; }
    public void setBusId(String busId) { this.busId = busId; }
    
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    
    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }
    
    public Double getDistanceTraveled() { return distanceTraveled; }
    public void setDistanceTraveled(Double distanceTraveled) { 
        this.distanceTraveled = distanceTraveled;
        calculateEfficiency();
        determineStatus();
    }
    
    public Double getFuelUsed() { return fuelUsed; }
    public void setFuelUsed(Double fuelUsed) { 
        this.fuelUsed = fuelUsed;
        calculateEfficiency();
        determineStatus();
    }
    
    public Double getEfficiency() { return efficiency; }
    public void setEfficiency(Double efficiency) { 
        this.efficiency = efficiency;
        determineStatus();
    }
    
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}