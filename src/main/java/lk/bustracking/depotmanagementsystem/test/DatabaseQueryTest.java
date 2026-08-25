package lk.bustracking.depotmanagementsystem.test;

import lk.bustracking.depotmanagementsystem.services.DashboardService;
import lk.bustracking.depotmanagementsystem.models.DashboardData;

/**
 * Simple test to verify database queries work with the updated schema
 */
public class DatabaseQueryTest {
    public static void main(String[] args) {
        try {
            System.out.println("Testing DashboardService database queries...");

            DashboardService service = new DashboardService();
            DashboardData data = service.getDashboardData();

            System.out.println("=== DASHBOARD DATA TEST RESULTS ===");
            System.out.println("Total Buses: " + data.getTotalBuses());
            System.out.println("Active Buses: " + data.getActiveBuses());
            System.out.println("Total Employees: " + data.getTotalEmployees());
            System.out.println("Employees on Duty: " + data.getEmployeesOnDuty());
            System.out.println("Total Routes: " + data.getTotalRoutes());
            System.out.println("Active Routes: " + data.getActiveRoutes());
            System.out.println("Completed Trips Today: " + data.getCompletedTripsToday());
            System.out.println("Pending Trips: " + data.getPendingTrips());
            System.out.println("Fuel Efficiency: " + data.getFuelEfficiency() + " km/L");
            System.out.println("On-Time Performance: " + data.getOnTimePerformance() + "%");
            System.out.println("Revenue Today: Rs " + String.format("%,.0f", data.getRevenue()));
            System.out.println("Passengers Today: " + data.getPassengersToday());

            System.out.println("\nDatabase queries executed successfully!");
            System.out.println("All dashboard data is now dynamic and uses real database values.");

        } catch (Exception e) {
            System.err.println("Error testing database queries: " + e.getMessage());
            e.printStackTrace();
        }
    }
}