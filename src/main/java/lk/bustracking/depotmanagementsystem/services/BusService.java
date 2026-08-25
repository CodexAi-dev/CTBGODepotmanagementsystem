package lk.bustracking.depotmanagementsystem.services;

import lk.bustracking.depotmanagementsystem.models.Bus;
import lk.bustracking.depotmanagementsystem.models.Route;
import lk.bustracking.depotmanagementsystem.dao.BusDAO;
import lk.bustracking.depotmanagementsystem.services.RouteService;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

/**
 * Bus Service - Fixed version with proper route integration
 */
public class BusService {
    
    private static final Logger LOGGER = Logger.getLogger(BusService.class.getName());
    
    private final BusDAO busDAO;
    private final RouteService routeService; // FIXED: Proper RouteService integration
    
    public BusService() {
        this.busDAO = new BusDAO();
        this.routeService = new RouteService(); // FIXED: Real route service
    }
    
    // =====================================================================================
    // BUS MANAGEMENT OPERATIONS - FIXED METHODS
    // =====================================================================================
    
    /**
     * Get all buses with route information
     */
    public List<Bus> getAllBuses() throws SQLException {
        try {
            return busDAO.getAllBuses();
        } catch (Exception e) {
            LOGGER.severe("Error retrieving all buses: " + e.getMessage());
            throw new SQLException("Failed to retrieve bus fleet data", e);
        }
    }
    
    /**
     * Get active buses only
     */
    public List<Bus> getActiveBuses() throws SQLException {
        try {
            return busDAO.getAllBuses().stream()
                    .filter(bus -> bus.getOperationalStatus() == Bus.OperationalStatus.ACTIVE)
                    .toList();
        } catch (Exception e) {
            LOGGER.severe("Error retrieving active buses: " + e.getMessage());
            throw new SQLException("Failed to retrieve active buses", e);
        }
    }
    
    /**
     * Get bus by ID
     */
    public Bus getBusById(int busId) throws SQLException {
        try {
            if (busId <= 0) {
                throw new IllegalArgumentException("Invalid bus ID: " + busId);
            }
            return busDAO.getBusById(busId);
        } catch (Exception e) {
            LOGGER.severe("Error retrieving bus by ID " + busId + ": " + e.getMessage());
            throw new SQLException("Failed to retrieve bus", e);
        }
    }
    
    /**
     * FIXED: Get available routes for assignment - Now uses real RouteService
     */
    public List<Route> getAvailableRoutes() throws SQLException {
        try {
            List<Route> routes = routeService.getActiveRoutes();
            LOGGER.info("Retrieved " + routes.size() + " available routes for assignment");
            return routes;
        } catch (SQLException e) {
            LOGGER.severe("Error retrieving available routes: " + e.getMessage());
            throw new SQLException("Failed to retrieve available routes", e);
        }
    }
    
    /**
     * Save or update bus
     */
    public boolean saveBus(Bus bus) throws SQLException {
        try {
            // Validate bus data
            validateBusData(bus);
            
            if (bus.getBusId() == 0) {
                // New bus
                boolean result = busDAO.createBus(bus);
                if (result) {
                    LOGGER.info("New bus saved successfully: " + bus.getBusNumber());
                }
                return result;
            } else {
                // Update existing bus
                boolean result = busDAO.updateBus(bus);
                if (result) {
                    LOGGER.info("Bus updated successfully: " + bus.getBusNumber());
                }
                return result;
            }
        } catch (Exception e) {
            LOGGER.severe("Error saving bus: " + e.getMessage());
            throw new SQLException("Failed to save bus", e);
        }
    }
    
