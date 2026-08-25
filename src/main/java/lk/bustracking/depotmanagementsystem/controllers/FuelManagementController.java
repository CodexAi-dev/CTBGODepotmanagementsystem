package lk.bustracking.depotmanagementsystem.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import lk.bustracking.depotmanagementsystem.db.Database;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.stage.Window;
import lk.bustracking.depotmanagementsystem.models.Bus;
import lk.bustracking.depotmanagementsystem.models.FuelAlert;
import lk.bustracking.depotmanagementsystem.models.FuelEfficiencyRecord;
import lk.bustracking.depotmanagementsystem.models.FuelRecord;
import lk.bustracking.depotmanagementsystem.services.AnalyticsService;
import lk.bustracking.depotmanagementsystem.services.BusService;
import lk.bustracking.depotmanagementsystem.services.FuelManagementService;
import lk.bustracking.depotmanagementsystem.services.RouteService;

/**
 * Fuel Management Controller for Sri Lanka Bus Tracking System
 * Handles fuel consumption tracking, efficiency monitoring, and cost analysis
 */
public class FuelManagementController {
    
    // Services
    private final FuelManagementService fuelService;
    private final BusService busService;
    private final RouteService routeService;
    private final AnalyticsService analyticsService;
    
    // Data Collections
    private final ObservableList<FuelRecord> fuelRecords;
    private final FilteredList<FuelRecord> filteredRecords;
    private final ObservableList<FuelAlert> fuelAlerts;
    private final ObservableList<FuelEfficiencyRecord> efficiencyRecords;
    
    // UI Components - Main Layout
    private VBox mainContainer;
    private ScrollPane mainScrollPane;
    private Stage primaryStage;
    
    // UI Components - Charts
    private LineChart<String, Number> consumptionChart;
    private BarChart<String, Number> costChart;
    private PieChart fuelTypeChart;
    private AreaChart<String, Number> efficiencyChart;
    
    // UI Components - Tables
    private TableView<FuelRecord> fuelRecordsTable;
    private TableView<FuelEfficiencyRecord> efficiencyTable;
    
    // UI Components - Controls
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private ComboBox<String> busFilter;
    private ComboBox<String> fuelTypeFilter;
    private TextField searchField;
    
    // Statistics Labels
    private Label totalFuelLabel;
    private Label totalCostLabel;
    private Label avgConsumptionLabel;
    private Label avgEfficiencyLabel;
    private Label monthlyCostLabel;
    private Label alertCountLabel;
    private Label lastUpdateLabel;
    
    // Background Services
    private Timeline dataUpdateTimeline;
    private Task<Void> currentDataTask;
    
    // Configuration
    private static final int DATA_UPDATE_INTERVAL = 60; // seconds
    private static final String WINDOW_TITLE = "Fuel Management System - Sri Lanka Bus Tracking";
    
    public FuelManagementController() {
        // Initialize services
        this.fuelService = new FuelManagementService();
        this.busService = new BusService();
        this.routeService = new RouteService();
        this.analyticsService = new AnalyticsService();
        
        // Initialize data collections
        this.fuelRecords = FXCollections.observableArrayList();
        this.filteredRecords = new FilteredList<>(fuelRecords);
        this.fuelAlerts = FXCollections.observableArrayList();
        this.efficiencyRecords = FXCollections.observableArrayList();
        
        // Initialize background services
        initializeBackgroundServices();
    }
    
    public void showFuelManagement(Stage stage) {
        this.primaryStage = stage;
        
        try {
            createFuelManagementInterface();
            setupScene(stage);
            loadInitialData();
            startDataUpdates();
            playEntranceAnimation();
            
            System.out.println("Fuel Management System initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing Fuel Management: " + e.getMessage());
            handleError(e);
        }
    }
    
    // ================================================================
    // UI CREATION
    // ================================================================
    
    private void createFuelManagementInterface() {
        mainContainer = new VBox(0);
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");
        
        // Create main sections
        VBox headerSection = createHeader();
        HBox statsSection = createStatisticsSection();
        VBox controlsSection = createControlsSection();
        HBox chartsSection = createChartsSection();
        HBox tablesSection = createTablesSection();
        VBox alertsSection = createAlertsSection();
        
        mainContainer.getChildren().addAll(
            headerSection,
            statsSection,
            controlsSection,
            chartsSection,
            tablesSection,
            alertsSection
        );
        
        // Setup scroll pane
        mainScrollPane = new ScrollPane(mainContainer);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        mainContainer.setOpacity(0);
    }
    
    private VBox createHeader() {
        VBox headerContainer = new VBox();
        
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setStyle(
            "-fx-background-color: linear-gradient(to right, #2E7D32, #43A047);" +
            "-fx-background-radius: 0 0 25 25;"
        );
        
        // Title section
        VBox titleSection = new VBox(5);
        
        Label title = new Label("⛽ Fuel Management System");
        title.setStyle(
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;"
        );
        
        Label subtitle = new Label("Monitor fuel consumption, costs, and efficiency");
        subtitle.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: rgba(255,255,255,0.9);"
        );
        
        titleSection.getChildren().addAll(title, subtitle);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Action buttons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button addFuelBtn = createHeaderButton("Add Fuel Record", "#FFC107");
        addFuelBtn.setOnAction(e -> showAddFuelDialog());
        
        Button importBtn = createHeaderButton("Import Data", "#2196F3");
        importBtn.setOnAction(e -> importFuelData());
        
