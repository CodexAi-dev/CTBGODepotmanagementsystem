// ================================================================
// File: src/main/java/lk/bustracking/service/FuelManagementService.java
// ================================================================
package lk.bustracking.depotmanagementsystem.services;


import lk.bustracking.depotmanagementsystem.db.Database;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import lk.bustracking.depotmanagementsystem.models.FuelAlert;
import lk.bustracking.depotmanagementsystem.models.FuelEfficiencyRecord;
import lk.bustracking.depotmanagementsystem.models.FuelRecord;

/**
 * Service class for fuel management operations
 */
public class FuelManagementService {
    
    /**
     * Get fuel records for date range - UPDATED WITH CORRECT COLUMN NAMES
     */
    public List<FuelRecord> getFuelRecords(LocalDate startDate, LocalDate endDate) {
        List<FuelRecord> records = new ArrayList<>();
        
        String sql = """
            SELECT 
                fp.PurchaseID, fp.BusID, b.bus_number, fp.FuelTypeID, ft.FuelTypeName,
                fp.Quantity, fp.PricePerLiter, fp.TotalCost, fp.PurchaseDate,
                fp.OdometerReading, fp.FuelStation, fp.ReceiptNumber,
                e.first_name + ' ' + e.last_name as DriverName, fp.Notes
            FROM FuelPurchases fp
            INNER JOIN Buses b ON fp.BusID = b.bus_id
            INNER JOIN FuelTypes ft ON fp.FuelTypeID = ft.FuelTypeID
            LEFT JOIN Employees e ON fp.DriverID = e.employee_id
            WHERE fp.PurchaseDate BETWEEN ? AND ?
            ORDER BY fp.PurchaseDate DESC
        """;
        
         try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                FuelRecord record = new FuelRecord();
                record.setId(rs.getLong("PurchaseID"));
                record.setBusNumber(rs.getString("bus_number")); // Updated: bus_number
                record.setBusId(rs.getString("BusID"));
                record.setDate(rs.getTimestamp("PurchaseDate").toLocalDateTime());
                record.setFuelType(rs.getString("FuelTypeName"));
                record.setQuantity(rs.getDouble("Quantity"));
                record.setPricePerLiter(rs.getDouble("PricePerLiter"));
                record.setTotalCost(rs.getDouble("TotalCost"));
                record.setOdometerReading(rs.getDouble("OdometerReading"));
                record.setFuelStation(rs.getString("FuelStation"));
                record.setReceiptNumber(rs.getString("ReceiptNumber"));
                record.setDriverName(rs.getString("DriverName")); // Updated: first_name + last_name
                record.setNotes(rs.getString("Notes"));
                records.add(record);
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching fuel records: " + e.getMessage());
            // Return sample data for demonstration
            return getSampleFuelRecords();
        }
        
        return records;
    }
    
    /**
     * Get fuel efficiency records - UPDATED WITH CORRECT COLUMN NAMES
     */
    public List<FuelEfficiencyRecord> getEfficiencyRecords() {
        List<FuelEfficiencyRecord> records = new ArrayList<>();
        
        String sql = """
            SELECT 
                b.bus_number,
                r.route_name,
                SUM(t.DistanceTravelled) as TotalDistance,
                SUM(t.FuelConsumed) as TotalFuel,
                SUM(t.DistanceTravelled) / NULLIF(SUM(t.FuelConsumed), 0) as Efficiency
            FROM Trips t
            INNER JOIN Buses b ON t.BusID = b.bus_id
            INNER JOIN Routes r ON t.RouteID = r.route_id
            WHERE t.TripDate >= DATEADD(MONTH, -1, GETDATE())
            GROUP BY b.bus_number, r.route_name
            HAVING SUM(t.FuelConsumed) > 0
        """;
        
         try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                FuelEfficiencyRecord record = new FuelEfficiencyRecord();
                record.setBusNumber(rs.getString("bus_number")); // Updated: bus_number
                record.setRouteName(rs.getString("route_name")); // Updated: route_name
                record.setDistanceTraveled(rs.getDouble("TotalDistance"));
                record.setFuelUsed(rs.getDouble("TotalFuel"));
                record.setEfficiency(rs.getDouble("Efficiency"));
                record.determineStatus();
                records.add(record);
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching efficiency records: " + e.getMessage());
            // Return sample data for demonstration
            return getSampleEfficiencyRecords();
        }
        
        return records;
    }
    
    /**
     * Get active fuel alerts
     */
    public List<FuelAlert> getActiveAlerts() {
        List<FuelAlert> alerts = new ArrayList<>();
        
        // Check for high consumption
        checkHighConsumptionAlerts(alerts);
        
        // Check for unusual patterns
        checkUnusualPatternAlerts(alerts);
        
        // Check for efficiency targets
        checkEfficiencyAlerts(alerts);
        
        return alerts;
    }
    
    /**
 * Save fuel record to database - FIXED CONNECTION MANAGEMENT
 */
