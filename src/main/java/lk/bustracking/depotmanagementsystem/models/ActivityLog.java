package lk.bustracking.depotmanagementsystem.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Activity Log Model - Represents system activities and events
 */
public class ActivityLog {
    
    private String description;
    private LocalDateTime timestamp;
    private ActivityType type;
    private String status; // success, warning, error, info
    private String details;
    private String userId;
    
    public ActivityLog() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ActivityLog(String description, LocalDateTime timestamp, ActivityType type, String status) {
        this.description = description;
        this.timestamp = timestamp;
        this.type = type;
        this.status = status;
    }
    
    public ActivityLog(String description, LocalDateTime timestamp, ActivityType type, String status, String details) {
        this(description, timestamp, type, status);
        this.details = details;
    }
    
    // Getters and Setters
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public ActivityType getType() { return type; }
    public void setType(ActivityType type) { this.type = type; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    /**
     * Get formatted timestamp for display
     */
    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");
        return timestamp.format(formatter);
    }
    
    /**
     * Get relative time string (e.g., "5 minutes ago")
     */
    public String getRelativeTime() {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(timestamp, now).toMinutes();
        
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " minutes ago";
        
        long hours = minutes / 60;
        if (hours < 24) return hours + " hours ago";
        
        long days = hours / 24;
        return days + " days ago";
    }
    
    /**
     * Get color based on status for UI display
     */
    public String getStatusColor() {
        return switch (status.toLowerCase()) {
            case "success" -> "#4CAF50";
            case "warning" -> "#FF9800";
            case "error" -> "#F44336";
            default -> "#2196F3"; // info
        };
    }
    
    @Override
    public String toString() {
        return "ActivityLog{" +
                "description='" + description + '\'' +
                ", timestamp=" + timestamp +
                ", type=" + type +
                ", status='" + status + '\'' +
                '}';
    }
}