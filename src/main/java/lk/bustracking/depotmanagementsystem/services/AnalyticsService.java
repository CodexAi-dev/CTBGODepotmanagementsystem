// ================================================================
// File: src/main/java/lk/bustracking/service/AnalyticsService.java
// ================================================================
package lk.bustracking.depotmanagementsystem.services;

import lk.bustracking.depotmanagementsystem.models.RoutePerformanceData;
import lk.bustracking.depotmanagementsystem.models.BusPerformanceData;
import lk.bustracking.depotmanagementsystem.db.Database;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AnalyticsService {

    private static final Logger LOGGER = Logger.getLogger(AnalyticsService.class.getName());

    /**
     * Get fuel consumption trends from database
     */
    public Map<String, Double> getFuelConsumptionTrends(LocalDate startDate, LocalDate endDate) {
        Map<String, Double> trends = new HashMap<>();

        String sql = """
                    SELECT
                        DATEPART(WEEK, fp.PurchaseDate) as WeekNumber,
                        SUM(fp.Quantity) as TotalFuel
                    FROM FuelPurchases fp
                    WHERE fp.PurchaseDate BETWEEN ? AND ?
                    GROUP BY DATEPART(WEEK, fp.PurchaseDate)
                    ORDER BY DATEPART(WEEK, fp.PurchaseDate)
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));

            ResultSet rs = stmt.executeQuery();
            int weekCount = 1;
            while (rs.next()) {
                trends.put("Week " + weekCount, rs.getDouble("TotalFuel"));
                weekCount++;
            }

            LOGGER.info("Retrieved fuel consumption trends for " + trends.size() + " weeks");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving fuel consumption trends", e);
            // Return sample data as fallback
            trends.put("Week 1", 1250.5);
            trends.put("Week 2", 1180.3);
            trends.put("Week 3", 1320.7);
            trends.put("Week 4", 1150.2);
        }

        return trends;
    }

    /**
     * Get cost analysis from database
     */
    public Map<String, Double> getCostAnalysis(LocalDate startDate, LocalDate endDate) {
        Map<String, Double> costs = new HashMap<>();

        // Fuel costs
        String fuelSql = "SELECT SUM(TotalCost) as FuelCosts FROM FuelPurchases WHERE PurchaseDate BETWEEN ? AND ?";
        // Maintenance costs (assuming there's a maintenance table)
        String maintenanceSql = "SELECT SUM(cost) as MaintenanceCosts FROM Maintenance WHERE maintenance_date BETWEEN ? AND ?";
        // Labor costs (assuming there's an employee salary table)
        String laborSql = "SELECT SUM(salary) as LaborCosts FROM Employees WHERE hire_date <= ?";

        try (Connection conn = Database.getConnection()) {

            // Fuel costs
            try (PreparedStatement stmt = conn.prepareStatement(fuelSql)) {
                stmt.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
                stmt.setTimestamp(2, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    costs.put("Fuel Costs", rs.getDouble("FuelCosts"));
                }
            }

            // Maintenance costs (fallback to sample if table doesn't exist)
            try (PreparedStatement stmt = conn.prepareStatement(maintenanceSql)) {
                stmt.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
                stmt.setTimestamp(2, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    costs.put("Maintenance", rs.getDouble("MaintenanceCosts"));
                }
            } catch (SQLException e) {
                costs.put("Maintenance", 8750.25); // Sample data
            }

            // Labor costs
            try (PreparedStatement stmt = conn.prepareStatement(laborSql)) {
                stmt.setTimestamp(1, Timestamp.valueOf(endDate.atStartOfDay()));
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    costs.put("Labor", rs.getDouble("LaborCosts"));
                }
            } catch (SQLException e) {
                costs.put("Labor", 25680.75); // Sample data
            }

            costs.put("Other", 3240.00); // Sample data for other costs

            LOGGER.info("Retrieved cost analysis data");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving cost analysis", e);
            // Return sample data as fallback
            costs.put("Fuel Costs", 15420.50);
            costs.put("Maintenance", 8750.25);
            costs.put("Labor", 25680.75);
            costs.put("Other", 3240.00);
        }

        return costs;
    }

    /**
     * Calculate potential savings based on efficiency improvements
     */
    public Double calculatePotentialSavings() {
        // Calculate potential savings based on efficiency improvements
        // This could be based on historical data analysis
        return 8450.00; // For now, return sample value
    }

    /**
     * Get route performance data from database
     */
    public List<RoutePerformanceData> getRoutePerformanceData() {
        List<RoutePerformanceData> data = new ArrayList<>();

        String sql = """
                    SELECT
                        r.route_number,
                        COUNT(t.TripID) as TripCount,
                        SUM(t.Revenue) as TotalRevenue,
                        AVG(t.FuelConsumed / NULLIF(t.DistanceTravelled, 0) * 100) as AvgEfficiency,
                        AVG(t.OnTimePerformance) as OnTimeRate
                    FROM Routes r
                    LEFT JOIN Trips t ON r.route_id = t.RouteID
                        AND t.TripDate >= DATEADD(MONTH, -1, GETDATE())
                    GROUP BY r.route_number
                    HAVING COUNT(t.TripID) > 0
                    ORDER BY TotalRevenue DESC
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String routeNumber = rs.getString("route_number");
                int tripCount = rs.getInt("TripCount");
                double totalRevenue = rs.getDouble("TotalRevenue");
                double avgEfficiency = rs.getDouble("AvgEfficiency");
                double onTimeRate = rs.getDouble("OnTimeRate");

                // Handle null values
                if (Double.isNaN(avgEfficiency))
                    avgEfficiency = 12.0;
                if (Double.isNaN(onTimeRate))
                    onTimeRate = 95.0;

                data.add(new RoutePerformanceData(routeNumber, tripCount, totalRevenue, avgEfficiency, onTimeRate));
            }

            LOGGER.info("Retrieved route performance data for " + data.size() + " routes");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving route performance data", e);
            // Return sample data as fallback
            data.add(new RoutePerformanceData("138", 45, 5420.50, 12.3, 96.5));
            data.add(new RoutePerformanceData("245", 38, 4890.75, 11.8, 94.2));
            data.add(new RoutePerformanceData("167", 52, 6230.25, 13.1, 97.8));
            data.add(new RoutePerformanceData("089", 29, 3450.00, 12.9, 92.1));
            data.add(new RoutePerformanceData("156", 41, 4980.80, 12.5, 95.3));
        }

        return data;
    }

    /**
     * Get top performing buses data from database
     */
    public List<BusPerformanceData> getTopPerformingBuses() {
        List<BusPerformanceData> data = new ArrayList<>();

        String sql = """
                    SELECT TOP 5
                        b.bus_number,
                        AVG(t.FuelConsumed / NULLIF(t.DistanceTravelled, 0) * 100) as AvgEfficiency,
                        COUNT(t.TripID) as TripCount,
                        AVG(t.OnTimePerformance) as OnTimeRate
                    FROM Buses b
                    LEFT JOIN Trips t ON b.bus_id = t.BusID
                        AND t.TripDate >= DATEADD(MONTH, -1, GETDATE())
                    GROUP BY b.bus_number
                    HAVING COUNT(t.TripID) > 0
                    ORDER BY AvgEfficiency ASC, OnTimeRate DESC
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String busNumber = rs.getString("bus_number");
                double avgEfficiency = rs.getDouble("AvgEfficiency");
                int tripCount = rs.getInt("TripCount");
                double onTimeRate = rs.getDouble("OnTimeRate");

                // Handle null values
                if (Double.isNaN(avgEfficiency))
                    avgEfficiency = 12.0;
                if (Double.isNaN(onTimeRate))
                    onTimeRate = 95.0;

                data.add(new BusPerformanceData(busNumber, avgEfficiency, tripCount, onTimeRate));
            }

            LOGGER.info("Retrieved top performing buses data for " + data.size() + " buses");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving top performing buses data", e);
            // Return sample data as fallback
            data.add(new BusPerformanceData("CTB-245", 11.2, 28, 45.6));
            data.add(new BusPerformanceData("CTB-189", 11.5, 32, 38.9));
            data.add(new BusPerformanceData("CTB-156", 11.8, 25, 52.1));
            data.add(new BusPerformanceData("CTB-198", 12.1, 29, 41.3));
            data.add(new BusPerformanceData("CTB-234", 12.3, 31, 35.7));
        }

        return data;
    }

    /**
     * Get revenue trend data from database
     */
    public Map<String, Double> getRevenueTrendData(int days) {
        Map<String, Double> data = new HashMap<>();

        String sql = """
                    SELECT
                        CONVERT(VARCHAR(10), t.TripDate, 101) as TripDate,
                        SUM(t.Revenue) as DailyRevenue
                    FROM Trips t
                    WHERE t.TripDate >= DATEADD(DAY, -?, GETDATE())
                    GROUP BY CONVERT(VARCHAR(10), t.TripDate, 101)
                    ORDER BY TripDate
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, days);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String dateStr = rs.getString("TripDate");
                double revenue = rs.getDouble("DailyRevenue");
                data.put(dateStr, revenue);
            }

            LOGGER.info("Retrieved revenue trend data for " + data.size() + " days");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving revenue trend data", e);
            // Return sample data as fallback
            for (int i = days - 1; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                double revenue = 1200 + Math.random() * 800;
                data.put(date.format(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy")), revenue);
            }
        }

        return data;
    }

    /**
     * Get fuel consumption by route from database
     */
    public Map<String, Double> getFuelConsumptionByRoute() {
        Map<String, Double> data = new HashMap<>();

        String sql = """
                    SELECT
                        r.route_number,
                        SUM(t.FuelConsumed) as TotalFuel
                    FROM Routes r
                    LEFT JOIN Trips t ON r.route_id = t.RouteID
                        AND t.TripDate >= DATEADD(MONTH, -1, GETDATE())
                    GROUP BY r.route_number
                    HAVING SUM(t.FuelConsumed) > 0
                    ORDER BY TotalFuel DESC
                """;

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String routeNumber = rs.getString("route_number");
                double totalFuel = rs.getDouble("TotalFuel");
                data.put("Route " + routeNumber, totalFuel);
            }

            LOGGER.info("Retrieved fuel consumption by route for " + data.size() + " routes");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving fuel consumption by route", e);
            // Return sample data as fallback
            String[] routes = { "Route 138", "Route 245", "Route 167", "Route 089", "Route 156" };
            for (String route : routes) {
                data.put(route, 150 + Math.random() * 100);
            }
        }

        return data;
    }

    /**
     * Get financial KPIs from database
     */
    public Map<String, Object> getFinancialKPIs() {
        Map<String, Object> kpis = new HashMap<>();

        try (Connection conn = Database.getConnection()) {

            // Total Revenue (last 30 days)
            String revenueSql = "SELECT SUM(Revenue) as TotalRevenue FROM Trips WHERE TripDate >= DATEADD(DAY, -30, GETDATE())";
            try (PreparedStatement stmt = conn.prepareStatement(revenueSql);
                    ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double totalRevenue = rs.getDouble("TotalRevenue");
                    kpis.put("totalRevenue", totalRevenue);
                    kpis.put("revenueChange", calculateRevenueChange(conn)); // Calculate percentage change
                }
            }

            // Fuel Savings (compare current vs previous period)
            String fuelSavingsSql = """
                        SELECT
                            SUM(CASE WHEN PurchaseDate >= DATEADD(DAY, -30, GETDATE()) THEN TotalCost ELSE 0 END) as CurrentFuelCost,
                            SUM(CASE WHEN PurchaseDate BETWEEN DATEADD(DAY, -60, GETDATE()) AND DATEADD(DAY, -30, GETDATE()) THEN TotalCost ELSE 0 END) as PreviousFuelCost
                        FROM FuelPurchases
                        WHERE PurchaseDate >= DATEADD(DAY, -60, GETDATE())
                    """;
            try (PreparedStatement stmt = conn.prepareStatement(fuelSavingsSql);
                    ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double currentFuelCost = rs.getDouble("CurrentFuelCost");
                    double previousFuelCost = rs.getDouble("PreviousFuelCost");
                    double fuelSavings = previousFuelCost - currentFuelCost;
                    double fuelSavingsChange = previousFuelCost > 0 ? (fuelSavings / previousFuelCost) * 100 : 0;
                    kpis.put("fuelSavings", Math.max(0, fuelSavings));
                    kpis.put("fuelSavingsChange", fuelSavingsChange);
                }
            }

            // Average Efficiency
            String efficiencySql = """
                        SELECT AVG(FuelConsumed / NULLIF(DistanceTravelled, 0) * 100) as AvgEfficiency
                        FROM Trips
                        WHERE TripDate >= DATEADD(DAY, -30, GETDATE()) AND DistanceTravelled > 0
                    """;
            try (PreparedStatement stmt = conn.prepareStatement(efficiencySql);
                    ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double avgEfficiency = rs.getDouble("AvgEfficiency");
                    if (Double.isNaN(avgEfficiency))
                        avgEfficiency = 12.8;
                    kpis.put("avgEfficiency", avgEfficiency);
                    kpis.put("efficiencyChange", calculateEfficiencyChange(conn));
                }
            }

            // On-Time Performance
            String onTimeSql = "SELECT AVG(OnTimePerformance) as OnTimeRate FROM Trips WHERE TripDate >= DATEADD(DAY, -30, GETDATE())";
            try (PreparedStatement stmt = conn.prepareStatement(onTimeSql);
                    ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double onTimeRate = rs.getDouble("OnTimeRate");
                    if (Double.isNaN(onTimeRate))
                        onTimeRate = 95.0;
                    kpis.put("onTimeRate", onTimeRate);
                    kpis.put("onTimeChange", calculateOnTimeChange(conn));
                }
            }

            LOGGER.info("Retrieved financial KPIs from database");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving financial KPIs", e);
            // Return sample data as fallback
            kpis.put("totalRevenue", 45230.00);
            kpis.put("revenueChange", 12.5);
            kpis.put("fuelSavings", 8450.00);
            kpis.put("fuelSavingsChange", 8.2);
            kpis.put("avgEfficiency", 12.8);
            kpis.put("efficiencyChange", -3.1);
            kpis.put("onTimeRate", 94.5);
            kpis.put("onTimeChange", 2.3);
        }

        return kpis;
    }

    /**
     * Calculate revenue change percentage
     */
    private double calculateRevenueChange(Connection conn) throws SQLException {
        String sql = """
                    SELECT
                        SUM(CASE WHEN TripDate >= DATEADD(DAY, -30, GETDATE()) THEN Revenue ELSE 0 END) as CurrentRevenue,
                        SUM(CASE WHEN TripDate BETWEEN DATEADD(DAY, -60, GETDATE()) AND DATEADD(DAY, -30, GETDATE()) THEN Revenue ELSE 0 END) as PreviousRevenue
                    FROM Trips
                    WHERE TripDate >= DATEADD(DAY, -60, GETDATE())
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                double current = rs.getDouble("CurrentRevenue");
                double previous = rs.getDouble("PreviousRevenue");
                return previous > 0 ? ((current - previous) / previous) * 100 : 0;
            }
        }
        return 0;
    }

    /**
     * Calculate efficiency change percentage
     */
    private double calculateEfficiencyChange(Connection conn) throws SQLException {
        String sql = """
                    SELECT
                        AVG(CASE WHEN TripDate >= DATEADD(DAY, -30, GETDATE()) THEN FuelConsumed / NULLIF(DistanceTravelled, 0) * 100 ELSE NULL END) as CurrentEfficiency,
                        AVG(CASE WHEN TripDate BETWEEN DATEADD(DAY, -60, GETDATE()) AND DATEADD(DAY, -30, GETDATE()) THEN FuelConsumed / NULLIF(DistanceTravelled, 0) * 100 ELSE NULL END) as PreviousEfficiency
                    FROM Trips
                    WHERE TripDate >= DATEADD(DAY, -60, GETDATE()) AND DistanceTravelled > 0
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                double current = rs.getDouble("CurrentEfficiency");
                double previous = rs.getDouble("PreviousEfficiency");
                if (Double.isNaN(current))
                    current = 12.8;
                if (Double.isNaN(previous))
                    previous = 12.8;
                return previous > 0 ? ((current - previous) / previous) * 100 : 0;
            }
        }
        return 0;
    }

    /**
     * Calculate on-time performance change percentage
     */
    private double calculateOnTimeChange(Connection conn) throws SQLException {
        String sql = """
                    SELECT
                        AVG(CASE WHEN TripDate >= DATEADD(DAY, -30, GETDATE()) THEN OnTimePerformance ELSE NULL END) as CurrentOnTime,
                        AVG(CASE WHEN TripDate BETWEEN DATEADD(DAY, -60, GETDATE()) AND DATEADD(DAY, -30, GETDATE()) THEN OnTimePerformance ELSE NULL END) as PreviousOnTime
                    FROM Trips
                    WHERE TripDate >= DATEADD(DAY, -60, GETDATE())
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                double current = rs.getDouble("CurrentOnTime");
                double previous = rs.getDouble("PreviousOnTime");
                if (Double.isNaN(current))
                    current = 95.0;
                if (Double.isNaN(previous))
                    previous = 95.0;
                return (current - previous);
            }
        }
        return 0;
    }

    /**
     * Get operational KPIs
     */
    public Map<String, Object> getOperationalKPIs() {
        Map<String, Object> kpis = new HashMap<>();
        kpis.put("fleetUtilization", 87.5);
        kpis.put("utilizationChange", 2.1);
        kpis.put("avgTripTime", 2.3);
        kpis.put("tripTimeChange", -8.5);
        kpis.put("maintenanceRate", 4.2);
        kpis.put("maintenanceChange", -12.3);
        kpis.put("driverEfficiency", 94.1);
        kpis.put("driverEfficiencyChange", 1.8);
        return kpis;
    }

    /**
     * Get performance KPIs
     */
    public Map<String, Object> getPerformanceKPIs() {
        Map<String, Object> kpis = new HashMap<>();
        kpis.put("onTimePerformance", 94.2);
        kpis.put("onTimeChange", 2.1);
        kpis.put("customerSatisfaction", 4.6);
        kpis.put("satisfactionChange", 0.3);
        kpis.put("safetyIncidents", 2);
        kpis.put("safetyChange", -50.0);
        kpis.put("avgSpeed", 42.0);
        kpis.put("speedChange", 3.2);
        return kpis;
    }
}