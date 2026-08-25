package lk.bustracking.depotmanagementsystem.services;

import lk.bustracking.depotmanagementsystem.models.Route;
import lk.bustracking.depotmanagementsystem.db.Database;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Route Service - Complete SQL Server compatible version
 */
public class RouteService {
    
    private static final Logger LOGGER = Logger.getLogger(RouteService.class.getName());
    
    /**
     * Get all routes from database - FIXED SQL Server syntax
     */
    public List<Route> getAllRoutes() throws SQLException {
        List<Route> routes = new ArrayList<>();
        String sql = """
            SELECT route_id, route_number, route_name, start_location, end_location,
                   total_distance_km, estimated_duration_minutes, route_type, fare_price, 
                   is_active, operating_hours_start, operating_hours_end, frequency_minutes,
                   created_at, updated_at
            FROM Routes
            ORDER BY route_number
        """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Route route = mapResultSetToRoute(rs);
                routes.add(route);
            }
        }
        
        LOGGER.info("Retrieved " + routes.size() + " routes from database");
        return routes;
    }
    
    /**
     * Get active routes only (for bus assignment dropdown)
     */
    public List<Route> getActiveRoutes() throws SQLException {
        List<Route> routes = new ArrayList<>();
        String sql = """
            SELECT route_id, route_number, route_name, start_location, end_location,
                   total_distance_km, estimated_duration_minutes, route_type, fare_price, 
                   is_active, operating_hours_start, operating_hours_end, frequency_minutes,
                   created_at, updated_at
            FROM Routes
            WHERE is_active = 1
            ORDER BY route_number
        """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Route route = mapResultSetToRoute(rs);
                routes.add(route);
            }
        }
        
        return routes;
    }
    
    /**
     * Get route by ID
     */
    public Route getRouteById(int routeId) throws SQLException {
        String sql = """
            SELECT route_id, route_number, route_name, start_location, end_location,
                   total_distance_km, estimated_duration_minutes, route_type, fare_price, 
                   is_active, operating_hours_start, operating_hours_end, frequency_minutes,
                   created_at, updated_at
            FROM Routes
            WHERE route_id = ?
        """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, routeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRoute(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Save new route - FIXED SQL Server syntax
     */
    public boolean saveRoute(Route route) throws SQLException {
        String sql = """
            INSERT INTO Routes (route_number, route_name, start_location, end_location,
                              total_distance_km, estimated_duration_minutes, route_type, 
                              fare_price, is_active, operating_hours_start, operating_hours_end, 
                              frequency_minutes, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())
        """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, route.getRouteNumber());
            stmt.setString(2, route.getRouteName());
            stmt.setString(3, route.getStartLocation());
            stmt.setString(4, route.getEndLocation());
            
            if (route.getTotalDistanceKm() != null) {
                stmt.setBigDecimal(5, route.getTotalDistanceKm());
            } else {
                stmt.setNull(5, Types.DECIMAL);
            }
            
            if (route.getEstimatedDurationMinutes() != null) {
                stmt.setInt(6, route.getEstimatedDurationMinutes());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            
            stmt.setString(7, route.getRouteType().toString());
            
            if (route.getFarePrice() != null) {
                stmt.setBigDecimal(8, route.getFarePrice());
            } else {
                stmt.setNull(8, Types.DECIMAL);
            }
            
            stmt.setBoolean(9, route.isActive());
            
            if (route.getOperatingHoursStart() != null) {
                stmt.setTime(10, Time.valueOf(route.getOperatingHoursStart()));
            } else {
                stmt.setNull(10, Types.TIME);
            }
            
            if (route.getOperatingHoursEnd() != null) {
                stmt.setTime(11, Time.valueOf(route.getOperatingHoursEnd()));
            } else {
                stmt.setNull(11, Types.TIME);
            }
            
            if (route.getFrequencyMinutes() != null) {
                stmt.setInt(12, route.getFrequencyMinutes());
            } else {
                stmt.setNull(12, Types.INTEGER);
            }
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        route.setRouteId(rs.getInt(1));
                    }
                }
                LOGGER.info("Route saved successfully: " + route.getRouteNumber());
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Update existing route - FIXED SQL Server syntax
     */
    public boolean updateRoute(Route route) throws SQLException {
        String sql = """
            UPDATE Routes SET 
                route_number = ?, route_name = ?, start_location = ?, end_location = ?,
                total_distance_km = ?, estimated_duration_minutes = ?, route_type = ?, 
                fare_price = ?, is_active = ?, operating_hours_start = ?, 
                operating_hours_end = ?, frequency_minutes = ?, updated_at = GETDATE()
            WHERE route_id = ?
        """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, route.getRouteNumber());
            stmt.setString(2, route.getRouteName());
            stmt.setString(3, route.getStartLocation());
            stmt.setString(4, route.getEndLocation());
            
            if (route.getTotalDistanceKm() != null) {
                stmt.setBigDecimal(5, route.getTotalDistanceKm());
            } else {
                stmt.setNull(5, Types.DECIMAL);
            }
            
            if (route.getEstimatedDurationMinutes() != null) {
                stmt.setInt(6, route.getEstimatedDurationMinutes());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            
            stmt.setString(7, route.getRouteType().toString());
            
            if (route.getFarePrice() != null) {
                stmt.setBigDecimal(8, route.getFarePrice());
            } else {
                stmt.setNull(8, Types.DECIMAL);
            }
            
            stmt.setBoolean(9, route.isActive());
            
            if (route.getOperatingHoursStart() != null) {
                stmt.setTime(10, Time.valueOf(route.getOperatingHoursStart()));
            } else {
                stmt.setNull(10, Types.TIME);
            }
            
            if (route.getOperatingHoursEnd() != null) {
                stmt.setTime(11, Time.valueOf(route.getOperatingHoursEnd()));
            } else {
                stmt.setNull(11, Types.TIME);
            }
            
            if (route.getFrequencyMinutes() != null) {
                stmt.setInt(12, route.getFrequencyMinutes());
            } else {
                stmt.setNull(12, Types.INTEGER);
            }
            
            stmt.setInt(13, route.getRouteId());
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                LOGGER.info("Route updated successfully: " + route.getRouteNumber());
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Delete route
     */
    public boolean deleteRoute(int routeId) throws SQLException {
        String sql = "DELETE FROM Routes WHERE route_id = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, routeId);
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                LOGGER.info("Route deleted successfully: " + routeId);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Search routes by criteria
     */
    public List<Route> searchRoutes(String searchText, Route.RouteType routeType, Boolean activeOnly) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT route_id, route_number, route_name, start_location, end_location,
                   total_distance_km, estimated_duration_minutes, route_type, fare_price, 
                   is_active, operating_hours_start, operating_hours_end, frequency_minutes,
                   created_at, updated_at
            FROM Routes
            WHERE 1=1
        """);
        
        List<Object> parameters = new ArrayList<>();
        
        if (searchText != null && !searchText.trim().isEmpty()) {
            sql.append(" AND (route_number LIKE ? OR route_name LIKE ? OR start_location LIKE ? OR end_location LIKE ?)");
            String searchPattern = "%" + searchText.trim() + "%";
            parameters.add(searchPattern);
            parameters.add(searchPattern);
            parameters.add(searchPattern);
            parameters.add(searchPattern);
        }
        
        if (routeType != null) {
            sql.append(" AND route_type = ?");
            parameters.add(routeType.toString());
        }
        
        if (activeOnly != null && activeOnly) {
            sql.append(" AND is_active = 1");
        }
        
        sql.append(" ORDER BY route_number");
        
        List<Route> routes = new ArrayList<>();
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    routes.add(mapResultSetToRoute(rs));
                }
            }
        }
        
        return routes;
    }
    
    /**
     * Get routes by type
     */
    public List<Route> getRoutesByType(Route.RouteType routeType) throws SQLException {
        String sql = """
            SELECT route_id, route_number, route_name, start_location, end_location,
                   total_distance_km, estimated_duration_minutes, route_type, fare_price, 
                   is_active, operating_hours_start, operating_hours_end, frequency_minutes,
                   created_at, updated_at
            FROM Routes
            WHERE route_type = ?
            ORDER BY route_number
        """;
        
        List<Route> routes = new ArrayList<>();
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, routeType.toString());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    routes.add(mapResultSetToRoute(rs));
                }
            }
        }
        
        return routes;
    }
    
    /**
     * Create new route (alias for saveRoute)
     */
    public boolean createRoute(Route route, int createdBy) throws SQLException {
        // For now, ignore createdBy parameter as it's not in the current table structure
        return saveRoute(route);
    }
    
    /**
     * Check if route number exists
     */
    public boolean isRouteNumberExists(String routeNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Routes WHERE route_number = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, routeNumber);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Check if route number exists for other routes (for updates)
     */
    public boolean isRouteNumberExistsForOtherRoute(String routeNumber, int excludeRouteId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Routes WHERE route_number = ? AND route_id != ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, routeNumber);
            stmt.setInt(2, excludeRouteId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Map ResultSet to Route object - FIXED for SQL Server
     */
    private Route mapResultSetToRoute(ResultSet rs) throws SQLException {
        Route route = new Route();
        
        route.setRouteId(rs.getInt("route_id"));
        route.setRouteNumber(rs.getString("route_number"));
        route.setRouteName(rs.getString("route_name"));
        route.setStartLocation(rs.getString("start_location"));
        route.setEndLocation(rs.getString("end_location"));
        route.setTotalDistanceKm(rs.getBigDecimal("total_distance_km"));
        
        // Handle null values properly
        int duration = rs.getInt("estimated_duration_minutes");
        if (!rs.wasNull()) {
            route.setEstimatedDurationMinutes(duration);
        }
        
        String routeTypeStr = rs.getString("route_type");
        if (routeTypeStr != null) {
            try {
                route.setRouteType(Route.RouteType.valueOf(routeTypeStr));
            } catch (IllegalArgumentException e) {
                route.setRouteType(Route.RouteType.REGULAR); // Default fallback
            }
        }
        
        route.setFarePrice(rs.getBigDecimal("fare_price"));
        route.setActive(rs.getBoolean("is_active"));
        
        Time startTime = rs.getTime("operating_hours_start");
        if (startTime != null) {
            route.setOperatingHoursStart(startTime.toLocalTime());
        }
        
        Time endTime = rs.getTime("operating_hours_end");
        if (endTime != null) {
            route.setOperatingHoursEnd(endTime.toLocalTime());
        }
        
        int frequency = rs.getInt("frequency_minutes");
        if (!rs.wasNull()) {
            route.setFrequencyMinutes(frequency);
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            route.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            route.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return route;
    }
    
    /**
     * Get route statistics for dashboard
     */
    public RouteStats getRouteStats() throws SQLException {
        String sql = """
            SELECT 
                COUNT(*) as total_routes,
                SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) as active_routes,
                SUM(CASE WHEN route_type = 'EXPRESS' THEN 1 ELSE 0 END) as express_routes,
                SUM(CASE WHEN route_type = 'NIGHT_SERVICE' THEN 1 ELSE 0 END) as night_routes,
                AVG(total_distance_km) as avg_distance,
                AVG(fare_price) as avg_fare
            FROM Routes
        """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return new RouteStats(
                    rs.getInt("total_routes"),
                    rs.getInt("active_routes"),
                    rs.getInt("express_routes"),
                    rs.getInt("night_routes"),
                    rs.getBigDecimal("avg_distance"),
                    rs.getBigDecimal("avg_fare")
                );
            }
        }
        
        return new RouteStats(0, 0, 0, 0, null, null);
    }
    
    /**
     * Route statistics data class
     */
    public static class RouteStats {
        private final int totalRoutes;
        private final int activeRoutes;
        private final int expressRoutes;
        private final int nightRoutes;
        private final java.math.BigDecimal avgDistance;
        private final java.math.BigDecimal avgFare;
        
        public RouteStats(int totalRoutes, int activeRoutes, int expressRoutes, 
                         int nightRoutes, java.math.BigDecimal avgDistance, java.math.BigDecimal avgFare) {
            this.totalRoutes = totalRoutes;
            this.activeRoutes = activeRoutes;
            this.expressRoutes = expressRoutes;
            this.nightRoutes = nightRoutes;
            this.avgDistance = avgDistance;
            this.avgFare = avgFare;
        }
        
        // Getters
        public int getTotalRoutes() { return totalRoutes; }
        public int getActiveRoutes() { return activeRoutes; }
        public int getExpressRoutes() { return expressRoutes; }
        public int getNightRoutes() { return nightRoutes; }
        public java.math.BigDecimal getAvgDistance() { return avgDistance; }
        public java.math.BigDecimal getAvgFare() { return avgFare; }
    }
}