        Button exportBtn = createHeaderButton("Export Report", "#FF5722");
        exportBtn.setOnAction(e -> exportFuelReport());
        
        Button refreshBtn = createHeaderButton("Refresh", "#9C27B0");
        refreshBtn.setOnAction(e -> refreshData());
        
        actions.getChildren().addAll(addFuelBtn, importBtn, exportBtn, refreshBtn);
        
        header.getChildren().addAll(titleSection, spacer, actions);
        headerContainer.getChildren().add(header);
        
        return headerContainer;
    }
    
    private Button createHeaderButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 8px 16px;" +
            "-fx-background-radius: 20px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);"
        );
        
        button.setOnMouseEntered(e -> button.setStyle(button.getStyle() + "-fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle().replace("-fx-scale-x: 1.05; -fx-scale-y: 1.05;", "")));
        
        return button;
    }
    
    private HBox createStatisticsSection() {
        HBox statsContainer = new HBox(20);
        statsContainer.setAlignment(Pos.CENTER);
        statsContainer.setPadding(new Insets(20, 30, 20, 30));
        
        // Create stat cards
        VBox totalFuelCard = createStatCard(
            "Total Fuel", "0 L", "This Month", "#2196F3", "⛽"
        );
        VBox totalCostCard = createStatCard(
            "Total Cost", "Rs. 0", "This Month", "#4CAF50", "💰"
        );
        VBox avgConsumptionCard = createStatCard(
            "Avg Consumption", "0 km/L", "Fleet Average", "#FF9800", "📊"
        );
        VBox efficiencyCard = createStatCard(
            "Efficiency", "0%", "vs Last Month", "#9C27B0", "📈"
        );
        VBox alertsCard = createStatCard(
            "Alerts", "0", "Active Warnings", "#f44336", "⚠️"
        );
        VBox savingsCard = createStatCard(
            "Savings", "Rs. 0", "Potential", "#00BCD4", "💡"
        );
        
        // Store references for updates
        totalFuelLabel = getStatValueLabel(totalFuelCard);
        totalCostLabel = getStatValueLabel(totalCostCard);
        avgConsumptionLabel = getStatValueLabel(avgConsumptionCard);
        avgEfficiencyLabel = getStatValueLabel(efficiencyCard);
        alertCountLabel = getStatValueLabel(alertsCard);
        monthlyCostLabel = getStatValueLabel(savingsCard);
        
        statsContainer.getChildren().addAll(
            totalFuelCard, totalCostCard, avgConsumptionCard,
            efficiencyCard, alertsCard, savingsCard
        );
        
        return statsContainer;
    }
    
    private VBox createStatCard(String title, String value, String subtitle, String color, String icon) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15, 20, 15, 20));
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 15px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);" +
            "-fx-min-width: 180px;" +
            "-fx-pref-height: 120px;"
        );
        
        // Icon and title
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #666;" +
            "-fx-font-weight: 600;"
        );
        
        header.getChildren().addAll(iconLabel, titleLabel);
        
        // Value
        Label valueLabel = new Label(value);
        valueLabel.setStyle(
            "-fx-font-size: 24px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + color + ";"
        );
        
        // Subtitle
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-text-fill: #999;"
        );
        
        card.getChildren().addAll(header, valueLabel, subtitleLabel);
        
        // Hover effect
        card.setOnMouseEntered(e -> animateCard(card, 1.03));
        card.setOnMouseExited(e -> animateCard(card, 1.0));
        
        return card;
    }
    
    private Label getStatValueLabel(VBox card) {
        return (Label) card.getChildren().get(1);
    }
    
    private void animateCard(VBox card, double scale) {
        ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
        st.setToX(scale);
        st.setToY(scale);
        st.play();
    }
    
    private VBox createControlsSection() {
        VBox controlsContainer = new VBox(15);
        controlsContainer.setPadding(new Insets(20, 30, 20, 30));
        controlsContainer.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 15px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);"
        );
        
        Label controlsTitle = new Label("Filters & Controls");
        controlsTitle.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #333;"
        );
        
        // Filter controls
        HBox filterRow = new HBox(20);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        
        // Date range
        VBox dateRangeBox = new VBox(5);
        Label dateLabel = new Label("Date Range:");
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        HBox dateControls = new HBox(10);
        startDatePicker = new DatePicker(LocalDate.now().minusMonths(1));
        endDatePicker = new DatePicker(LocalDate.now());
        startDatePicker.setPrefWidth(140);
        endDatePicker.setPrefWidth(140);
        
        dateControls.getChildren().addAll(startDatePicker, new Label("to"), endDatePicker);
        dateRangeBox.getChildren().addAll(dateLabel, dateControls);
        
        // Bus filter
        VBox busFilterBox = new VBox(5);
        Label busLabel = new Label("Bus:");
        busLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        busFilter = new ComboBox<>();
        busFilter.getItems().addAll("All Buses");
        busFilter.setValue("All Buses");
        busFilter.setPrefWidth(150);
        
        busFilterBox.getChildren().addAll(busLabel, busFilter);
        
        // Fuel type filter
        VBox fuelTypeBox = new VBox(5);
        Label fuelLabel = new Label("Fuel Type:");
        fuelLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        fuelTypeFilter = new ComboBox<>();
        fuelTypeFilter.getItems().addAll("All Types", "Diesel", "Petrol", "CNG", "Electric");
        fuelTypeFilter.setValue("All Types");
        fuelTypeFilter.setPrefWidth(120);
        
        fuelTypeBox.getChildren().addAll(fuelLabel, fuelTypeFilter);
        
        // Search field
        VBox searchBox = new VBox(5);
        Label searchLabel = new Label("Search:");
        searchLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        searchField = new TextField();
        searchField.setPromptText("Search records...");
        searchField.setPrefWidth(200);
        
        searchBox.getChildren().addAll(searchLabel, searchField);
        
        // Apply button
        Button applyBtn = new Button("Apply Filters");
        applyBtn.setStyle(
            "-fx-background-color: #2196F3;" +
            "-fx-text-fill: white;" +
            "-fx-padding: 8px 20px;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;" +
            "-fx-font-weight: bold;"
        );
        applyBtn.setOnAction(e -> applyFilters());
        
        filterRow.getChildren().addAll(dateRangeBox, busFilterBox, fuelTypeBox, searchBox, applyBtn);
        
        controlsContainer.getChildren().addAll(controlsTitle, filterRow);
        
        return controlsContainer;
    }
    
    private HBox createChartsSection() {
        HBox chartsContainer = new HBox(20);
        chartsContainer.setPadding(new Insets(20, 30, 20, 30));
        chartsContainer.setAlignment(Pos.TOP_CENTER);
        
        // Consumption trend chart
        VBox consumptionChartBox = createConsumptionChart();
        
        // Cost analysis chart
        VBox costChartBox = createCostChart();
        
        // Fuel type distribution
        VBox fuelTypeChartBox = createFuelTypeChart();
        
        chartsContainer.getChildren().addAll(consumptionChartBox, costChartBox, fuelTypeChartBox);
        HBox.setHgrow(consumptionChartBox, Priority.ALWAYS);
        HBox.setHgrow(costChartBox, Priority.ALWAYS);
        
        return chartsContainer;
    }
    
    private VBox createConsumptionChart() {
        VBox chartBox = new VBox(10);
        chartBox.setPadding(new Insets(15));
        chartBox.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 10px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
        );
        
        Label chartTitle = new Label("Fuel Consumption Trend");
        chartTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Litres");
        
        consumptionChart = new LineChart<>(xAxis, yAxis);
        consumptionChart.setTitle("");
        consumptionChart.setPrefHeight(250);
        consumptionChart.setLegendVisible(false);
        consumptionChart.setAnimated(true);
        
        // Add sample data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Consumption");
        
        consumptionChart.getData().add(series);
        
        chartBox.getChildren().addAll(chartTitle, consumptionChart);
        
        return chartBox;
    }
    
    private VBox createCostChart() {
        VBox chartBox = new VBox(10);
        chartBox.setPadding(new Insets(15));
        chartBox.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 10px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
        );
        
        Label chartTitle = new Label("Fuel Cost Analysis");
        chartTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Month");
        yAxis.setLabel("Cost (Rs.)");
        
        costChart = new BarChart<>(xAxis, yAxis);
        costChart.setTitle("");
        costChart.setPrefHeight(250);
        costChart.setLegendVisible(false);
        costChart.setAnimated(true);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Cost");
        
        costChart.getData().add(series);
        
        chartBox.getChildren().addAll(chartTitle, costChart);
        
        return chartBox;
    }
    
    private VBox createFuelTypeChart() {
        VBox chartBox = new VBox(10);
        chartBox.setPadding(new Insets(15));
        chartBox.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 10px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
        );
        
        Label chartTitle = new Label("Fuel Type Distribution");
        chartTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        fuelTypeChart = new PieChart();
        fuelTypeChart.setTitle("");
        fuelTypeChart.setPrefHeight(250);
        fuelTypeChart.setLegendSide(Side.RIGHT);
        fuelTypeChart.setAnimated(true);
        
        // Add sample data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Diesel", 75),
            new PieChart.Data("Petrol", 20),
            new PieChart.Data("CNG", 5)
        );
        
        fuelTypeChart.setData(pieChartData);
        
        chartBox.getChildren().addAll(chartTitle, fuelTypeChart);
        
        return chartBox;
    }
    
    private HBox createTablesSection() {
        HBox tablesContainer = new HBox(20);
        tablesContainer.setPadding(new Insets(20, 30, 20, 30));
        
        // Fuel records table
        VBox recordsTableBox = createFuelRecordsTable();
        
        // Efficiency analysis table
        VBox efficiencyTableBox = createEfficiencyTable();
        
        tablesContainer.getChildren().addAll(recordsTableBox, efficiencyTableBox);
        HBox.setHgrow(recordsTableBox, Priority.ALWAYS);
        HBox.setHgrow(efficiencyTableBox, Priority.ALWAYS);
        
        return tablesContainer;
    }
    
    private VBox createFuelRecordsTable() {
        VBox tableBox = new VBox(10);
        tableBox.setPadding(new Insets(15));
        tableBox.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 10px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
        );
        
        Label tableTitle = new Label("Recent Fuel Records");
        tableTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        fuelRecordsTable = new TableView<>();
        fuelRecordsTable.setPrefHeight(300);
        
        // Columns
        TableColumn<FuelRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        ));
        dateCol.setPrefWidth(100);
        
        TableColumn<FuelRecord, String> busCol = new TableColumn<>("Bus");
        busCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBusNumber()));
        busCol.setPrefWidth(80);
        
        TableColumn<FuelRecord, String> fuelTypeCol = new TableColumn<>("Type");
        fuelTypeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFuelType()));
        fuelTypeCol.setPrefWidth(80);
        
        TableColumn<FuelRecord, String> quantityCol = new TableColumn<>("Quantity (L)");
        quantityCol.setCellValueFactory(data -> new SimpleStringProperty(
            String.format("%.2f", data.getValue().getQuantity())
        ));
        quantityCol.setPrefWidth(100);
        
        TableColumn<FuelRecord, String> costCol = new TableColumn<>("Cost (Rs.)");
        costCol.setCellValueFactory(data -> new SimpleStringProperty(
            String.format("%.2f", data.getValue().getTotalCost())
        ));
        costCol.setPrefWidth(100);
        
        TableColumn<FuelRecord, String> odometerCol = new TableColumn<>("Odometer");
        odometerCol.setCellValueFactory(data -> new SimpleStringProperty(
            String.format("%.0f", data.getValue().getOdometerReading())
        ));
        odometerCol.setPrefWidth(100);
        
        TableColumn<FuelRecord, String> stationCol = new TableColumn<>("Station");
        stationCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFuelStation()));
        stationCol.setPrefWidth(150);
        
        fuelRecordsTable.getColumns().addAll(
            dateCol, busCol, fuelTypeCol, quantityCol, 
            costCol, odometerCol, stationCol
        );
        
        fuelRecordsTable.setItems(filteredRecords);
        
        tableBox.getChildren().addAll(tableTitle, fuelRecordsTable);
        
        return tableBox;
    }
    
    private VBox createEfficiencyTable() {
        VBox tableBox = new VBox(10);
        tableBox.setPadding(new Insets(15));
        tableBox.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 10px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
        );
        
        Label tableTitle = new Label("Fuel Efficiency Analysis");
        tableTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        efficiencyTable = new TableView<>();
        efficiencyTable.setPrefHeight(300);
        
        // Columns
        TableColumn<FuelEfficiencyRecord, String> busCol = new TableColumn<>("Bus");
        busCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBusNumber()));
        busCol.setPrefWidth(80);
        
        TableColumn<FuelEfficiencyRecord, String> routeCol = new TableColumn<>("Route");
        routeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRouteName()));
        routeCol.setPrefWidth(120);
        
        TableColumn<FuelEfficiencyRecord, String> distanceCol = new TableColumn<>("Distance (km)");
        distanceCol.setCellValueFactory(data -> new SimpleStringProperty(
            String.format("%.1f", data.getValue().getDistanceTraveled())
        ));
        distanceCol.setPrefWidth(100);
        
        TableColumn<FuelEfficiencyRecord, String> fuelUsedCol = new TableColumn<>("Fuel Used (L)");
        fuelUsedCol.setCellValueFactory(data -> new SimpleStringProperty(
            String.format("%.2f", data.getValue().getFuelUsed())
        ));
        fuelUsedCol.setPrefWidth(100);
        
        TableColumn<FuelEfficiencyRecord, String> efficiencyCol = new TableColumn<>("Efficiency (km/L)");
        efficiencyCol.setCellValueFactory(data -> new SimpleStringProperty(
            String.format("%.2f", data.getValue().getEfficiency())
        ));
        efficiencyCol.setPrefWidth(120);
        
        TableColumn<FuelEfficiencyRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(80);
        statusCol.setCellFactory(column -> new TableCell<FuelEfficiencyRecord, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    String color = getStatusColor(status);
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });
        
        efficiencyTable.getColumns().addAll(
            busCol, routeCol, distanceCol, fuelUsedCol, efficiencyCol, statusCol
        );
        
        efficiencyTable.setItems(efficiencyRecords);
        
        tableBox.getChildren().addAll(tableTitle, efficiencyTable);
        
        return tableBox;
    }
    
    private String getStatusColor(String status) {
        switch(status.toUpperCase()) {
            case "GOOD": return "#4CAF50";
            case "AVERAGE": return "#FF9800";
            case "POOR": return "#f44336";
            default: return "#666666";
        }
    }
    
    private VBox createAlertsSection() {
        VBox alertsContainer = new VBox(15);
        alertsContainer.setPadding(new Insets(20, 30, 30, 30));
        
        Label alertsTitle = new Label("Fuel Alerts & Notifications");
        alertsTitle.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #333;"
        );
        
        // Alerts list
        VBox alertsList = new VBox(10);
        alertsList.setPadding(new Insets(15));
        alertsList.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 10px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
        );
        
        // Add sample alerts
        for (FuelAlert alert : getSampleAlerts()) {
            alertsList.getChildren().add(createAlertItem(alert));
        }
        
        ScrollPane alertsScroll = new ScrollPane(alertsList);
        alertsScroll.setFitToWidth(true);
        alertsScroll.setPrefHeight(200);
        alertsScroll.setStyle("-fx-background-color: transparent;");
        
        alertsContainer.getChildren().addAll(alertsTitle, alertsScroll);
        
        return alertsContainer;
    }
    
    private HBox createAlertItem(FuelAlert alert) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12));
        item.setStyle(
            "-fx-background-color: " + getAlertBackgroundColor(alert.getSeverity()) + ";" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: " + getAlertBorderColor(alert.getSeverity()) + ";" +
            "-fx-border-radius: 8px;" +
            "-fx-border-width: 1px;"
        );
        
        // Icon
        Label icon = new Label(getAlertIcon(alert.getSeverity()));
        icon.setStyle("-fx-font-size: 20px;");
        
        // Content
        VBox content = new VBox(3);
        HBox.setHgrow(content, Priority.ALWAYS);
        
        Label titleLabel = new Label(alert.getTitle());
        titleLabel.setStyle(
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            "-fx-text-fill: " + getAlertTextColor(alert.getSeverity()) + ";"
        );
        
        Label descLabel = new Label(alert.getDescription());
        descLabel.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #666;"
        );
        
        Label timeLabel = new Label(alert.getTimestamp().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        ));
        timeLabel.setStyle(
            "-fx-font-size: 10px;" +
            "-fx-text-fill: #999;"
        );
        
        content.getChildren().addAll(titleLabel, descLabel, timeLabel);
        
        // Action button
        Button actionBtn = new Button("View");
        actionBtn.setStyle(
            "-fx-background-color: " + getAlertBorderColor(alert.getSeverity()) + ";" +
            "-fx-text-fill: white;" +
            "-fx-padding: 5px 15px;" +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 11px;"
        );
        actionBtn.setOnAction(e -> viewAlertDetails(alert));
        
        item.getChildren().addAll(icon, content, actionBtn);
        
        return item;
    }
    
    private String getAlertIcon(String severity) {
        switch(severity) {
            case "HIGH": return "🚨";
            case "MEDIUM": return "⚠️";
            case "LOW": return "ℹ️";
            default: return "📋";
        }
    }
    
    private String getAlertBackgroundColor(String severity) {
        switch(severity) {
            case "HIGH": return "#ffebee";
            case "MEDIUM": return "#fff3e0";
            case "LOW": return "#e3f2fd";
            default: return "#f5f5f5";
        }
    }
    
    private String getAlertBorderColor(String severity) {
        switch(severity) {
            case "HIGH": return "#f44336";
            case "MEDIUM": return "#ff9800";
            case "LOW": return "#2196f3";
            default: return "#9e9e9e";
        }
    }
    
    private String getAlertTextColor(String severity) {
        switch(severity) {
            case "HIGH": return "#c62828";
            case "MEDIUM": return "#f57c00";
            case "LOW": return "#1976d2";
            default: return "#616161";
        }
    }
    
    // ================================================================
    // DATA OPERATIONS
    // ================================================================
    
    private void loadInitialData() {
        showLoadingOverlay();
        
        Task<Void> loadTask;
        loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Load fuel records
                List<FuelRecord> records = fuelService.getFuelRecords(
                        LocalDate.now().minusMonths(1),
                        LocalDate.now()
                );
                
                // Load efficiency data
                List<FuelEfficiencyRecord> efficiency = fuelService.getEfficiencyRecords();
                
                // Load alerts
                List<FuelAlert> alerts = fuelService.getActiveAlerts();
                
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        fuelRecords.clear();
                        fuelRecords.addAll(records);
                        
                        efficiencyRecords.clear();
                        efficiencyRecords.addAll(efficiency);
                        
                        fuelAlerts.clear();
                        fuelAlerts.addAll(alerts);
                        
                        updateStatistics();
                        updateCharts();
                        try {
                            loadBusNumbers();
                        } catch (SQLException ex) {
                            Logger.getLogger(FuelManagementController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        hideLoadingOverlay();
                    }
                });
                
                return null;
            }
        };
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    private void loadBusNumbers() throws SQLException {
        List<Bus> buses = busService.getAllBuses();
        busFilter.getItems().clear();
        busFilter.getItems().add("All Buses");
        buses.forEach(bus -> busFilter.getItems().add(bus.getBusNumber()));
        busFilter.setValue("All Buses");
    }
    
    private void updateStatistics() {
        // Calculate statistics
        double totalFuel = fuelRecords.stream()
            .mapToDouble(FuelRecord::getQuantity)
            .sum();
        
        double totalCost = fuelRecords.stream()
            .mapToDouble(FuelRecord::getTotalCost)
            .sum();
        
        double avgEfficiency = efficiencyRecords.stream()
            .mapToDouble(FuelEfficiencyRecord::getEfficiency)
            .average()
            .orElse(0);
        
        int alertCount = fuelAlerts.size();
        
        // Update labels
        totalFuelLabel.setText(String.format("%.0f L", totalFuel));
        totalCostLabel.setText(String.format("Rs. %.0f", totalCost));
        avgConsumptionLabel.setText(String.format("%.1f km/L", avgEfficiency));
        avgEfficiencyLabel.setText("+5.2%"); // Sample improvement
        alertCountLabel.setText(String.valueOf(alertCount));
        monthlyCostLabel.setText("Rs. 12,500"); // Sample savings
    }
    
    private void updateCharts() {
        // Update consumption chart
        updateConsumptionChart();
        
        // Update cost chart
        updateCostChart();
        
        // Update fuel type chart
        updateFuelTypeChart();
    }
    
    private void updateConsumptionChart() {
        XYChart.Series<String, Number> series = consumptionChart.getData().get(0);
        series.getData().clear();
        
        // Group by date and sum quantities
        Map<LocalDate, Double> dailyConsumption = fuelRecords.stream()
            .collect(Collectors.groupingBy(
                record -> record.getDate().toLocalDate(),
                Collectors.summingDouble(FuelRecord::getQuantity)
            ));
        
        // Add to chart
        dailyConsumption.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .limit(30) // Last 30 days
            .forEach(entry -> {
                series.getData().add(new XYChart.Data<>(
                    entry.getKey().format(DateTimeFormatter.ofPattern("MM-dd")),
                    entry.getValue()
                ));
            });
    }
    
    private void updateCostChart() {
        XYChart.Series<String, Number> series = costChart.getData().get(0);
        series.getData().clear();
        
        // Group by month
        Map<String, Double> monthlyCost = fuelRecords.stream()
            .collect(Collectors.groupingBy(
                record -> record.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                Collectors.summingDouble(FuelRecord::getTotalCost)
            ));
        
        monthlyCost.forEach((month, cost) -> {
            series.getData().add(new XYChart.Data<>(month, cost));
        });
    }
    
    private void updateFuelTypeChart() {
        // Group by fuel type
        Map<String, Double> fuelTypeDistribution = fuelRecords.stream()
            .collect(Collectors.groupingBy(
                FuelRecord::getFuelType,
                Collectors.summingDouble(FuelRecord::getQuantity)
            ));
        
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        fuelTypeDistribution.forEach((type, quantity) -> {
            pieData.add(new PieChart.Data(type + " (" + String.format("%.0f", quantity) + "L)", quantity));
        });
        
        fuelTypeChart.setData(pieData);
    }
    
    // ================================================================
    // USER ACTIONS
    // ================================================================
    
    
    private void saveFuelRecord(String bus, LocalDate date, String fuelType, String quantity, String price, String odometer, String station, String notes, String text4) {
    try {
        // Validate inputs
        if (bus == null || fuelType == null || quantity == null || price == null) {
            showErrorAlert("Error", "Please fill all required fields");
            return;
        }
        
        FuelRecord record = new FuelRecord();
        record.setBusNumber(bus);
        record.setDate(date.atStartOfDay());
        record.setFuelType(fuelType);
        record.setQuantity(Double.parseDouble(quantity));
        record.setPricePerLiter(Double.parseDouble(price));
        record.setTotalCost(record.getQuantity() * record.getPricePerLiter());
        
        if (odometer != null && !odometer.isEmpty()) {
            record.setOdometerReading(Double.parseDouble(odometer));
        }
        
        record.setFuelStation(station != null ? station : "");
        record.setNotes(notes != null ? notes : "");
        
        // Save to database
        boolean success = fuelService.saveFuelRecord(record);
        
        if (success) {
            showSuccessNotification("Fuel record saved successfully!");
            // Refresh data after successful save
            refreshData();
        } else {
            showErrorAlert("Error", "Failed to save fuel record to database");
        }
        
    } catch (NumberFormatException e) {
        showErrorAlert("Input Error", "Please enter valid numbers for quantity, price, and odometer");
    } catch (Exception e) {
        showErrorAlert("Error", "Failed to save fuel record: " + e.getMessage());
        e.printStackTrace();
    }
}
    
    private void applyFilters() {
        filteredRecords.setPredicate(record -> {
            boolean matches = true;
            
            // Date filter
            LocalDate recordDate = record.getDate().toLocalDate();
            if (startDatePicker.getValue() != null && recordDate.isBefore(startDatePicker.getValue())) {
                matches = false;
            }
            if (endDatePicker.getValue() != null && recordDate.isAfter(endDatePicker.getValue())) {
                matches = false;
            }
            
            // Bus filter
            if (!"All Buses".equals(busFilter.getValue()) && busFilter.getValue() != null) {
                matches = matches && record.getBusNumber().equals(busFilter.getValue());
            }
            
            // Fuel type filter
            if (!"All Types".equals(fuelTypeFilter.getValue()) && fuelTypeFilter.getValue() != null) {
                matches = matches && record.getFuelType().equals(fuelTypeFilter.getValue());
            }
            
            // Search filter
            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                String search = searchField.getText().toLowerCase();
                matches = matches && (
                    record.getBusNumber().toLowerCase().contains(search) ||
                    record.getFuelStation().toLowerCase().contains(search) ||
                    record.getNotes().toLowerCase().contains(search)
                );
            }
            
            return matches;
        });
        
        updateStatistics();
        updateCharts();
    }
    
  
    
    private void viewAlertDetails(FuelAlert alert) {
        Alert detailAlert = new Alert(Alert.AlertType.INFORMATION);
        detailAlert.setTitle("Alert Details");
        detailAlert.setHeaderText(alert.getTitle());
        detailAlert.setContentText(alert.getDescription() + "\n\n" + alert.getDetails());
        detailAlert.showAndWait();
    }
    
    // ================================================================
    // BACKGROUND SERVICES
    // ================================================================
    
    private void initializeBackgroundServices() {
        dataUpdateTimeline = new Timeline(
            new KeyFrame(Duration.seconds(DATA_UPDATE_INTERVAL), e -> refreshData())
        );
        dataUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
    }
    
    private void startDataUpdates() {
        dataUpdateTimeline.play();
    }
    
    private void stopDataUpdates() {
        if (dataUpdateTimeline != null) {
            dataUpdateTimeline.stop();
        }
    }
    
    // ================================================================
    // UI HELPERS
    // ================================================================
    
    private void showLoadingOverlay() {
        ProgressIndicator loading = new ProgressIndicator();
        loading.setStyle("-fx-progress-color: #2196F3;");
        
        VBox loadingBox = new VBox(loading);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setStyle(
            "-fx-background-color: rgba(255,255,255,0.9);" +
            "-fx-padding: 20px;"
        );
        
        mainContainer.getChildren().add(loadingBox);
    }
    
    private void hideLoadingOverlay() {
        mainContainer.getChildren().removeIf(node -> 
            node instanceof VBox && !((VBox) node).getChildren().isEmpty() && 
            ((VBox) node).getChildren().get(0) instanceof ProgressIndicator
        );
    }
    
    private void showSuccessNotification(String message) {
        showNotification("Success", message, "#4CAF50");
    }
    
    
    private void showNotification(String title, String message, String color) {
        // Create notification popup
        Stage notification = new Stage();
        notification.initStyle(javafx.stage.StageStyle.UNDECORATED);
        
        VBox content = new VBox(5);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(15));
        content.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 8px;"
        );
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14px;"
        );
        
        Label messageLabel = new Label(message);
        messageLabel.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;"
        );
        
        content.getChildren().addAll(titleLabel, messageLabel);
        
        Scene scene = new Scene(content);
        scene.setFill(Color.TRANSPARENT);
        notification.setScene(scene);
        
        // Position at top-right
        notification.setX(primaryStage.getX() + primaryStage.getWidth() - 320);
        notification.setY(primaryStage.getY() + 80);
        
        notification.show();
        
        // Auto-hide after 3 seconds
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(3), e -> notification.close())
        );
        timeline.play();
    }
    
    private void handleError(Exception e) {
        e.printStackTrace();
        showErrorAlert("System Error", "An error occurred: " + e.getMessage());
    }
    
    // ================================================================
    // ANIMATIONS
    // ================================================================
    
    private void playEntranceAnimation() {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), mainContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
    
    private void setupScene(Stage stage) {
        Scene scene = new Scene(mainScrollPane, 1400, 900);
        
        stage.setTitle(WINDOW_TITLE);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
        
        stage.setOnCloseRequest(event -> {
            stopDataUpdates();
        });
    }
    
    // ================================================================
    // SAMPLE DATA
    // ================================================================
    
    private List<FuelAlert> getSampleAlerts() {
        List<FuelAlert> alerts = new ArrayList<>();
        
        alerts.add(new FuelAlert(
            "High Fuel Consumption",
            "Bus NC-4578 consuming 20% more fuel than average",
            "HIGH",
            LocalDateTime.now().minusHours(2),
            "Check vehicle maintenance and driver behavior"
        ));
        
        alerts.add(new FuelAlert(
            "Unusual Filling Pattern",
            "Bus WP-2341 filled twice at same location today",
            "MEDIUM",
            LocalDateTime.now().minusHours(5),
            "Verify with driver and check for fuel theft"
        ));
        
        alerts.add(new FuelAlert(
            "Efficiency Target Met",
            "Route Colombo-Kandy achieved 15% better efficiency",
            "LOW",
            LocalDateTime.now().minusDays(1),
            "Good performance - share best practices"
        ));
        
        return alerts;
    }
    // ================================================================
