// ================================================================
// File: src/main/java/lk/bustracking/model/FuelAlert.java
// ================================================================
package lk.bustracking.depotmanagementsystem.models;

import java.time.LocalDateTime;

/**
 * Model class for fuel alerts
 */
public class FuelAlert {
    private Long id;
    private String title;
    private String description;
    private String severity; // HIGH, MEDIUM, LOW
    private LocalDateTime timestamp;
    private String details;
    private String busNumber;
    private boolean isActive;
    private boolean isRead;
    
    public FuelAlert() {
        this.timestamp = LocalDateTime.now();
        this.isActive = true;
        this.isRead = false;
    }
    
    public FuelAlert(String title, String description, String severity, 
                    LocalDateTime timestamp, String details) {
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.timestamp = timestamp;
        this.details = details;
        this.isActive = true;
        this.isRead = false;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public String getBusNumber() { return busNumber; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
