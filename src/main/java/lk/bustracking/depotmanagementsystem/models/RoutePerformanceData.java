package lk.bustracking.depotmanagementsystem.models;

import javafx.beans.property.*;

/**
 * Data model for route performance analytics
 */
public class RoutePerformanceData {
    private final StringProperty route = new SimpleStringProperty();
    private final IntegerProperty trips = new SimpleIntegerProperty();
    private final DoubleProperty revenue = new SimpleDoubleProperty();
    private final DoubleProperty efficiency = new SimpleDoubleProperty();
    private final DoubleProperty onTimeRate = new SimpleDoubleProperty();

    public RoutePerformanceData(String route, int trips, double revenue, double efficiency, double onTimeRate) {
        setRoute(route);
        setTrips(trips);
        setRevenue(revenue);
        setEfficiency(efficiency);
        setOnTimeRate(onTimeRate);
    }

    // Property getters
    public StringProperty routeProperty() {
        return route;
    }

    public IntegerProperty tripsProperty() {
        return trips;
    }

    public DoubleProperty revenueProperty() {
        return revenue;
    }

    public DoubleProperty efficiencyProperty() {
        return efficiency;
    }

    public DoubleProperty onTimeRateProperty() {
        return onTimeRate;
    }

    // Value getters/setters
    public String getRoute() {
        return route.get();
    }

    public void setRoute(String value) {
        route.set(value);
    }

    public int getTrips() {
        return trips.get();
    }

    public void setTrips(int value) {
        trips.set(value);
    }

    public double getRevenue() {
        return revenue.get();
    }

    public void setRevenue(double value) {
        revenue.set(value);
    }

    public double getEfficiency() {
        return efficiency.get();
    }

    public void setEfficiency(double value) {
        efficiency.set(value);
    }

    public double getOnTimeRate() {
        return onTimeRate.get();
    }

    public void setOnTimeRate(double value) {
        onTimeRate.set(value);
    }
}