public boolean saveFuelRecord(FuelRecord record) {
    Connection conn = null;
    PreparedStatement stmt = null;
    
    String sql = """
        INSERT INTO FuelPurchases (
            BusID, FuelTypeID, Quantity, PricePerLiter, TotalCost,
            PurchaseDate, OdometerReading, FuelStation, ReceiptNumber,
            DriverID, Notes, CreatedDate
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())
    """;
    
    try {
        conn = Database.getConnection();
        stmt = conn.prepareStatement(sql);
        
        stmt.setInt(1, getBusId(record.getBusNumber()));
        stmt.setInt(2, getFuelTypeId(record.getFuelType()));
        stmt.setDouble(3, record.getQuantity());
        stmt.setDouble(4, record.getPricePerLiter());
        stmt.setDouble(5, record.getTotalCost());
        stmt.setTimestamp(6, Timestamp.valueOf(record.getDate()));
        stmt.setDouble(7, record.getOdometerReading());
        stmt.setString(8, record.getFuelStation());
        stmt.setString(9, record.getReceiptNumber());
        
        // FIXED: Handle NULL driver ID properly
        Integer driverId = getDriverId(record.getDriverName());
        if (driverId != null) {
            stmt.setInt(10, driverId);
        } else {
            stmt.setNull(10, java.sql.Types.INTEGER);
        }
        
        stmt.setString(11, record.getNotes());
        
        int rowsAffected = stmt.executeUpdate();
        System.out.println("✅ Fuel record saved successfully! Rows affected: " + rowsAffected);
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        System.err.println("❌ SQL Error saving fuel record: " + e.getMessage());
        e.printStackTrace();
        return false;
    } finally {
        Database.closeResources(conn, stmt);
    }
}

    // Helper methods - UPDATED WITH CORRECT COLUMN NAMES
    
    private String getFuelTypeName(int fuelTypeId) {
        switch(fuelTypeId) {
            case 1: return "Diesel";
            case 2: return "Petrol";
            case 3: return "CNG";
            case 4: return "Electric";
            default: return "Unknown";
        }
    }
    
    private int getFuelTypeId(String fuelType) {
        switch(fuelType) {
            case "Diesel": return 1;
            case "Petrol": return 2;
            case "CNG": return 3;
            case "Electric": return 4;
            default: return 1;
        }
    }
    
    private int getBusId(String busNumber) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    String sql = "SELECT bus_id FROM Buses WHERE bus_number = ?";
    
    try {
        conn = Database.getConnection();
        if (conn == null || conn.isClosed()) {
            System.err.println("Connection unavailable in getBusId");
            return 1; // Fallback
        }
        
        stmt = conn.prepareStatement(sql);
        stmt.setString(1, busNumber);
        rs = stmt.executeQuery();
        
        if (rs.next()) {
            return rs.getInt("bus_id");
        }
    } catch (SQLException e) {
        System.err.println("Error getting bus ID: " + e.getMessage());
    } finally {
        // Close resources
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing resources in getBusId: " + e.getMessage());
        }
    }
    
    return 1; // Fallback to first bus
}

    private Integer getDriverId(String driverName) {
    if (driverName == null || driverName.trim().isEmpty()) {
        return null; // Use NULL instead of 0 for no driver
    }
    
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    // FIXED: Better query to handle name matching
    String sql = "SELECT employee_id FROM Employees WHERE CONCAT(first_name, ' ', last_name) = ? OR employee_id = ?";
    
    try {
        conn = Database.getConnection();
        stmt = conn.prepareStatement(sql);
        
        stmt.setString(1, driverName.trim());
        
        // Also try to parse as ID if provided as number
        try {
            stmt.setInt(2, Integer.parseInt(driverName.trim()));
        } catch (NumberFormatException e) {
            stmt.setNull(2, java.sql.Types.INTEGER);
        }
        
        rs = stmt.executeQuery();
        
        if (rs.next()) {
            int driverId = rs.getInt("employee_id");
            System.out.println("Found driver ID: " + driverId + " for name: " + driverName);
            return driverId;
        } else {
            System.out.println("⚠️ No driver found for: " + driverName + " - setting DriverID to NULL");
            return null; // Use NULL for non-existent drivers
        }
        
    } catch (SQLException e) {
        System.err.println("Error getting driver ID: " + e.getMessage());
        return null; // Return null on error
    } finally {
        Database.closeResources(conn, stmt, rs);
    }
}
    

    
    private void checkHighConsumptionAlerts(List<FuelAlert> alerts) {
        // Check for buses with high consumption - UPDATED WITH CORRECT COLUMN NAMES
        String sql = """
            SELECT b.bus_number, AVG(fp.Quantity) as AvgConsumption
            FROM FuelPurchases fp
            INNER JOIN Buses b ON fp.BusID = b.bus_id
            WHERE fp.PurchaseDate >= DATEADD(WEEK, -1, GETDATE())
            GROUP BY b.bus_number
            HAVING AVG(fp.Quantity) > 50
        """;
        
        // Execute query and create alerts
    }
    
    private void checkUnusualPatternAlerts(List<FuelAlert> alerts) {
        // Check for unusual filling patterns - UPDATED WITH CORRECT COLUMN NAMES
        String sql = """
            SELECT b.bus_number, COUNT(*) as FillCount, fp.FuelStation
            FROM FuelPurchases fp
            INNER JOIN Buses b ON fp.BusID = b.bus_id
            WHERE CAST(fp.PurchaseDate as DATE) = CAST(GETDATE() as DATE)
            GROUP BY b.bus_number, fp.FuelStation
            HAVING COUNT(*) > 1
        """;
        
        // Execute query and create alerts
    }
    
    private void checkEfficiencyAlerts(List<FuelAlert> alerts) {
        // Check for efficiency improvements
        // Add alerts for good performance
    }
    
    // Sample data methods for demonstration
    
    private List<FuelRecord> getSampleFuelRecords() {
        List<FuelRecord> records = new ArrayList<>();
        
        Random random = new Random();
        String[] buses = {"NC-4578", "WP-2341", "SP-6789", "EP-3456", "NC-9012"};
        String[] stations = {"IOC Colombo", "CPC Kandy", "Ceypetco Galle", "Lanka Filling Station"};
        String[] fuelTypes = {"Diesel", "Petrol"};
        
        for (int i = 0; i < 20; i++) {
            FuelRecord record = new FuelRecord();
            record.setId((long) i);
            record.setBusNumber(buses[random.nextInt(buses.length)]);
            record.setDate(LocalDateTime.now().minusDays(random.nextInt(30)));
            record.setFuelType(fuelTypes[random.nextInt(fuelTypes.length)]);
            record.setQuantity(40 + random.nextDouble() * 60);
            record.setPricePerLiter(380 + random.nextDouble() * 20);
            record.calculateTotalCost();
            record.setOdometerReading(50000 + random.nextDouble() * 50000);
            record.setFuelStation(stations[random.nextInt(stations.length)]);
            record.setDriverName("Driver " + (i + 1));
            records.add(record);
        }
        
        return records;
    }
    
    private List<FuelEfficiencyRecord> getSampleEfficiencyRecords() {
        List<FuelEfficiencyRecord> records = new ArrayList<>();
        
        String[][] data = {
            {"NC-4578", "Colombo-Kandy", "450", "90"},
            {"WP-2341", "Colombo-Galle", "380", "95"},
            {"SP-6789", "Colombo-Negombo", "120", "25"},
            {"EP-3456", "Kandy-Nuwara Eliya", "150", "40"},
            {"NC-9012", "Galle-Matara", "85", "18"}
        };
        
        for (String[] row : data) {
            FuelEfficiencyRecord record = new FuelEfficiencyRecord(
                row[0], row[1],
                Double.parseDouble(row[2]),
                Double.parseDouble(row[3])
            );
            records.add(record);
        }
        
        return records;
    }
}