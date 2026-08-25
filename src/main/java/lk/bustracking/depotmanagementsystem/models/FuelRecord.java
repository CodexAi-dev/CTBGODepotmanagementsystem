// ================================================================
// File: src/main/java/lk/bustracking/model/FuelRecord.java
// ================================================================
package lk.bustracking.depotmanagementsystem.models;

import java.time.LocalDateTime;

/**
 * Model class for fuel records
 */
public class FuelRecord {
    private Long id;
    private String busNumber;
    private String busId;
    private LocalDateTime date;
    private String fuelType;
    private Double quantity; // in litres
    private Double pricePerLiter;
    private Double totalCost;
    private Double odometerReading;
    private String fuelStation;
    private String receiptNumber;
    private String driverName;
    private String notes;
    private String addedBy;
    private LocalDateTime createdAt;
    
    // Constructors
    public FuelRecord() {
        this.date = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }
    
    public FuelRecord(String busNumber, String fuelType, Double quantity, Double pricePerLiter) {
        this();
        this.busNumber = busNumber;
        this.fuelType = fuelType;
        this.quantity = quantity;
        this.pricePerLiter = pricePerLiter;
        this.totalCost = quantity * pricePerLiter;
    }
    
    // Business methods
    public void calculateTotalCost() {
        if (quantity != null && pricePerLiter != null) {
            this.totalCost = quantity * pricePerLiter;
        }
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getBusNumber() { return busNumber; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }
    
    public String getBusId() { return busId; }
    public void setBusId(String busId) { this.busId = busId; }
    
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    
    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }
    
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { 
        this.quantity = quantity;
        calculateTotalCost();
    }
    
    public Double getPricePerLiter() { return pricePerLiter; }
    public void setPricePerLiter(Double pricePerLiter) { 
        this.pricePerLiter = pricePerLiter;
        calculateTotalCost();
    }
    
    public Double getTotalCost() { return totalCost; }
    public void setTotalCost(Double totalCost) { this.totalCost = totalCost; }
    
    public Double getOdometerReading() { return odometerReading; }
    public void setOdometerReading(Double odometerReading) { this.odometerReading = odometerReading; }
    
    public String getFuelStation() { return fuelStation; }
    public void setFuelStation(String fuelStation) { this.fuelStation = fuelStation; }
    
    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
    
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getAddedBy() { return addedBy; }
    public void setAddedBy(String addedBy) { this.addedBy = addedBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}