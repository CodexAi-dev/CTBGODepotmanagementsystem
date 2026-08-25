package lk.bustracking.depotmanagementsystem.dao;

import lk.bustracking.depotmanagementsystem.db.Database;
import lk.bustracking.depotmanagementsystem.models.Bus;
import lk.bustracking.depotmanagementsystem.models.Route;
import lk.bustracking.depotmanagementsystem.models.RouteStop;
import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Bus Data Access Object - Handles all database operations for buses and routes
 */
public class BusDAO {
    
    private static final Logger LOGGER = Logger.getLogger(BusDAO.class.getName());
    
    // =====================================================================================
    // BUS CRUD OPERATIONS
    // =====================================================================================
    
    /**
     * Get all buses with current status and route information
     */
    public List<Bus> getAllBuses() {
        List<Bus> buses = new ArrayList<>();
        String sql = """
            SELECT b.*, r.route_number, r.route_name,
                   gt.latitude, gt.longitude, gt.speed_kmh, gt.fuel_level_percent, gt.gps_timestamp,
                   CASE 
                       WHEN b.next_service_due_date < GETDATE() THEN 'OVERDUE'
                       WHEN b.next_service_due_date <= DATEADD(DAY, 7, GETDATE()) THEN 'DUE_SOON'
                       ELSE 'OK'
                   END AS maintenance_status
            FROM Buses b
            LEFT JOIN Routes r ON b.current_route_id = r.route_id
            LEFT JOIN (
                SELECT bus_id, latitude, longitude, speed_kmh, fuel_level_percent, gps_timestamp,
                       ROW_NUMBER() OVER (PARTITION BY bus_id ORDER BY gps_timestamp DESC) as rn
                FROM GPS_Tracking
                WHERE gps_timestamp >= DATEADD(HOUR, -24, GETDATE())
            ) gt ON b.bus_id = gt.bus_id AND gt.rn = 1
            ORDER BY b.bus_number
            """;
        
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Bus bus = mapResultSetToBus(rs);
                buses.add(bus);
            }
            