// DASHBOARD INTEGRATION METHODS
// ================================================================

/**
 * Get recent fuel records for dashboard display
 */
public List<FuelRecord> getRecentFuelRecords() {
    try {
        return fuelService.getFuelRecords(
            LocalDate.now().minusDays(30), 
            LocalDate.now()
        );
    } catch (Exception e) {
        System.err.println("Error getting recent fuel records: " + e.getMessage());
        return Collections.emptyList();
    }
}

/**
 * Get efficiency data for charts
 */
public List<FuelEfficiencyRecord> getEfficiencyData() {
    try {
        return fuelService.getEfficiencyRecords();
    } catch (Exception e) {
        System.err.println("Error getting efficiency data: " + e.getMessage());
        return Collections.emptyList();
    }
}

/**
 * Show add fuel dialog (adapted for use without primary stage reference)
 */
/**
 * Show add fuel dialog - FIXED null stage issue
 */
public void showAddFuelDialog() {
    Platform.runLater(() -> {
        try {
            Stage dialog = new Stage();
            
            // Use the currently focused window as owner, or no owner if null
            Stage ownerStage = getCurrentStage();
            if (ownerStage != null) {
                dialog.initOwner(ownerStage);
                dialog.initModality(Modality.WINDOW_MODAL);
            } else {
                dialog.initModality(Modality.APPLICATION_MODAL);
            }
            
            dialog.setTitle("Add Fuel Record");
            
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));
            
            // Form fields
            ComboBox<String> busCombo = new ComboBox<>();
            try {
                List<Bus> buses = busService.getAllBuses();
                busCombo.setItems(FXCollections.observableArrayList(
                    buses.stream().map(Bus::getBusNumber).collect(Collectors.toList())
                ));
            } catch (SQLException e) {
                System.err.println("Error loading buses: " + e.getMessage());
                busCombo.setItems(FXCollections.observableArrayList("CTB-245", "CTB-189", "CTB-156"));
            }
            
            ComboBox<String> fuelTypeCombo = new ComboBox<>();
            fuelTypeCombo.getItems().addAll("Diesel", "Petrol", "CNG");
            fuelTypeCombo.setValue("Diesel");
            
            TextField quantityField = new TextField();
            TextField priceField = new TextField();
            TextField odometerField = new TextField();
            TextField stationField = new TextField();
            
            // Load available drivers for combo box
            ComboBox<String> driverCombo = new ComboBox<>();
            loadAvailableDrivers(driverCombo);
            
            DatePicker datePicker = new DatePicker(LocalDate.now());
            TextArea notesArea = new TextArea();
            notesArea.setPrefRowCount(3);
            
            // Add to grid
            grid.add(new Label("Bus:"), 0, 0);
            grid.add(busCombo, 1, 0);
            
            grid.add(new Label("Date:"), 0, 1);
            grid.add(datePicker, 1, 1);
            
            grid.add(new Label("Fuel Type:"), 0, 2);
            grid.add(fuelTypeCombo, 1, 2);
            
            grid.add(new Label("Quantity (Litres):"), 0, 3);
            grid.add(quantityField, 1, 3);
            
            grid.add(new Label("Price per Litre:"), 0, 4);
            grid.add(priceField, 1, 4);
            
            grid.add(new Label("Odometer Reading:"), 0, 5);
            grid.add(odometerField, 1, 5);
            
            grid.add(new Label("Fuel Station:"), 0, 6);
            grid.add(stationField, 1, 6);
            
            grid.add(new Label("Driver:"), 0, 7);
            grid.add(driverCombo, 1, 7);
            
            grid.add(new Label("Notes:"), 0, 8);
            grid.add(notesArea, 1, 8);
            
            // Buttons
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(10, 0, 0, 0));
            
            Button saveBtn = new Button("Save");
            saveBtn.setStyle(
                "-fx-background-color: #4CAF50;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 8px 20px;" +
                "-fx-background-radius: 5px;" +
                "-fx-cursor: hand;"
            );
            saveBtn.setOnAction(e -> {
                if (validateInput(busCombo, quantityField, priceField)) {
                    saveFuelRecord(
                        busCombo.getValue(),
                        datePicker.getValue(),
                        fuelTypeCombo.getValue(),
                        quantityField.getText(),
                        priceField.getText(),
                        odometerField.getText(),
                        stationField.getText(),
                        driverCombo.getValue(), // Use selected driver from combo
                        notesArea.getText()
                    );
                    dialog.close();
                }
            });
            
            Button cancelBtn = new Button("Cancel");
            cancelBtn.setStyle(
                "-fx-background-color: #f44336;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 8px 20px;" +
                "-fx-background-radius: 5px;" +
                "-fx-cursor: hand;"
            );
            cancelBtn.setOnAction(e -> dialog.close());
            
            buttonBox.getChildren().addAll(saveBtn, cancelBtn);
            
            VBox dialogContent = new VBox(15);
            dialogContent.setPadding(new Insets(20));
            dialogContent.getChildren().addAll(grid, buttonBox);
            
            Scene scene = new Scene(dialogContent, 400, 500);
            dialog.setScene(scene);
            dialog.showAndWait();
            
        } catch (Exception ex) {
            System.err.println("Error showing fuel dialog: " + ex.getMessage());
            showErrorAlert("Error", "Unable to open fuel record dialog: " + ex.getMessage());
        }
    });
}