    /**
     * FIXED: Assign bus to route with proper validation
     */
    public boolean assignBusToRoute(int busId, int routeId) throws SQLException {
        try {
            // Validate route exists and is active
            Route route = routeService.getRouteById(routeId);
            if (route == null) {
                throw new SQLException("Route not found: " + routeId);
            }
            
            if (!route.isActive()) {
                throw new SQLException("Cannot assign bus to inactive route");
            }
            
            // Validate bus exists and is available
            Bus bus = busDAO.getBusById(busId);
            if (bus == null) {
                throw new SQLException("Bus not found: " + busId);
            }
            
            if (bus.getOperationalStatus() != Bus.OperationalStatus.ACTIVE) {
                throw new SQLException("Bus is not operational and cannot be assigned");
            }
            
            // FIXED: Create assignment using proper method signature
            boolean result = busDAO.assignBusToRoute(
                busId, 
                routeId, 
                0, // driver_id - set to 0 for now, will be updated when driver is assigned
                LocalDate.now(), // start_date
                "DAY", // shift_type - default to DAY
                1, // created_by - use default user for now
                "Bus assigned via system" // notes
            );
            
            if (result) {
                LOGGER.info("Bus " + bus.getBusNumber() + " assigned to route " + route.getRouteNumber());
            }
            return result;
            
        } catch (Exception e) {
            LOGGER.severe("Error assigning bus to route: " + e.getMessage());
            throw new SQLException("Failed to assign bus to route", e);
        }
    }
    
    /**
     * FIXED: Remove bus from route
     */
    public boolean removeBusFromRoute(int busId) throws SQLException {
        try {
            boolean result = busDAO.endBusAssignment(busId, LocalDate.now(), 1);
            if (result) {
                LOGGER.info("Bus removed from route assignment: " + busId);
            }
            return result;
        } catch (Exception e) {
            LOGGER.severe("Error removing bus from route: " + e.getMessage());
            throw new SQLException("Failed to remove bus from route", e);
        }
    }
    
    /**
     * FIXED: Search buses with filters
     */
    public List<Bus> searchBuses(String searchText, Bus.OperationalStatus status, 
                                Integer routeId, Boolean maintenanceAlert) throws SQLException {
        try {
            // Get all buses and filter
            List<Bus> allBuses = busDAO.getAllBuses();
            
            return allBuses.stream()
                    .filter(bus -> {
                        // Search text filter
                        if (searchText != null && !searchText.trim().isEmpty()) {
                            String search = searchText.toLowerCase();
                            boolean matches = bus.getBusNumber().toLowerCase().contains(search) ||
                                            bus.getRegistrationNumber().toLowerCase().contains(search) ||
                                            bus.getMake().toLowerCase().contains(search) ||
                                            bus.getModel().toLowerCase().contains(search);
                            if (!matches) return false;
                        }
                        
                        // Status filter
                        if (status != null && bus.getOperationalStatus() != status) {
                            return false;
                        }
                        
                        // Route filter - FIXED null comparison
                        if (routeId != null) {
                            Integer busRouteId = bus.getCurrentRouteId();
                            if (busRouteId == null || !busRouteId.equals(routeId)) {
                                return false;
                            }
                        }
                        
                        // Maintenance alert filter
                        if (maintenanceAlert != null && maintenanceAlert && !bus.needsAttention()) {
                            return false;
                        }
                        
                        return true;
                    })
                    .toList();
                    
        } catch (Exception e) {
            LOGGER.severe("Error searching buses: " + e.getMessage());
            throw new SQLException("Failed to search buses", e);
        }
    }
    
    /**
     * Get buses assigned to specific route
     */
    public List<Bus> getBusesByRoute(int routeId) throws SQLException {
        try {
            return busDAO.getBusesByRoute(routeId);
        } catch (Exception e) {
            LOGGER.severe("Error getting buses by route: " + e.getMessage());
            throw new SQLException("Failed to get buses by route", e);
        }
    }
    
    /**
     * Get fleet statistics
     */
    public BusDAO.BusStatistics getFleetStats() throws SQLException {
        try {
            return busDAO.getBusStatistics();
        } catch (Exception e) {
            LOGGER.severe("Error getting fleet stats: " + e.getMessage());
            throw new SQLException("Failed to get fleet statistics", e);
        }
    }
    
    /**
     * FIXED: Get route by ID (proxy method to RouteService)
     */
    public Route getRouteById(int routeId) throws SQLException {
        try {
            return routeService.getRouteById(routeId);
        } catch (Exception e) {
            LOGGER.severe("Error getting route by ID: " + e.getMessage());
            throw new SQLException("Failed to get route", e);
        }
    }
    
    // =====================================================================================
    // VALIDATION AND BUSINESS RULES
    // =====================================================================================
    