            LOGGER.info("Retrieved " + buses.size() + " buses from database");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all buses", e);
            throw new RuntimeException("Failed to retrieve buses", e);
        }
        
        return buses;
    }
    
    /**
     * Get bus by ID with full details
     */
    public Bus getBusById(int busId) {
        String sql = """
            SELECT b.*, r.route_number, r.route_name,
                   gt.latitude, gt.longitude, gt.speed_kmh, gt.fuel_level_percent, gt.gps_timestamp,
                   CASE 
                       WHEN b.next_service_due_date < GETDATE() THEN 'OVERDUE'
                       WHEN b.next_service_due_date <= DATEADD(DAY, 7, GETDATE()) THEN 'DUE_SOON'
                       ELSE 'OK'
                   END AS maintenance_status
            FROM Buses b
            LEFT JOIN Routes r ON b.current_route_id = r.route_id
            LEFT JOIN (
                SELECT bus_id, latitude, longitude, speed_kmh, fuel_level_percent, gps_timestamp,
                       ROW_NUMBER() OVER (PARTITION BY bus_id ORDER BY gps_timestamp DESC) as rn
                FROM GPS_Tracking
                WHERE gps_timestamp >= DATEADD(HOUR, -24, GETDATE())
            ) gt ON b.bus_id = gt.bus_id AND gt.rn = 1
            WHERE b.bus_id = ?
            """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, busId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToBus(rs);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving bus by ID: " + busId, e);
        }
        
        return null;
    }
    
    /**
     * Create a new bus
     */
    public boolean createBus(Bus bus) {
        String sql = """
            INSERT INTO Buses (bus_number, registration_number, make, model, year_manufactured, 
                              seating_capacity, fuel_tank_capacity, operational_status, condition_rating,
                              gps_device_id, gps_device_status, purchase_date, purchase_cost,
                              insurance_expiry, fitness_certificate_expiry, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, bus.getBusNumber());
            stmt.setString(2, bus.getRegistrationNumber());
            stmt.setString(3, bus.getMake());
            stmt.setString(4, bus.getModel());
            stmt.setInt(5, bus.getYearManufactured());
            stmt.setInt(6, bus.getSeatingCapacity());
            stmt.setBigDecimal(7, bus.getFuelTankCapacity());
            stmt.setString(8, bus.getOperationalStatus().name());
            stmt.setString(9, bus.getConditionRating().name());
            stmt.setString(10, bus.getGpsDeviceId());
            stmt.setString(11, bus.getGpsDeviceStatus().name());
            stmt.setDate(12, bus.getPurchaseDate() != null ? Date.valueOf(bus.getPurchaseDate()) : null);
            stmt.setBigDecimal(13, bus.getPurchaseCost());
            stmt.setDate(14, bus.getInsuranceExpiry() != null ? Date.valueOf(bus.getInsuranceExpiry()) : null);
            stmt.setDate(15, bus.getFitnessCertificateExpiry() != null ? Date.valueOf(bus.getFitnessCertificateExpiry()) : null);
            stmt.setInt(16, bus.getCreatedBy());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    bus.setBusId(generatedKeys.getInt(1));
                }
                LOGGER.info("Created new bus: " + bus.getBusNumber());
                return true;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating bus: " + bus.getBusNumber(), e);
        }
        
        return false;
    }
    
    /**
     * Update an existing bus
     */
    public boolean updateBus(Bus bus) {
        String sql = """
            UPDATE Buses 
            SET bus_number = ?, registration_number = ?, make = ?, model = ?, year_manufactured = ?,
                seating_capacity = ?, fuel_tank_capacity = ?, operational_status = ?, condition_rating = ?,
                gps_device_id = ?, gps_device_status = ?, purchase_date = ?, purchase_cost = ?,
                insurance_expiry = ?, fitness_certificate_expiry = ?, updated_at = GETDATE()
            WHERE bus_id = ?
            """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, bus.getBusNumber());
            stmt.setString(2, bus.getRegistrationNumber());
            stmt.setString(3, bus.getMake());
            stmt.setString(4, bus.getModel());
            stmt.setInt(5, bus.getYearManufactured());
            stmt.setInt(6, bus.getSeatingCapacity());
            stmt.setBigDecimal(7, bus.getFuelTankCapacity());
            stmt.setString(8, bus.getOperationalStatus().name());
            stmt.setString(9, bus.getConditionRating().name());
            stmt.setString(10, bus.getGpsDeviceId());
            stmt.setString(11, bus.getGpsDeviceStatus().name());
            stmt.setDate(12, bus.getPurchaseDate() != null ? Date.valueOf(bus.getPurchaseDate()) : null);
            stmt.setBigDecimal(13, bus.getPurchaseCost());
            stmt.setDate(14, bus.getInsuranceExpiry() != null ? Date.valueOf(bus.getInsuranceExpiry()) : null);
            stmt.setDate(15, bus.getFitnessCertificateExpiry() != null ? Date.valueOf(bus.getFitnessCertificateExpiry()) : null);
            stmt.setInt(16, bus.getBusId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                LOGGER.info("Updated bus: " + bus.getBusNumber());
                return true;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating bus: " + bus.getBusNumber(), e);
        }
        
        return false;
    }
    
    /**
     * Assign bus to route
     */
    public boolean assignBusToRoute(int busId, int routeId, int driverId, LocalDate startDate, 
                               String shiftType, int createdBy, String notes) {
    Connection conn = null;
    try {
        conn = Database.getConnection();
        conn.setAutoCommit(false); // Start transaction
        
        // Step 1: End any existing ACTIVE assignments for this bus
        String endAssignmentSql = """
            UPDATE Bus_Assignments 
            SET assignment_status = 'COMPLETED', 
                end_date = GETDATE()
            WHERE bus_id = ? 
            AND assignment_status = 'ACTIVE' 
            AND (end_date IS NULL OR end_date > GETDATE())
            """;
        
        try (PreparedStatement endStmt = conn.prepareStatement(endAssignmentSql)) {
            endStmt.setInt(1, busId);
            int endedAssignments = endStmt.executeUpdate();
            
            if (endedAssignments > 0) {
                LOGGER.info("Ended " + endedAssignments + " existing assignments for bus: " + busId);
            }
        }
        
        // Step 2: Create new assignment
        String createAssignmentSql = """
            INSERT INTO Bus_Assignments (bus_id, route_id, driver_id, start_date, shift_type, 
                                       assignment_status, created_by, notes)
            VALUES (?, ?, ?, ?, ?, 'ACTIVE', ?, ?)
            """;
        
        try (PreparedStatement createStmt = conn.prepareStatement(createAssignmentSql)) {
            createStmt.setInt(1, busId);
            createStmt.setInt(2, routeId);
            createStmt.setInt(3, driverId);
            createStmt.setDate(4, Date.valueOf(startDate));
            createStmt.setString(5, shiftType);
            createStmt.setInt(6, createdBy);
            createStmt.setString(7, notes);
            
            int affectedRows = createStmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Step 3: Update current_route_id in Buses table
                String updateBusSql = "UPDATE Buses SET current_route_id = ?, updated_at = GETDATE() WHERE bus_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateBusSql)) {
                    updateStmt.setInt(1, routeId);
                    updateStmt.setInt(2, busId);
                    updateStmt.executeUpdate();
                }
                
                conn.commit(); // Commit transaction
                LOGGER.info(String.format("Successfully assigned bus %d to route %d", busId, routeId));
                return true;
            }
        }
        
        conn.rollback(); // Rollback if creation failed
        return false;
        
    } catch (SQLException e) {
        try {
            if (conn != null) {
                conn.rollback();
            }
        } catch (SQLException rollbackEx) {
            LOGGER.log(Level.SEVERE, "Error rolling back transaction", rollbackEx);
        }
        LOGGER.log(Level.SEVERE, "Error assigning bus to route", e);
        return false;
    } finally {
        try {
            if (conn != null) {
                conn.setAutoCommit(true); // Reset auto-commit
                conn.close();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error closing connection", e);
        }
    }
}
    
    /**
     * End current bus assignment
     */
    public boolean endBusAssignment(int busId, LocalDate endDate, int userId) {
        String sql = """
            UPDATE Bus_Assignments 
            SET end_date = ?, assignment_status = 'COMPLETED'
            WHERE bus_id = ? AND assignment_status = 'ACTIVE' AND (end_date IS NULL OR end_date > GETDATE())
            """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(endDate));
            stmt.setInt(2, busId);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                LOGGER.info("Ended assignment for bus: " + busId);
                return true;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error ending bus assignment", e);
        }
        
        return false;
    }
    
    // =====================================================================================
    // ROUTE OPERATIONS
    // =====================================================================================
    
    /**
     * Get all active routes with performance metrics
     */
    public List<Route> getAllRoutes() {
        List<Route> routes = new ArrayList<>();
        String sql = """
            SELECT r.*, 
                   COUNT(DISTINCT ba.bus_id) as buses_assigned,
                   COUNT(tl.trip_id) as total_trips,
                   AVG(CAST(tl.delay_minutes AS DECIMAL(10,2))) as avg_delay_minutes,
                   SUM(tl.passenger_count) as total_passengers,
                   SUM(tl.revenue_collected) as total_revenue,
                   AVG(tl.fuel_consumed_liters) as avg_fuel_per_trip,
                   CAST(COUNT(CASE WHEN tl.delay_minutes <= 5 THEN 1 END) AS DECIMAL(10,2)) * 100 / 
                   NULLIF(COUNT(tl.trip_id), 0) as on_time_percentage
            FROM Routes r
            LEFT JOIN Bus_Assignments ba ON r.route_id = ba.route_id AND ba.assignment_status = 'ACTIVE'
            LEFT JOIN Trip_Logs tl ON r.route_id = tl.route_id 
                AND tl.trip_start_time >= DATEADD(DAY, -30, GETDATE())
                AND tl.trip_status = 'COMPLETED'
            WHERE r.is_active = 1
            GROUP BY r.route_id, r.route_number, r.route_name, r.start_location, r.end_location,
                     r.total_distance_km, r.estimated_duration_minutes, r.route_type, r.fare_price,
                     r.is_active, r.operating_hours_start, r.operating_hours_end, r.frequency_minutes,
                     r.created_at, r.updated_at
            ORDER BY r.route_number
            """;
        
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Route route = mapResultSetToRoute(rs);
                routes.add(route);
            }
            
            LOGGER.info("Retrieved " + routes.size() + " routes from database");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all routes", e);
        }
        
        return routes;
    }
    
    /**
     * Get route by ID with stops
     */
    public Route getRouteById(int routeId) {
        String sql = """
            SELECT r.*, 
                   COUNT(DISTINCT ba.bus_id) as buses_assigned
            FROM Routes r
            LEFT JOIN Bus_Assignments ba ON r.route_id = ba.route_id AND ba.assignment_status = 'ACTIVE'
            WHERE r.route_id = ?
            GROUP BY r.route_id, r.route_number, r.route_name, r.start_location, r.end_location,
                     r.total_distance_km, r.estimated_duration_minutes, r.route_type, r.fare_price,
                     r.is_active, r.operating_hours_start, r.operating_hours_end, r.frequency_minutes,
                     r.created_at, r.updated_at
            """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, routeId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Route route = mapResultSetToRoute(rs);
                // Load route stops
                route.setStops(getRouteStops(routeId));
                return route;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving route by ID: " + routeId, e);
        }
        
        return null;
    }
    
    /**
     * Get route stops for a specific route
     */
    public List<RouteStop> getRouteStops(int routeId) {
        List<RouteStop> stops = new ArrayList<>();
        String sql = """
            SELECT * FROM Route_Stops 
            WHERE route_id = ? 
            ORDER BY stop_order
            """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, routeId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                RouteStop stop = mapResultSetToRouteStop(rs);
                stops.add(stop);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving route stops for route: " + routeId, e);
        }
        
        return stops;
    }
    
    /**
     * Get buses assigned to a specific route
     */
    public List<Bus> getBusesByRoute(int routeId) {
        List<Bus> buses = new ArrayList<>();
        String sql = """
            SELECT b.*, r.route_number, r.route_name
            FROM Buses b
            INNER JOIN Bus_Assignments ba ON b.bus_id = ba.bus_id
            INNER JOIN Routes r ON ba.route_id = r.route_id
            WHERE ba.route_id = ? AND ba.assignment_status = 'ACTIVE'
            AND (ba.end_date IS NULL OR ba.end_date > GETDATE())
            ORDER BY b.bus_number
            """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, routeId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Bus bus = mapResultSetToBus(rs);
                buses.add(bus);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving buses for route: " + routeId, e);
        }
        
        return buses;
    }
    
    // =====================================================================================
    // DASHBOARD STATISTICS
    // =====================================================================================
    
    /**
     * Get bus statistics for dashboard
     */
    public BusStatistics getBusStatistics() {
        String sql = """
            SELECT 
                COUNT(*) as total_buses,
                SUM(CASE WHEN operational_status = 'ACTIVE' THEN 1 ELSE 0 END) as active_buses,
                SUM(CASE WHEN operational_status = 'MAINTENANCE' THEN 1 ELSE 0 END) as maintenance_buses,
                SUM(CASE WHEN operational_status IN ('OUT_OF_SERVICE', 'RETIRED') THEN 1 ELSE 0 END) as offline_buses,
                SUM(CASE WHEN gps_device_status = 'ACTIVE' AND last_gps_update >= DATEADD(MINUTE, -30, GETDATE()) THEN 1 ELSE 0 END) as online_buses
            FROM Buses
            WHERE operational_status != 'RETIRED'
            """;
        
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return new BusStatistics(
                    rs.getInt("total_buses"),
                    rs.getInt("active_buses"),
                    rs.getInt("maintenance_buses"),
                    rs.getInt("offline_buses"),
                    rs.getInt("online_buses")
                );
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving bus statistics", e);
        }
        
        return new BusStatistics(0, 0, 0, 0, 0);
    }
    
    // =====================================================================================
    // UTILITY METHODS
    // =====================================================================================
    
    /**
     * Map ResultSet to Bus object
     */
    private Bus mapResultSetToBus(ResultSet rs) throws SQLException {
        Bus bus = new Bus();
        
        bus.setBusId(rs.getInt("bus_id"));
        bus.setBusNumber(rs.getString("bus_number"));
        bus.setRegistrationNumber(rs.getString("registration_number"));
        bus.setMake(rs.getString("make"));
        bus.setModel(rs.getString("model"));
        bus.setYearManufactured(rs.getInt("year_manufactured"));
        bus.setSeatingCapacity(rs.getInt("seating_capacity"));
        bus.setFuelTankCapacity(rs.getBigDecimal("fuel_tank_capacity"));
        
        // Handle enum fields safely
        String operationalStatus = rs.getString("operational_status");
        if (operationalStatus != null) {
            try {
                bus.setOperationalStatus(Bus.OperationalStatus.valueOf(operationalStatus));
            } catch (IllegalArgumentException e) {
                bus.setOperationalStatus(Bus.OperationalStatus.ACTIVE);
            }
        }
        
        String conditionRating = rs.getString("condition_rating");
        if (conditionRating != null) {
            try {
                bus.setConditionRating(Bus.ConditionRating.valueOf(conditionRating));
            } catch (IllegalArgumentException e) {
                bus.setConditionRating(Bus.ConditionRating.GOOD);
            }
        }
        
        String gpsStatus = rs.getString("gps_device_status");
        if (gpsStatus != null) {
            try {
                bus.setGpsDeviceStatus(Bus.GpsStatus.valueOf(gpsStatus));
            } catch (IllegalArgumentException e) {
                bus.setGpsDeviceStatus(Bus.GpsStatus.ACTIVE);
            }
        }
        
        bus.setGpsDeviceId(rs.getString("gps_device_id"));
        
        // Handle dates
        Timestamp lastGpsUpdate = rs.getTimestamp("last_gps_update");
        if (lastGpsUpdate != null) {
            bus.setLastGpsUpdate(lastGpsUpdate.toLocalDateTime());
        }
        
        Date lastServiceDate = rs.getDate("last_service_date");
        if (lastServiceDate != null) {
            bus.setLastServiceDate(lastServiceDate.toLocalDate());
        }
        
        Date nextServiceDue = rs.getDate("next_service_due_date");
        if (nextServiceDue != null) {
            bus.setNextServiceDueDate(nextServiceDue.toLocalDate());
        }
        
        Date purchaseDate = rs.getDate("purchase_date");
        if (purchaseDate != null) {
            bus.setPurchaseDate(purchaseDate.toLocalDate());
        }
        
        bus.setPurchaseCost(rs.getBigDecimal("purchase_cost"));
        bus.setTotalMileageKm(rs.getBigDecimal("total_mileage_km"));
        
        // Current route information
        bus.setCurrentRouteId(rs.getInt("current_route_id"));
        bus.setCurrentRouteNumber(rs.getString("route_number"));
        bus.setCurrentRouteName(rs.getString("route_name"));
        
        // GPS data
        bus.setCurrentLatitude(rs.getBigDecimal("latitude"));
        bus.setCurrentLongitude(rs.getBigDecimal("longitude"));
        bus.setCurrentSpeed(rs.getBigDecimal("speed_kmh"));
        bus.setCurrentFuelLevel(rs.getBigDecimal("fuel_level_percent"));
        
        Timestamp gpsTimestamp = rs.getTimestamp("gps_timestamp");
        if (gpsTimestamp != null) {
            bus.setLastLocationUpdate(gpsTimestamp.toLocalDateTime());
        }
        
        // Maintenance status
        bus.setMaintenanceStatus(rs.getString("maintenance_status"));
        
        // Set online status based on GPS updates
        bus.setOnline(bus.isGpsActive());
        
        return bus;
    }
    
    /**
     * Map ResultSet to Route object
     */
    private Route mapResultSetToRoute(ResultSet rs) throws SQLException {
        Route route = new Route();
        
        route.setRouteId(rs.getInt("route_id"));
        route.setRouteNumber(rs.getString("route_number"));
        route.setRouteName(rs.getString("route_name"));
        route.setStartLocation(rs.getString("start_location"));
        route.setEndLocation(rs.getString("end_location"));
        route.setTotalDistanceKm(rs.getBigDecimal("total_distance_km"));
        route.setEstimatedDurationMinutes(rs.getInt("estimated_duration_minutes"));
        route.setFarePrice(rs.getBigDecimal("fare_price"));
        route.setActive(rs.getBoolean("is_active"));
        route.setFrequencyMinutes(rs.getInt("frequency_minutes"));
        
        // Handle route type
        String routeType = rs.getString("route_type");
        if (routeType != null) {
            try {
                route.setRouteType(Route.RouteType.valueOf(routeType));
            } catch (IllegalArgumentException e) {
                route.setRouteType(Route.RouteType.REGULAR);
            }
        }
        
        // Handle times
        Time startTime = rs.getTime("operating_hours_start");
        if (startTime != null) {
            route.setOperatingHoursStart(startTime.toLocalTime());
        }
        
        Time endTime = rs.getTime("operating_hours_end");
        if (endTime != null) {
            route.setOperatingHoursEnd(endTime.toLocalTime());
        }
        
        // Performance metrics
        route.setBusesAssigned(rs.getInt("buses_assigned"));
        route.setTotalTrips(rs.getInt("total_trips"));
        route.setAvgDelayMinutes(rs.getBigDecimal("avg_delay_minutes"));
        route.setTotalPassengers(rs.getInt("total_passengers"));
        route.setTotalRevenue(rs.getBigDecimal("total_revenue"));
        route.setAvgFuelPerTrip(rs.getBigDecimal("avg_fuel_per_trip"));
        route.setOnTimePercentage(rs.getBigDecimal("on_time_percentage"));
        
        return route;
    }
    
    /**
     * Map ResultSet to RouteStop object
     */
    private RouteStop mapResultSetToRouteStop(ResultSet rs) throws SQLException {
        RouteStop stop = new RouteStop();
        
        stop.setStopId(rs.getInt("stop_id"));
        stop.setRouteId(rs.getInt("route_id"));
        stop.setStopName(rs.getString("stop_name"));
        stop.setStopOrder(rs.getInt("stop_order"));
        stop.setDistanceFromStartKm(rs.getBigDecimal("distance_from_start_km"));
        stop.setEstimatedTimeMinutes(rs.getInt("estimated_time_minutes"));
        stop.setLatitude(rs.getBigDecimal("latitude"));
        stop.setLongitude(rs.getBigDecimal("longitude"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            stop.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return stop;
    }
    
    /**
     * Check if bus number is available
     */
    public boolean isBusNumberAvailable(String busNumber, int excludeBusId) {
        String sql = "SELECT COUNT(*) FROM Buses WHERE bus_number = ? AND bus_id != ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, busNumber);
            stmt.setInt(2, excludeBusId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking bus number availability", e);
        }
        
        return false;
    }
    
    /**
     * Inner class for bus statistics
     */
    public static class BusStatistics {
        public final int totalBuses;
        public final int activeBuses;
        public final int maintenanceBuses;
        public final int offlineBuses;
        public final int onlineBuses;
        
        public BusStatistics(int totalBuses, int activeBuses, int maintenanceBuses, 
                           int offlineBuses, int onlineBuses) {
            this.totalBuses = totalBuses;
            this.activeBuses = activeBuses;
            this.maintenanceBuses = maintenanceBuses;
            this.offlineBuses = offlineBuses;
            this.onlineBuses = onlineBuses;
        }
    }
}