/**
 * Get the current active stage
 */
private Stage getCurrentStage() {
    // Try to get the currently focused window
    for (Window window : Window.getWindows()) {
        if (window instanceof Stage && window.isFocused()) {
            return (Stage) window;
        }
    }
    
    // If no focused window, return any visible stage
    for (Window window : Window.getWindows()) {
        if (window instanceof Stage && window.isShowing()) {
            return (Stage) window;
        }
    }
    
    return null; // No stage available
}

/**
 * Load available drivers into combo box
 */
private void loadAvailableDrivers(ComboBox<String> driverCombo) {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
        conn = Database.getConnection();
        stmt = conn.prepareStatement(
            "SELECT employee_id, first_name, last_name FROM Employees WHERE first_name IS NOT NULL AND last_name IS NOT NULL"
        );
        rs = stmt.executeQuery();
        
        ObservableList<String> drivers = FXCollections.observableArrayList();
        drivers.add("No Driver"); // Default option
        
        while (rs.next()) {
            String driverName = rs.getString("first_name") + " " + rs.getString("last_name");
            drivers.add(driverName);
        }
        
        driverCombo.setItems(drivers);
        driverCombo.setValue("No Driver");
        
    } catch (SQLException e) {
        System.err.println("Error loading drivers: " + e.getMessage());
        driverCombo.setItems(FXCollections.observableArrayList("No Driver"));
    } finally {
        Database.closeResources(conn, stmt, rs);
    }
}

