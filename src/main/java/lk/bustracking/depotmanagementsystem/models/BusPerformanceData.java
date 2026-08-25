package lk.bustracking.depotmanagementsystem.models;

import javafx.beans.property.*;

/**
 * Data model for bus performance analytics
 */
public class BusPerformanceData {
    private final StringProperty busNumber = new SimpleStringProperty();
    private final DoubleProperty efficiency = new SimpleDoubleProperty();
    private final IntegerProperty trips = new SimpleIntegerProperty();
    private final DoubleProperty fuelSaved = new SimpleDoubleProperty();

    public BusPerformanceData(String busNumber, double efficiency, int trips, double fuelSaved) {
        setBusNumber(busNumber);
        setEfficiency(efficiency);
        setTrips(trips);
        setFuelSaved(fuelSaved);
    }

    // Property getters
    public StringProperty busNumberProperty() {
        return busNumber;
    }

    public DoubleProperty efficiencyProperty() {
        return efficiency;
    }

    public IntegerProperty tripsProperty() {
        return trips;
    }

    public DoubleProperty fuelSavedProperty() {
        return fuelSaved;
    }

    // Value getters/setters
    public String getBusNumber() {
        return busNumber.get();
    }

    public void setBusNumber(String value) {
        busNumber.set(value);
    }

    public double getEfficiency() {
        return efficiency.get();
    }

    public void setEfficiency(double value) {
        efficiency.set(value);
    }

    public int getTrips() {
        return trips.get();
    }

    public void setTrips(int value) {
        trips.set(value);
    }

    public double getFuelSaved() {
        return fuelSaved.get();
    }

    public void setFuelSaved(double value) {
        fuelSaved.set(value);
    }
}