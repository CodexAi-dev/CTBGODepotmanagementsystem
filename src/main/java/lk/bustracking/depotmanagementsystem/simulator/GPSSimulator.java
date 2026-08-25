package lk.bustracking.depotmanagementsystem.simulator;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Updated GPS Simulator - Works with GPS Device IDs (Independent of Bus Assignments)
 * Simulates GPS devices sending coordinates to the database
 */
public class GPSSimulator {
    
    private static final Logger LOGGER = Logger.getLogger(GPSSimulator.class.getName());
    
    // Database connection parameters
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=DepotDB;trustServerCertificate=true";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "6238"; // Update with your password
    
    // Register SQL Server JDBC driver
    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            LOGGER.info("SQL Server JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            LOGGER.severe("Failed to load SQL Server JDBC driver: " + e.getMessage());
        }
    }
    
    // Simulation parameters
    private static final int UPDATE_INTERVAL_SECONDS = 5;
    private static final double AVERAGE_SPEED_KMH = 35.0;
    private static final double SPEED_VARIANCE = 0.3;
    
    // Simulator state
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<String, GPSDeviceSimulation> activeDevices = new HashMap<>();
    private volatile boolean running = true;
    
    /**
     * Get a database connection with autocommit enabled
     * CRITICAL: autocommit must be enabled for database updates to persist
     */
    private static Connection getDBConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        conn.setAutoCommit(true); // CRITICAL FIX: This ensures INSERT/UPDATE queries actually commit
        return conn;
    }
    
    /**
     * Safely close database connection
     */
    private static void closeDBConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                LOGGER.warning("Error closing connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * GPS Device simulation state
     */
    private static class GPSDeviceSimulation {
        String gpsDeviceId;
        List<RoutePoint> simulationPath;
        int currentPointIndex;
        double progressBetweenPoints;
        double currentSpeedKmh;
        LocalDateTime lastUpdate;
        String deviceStatus;
        
        GPSDeviceSimulation(String gpsDeviceId, List<RoutePoint> simulationPath) {
            this.gpsDeviceId = gpsDeviceId;
            this.simulationPath = simulationPath;
            this.currentPointIndex = 0;
            this.progressBetweenPoints = 0.0;
            this.currentSpeedKmh = AVERAGE_SPEED_KMH;
            this.lastUpdate = LocalDateTime.now();
            this.deviceStatus = "ACTIVE";
        }
    }
    
    /**
     * Coordinate point for simulation path
     */
    private static class RoutePoint {
        double latitude;
        double longitude;
        double distanceKm;
        
        RoutePoint(double latitude, double longitude, double distanceKm) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.distanceKm = distanceKm;
        }
    }
    
    public static void main(String[] args) {
        LOGGER.info("Starting GPS Device Simulator for CTB Bus Management System");
        
        GPSSimulator simulator = new GPSSimulator();
        
        // Add shutdown hook for graceful cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(simulator::stop));
        
        try {
            simulator.start();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start GPS Simulator", e);
        }
    }
    
    /**
     * Start the GPS device simulation
     */
    public void start() {
        LOGGER.info("Initializing GPS Device Simulator...");
        
        try {
            // Load active GPS devices
            loadActiveGPSDevices();
            
            // Start the simulation loop
            scheduler.scheduleAtFixedRate(
                this::simulationLoop, 
                0, 
                UPDATE_INTERVAL_SECONDS, 
                TimeUnit.SECONDS
            );
            
            LOGGER.info("GPS Device Simulator started successfully. Updating every " + UPDATE_INTERVAL_SECONDS + " seconds.");
            LOGGER.info("Active GPS devices being simulated: " + activeDevices.size());
            
            // Keep main thread alive
            while (running) {
                Thread.sleep(1000);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in GPS Device Simulator", e);
        }
    }
    
    /**
     * Stop the GPS simulation
     */
    public void stop() {
        LOGGER.info("Stopping GPS Device Simulator...");
        running = false;
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        LOGGER.info("GPS Device Simulator stopped.");
    }
    
    /**
     * Load active GPS devices from database
     */
    private void loadActiveGPSDevices() {
        String query = """
            SELECT DISTINCT b.gps_device_id, b.bus_number, r.route_name, r.start_location, r.end_location
            FROM Buses b
            LEFT JOIN Bus_Assignments ba ON b.bus_id = ba.bus_id 
                AND ba.assignment_status = 'ACTIVE'
                AND (ba.end_date IS NULL OR ba.end_date > GETDATE())
            LEFT JOIN Routes r ON ba.route_id = r.route_id
            WHERE b.gps_device_id IN ('GPS001245', 'GPS001189', 'GPS001156', '1996332365', 'GPS45669')
            AND b.gps_device_status = 'ACTIVE'
            AND b.operational_status = 'ACTIVE'
            """;
        
        Connection conn = null;
        try {
            conn = getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                int loadedCount = 0;
                while (rs.next()) {
                    String gpsDeviceId = rs.getString("gps_device_id");
                    String busNumber = rs.getString("bus_number");
                    String routeName = rs.getString("route_name");
                    String startLocation = rs.getString("start_location");
                    String endLocation = rs.getString("end_location");
                    
                    // Get current position from database as starting point
                    List<RoutePoint> simulationPath = createSimulationPathFromCurrentLocation(gpsDeviceId, startLocation, endLocation);
                    
                    if (!simulationPath.isEmpty()) {
                        GPSDeviceSimulation deviceSim = new GPSDeviceSimulation(gpsDeviceId, simulationPath);
                        activeDevices.put(gpsDeviceId, deviceSim);
                        loadedCount++;
                        
                        String routeInfo = (routeName != null) ? 
                            " (Bus: " + busNumber + ", Route: " + routeName + ")" : 
                            " (Bus: " + busNumber + ", No route assigned)";
                        LOGGER.info("Loaded GPS device: " + gpsDeviceId + routeInfo + " with " + simulationPath.size() + " waypoints");
                    } else {
                        LOGGER.warning("No simulation path created for GPS device: " + gpsDeviceId);
                    }
                }
                LOGGER.info("Successfully loaded " + loadedCount + " active GPS devices");
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading active GPS devices from database", e);
        } finally {
            closeDBConnection(conn);
        }
    }
    
    /**
     * Create simulation path starting from current database location
     */
    private List<RoutePoint> createSimulationPathFromCurrentLocation(String gpsDeviceId, String startLocation, String endLocation) {
        List<RoutePoint> path = new ArrayList<>();
        
        // Get current location from database
        double[] currentLocation = getCurrentLocationFromDatabase(gpsDeviceId);
        
        if (currentLocation != null) {
            if (startLocation != null && endLocation != null) {
                // Try to create route-based path
                path = createRouteBasedPath(startLocation, endLocation);
                
                // If route-based path fails or is empty, create circular path
                if (path.isEmpty()) {
                    LOGGER.info("Route-based path failed for " + gpsDeviceId + ", creating circular path instead");
                    path = createCircularPathAroundLocation(currentLocation);
                }
            } else {
                // No route assigned, create circular path around current location
                LOGGER.info("No route assigned for " + gpsDeviceId + ", creating circular path");
                path = createCircularPathAroundLocation(currentLocation);
            }
        } else {
            // Fallback to default locations if no data in database
            LOGGER.warning("No current location found for " + gpsDeviceId + ", using default path");
            path = createDefaultSimulationPath(gpsDeviceId);
        }
        
        return path;
    }
    
    /**
     * Get current location from database
     */
    private double[] getCurrentLocationFromDatabase(String gpsDeviceId) {
        String query = """
            SELECT TOP 1 latitude, longitude 
            FROM GPS_Tracking 
            WHERE gps_device_id = ? 
            ORDER BY gps_timestamp DESC
            """;
        
        Connection conn = null;
        try {
            conn = getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, gpsDeviceId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    double lat = rs.getDouble("latitude");
                    double lon = rs.getDouble("longitude");
                    LOGGER.info("Found current location for " + gpsDeviceId + ": " + lat + ", " + lon);
                    return new double[]{lat, lon};
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error getting current location for " + gpsDeviceId, e);
        } finally {
            closeDBConnection(conn);
        }
        
        return null;
    }
    
    /**
     * Create circular path around current location (for local area movement)
     */
    private List<RoutePoint> createCircularPathAroundLocation(double[] currentLocation) {
        List<RoutePoint> points = new ArrayList<>();
        
        double centerLat = currentLocation[0];
        double centerLon = currentLocation[1];
        double radius = 0.01; // About 1km radius for local movement
        
        int numPoints = 16;
        for (int i = 0; i < numPoints; i++) {
            double angle = (2 * Math.PI * i) / numPoints;
            double lat = centerLat + radius * Math.cos(angle);
            double lon = centerLon + radius * Math.sin(angle);
            
            double distance = radius * angle;
            points.add(new RoutePoint(lat, lon, distance));
        }
        
        LOGGER.info("Created circular path around current location");
        return points;
    }
    
    /**
     * Create simulation path for GPS device
     */
    private List<RoutePoint> createSimulationPath(String gpsDeviceId, String startLocation, String endLocation) {
        List<RoutePoint> path = new ArrayList<>();
        
        // If GPS device is assigned to a route, use route path
        if (startLocation != null && endLocation != null) {
            path = createRouteBasedPath(startLocation, endLocation);
        } else {
            // Create default simulation path for unassigned GPS devices
            path = createDefaultSimulationPath(gpsDeviceId);
        }
        
        return path;
    }
    
    /**
     * Create route-based simulation path
     */
    private List<RoutePoint> createRouteBasedPath(String startLocation, String endLocation) {
        List<RoutePoint> points = new ArrayList<>();
        
        // Enhanced coordinates for major Sri Lankan locations
        Map<String, double[]> locationCoords = new HashMap<>();
        locationCoords.put("Colombo Fort", new double[]{6.9319, 79.8478});
        locationCoords.put("Colombo Central", new double[]{6.9319, 79.8478});
        locationCoords.put("Colombo", new double[]{6.9319, 79.8478});
        locationCoords.put("Kandy Bus Station", new double[]{7.2906, 80.6337});
        locationCoords.put("Kandy Bus Stand", new double[]{7.2906, 80.6337});
        locationCoords.put("Kandy", new double[]{7.2906, 80.6337});
        locationCoords.put("Negombo Bus Stand", new double[]{7.2083, 79.8358});
        locationCoords.put("Negombo", new double[]{7.2083, 79.8358});
        locationCoords.put("Pettah Bus Station", new double[]{6.9395, 79.8587});
        locationCoords.put("Pettah", new double[]{6.9395, 79.8587});
        locationCoords.put("Galle Bus Station", new double[]{6.0367, 80.2170});
        locationCoords.put("Galle", new double[]{6.0367, 80.2170});
        locationCoords.put("Matara Bus Station", new double[]{5.9549, 80.5550});
        locationCoords.put("Matara", new double[]{5.9549, 80.5550});
        
        // Find start and end coordinates
        double[] startCoords = findLocationCoords(startLocation, locationCoords);
        double[] endCoords = findLocationCoords(endLocation, locationCoords);
        
        if (startCoords != null && endCoords != null) {
            // Create interpolated path
            int numPoints = 15;
            for (int i = 0; i < numPoints; i++) {
                double ratio = (double) i / (numPoints - 1);
                double lat = startCoords[0] + (endCoords[0] - startCoords[0]) * ratio;
                double lon = startCoords[1] + (endCoords[1] - startCoords[1]) * ratio;
                
                // Add realistic variation to path
                if (i > 0 && i < numPoints - 1) {
                    lat += (Math.random() - 0.5) * 0.008;
                    lon += (Math.random() - 0.5) * 0.008;
                }
                
                double distance = calculateDistance(startCoords[0], startCoords[1], lat, lon);
                points.add(new RoutePoint(lat, lon, distance));
            }
            
            LOGGER.info("Created route-based path: " + startLocation + " → " + endLocation + " (" + points.size() + " points)");
        } else {
            LOGGER.warning("Could not find coordinates for: " + startLocation + " → " + endLocation);
        }
        
        return points;
    }
    
    /**
     * Create default simulation path for GPS devices without route assignment
     */
    private List<RoutePoint> createDefaultSimulationPath(String gpsDeviceId) {
        List<RoutePoint> points = new ArrayList<>();
        
        // Create a circular path around Colombo for unassigned devices
        double centerLat = 6.9319; // Colombo Fort
        double centerLon = 79.8478;
        double radius = 0.05; // About 5km radius
        
        int numPoints = 20;
        for (int i = 0; i < numPoints; i++) {
            double angle = (2 * Math.PI * i) / numPoints;
            double lat = centerLat + radius * Math.cos(angle);
            double lon = centerLon + radius * Math.sin(angle);
            
            double distance = radius * angle;
            points.add(new RoutePoint(lat, lon, distance));
        }
        
        LOGGER.info("Created default circular path for GPS device: " + gpsDeviceId);
        return points;
    }
    
    /**
     * Find coordinates for a location name
     */
    private double[] findLocationCoords(String location, Map<String, double[]> locationCoords) {
        // Direct match
        if (locationCoords.containsKey(location)) {
            return locationCoords.get(location);
        }
        
        // Partial match
        for (String key : locationCoords.keySet()) {
            if (location.toLowerCase().contains(key.toLowerCase()) || 
                key.toLowerCase().contains(location.toLowerCase())) {
                return locationCoords.get(key);
            }
        }
        
        return null;
    }
    
    /**
     * Main simulation loop - updates all GPS device positions
     */
    private void simulationLoop() {
        try {
            if (activeDevices.isEmpty()) {
                LOGGER.warning("No active GPS devices loaded for simulation");
                return;
            }
            
            for (GPSDeviceSimulation device : activeDevices.values()) {
                try {
                    updateDevicePosition(device);
                    saveGPSLocation(device);
                    LOGGER.fine("Updated GPS device: " + device.gpsDeviceId);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error updating GPS device " + device.gpsDeviceId, e);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Critical error in simulation loop", e);
        }
    }
    
    /**
     * Update a GPS device position along its simulation path
     */
    private void updateDevicePosition(GPSDeviceSimulation device) {
        if (device.simulationPath.size() < 2) return;
        
        // Calculate time elapsed
        LocalDateTime now = LocalDateTime.now();
        long secondsElapsed = java.time.Duration.between(device.lastUpdate, now).getSeconds();
        if (secondsElapsed == 0) return;
        
        // Vary speed for realism
        device.currentSpeedKmh = AVERAGE_SPEED_KMH * (1.0 + (Math.random() - 0.5) * SPEED_VARIANCE);
        
        // Calculate distance traveled
        double distanceKm = (device.currentSpeedKmh / 3600.0) * secondsElapsed;
        
        // Update progress along path
        device.progressBetweenPoints += distanceKm / getSegmentDistance(device);
        
        // Check if we've reached the next point
        while (device.progressBetweenPoints >= 1.0) {
            device.progressBetweenPoints -= 1.0;
            device.currentPointIndex++;
            
            // If reached end of path, loop back to start
            if (device.currentPointIndex >= device.simulationPath.size()) {
                device.currentPointIndex = 0;
                device.progressBetweenPoints = 0.0;
            }
        }
        
        device.lastUpdate = now;
    }
    
    /**
     * Get distance between current and next points in simulation path
     */
    private double getSegmentDistance(GPSDeviceSimulation device) {
        if (device.currentPointIndex >= device.simulationPath.size() - 1) {
            return 2.0; // Default 2km segment
        }
        
        RoutePoint current = device.simulationPath.get(device.currentPointIndex);
        RoutePoint next = device.simulationPath.get(device.currentPointIndex + 1);
        
        return calculateDistance(current.latitude, current.longitude, next.latitude, next.longitude);
    }
    
    /**
     * Calculate current GPS position for device
     */
    private double[] getCurrentPosition(GPSDeviceSimulation device) {
        if (device.currentPointIndex >= device.simulationPath.size() - 1) {
            RoutePoint last = device.simulationPath.get(device.simulationPath.size() - 1);
            return new double[]{last.latitude, last.longitude};
        }
        
        RoutePoint current = device.simulationPath.get(device.currentPointIndex);
        RoutePoint next = device.simulationPath.get(device.currentPointIndex + 1);
        
        // Interpolate between current and next point
        double lat = current.latitude + (next.latitude - current.latitude) * device.progressBetweenPoints;
        double lon = current.longitude + (next.longitude - current.longitude) * device.progressBetweenPoints;
        
        return new double[]{lat, lon};
    }
    
    /**
     * Save GPS location to database using GPS device ID
     * Ensures only ONE record per GPS device ID exists
     */
    private void saveGPSLocation(GPSDeviceSimulation device) {
        double[] position = getCurrentPosition(device);
        Connection conn = null;
        
        try {
            conn = getDBConnection();
            
            // Delete any existing records for this GPS device first
            String deleteSql = "DELETE FROM GPS_Tracking WHERE gps_device_id = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setString(1, device.gpsDeviceId);
                int deletedRows = deleteStmt.executeUpdate();
                LOGGER.finest("Deleted " + deletedRows + " old records for device: " + device.gpsDeviceId);
            }
            
            // Insert new record (always fresh)
            String insertSql = """
                INSERT INTO GPS_Tracking (gps_device_id, latitude, longitude, speed_kmh, heading_degrees, 
                                        engine_status, fuel_level_percent, gps_timestamp, signal_strength)
                VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE(), ?)
                """;
            
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, device.gpsDeviceId);
                insertStmt.setBigDecimal(2, BigDecimal.valueOf(position[0]).setScale(8, RoundingMode.HALF_UP));
                insertStmt.setBigDecimal(3, BigDecimal.valueOf(position[1]).setScale(8, RoundingMode.HALF_UP));
                insertStmt.setBigDecimal(4, BigDecimal.valueOf(device.currentSpeedKmh).setScale(2, RoundingMode.HALF_UP));
                insertStmt.setInt(5, calculateHeading(device));
                insertStmt.setString(6, "ON");
                insertStmt.setBigDecimal(7, BigDecimal.valueOf(75 + Math.random() * 20));
                insertStmt.setInt(8, 80 + (int)(Math.random() * 20));
                
                int insertedRows = insertStmt.executeUpdate();
                if (insertedRows > 0) {
                    LOGGER.finest("Successfully updated GPS location - Device: " + device.gpsDeviceId + 
                                 ", Position: (" + String.format("%.6f", position[0]) + ", " + 
                                 String.format("%.6f", position[1]) + ")");
                } else {
                    LOGGER.warning("No rows inserted for device: " + device.gpsDeviceId);
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving GPS location for device " + device.gpsDeviceId, e);
        } finally {
            closeDBConnection(conn);
        }
    }
    
    /**
     * Calculate heading direction (0-360 degrees)
     */
    private int calculateHeading(GPSDeviceSimulation device) {
        if (device.currentPointIndex >= device.simulationPath.size() - 1) {
            return 0;
        }
        
        RoutePoint current = device.simulationPath.get(device.currentPointIndex);
        RoutePoint next = device.simulationPath.get(device.currentPointIndex + 1);
        
        double deltaLon = next.longitude - current.longitude;
        double deltaLat = next.latitude - current.latitude;
        
        double heading = Math.toDegrees(Math.atan2(deltaLon, deltaLat));
        if (heading < 0) heading += 360;
        
        return (int) Math.round(heading);
    }
    
    /**
     * Calculate distance between two coordinates in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Earth's radius in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
}