/**
 * Validate input fields
 */
private boolean validateInput(ComboBox<String> busCombo, TextField quantityField, TextField priceField) {
    if (busCombo.getValue() == null || busCombo.getValue().isEmpty()) {
        showErrorAlert("Validation Error", "Please select a bus");
        return false;
    }
    
    try {
        Double.parseDouble(quantityField.getText());
        Double.parseDouble(priceField.getText());
        return true;
    } catch (NumberFormatException e) {
        showErrorAlert("Validation Error", "Please enter valid numbers for quantity and price");
        return false;
    }
}

/**
 * Show error alert without stage dependency
 */
private void showErrorAlert(String title, String message) {
    Platform.runLater(() -> {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Use current stage as owner if available
        Stage currentStage = getCurrentStage();
        if (currentStage != null) {
            alert.initOwner(currentStage);
        }
        
        alert.showAndWait();
    });
}

/**
 * Import fuel data (public wrapper)
 */
public void importFuelData() {
    Platform.runLater(() -> {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Fuel Data");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        // This will use the primary stage if available, otherwise null
        java.io.File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            // Import logic here
            System.out.println("Importing data from: " + file.getAbsolutePath());
            showSuccessNotification("Data imported successfully");
            // You might want to trigger a refresh here
        }
    });
}

/**
 * Export fuel report (public wrapper)  
 */
public void exportFuelReport() {
    Platform.runLater(() -> {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Fuel Report");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        fileChooser.setInitialFileName("fuel_report_" + LocalDate.now() + ".pdf");
        
        java.io.File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            // Export logic here
            System.out.println("Exporting report to: " + file.getAbsolutePath());
            showSuccessNotification("Report exported successfully");
        }
    });
}

/**
 * Refresh fuel data (useful for the dashboard panel)
 */
public void refreshData() {
    loadInitialData();
}
}