    /**
     * Validate bus data before saving
     */
    private void validateBusData(Bus bus) throws SQLException {
        if (bus == null) {
            throw new SQLException("Bus data cannot be null");
        }
        
        // Required fields validation
        if (bus.getBusNumber() == null || bus.getBusNumber().trim().isEmpty()) {
            throw new SQLException("Bus number is required");
        }
        
        if (bus.getRegistrationNumber() == null || bus.getRegistrationNumber().trim().isEmpty()) {
            throw new SQLException("Registration number is required");
        }
        
        if (bus.getMake() == null || bus.getMake().trim().isEmpty()) {
            throw new SQLException("Bus make is required");
        }
        
        if (bus.getModel() == null || bus.getModel().trim().isEmpty()) {
            throw new SQLException("Bus model is required");
        }
        
        if (bus.getYearManufactured() < 1990 || bus.getYearManufactured() > java.time.LocalDate.now().getYear() + 1) {
            throw new SQLException("Invalid year of manufacture");
        }
        
        if (bus.getSeatingCapacity() < 10 || bus.getSeatingCapacity() > 100) {
            throw new SQLException("Seating capacity must be between 10 and 100");
        }
        
        // Check for duplicate bus number and registration
        if (bus.getBusId() == 0) { // New bus
            if (!busDAO.isBusNumberAvailable(bus.getBusNumber(), 0)) {
                throw new SQLException("Bus number already exists: " + bus.getBusNumber());
            }
        } else { // Updating existing bus
            if (!busDAO.isBusNumberAvailable(bus.getBusNumber(), bus.getBusId())) {
                throw new SQLException("Bus number already exists: " + bus.getBusNumber());
            }
        }
    }
    
    /**
     * Check if bus number is unique
     */
    public boolean isBusNumberUnique(String busNumber, int excludeBusId) throws SQLException {
        try {
            return busDAO.isBusNumberAvailable(busNumber, excludeBusId);
        } catch (Exception e) {
            LOGGER.severe("Error checking bus number uniqueness: " + e.getMessage());
            throw new SQLException("Failed to check bus number uniqueness", e);
        }
    }
    
    /**
     * Create new bus (alias for saveBus) - FIXED
     */
    public boolean createBus(Bus bus, int createdBy) throws SQLException {
        bus.setCreatedBy(createdBy);
        return saveBus(bus);
    }
    
    /**
     * Update existing bus (alias for saveBus) - FIXED
     */
    public boolean updateBus(Bus bus) throws SQLException {
        return saveBus(bus);
    }
    
    /**
     * Get buses by status - FIXED
     */
    public List<Bus> getBusesByStatus(Bus.OperationalStatus status) throws SQLException {
        try {
            return getAllBuses().stream()
                    .filter(bus -> bus.getOperationalStatus() == status)
                    .toList();
        } catch (Exception e) {
            LOGGER.severe("Error retrieving buses by status: " + e.getMessage());
            throw new SQLException("Failed to retrieve buses by status", e);
        }
    }
    
    /**
     * Get buses needing attention - FIXED
     */
    public List<Bus> getBusesNeedingAttention() throws SQLException {
        try {
            return getAllBuses().stream()
                    .filter(bus -> {
                        // Check if bus needs attention (maintenance, GPS issues, etc.)
                        return bus.getOperationalStatus() == Bus.OperationalStatus.MAINTENANCE ||
                               bus.getConditionRating() == Bus.ConditionRating.POOR ||
                               (bus.getGpsDeviceStatus() != null && 
                                bus.getGpsDeviceStatus() != Bus.GpsStatus.ACTIVE);
                    })
                    .toList();
        } catch (Exception e) {
            LOGGER.severe("Error retrieving buses needing attention: " + e.getMessage());
            throw new SQLException("Failed to retrieve buses needing attention", e);
        }
    }
    
    /**
     * Get all routes (proxy to RouteService) - FIXED
     */
    public List<Route> getAllRoutes() throws SQLException {
        try {
            return routeService.getAllRoutes();
        } catch (SQLException e) {
            LOGGER.severe("Error retrieving all routes: " + e.getMessage());
            throw new SQLException("Failed to retrieve routes", e);
        }
    }
    
    /**
     * End current assignment - FIXED method signature
     */
    public boolean endCurrentAssignment(int busId, LocalDate endDate, int userId) throws SQLException {
        try {
            return busDAO.endBusAssignment(busId, endDate, userId);
        } catch (Exception e) {
            LOGGER.severe("Error ending bus assignment: " + e.getMessage());
            throw new SQLException("Failed to end bus assignment", e);
        }
    }
}