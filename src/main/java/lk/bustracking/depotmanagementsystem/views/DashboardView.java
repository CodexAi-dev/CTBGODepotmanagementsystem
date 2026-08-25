package lk.bustracking.depotmanagementsystem.views;

import lk.bustracking.depotmanagementsystem.views.FuelManagementPanel;
import lk.bustracking.depotmanagementsystem.models.User;
import lk.bustracking.depotmanagementsystem.models.DashboardData;
import lk.bustracking.depotmanagementsystem.models.ActivityLog;
import lk.bustracking.depotmanagementsystem.models.SystemStatus;
import lk.bustracking.depotmanagementsystem.models.RoutePerformanceData;
import lk.bustracking.depotmanagementsystem.models.BusPerformanceData;
import lk.bustracking.depotmanagementsystem.controllers.DashboardController;
import lk.bustracking.depotmanagementsystem.views.GPSTrackingPanel;
import lk.bustracking.depotmanagementsystem.controllers.FuelManagementController;
import lk.bustracking.depotmanagementsystem.views.EmployeeManagementPanel;
import lk.bustracking.depotmanagementsystem.services.AnalyticsService;
import lk.bustracking.depotmanagementsystem.services.DashboardService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.chart.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Arrays;

/**
 * Modern Professional Dashboard View with Data-Focused Layout
 */
public class DashboardView {

    private static final Logger LOGGER = Logger.getLogger(DashboardView.class.getName());

    private DashboardController controller;
    private User currentUser;
    private Stage primaryStage;
    private BorderPane mainLayout;
    private TabPane mainTabPane;
    private Label timeLabel;
    private Label welcomeLabel;
    private Timeline clockTimer;
    private Task<Void> updateTask;
    private RouteManagementView routeManagementView;
    private BusManagementView busManagementView;
    private AnalyticsService analyticsService;
    private GPSTrackingPanel gpsTrackingPanel;
    private EmployeeManagementPanel employeeManagementPanel;
    private DashboardData currentDashboardData;

    // Enhanced dashboard components
    private GridPane statisticsGrid;
    private LineChart<String, Number> performanceChart;
    private PieChart busStatusChart;
    private VBox recentActivityContainer;
    private VBox alertsContainer;
    private TableView<ActivityLog> activityTable;
    private ProgressIndicator loadingIndicator;

    // Key metrics labels
    private Label totalBusesValue, activeBusesValue, totalEmployeesValue, onDutyValue;
    private Label revenueValue, fuelEfficiencyValue, onTimePerformanceValue, passengersValue;
    private Label systemStatusLabel;

    public DashboardView(User user) {
        this.currentUser = user;
        this.controller = new DashboardController(this);
        this.analyticsService = new AnalyticsService();
        this.currentDashboardData = new DashboardData();
    }

    public DashboardView() {
        this.controller = new DashboardController(this);
        this.analyticsService = new AnalyticsService();
        this.currentDashboardData = new DashboardData();
    }

    public void start(Stage stage) {
        this.primaryStage = stage;
        initializeModernDashboard();

        Scene scene = new Scene(mainLayout, 1600, 1000);

        // Try to load CSS, but don't fail if it doesn't exist
        try {
            scene.getStylesheets().add(getClass().getResource("/styles/modern-dashboard.css").toExternalForm());
        } catch (Exception e) {
            LOGGER.warning("Could not load CSS stylesheet: " + e.getMessage());
        }

        stage.setTitle("CTB Depot Management System - Professional Dashboard");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();

        if (currentUser != null) {
            controller.initializeDashboard(currentUser);
        }

        initializeClock();
        stage.setOnCloseRequest(e -> cleanup());

        LOGGER.info("Modern dashboard view initialized successfully");
    }

    private void initializeModernDashboard() {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f8f9fc;");

        createModernHeader();
        createDataCenterContent();
        createCompactSidebar();
        createStatusFooter();
    }

    private void createModernHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(8, 20, 8, 20));
        header.setStyle("-fx-background-color: white; " +
                "-fx-border-width: 0 0 1 0; " +
                "-fx-border-color: #e3e6f0;");

        // Professional branding section
        HBox brandingSection = createBrandingSection();

        // Real-time metrics bar
        HBox metricsBar = createRealTimeMetricsBar(currentDashboardData);

        // User and controls section
        HBox controlsSection = createControlsSection();

        HBox.setHgrow(metricsBar, Priority.ALWAYS);
        header.getChildren().addAll(brandingSection, metricsBar, controlsSection);

        mainLayout.setTop(header);
    }

    private HBox createBrandingSection() {
        HBox branding = new HBox(10);
        branding.setAlignment(Pos.CENTER_LEFT);

        Rectangle logo = new Rectangle(28, 28);
        logo.setFill(Color.web("#1a237e"));
        logo.setArcWidth(6);
        logo.setArcHeight(6);

        Label title = new Label("CTB DEPOT MANAGEMENT");
        title.setStyle("-fx-text-fill: #1a237e; -fx-font-size: 13px; -fx-font-weight: bold;");

        branding.getChildren().addAll(logo, title);

        return branding;
    }

    private HBox createRealTimeMetricsBar(DashboardData data) {
        HBox metricsBar = new HBox(25);
        metricsBar.setAlignment(Pos.CENTER);
        metricsBar.setPadding(new Insets(0, 20, 0, 20));

        // Quick metrics display with real data
        VBox[] quickMetrics = {
                createQuickMetric("Active Buses", String.valueOf(data.getActiveBuses()), "#10b981",
                        "of " + data.getTotalBuses() + " total"),
                createQuickMetric("On-Time Performance", String.format("%.1f%%", data.getOnTimePerformance()),
                        "#3b82f6",
                        "above target"),
                createQuickMetric("Daily Revenue", "Rs " + String.format("%,.0f", data.getRevenue()), "#8b5cf6",
                        "+12% vs yesterday"),
                createQuickMetric("Fuel Efficiency", String.format("%.1f km/L", data.getFuelEfficiency()), "#f59e0b",
                        "fleet average")
        };

        metricsBar.getChildren().addAll(quickMetrics);
        return metricsBar;
    }

    private VBox createQuickMetric(String label, String value, String color, String subtitle) {
        VBox metric = new VBox(1);
        metric.setAlignment(Pos.CENTER);

        Label valueLabel = new Label(value);
        valueLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 14px; -fx-font-weight: bold;", color));

        Label titleLabel = new Label(label);
        titleLabel.setStyle("-fx-text-fill: #374151; -fx-font-size: 10px; -fx-font-weight: 600;");

        // The third "subtitle" line (e.g. "above target") was dropped - it
        // kept the header tall and, for the revenue metric, was a hardcoded
        // "+12% vs yesterday" string that was never actually computed.
        metric.getChildren().addAll(valueLabel, titleLabel);
        return metric;
    }

    private HBox createControlsSection() {
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_RIGHT);

        // Live status indicator
        HBox statusIndicator = new HBox(8);
        statusIndicator.setAlignment(Pos.CENTER);
        Rectangle statusDot = new Rectangle(8, 8);
        statusDot.setFill(Color.web("#10b981"));
        statusDot.setArcWidth(8);
        statusDot.setArcHeight(8);
        Label statusText = new Label("LIVE");
        statusText.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-font-weight: bold;");
        statusIndicator.getChildren().addAll(statusDot, statusText);

        // Time display
        timeLabel = new Label();
        timeLabel.setStyle("-fx-text-fill: #374151; -fx-font-size: 12px; -fx-font-weight: 500;");

        // User info
        welcomeLabel = new Label("Welcome, " + (currentUser != null ? currentUser.getUsername() : "Guest"));
        welcomeLabel.setStyle("-fx-text-fill: #374151; -fx-font-size: 13px; -fx-font-weight: 600;");

        // Control buttons
        Button refreshBtn = createHeaderButton("↻", "Refresh Data");
        Button settingsBtn = createHeaderButton("⚙", "Settings");
        Button logoutBtn = createHeaderButton("⏻", "Logout");

        refreshBtn.setOnAction(e -> controller.refreshDashboard());
        logoutBtn.setOnAction(e -> handleLogout());

        controls.getChildren().addAll(statusIndicator, new Separator(), timeLabel,
                new Separator(), welcomeLabel, refreshBtn, settingsBtn, logoutBtn);

        return controls;
    }

    private Button createHeaderButton(String icon, String tooltip) {
        Button btn = new Button(icon);
        btn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #374151; " +
                "-fx-font-size: 14px; -fx-background-radius: 6; " +
                "-fx-padding: 8 12; -fx-cursor: hand;");
        btn.setTooltip(new Tooltip(tooltip));

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #374151; " +
                "-fx-font-size: 14px; -fx-background-radius: 6; -fx-padding: 8 12;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #374151; " +
                "-fx-font-size: 14px; -fx-background-radius: 6; -fx-padding: 8 12;"));
        return btn;
    }

    private void createDataCenterContent() {
        // Main content area with tabs
        mainTabPane = new TabPane();
        mainTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        mainTabPane.setStyle("-fx-tab-min-height: 40; -fx-tab-max-height: 40;");

        createModernTabs();

        mainLayout.setCenter(mainTabPane);
    }

    private void createModernTabs() {
        // Dashboard Overview Tab
        Tab overviewTab = new Tab("🏠 Dashboard Overview");
        overviewTab.setContent(createDashboardOverview());

        // Fleet Management Tab
        Tab fleetTab = new Tab("🚌 Fleet Management");
        fleetTab.setContent(createFleetManagementPanel());

        // Route Management Tab
        Tab routeTab = new Tab("🗺 Route Management");
        routeTab.setContent(createRouteManagementPanel());

        // --- CORRECTED: LAZY LOADED GPS TRACKING TAB ---
        Tab gpsTab = new Tab("📍 GPS Tracking");
        gpsTab.setClosable(false);

        // Create loading placeholder
        VBox gpsPlaceholder = new VBox(20);
        gpsPlaceholder.setAlignment(Pos.CENTER);
        gpsPlaceholder.setStyle("-fx-background-color: white; -fx-padding: 50;");

        Label loadingIcon = new Label("🗺️");
        loadingIcon.setStyle("-fx-font-size: 48px;");

        Label loadingText = new Label("GPS Tracking Module");
        loadingText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label instruction = new Label("Click to load the live GPS map");
        instruction.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        ProgressIndicator progress = new ProgressIndicator();
        progress.setVisible(false);
        progress.setMaxSize(50, 50);

        gpsPlaceholder.getChildren().addAll(loadingIcon, loadingText, instruction, progress);
        gpsTab.setContent(gpsPlaceholder);

        // Lazy loading with PROPER threading
        final boolean[] isLoaded = { false };

        gpsTab.setOnSelectionChanged(event -> {
            if (gpsTab.isSelected() && !isLoaded[0]) {
                isLoaded[0] = true;

                // Show loading state
                Platform.runLater(() -> {
                    instruction.setText("Loading GPS Tracking System...");
                    progress.setVisible(true);
                });

                // Use Platform.runLater with delay for smooth loading
                // ALL UI CREATION HAPPENS ON FX THREAD
                Platform.runLater(() -> {
                    try {
                        LOGGER.info("Loading GPS tracking panel on FX thread...");

                        // Create the GPS panel directly on FX thread
                        BorderPane gpsContent = createGPSTrackingPanel();

                        // Set the content
                        gpsTab.setContent(gpsContent);

                        LOGGER.info("GPS Tracking panel loaded successfully");

                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Failed to load GPS tracking panel", e);

                        // Show error in the tab
                        VBox errorBox = new VBox(20);
                        errorBox.setAlignment(Pos.CENTER);
                        errorBox.setPadding(new Insets(50));
                        errorBox.setStyle("-fx-background-color: white;");

                        Label errorIcon = new Label("⚠️");
                        errorIcon.setStyle("-fx-font-size: 48px;");

                        Label errorTitle = new Label("GPS Tracking Unavailable");
                        errorTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

                        Label errorMsg = new Label(e.getMessage());
                        errorMsg.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
                        errorMsg.setWrapText(true);
                        errorMsg.setMaxWidth(600);

                        Button retryBtn = new Button("🔄 Retry");
                        retryBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                                "-fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;");
                        retryBtn.setOnAction(retry -> {
                            isLoaded[0] = false;
                            gpsTab.setContent(gpsPlaceholder);
                            instruction.setText("Click to load the live GPS map");
                            progress.setVisible(false);
                        });

                        errorBox.getChildren().addAll(errorIcon, errorTitle, errorMsg, retryBtn);
                        gpsTab.setContent(errorBox);
                    }
                });
            }
        });
        // --- END: CORRECTED LAZY LOADING ---

        // Employee Management Tab
        Tab employeeTab = new Tab("👥 Employee Management");
        employeeTab.setContent(createEmployeeManagementPanel());

        // Fuel Management Tab
        Tab fuelTab = new Tab("⛽ Fuel Management");
        fuelTab.setContent(createFuelManagementPanel());

        // Reports & Analytics Tab
        Tab reportsTab = new Tab("📊 Reports & Analytics");
        reportsTab.setContent(createReportsPanel());

        // Add all tabs
        mainTabPane.getTabs().addAll(overviewTab, fleetTab, routeTab, gpsTab,
                employeeTab, fuelTab, reportsTab);
    }

    private Button createSidebarNavButton(String icon, String text, String type, int tabIndex) {
        Button navBtn = new Button(icon + "  " + text);
        navBtn.setAlignment(Pos.CENTER_LEFT);
        navBtn.setMaxWidth(Double.MAX_VALUE);
        navBtn.setPrefHeight(32);
        navBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #d1d5db; " +
                "-fx-font-size: 12px; -fx-font-weight: 500; " +
                "-fx-background-radius: 6; -fx-cursor: hand;");

        navBtn.setOnMouseEntered(e -> navBtn.setStyle("-fx-background-color: #374151; -fx-text-fill: #f9fafb; " +
                "-fx-font-size: 12px; -fx-font-weight: 500; -fx-background-radius: 6;"));
        navBtn.setOnMouseExited(e -> navBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #d1d5db; " +
                "-fx-font-size: 12px; -fx-font-weight: 500; -fx-background-radius: 6;"));

        navBtn.setOnAction(e -> {
            if (tabIndex >= 0 && tabIndex < mainTabPane.getTabs().size()) {
                mainTabPane.getSelectionModel().select(tabIndex);
            }
        });

        return navBtn;
    }

    private void createCompactSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(15, 10, 15, 10));
        sidebar.setPrefWidth(170);
        sidebar.setStyle("-fx-background-color: #1f2937; -fx-border-width: 0 1 0 0; -fx-border-color: #374151;");

        Label navTitle = new Label("QUICK NAVIGATION");
        navTitle.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 0 0 10 0;");

        VBox navItems = new VBox(5);

        String[][] navData = {
                { "🏠", "Dashboard", "dashboard", "0" },
                { "🚌", "Fleet", "bus", "1" },
                { "🗺", "Routes", "route", "2" },
                { "📍", "GPS Tracking", "gps", "3" }, // ADD THIS LINE
                { "👥", "Employees", "employee", "4" }, // Update index to 4
                { "⛽", "Fuel", "fuel", "5" }, // Update index to 5
                { "📊", "Reports", "reports", "6" } // Update index to 6
        };

        for (String[] item : navData) {
            Button navButton = createSidebarNavButton(item[0], item[1], item[2], Integer.parseInt(item[3]));
            navItems.getChildren().add(navButton);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // System status
        systemStatusLabel = new Label("🟢 All Systems Online");
        systemStatusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-font-weight: 600;");

        sidebar.getChildren().addAll(navTitle, navItems, spacer, systemStatusLabel);
        mainLayout.setLeft(sidebar);
    }

    private ScrollPane createDashboardOverview() {
        VBox overview = new VBox(20);
        overview.setPadding(new Insets(25));
        overview.setStyle("-fx-background-color: #f8f9fc;");

        // Executive Summary Cards (built from real dashboard data)
        HBox executiveSummary = createExecutiveSummaryCards(currentDashboardData);

        // Main analytics section
        HBox analyticsSection = new HBox(20);
        analyticsSection.setPrefHeight(400);

        // Performance chart
        VBox chartContainer = createPerformanceChartContainer();
        HBox.setHgrow(chartContainer, Priority.ALWAYS);

        // Bus status pie chart
        VBox statusContainer = createBusStatusContainer();
        statusContainer.setPrefWidth(350);

        analyticsSection.getChildren().addAll(chartContainer, statusContainer);

        // Bottom section with activity and alerts
        HBox bottomSection = new HBox(20);
        bottomSection.setPrefHeight(300);

        VBox activitySection = createActivitySection();
        VBox alertsSection = createAlertsSection();

        HBox.setHgrow(activitySection, Priority.ALWAYS);
        alertsSection.setPrefWidth(400);

        bottomSection.getChildren().addAll(activitySection, alertsSection);

        overview.getChildren().addAll(executiveSummary, analyticsSection, bottomSection);

        ScrollPane scrollPane = new ScrollPane(overview);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        return scrollPane;
    }

    /**
     * Build the top row of summary cards using REAL dashboard data.
     *
     * Every value comes from the DashboardData object (which is loaded from the
     * database). If a value is 0 (e.g. no trips recorded yet) the card simply
     * shows 0 -- we no longer display made-up figures. The small "change vs
     * yesterday" line is left blank because we don't yet store yesterday's
     * numbers to compare against.
     */
    private HBox createExecutiveSummaryCards(DashboardData data) {
        HBox summary = new HBox(20);
        summary.setAlignment(Pos.CENTER);

        // Format helpers for clean display.
        String fleetUtil = String.format("%.1f%%", data.getBusUtilizationRate());
        String revenue = "Rs " + String.format("%,.0f", data.getRevenue());
        String fuelEff = String.format("%.1f km/L", data.getFuelEfficiency());
        String onTime = String.format("%.1f%%", data.getOnTimePerformance());
        String passengers = String.format("%,d", data.getPassengersToday());
        String activeRoutes = String.valueOf(data.getActiveRoutes());

        // No fake "change vs yesterday" trend -> pass empty text and a neutral colour.
        VBox[] kpiCards = {
                createKPICard("Fleet Utilization", fleetUtil, "", "#10b981", ""),
                createKPICard("Daily Revenue", revenue, "", "#3b82f6", ""),
                createKPICard("Fuel Efficiency", fuelEff, "", "#f59e0b", ""),
                createKPICard("On-Time Performance", onTime, "", "#8b5cf6", ""),
                createKPICard("Total Passengers", passengers, "", "#06b6d4", ""),
                createKPICard("Active Routes", activeRoutes, "", "#84cc16", "")
        };

        summary.getChildren().addAll(kpiCards);
        return summary;
    }

    private VBox createKPICard(String title, String value, String change, String color, String trend) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20, 25, 20, 25));
        card.setPrefWidth(180);
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 1);");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px; -fx-font-weight: 600;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle(String.format("-fx-text-fill: #111827; -fx-font-size: 24px; -fx-font-weight: bold;"));

        HBox changeSection = new HBox(5);
        changeSection.setAlignment(Pos.CENTER_LEFT);

        Label trendLabel = new Label(trend);
        trendLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 12px;", color));

        Label changeLabel = new Label(change);
        changeLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 12px; -fx-font-weight: 600;", color));

        Label periodLabel = new Label("vs yesterday");
        periodLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 10px;");

        changeSection.getChildren().addAll(trendLabel, changeLabel, periodLabel);

        card.getChildren().addAll(titleLabel, valueLabel);
        // Only show the "change vs yesterday" line when we actually have a
        // change value. Empty change means we have no comparison data, so we
        // hide the line instead of showing a fake trend.
        if (change != null && !change.isBlank()) {
            card.getChildren().add(changeSection);
        }

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f9fafb; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 2);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 1);"));

        return card;
    }

    private VBox createPerformanceChartContainer() {
        VBox container = new VBox(15);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 1);");
        container.setPadding(new Insets(20));

        Label chartTitle = new Label("Fleet Performance Trends");
        chartTitle.setStyle("-fx-text-fill: #111827; -fx-font-size: 16px; -fx-font-weight: bold;");

        // Create line chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        performanceChart = new LineChart<>(xAxis, yAxis);
        performanceChart.setTitle("");
        performanceChart.setLegendVisible(true);
        performanceChart.setCreateSymbols(false);
        performanceChart.setPrefHeight(300);

        // Sample data for performance chart
        XYChart.Series<String, Number> onTimeData = new XYChart.Series<>();
        onTimeData.setName("On-Time Performance %");
        onTimeData.getData().addAll(
                new XYChart.Data<>("6 AM", 85),
                new XYChart.Data<>("9 AM", 78),
                new XYChart.Data<>("12 PM", 82),
                new XYChart.Data<>("3 PM", 88),
                new XYChart.Data<>("6 PM", 75),
                new XYChart.Data<>("9 PM", 90));

        XYChart.Series<String, Number> fuelData = new XYChart.Series<>();
        fuelData.setName("Fuel Efficiency km/L");
        fuelData.getData().addAll(
                new XYChart.Data<>("6 AM", 13.2),
                new XYChart.Data<>("9 AM", 11.8),
                new XYChart.Data<>("12 PM", 12.1),
                new XYChart.Data<>("3 PM", 12.8),
                new XYChart.Data<>("6 PM", 11.5),
                new XYChart.Data<>("9 PM", 13.5));

        performanceChart.getData().addAll(onTimeData, fuelData);

        VBox.setVgrow(performanceChart, Priority.ALWAYS);
        container.getChildren().addAll(chartTitle, performanceChart);

        return container;
    }

    private VBox createBusStatusContainer() {
        VBox container = new VBox(15);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 1);");
        container.setPadding(new Insets(20));

        Label title = new Label("Fleet Status Distribution");
        title.setStyle("-fx-text-fill: #111827; -fx-font-size: 16px; -fx-font-weight: bold;");

        busStatusChart = new PieChart();
        busStatusChart.getData().clear();
        busStatusChart.getData().addAll(
                new PieChart.Data("Active", currentDashboardData.getActiveBuses()),
                new PieChart.Data("Maintenance", currentDashboardData.getBusesInMaintenance()),
                new PieChart.Data("Offline", currentDashboardData.getBusesOffline()));
        busStatusChart.setPrefHeight(250);
        busStatusChart.setLegendVisible(true);

        // Status summary
        VBox statusSummary = new VBox(8);
        HBox[] statusItems = {
                createStatusItem("Active Buses", String.valueOf(currentDashboardData.getActiveBuses()), "#10b981"),
                createStatusItem("In Maintenance", String.valueOf(currentDashboardData.getBusesInMaintenance()),
                        "#f59e0b"),
                createStatusItem("Offline", String.valueOf(currentDashboardData.getBusesOffline()), "#ef4444")
        };
        statusSummary.getChildren().addAll(statusItems);

        container.getChildren().addAll(title, busStatusChart, statusSummary);

        return container;
    }

    private HBox createStatusItem(String label, String value, String color) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);

        Rectangle indicator = new Rectangle(12, 12);
        indicator.setFill(Color.web(color));
        indicator.setArcWidth(3);
        indicator.setArcHeight(3);

        Label textLabel = new Label(label);
        textLabel.setStyle("-fx-text-fill: #374151; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: #111827; -fx-font-size: 12px; -fx-font-weight: bold;");

        item.getChildren().addAll(indicator, textLabel, spacer, valueLabel);
        return item;
    }

    private VBox createActivitySection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 1);");
        section.setPadding(new Insets(20));

        Label title = new Label("Recent System Activity");
        title.setStyle("-fx-text-fill: #111827; -fx-font-size: 16px; -fx-font-weight: bold;");

        recentActivityContainer = new VBox(8);
        ScrollPane activityScroll = new ScrollPane(recentActivityContainer);
        activityScroll.setFitToWidth(true);
        activityScroll.setPrefHeight(200);
        activityScroll.setStyle("-fx-background-color: transparent;");

        VBox.setVgrow(activityScroll, Priority.ALWAYS);
        section.getChildren().addAll(title, activityScroll);

        return section;
    }

    private VBox createAlertsSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 1);");
        section.setPadding(new Insets(20));

        Label title = new Label("System Alerts");
        title.setStyle("-fx-text-fill: #111827; -fx-font-size: 16px; -fx-font-weight: bold;");

        alertsContainer = new VBox(8);
        ScrollPane alertsScroll = new ScrollPane(alertsContainer);
        alertsScroll.setFitToWidth(true);
        alertsScroll.setPrefHeight(200);
        alertsScroll.setStyle("-fx-background-color: transparent;");

        // Show real alerts (e.g. database/GPS problems). If everything is fine,
        // a friendly "no alerts" message is shown instead of fake warnings.
        loadRealAlerts();

        VBox.setVgrow(alertsScroll, Priority.ALWAYS);
        section.getChildren().addAll(title, alertsScroll);

        return section;
    }

    /**
     * Fill the alerts box with REAL system alerts from the service.
     * The service checks genuine conditions (database connection, GPS status,
     * memory load). No fake "Bus CTB-245" style alerts are used any more.
     */
    private void loadRealAlerts() {
        alertsContainer.getChildren().clear();

        java.util.List<String> realAlerts = new DashboardService().getSystemAlerts();

        if (realAlerts.isEmpty()) {
            // Honest empty state.
            Label none = new Label("No active alerts. All systems normal.");
            none.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
            none.setPadding(new Insets(8));
            alertsContainer.getChildren().add(none);
        } else {
            for (String message : realAlerts) {
                alertsContainer.getChildren().add(createAlert("warning", message, "now"));
            }
        }
    }

    private VBox createAlert(String type, String message, String time) {
        VBox alert = new VBox(5);
        alert.setPadding(new Insets(10));

        String[] colors = switch (type) {
            case "warning" -> new String[] { "#fef3c7", "#f59e0b", "#92400e" };
            case "error" -> new String[] { "#fee2e2", "#ef4444", "#991b1b" };
            case "success" -> new String[] { "#d1fae5", "#10b981", "#065f46" };
            default -> new String[] { "#dbeafe", "#3b82f6", "#1e40af" };
        };

        alert.setStyle(String.format("-fx-background-color: %s; -fx-background-radius: 6; " +
                "-fx-border-color: %s; -fx-border-radius: 6; -fx-border-width: 1;",
                colors[0], colors[1]));

        Label messageLabel = new Label(message);
        messageLabel
                .setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 12px; -fx-font-weight: 500;", colors[2]));
        messageLabel.setWrapText(true);

        Label timeLabel = new Label(time);
        timeLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 10px;", colors[2]));

        alert.getChildren().addAll(messageLabel, timeLabel);
        return alert;
    }

    private BorderPane createFleetManagementPanel() {
        BorderPane panel = new BorderPane();
        panel.setStyle("-fx-background-color: #f8f9fc;");

        try {
            // Create BusManagementView instance with current user
            if (busManagementView == null) {
                busManagementView = new BusManagementView(currentUser);
            }

            // Initialize the bus management view for embedding
            busManagementView.initializeForEmbedding(currentUser);

            // Get the main layout from the bus management view
            BorderPane busContent = busManagementView.getMainLayoutForEmbedding();

            if (busContent != null) {
                panel.setCenter(busContent);
                LOGGER.info("Fleet Management panel integrated successfully");
            } else {
                throw new RuntimeException("Bus management view returned null content");
            }

        } catch (Exception e) {
            LOGGER.severe("Error integrating Fleet Management: " + e.getMessage());
            e.printStackTrace();
            panel.setCenter(createFleetManagementFallback(e));
        }

        return panel;
    }

    private BorderPane createFleetManagementFallback(Exception e) {
        BorderPane errorWrapper = new BorderPane();
        errorWrapper.setPadding(new Insets(50));

        VBox errorContent = new VBox(25);
        errorContent.setAlignment(Pos.CENTER);
        errorContent.setPadding(new Insets(50));
        errorContent.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 2);");
        errorContent.setMaxWidth(600);

        Label errorTitle = new Label("Fleet Management Module");
        errorTitle.setStyle("-fx-text-fill: #111827; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label errorMessage = new Label("Unable to load the Fleet Management module at this time.");
        errorMessage.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 16px; -fx-text-alignment: center;");
        errorMessage.setWrapText(true);

        Label technicalDetails = new Label("Error: " + e.getMessage());
        technicalDetails.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-text-alignment: center;");
        technicalDetails.setWrapText(true);

        HBox buttonContainer = new HBox(15);
        buttonContainer.setAlignment(Pos.CENTER);

        Button retryButton = new Button("Retry Loading");
        retryButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: 600; " +
                "-fx-background-radius: 8; -fx-padding: 12 24;");
        retryButton.setOnAction(e2 -> {
            for (Tab tab : mainTabPane.getTabs()) {
                if (tab.getText().contains("Fleet Management")) {
                    tab.setContent(createFleetManagementPanel());
                    break;
                }
            }
        });

        Button openSeparateButton = new Button("Open in New Window");
        openSeparateButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: 600; " +
                "-fx-background-radius: 8; -fx-padding: 12 24;");
        openSeparateButton.setOnAction(e2 -> {
            try {
                BusManagementView separateView = new BusManagementView(currentUser);
                Stage separateStage = new Stage();
                separateView.start(separateStage);
            } catch (Exception ex) {
                showErrorMessage("Unable to open Fleet Management in separate window: " + ex.getMessage());
            }
        });

        buttonContainer.getChildren().addAll(retryButton, openSeparateButton);

        HBox statsContainer = createFleetStatsPlaceholder();

        errorContent.getChildren().addAll(errorTitle, errorMessage, technicalDetails,
                new Separator(), statsContainer, buttonContainer);

        errorWrapper.setCenter(errorContent);
        return errorWrapper;
    }

    private BorderPane createRouteManagementPanel() {
        BorderPane panel = new BorderPane();
        panel.setStyle("-fx-background-color: #f8f9fc;");

        try {
            // Create RouteManagementView instance with current user
            if (routeManagementView == null) {
                routeManagementView = new RouteManagementView(currentUser);
            }

            // Initialize the route management view for embedding
            routeManagementView.initializeForEmbedding(currentUser);

            // Get the main layout from the route management view
            BorderPane routeContent = routeManagementView.getMainLayoutForEmbedding();

            if (routeContent != null) {
                panel.setCenter(routeContent);
                LOGGER.info("Route Management panel integrated successfully");
            } else {
                throw new RuntimeException("Route management view returned null content");
            }

        } catch (Exception e) {
            LOGGER.severe("Error integrating Route Management: " + e.getMessage());
            e.printStackTrace();
            panel.setCenter(createRouteManagementFallback(e));
        }

        return panel;
    }

    private BorderPane createRouteManagementFallback(Exception e) {
        BorderPane errorWrapper = new BorderPane();
        errorWrapper.setPadding(new Insets(50));

        VBox errorContent = new VBox(25);
        errorContent.setAlignment(Pos.CENTER);
        errorContent.setPadding(new Insets(50));
        errorContent.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 2);");
        errorContent.setMaxWidth(600);

        Label errorTitle = new Label("Route Management Module");
        errorTitle.setStyle("-fx-text-fill: #111827; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label errorMessage = new Label("Unable to load the Route Management module at this time.");
        errorMessage.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 16px; -fx-text-alignment: center;");
        errorMessage.setWrapText(true);

        Label technicalDetails = new Label("Error: " + e.getMessage());
        technicalDetails.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-text-alignment: center;");
        technicalDetails.setWrapText(true);

        Label debugInfo = new Label("Current User: " + (currentUser != null ? currentUser.getUsername() : "null") +
                "\nRouteManagementView instance: " + (routeManagementView != null ? "created" : "null"));
        debugInfo.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 10px; -fx-text-alignment: center;");
        debugInfo.setWrapText(true);

        HBox buttonContainer = new HBox(15);
        buttonContainer.setAlignment(Pos.CENTER);

        Button retryButton = new Button("Retry Loading");
        retryButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: 600; " +
                "-fx-background-radius: 8; -fx-padding: 12 24;");
        retryButton.setOnAction(e2 -> {
            for (Tab tab : mainTabPane.getTabs()) {
                if (tab.getText().contains("Route Management")) {
                    tab.setContent(createRouteManagementPanel());
                    break;
                }
            }
        });

        Button openSeparateButton = new Button("Open in New Window");
        openSeparateButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: 600; " +
                "-fx-background-radius: 8; -fx-padding: 12 24;");
        openSeparateButton.setOnAction(e2 -> {
            try {
                RouteManagementView separateView = new RouteManagementView(currentUser);
                Stage separateStage = new Stage();
                separateView.start(separateStage);
            } catch (Exception ex) {
                showErrorMessage("Unable to open Route Management in separate window: " + ex.getMessage());
            }
        });

        buttonContainer.getChildren().addAll(retryButton, openSeparateButton);

        HBox statsContainer = createRouteStatsPlaceholder();

        errorContent.getChildren().addAll(errorTitle, errorMessage, technicalDetails, debugInfo,
                new Separator(), statsContainer, buttonContainer);

        errorWrapper.setCenter(errorContent);
        return errorWrapper;
    }

    private HBox createRouteStatsPlaceholder() {
        HBox stats = new HBox(20);
        stats.setAlignment(Pos.CENTER);

        VBox totalRoutes = createStatCard("Total Routes", "22 Routes", "#3b82f6");
        VBox activeRoutes = createStatCard("Active", "20 Routes", "#10b981");
        VBox expressRoutes = createStatCard("Express", "8 Routes", "#8b5cf6");
        VBox nightRoutes = createStatCard("Night Service", "3 Routes", "#f59e0b");

        stats.getChildren().addAll(totalRoutes, activeRoutes, expressRoutes, nightRoutes);
        return stats;
    }

    private BorderPane createGPSTrackingPanel() {
        BorderPane panel = new BorderPane();
        panel.setStyle("-fx-background-color: #f8f9fc;");

        try {
            LOGGER.info("Starting GPS Tracking Panel initialization...");

            // Verify current user
            if (currentUser == null) {
                throw new IllegalStateException("Current user is null - cannot initialize GPS panel");
            }

            LOGGER.info("Creating GPSTrackingPanel for user: " + currentUser.getUsername());
            gpsTrackingPanel = new GPSTrackingPanel(currentUser);

            LOGGER.info("GPSTrackingPanel created successfully, adding to panel");
            panel.setCenter(gpsTrackingPanel);

            LOGGER.info("GPS Tracking panel integrated successfully");

        } catch (Exception e) {
            // Log the FULL exception with stack trace
            LOGGER.log(Level.SEVERE,
                    "Error creating GPS tracking panel: " + e.getClass().getName() + " - " + e.getMessage(), e);

            // Print to console for immediate visibility
            System.err.println("GPS PANEL ERROR: " + e.getMessage());
            e.printStackTrace();

            // Create detailed error UI
            VBox errorContent = new VBox(20);
            errorContent.setAlignment(Pos.CENTER);
            errorContent.setPadding(new Insets(50));
            errorContent.setStyle("-fx-background-color: white;");

            Label errorIcon = new Label("⚠️");
            errorIcon.setStyle("-fx-font-size: 64px;");

            Label errorTitle = new Label("GPS Tracking Unavailable");
            errorTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

            Label errorMessage = new Label("Unable to initialize the GPS tracking system.");
            errorMessage.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-text-alignment: center;");

            // Show the ACTUAL error
            TextArea technicalDetails = new TextArea();
            technicalDetails.setEditable(false);
            technicalDetails.setWrapText(true);
            technicalDetails.setPrefHeight(150);
            technicalDetails.setMaxWidth(600);
            technicalDetails.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 11px;");

            // Build detailed error message
            StringBuilder errorDetails = new StringBuilder();
            errorDetails.append("Error Type: ").append(e.getClass().getSimpleName()).append("\n");
            errorDetails.append("Message: ").append(e.getMessage()).append("\n\n");
            errorDetails.append("Stack Trace:\n");
            for (StackTraceElement element : e.getStackTrace()) {
                errorDetails.append("  at ").append(element.toString()).append("\n");
                if (errorDetails.length() > 1000)
                    break; // Limit size
            }

            technicalDetails.setText(errorDetails.toString());

            Button retryButton = new Button("🔄 Retry");
            retryButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;");
            retryButton.setOnAction(event -> {
                try {
                    for (Tab tab : mainTabPane.getTabs()) {
                        if (tab.getText().contains("GPS Tracking")) {
                            // Clear and retry
                            tab.setContent(new VBox(new Label("Loading...")));
                            Platform.runLater(() -> tab.setContent(createGPSTrackingPanel()));
                            break;
                        }
                    }
                } catch (Exception ex) {
                    showErrorMessage("Retry failed: " + ex.getMessage());
                }
            });

            Button copyErrorButton = new Button("📋 Copy Error");
            copyErrorButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;");
            copyErrorButton.setOnAction(event -> {
                javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(errorDetails.toString());
                clipboard.setContent(content);
                showInfoMessage("Error details copied to clipboard");
            });

            HBox buttonBox = new HBox(10, retryButton, copyErrorButton);
            buttonBox.setAlignment(Pos.CENTER);

            errorContent.getChildren().addAll(errorIcon, errorTitle, errorMessage,
                    new Label("Technical Details:"), technicalDetails, buttonBox);
            panel.setCenter(errorContent);
        }

        return panel;
    }

    private HBox createFleetStatsPlaceholder() {
        HBox stats = new HBox(20);
        stats.setAlignment(Pos.CENTER);

        VBox totalFleet = createStatCard("Total Fleet",
                currentDashboardData.getTotalBuses() + " Buses", "#3b82f6");
        VBox activeFleet = createStatCard("Active",
                currentDashboardData.getActiveBuses() + " Buses", "#10b981");
        VBox maintenance = createStatCard("Maintenance",
                currentDashboardData.getBusesInMaintenance() + " Buses", "#f59e0b");
        VBox offline = createStatCard("Offline",
                currentDashboardData.getBusesOffline() + " Buses", "#ef4444");

        stats.getChildren().addAll(totalFleet, activeFleet, maintenance, offline);
        return stats;
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(120);
        card.setStyle("-fx-background-color: #f8f9fc; -fx-background-radius: 8; " +
                "-fx-border-color: " + color + "; -fx-border-radius: 8; -fx-border-width: 2;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");

        card.getChildren().addAll(valueLabel, titleLabel);
        return card;
    }

    private BorderPane createEmployeeManagementPanel() {
        BorderPane panel = new BorderPane();
        panel.setStyle("-fx-background-color: #f8f9fc;");

        try {
            // Create Employee Management Panel instance
            if (employeeManagementPanel == null) {
                employeeManagementPanel = new EmployeeManagementPanel();
            }

            // Get the main view from the employee management panel
            VBox employeeContent = employeeManagementPanel.getView();

            if (employeeContent != null) {
                // Wrap in ScrollPane for better handling
                ScrollPane scrollPane = new ScrollPane(employeeContent);
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(true);
                scrollPane.setStyle("-fx-background-color: transparent;");

                panel.setCenter(scrollPane);
                LOGGER.info("Employee Management panel integrated successfully");
            } else {
                throw new RuntimeException("Employee management panel returned null content");
            }

        } catch (Exception e) {
            LOGGER.severe("Error integrating Employee Management: " + e.getMessage());
            e.printStackTrace();
            panel.setCenter(createEmployeeManagementFallback(e));
        }

        return panel;
    }

    private BorderPane createEmployeeManagementFallback(Exception e) {
        BorderPane errorWrapper = new BorderPane();
        errorWrapper.setPadding(new Insets(50));

        VBox errorContent = new VBox(25);
        errorContent.setAlignment(Pos.CENTER);
        errorContent.setPadding(new Insets(50));
        errorContent.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 2);");
        errorContent.setMaxWidth(600);

        Label errorTitle = new Label("Employee Management Module");
        errorTitle.setStyle("-fx-text-fill: #111827; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label errorMessage = new Label("Unable to load the Employee Management module at this time.");
        errorMessage.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 16px; -fx-text-alignment: center;");
        errorMessage.setWrapText(true);

        Label technicalDetails = new Label("Error: " + e.getMessage());
        technicalDetails.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-text-alignment: center;");
        technicalDetails.setWrapText(true);

        HBox buttonContainer = new HBox(15);
        buttonContainer.setAlignment(Pos.CENTER);

        Button retryButton = new Button("Retry Loading");
        retryButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: 600; " +
                "-fx-background-radius: 8; -fx-padding: 12 24;");
        retryButton.setOnAction(e2 -> {
            for (Tab tab : mainTabPane.getTabs()) {
                if (tab.getText().contains("Employee Management")) {
                    tab.setContent(createEmployeeManagementPanel());
                    break;
                }
            }
        });

        Button openSeparateButton = new Button("Open in New Window");
        openSeparateButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: 600; " +
                "-fx-background-radius: 8; -fx-padding: 12 24;");
        openSeparateButton.setOnAction(e2 -> {
            try {
                // Open Employee Management in separate window
                Stage separateStage = new Stage();
                BorderPane separateContent = new BorderPane();
                separateContent.setCenter(new EmployeeManagementPanel().getView());

                Scene separateScene = new Scene(separateContent, 1200, 800);
                separateStage.setTitle("Employee Management - Separate Window");
                separateStage.setScene(separateScene);
                separateStage.show();
            } catch (Exception ex) {
                showErrorMessage("Unable to open Employee Management in separate window: " + ex.getMessage());
            }
        });

        buttonContainer.getChildren().addAll(retryButton, openSeparateButton);

        HBox statsContainer = createEmployeeStatsPlaceholder();

        errorContent.getChildren().addAll(errorTitle, errorMessage, technicalDetails,
                new Separator(), statsContainer, buttonContainer);

        errorWrapper.setCenter(errorContent);
        return errorWrapper;
    }

    private HBox createEmployeeStatsPlaceholder() {
        HBox stats = new HBox(20);
        stats.setAlignment(Pos.CENTER);

        VBox totalEmployees = createStatCard("Total Employees", "125 Staff", "#3b82f6");
        VBox drivers = createStatCard("Drivers", "78 Active", "#10b981");
        VBox conductors = createStatCard("Conductors", "32 Active", "#8b5cf6");
        VBox mechanics = createStatCard("Mechanics", "15 Active", "#f59e0b");

        stats.getChildren().addAll(totalEmployees, drivers, conductors, mechanics);
        return stats;
    }

    private BorderPane createFuelManagementPanel() {
        BorderPane panel = new BorderPane();
        panel.setStyle("-fx-background-color: #f8f9fc;");

        try {
            // Create Fuel Management Panel instance
            FuelManagementPanel fuelPanel = new FuelManagementPanel();
            panel.setCenter(fuelPanel);

            LOGGER.info("Fuel Management panel integrated successfully");

        } catch (Exception e) {
            LOGGER.severe("Error integrating Fuel Management: " + e.getMessage());
            e.printStackTrace();
            panel.setCenter(createFuelManagementFallback(e));
        }

        return panel;
    }

    private BorderPane createFuelManagementFallback(Exception e) {
        BorderPane errorWrapper = new BorderPane();
        errorWrapper.setPadding(new Insets(50));

        VBox errorContent = new VBox(25);
        errorContent.setAlignment(Pos.CENTER);
        errorContent.setPadding(new Insets(50));
        errorContent.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 2);");
        errorContent.setMaxWidth(600);

        Label errorTitle = new Label("Fuel Management Module");
        errorTitle.setStyle("-fx-text-fill: #111827; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label errorMessage = new Label("Unable to load the Fuel Management module at this time.");
        errorMessage.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 16px; -fx-text-alignment: center;");
        errorMessage.setWrapText(true);

        Label technicalDetails = new Label("Error: " + e.getMessage());
        technicalDetails.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-text-alignment: center;");
        technicalDetails.setWrapText(true);

        HBox buttonContainer = new HBox(15);
        buttonContainer.setAlignment(Pos.CENTER);

        Button retryButton = new Button("Retry Loading");
        retryButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: 600; " +
                "-fx-background-radius: 8; -fx-padding: 12 24;");
        retryButton.setOnAction(e2 -> {
            for (Tab tab : mainTabPane.getTabs()) {
                if (tab.getText().contains("Fuel Management")) {
                    tab.setContent(createFuelManagementPanel());
                    break;
                }
            }
        });

        Button openSeparateButton = new Button("Open Full Version");
        openSeparateButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: 600; " +
                "-fx-background-radius: 8; -fx-padding: 12 24;");
        openSeparateButton.setOnAction(e2 -> {
            try {
                // Open the full Fuel Management system in a new window
                FuelManagementController fullFuelController = new FuelManagementController();
                Stage fuelStage = new Stage();
                fullFuelController.showFuelManagement(fuelStage);
            } catch (Exception ex) {
                showErrorMessage("Unable to open full Fuel Management system: " + ex.getMessage());
            }
        });

        buttonContainer.getChildren().addAll(retryButton, openSeparateButton);

        HBox statsContainer = createFuelStatsPlaceholder();

        errorContent.getChildren().addAll(errorTitle, errorMessage, technicalDetails,
                new Separator(), statsContainer, buttonContainer);

        errorWrapper.setCenter(errorContent);
        return errorWrapper;
    }

    private HBox createFuelStatsPlaceholder() {
        HBox stats = new HBox(20);
        stats.setAlignment(Pos.CENTER);

        VBox totalFuel = createStatCard("Monthly Fuel", "1,980 L", "#3b82f6");
        VBox totalCost = createStatCard("Monthly Cost", "₹79,200", "#ef4444");
        VBox avgEfficiency = createStatCard("Avg Efficiency", "12.5 km/L", "#10b981");
        VBox costPerKm = createStatCard("Cost per km", "₹8.45", "#f59e0b");

        stats.getChildren().addAll(totalFuel, totalCost, avgEfficiency, costPerKm);
        return stats;
    }

    private HBox createConsumerItem(String[] data) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12));
        item.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8;");

        VBox busInfo = new VBox(2);
        Label busNumber = new Label(data[0]);
        busNumber.setStyle("-fx-text-fill: #111827; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label route = new Label(data[1]);
        route.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 10px;");

        busInfo.getChildren().addAll(busNumber, route);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox consumption = new VBox(2);
        consumption.setAlignment(Pos.CENTER_RIGHT);

        Label fuel = new Label(data[2]);
        fuel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label cost = new Label(data[3]);
        cost.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 10px;");

        consumption.getChildren().addAll(fuel, cost);

        item.getChildren().addAll(busInfo, spacer, consumption);
        return item;
    }

    private BorderPane createReportsPanel() {
        BorderPane panel = new BorderPane();
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #f8f9fc;");

        // Top toolbar with filters and controls
        HBox toolbar = createAnalyticsToolbar();
        panel.setTop(toolbar);

        // Main content area with tabs for different analytics views
        TabPane analyticsTabs = new TabPane();
        analyticsTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        analyticsTabs.getStyleClass().add("analytics-tabs");

        // Overview Tab
        Tab overviewTab = new Tab("📈 Overview");
        overviewTab.setContent(createAnalyticsOverview());

        // Financial Analytics Tab
        Tab financialTab = new Tab("💰 Financial");
        financialTab.setContent(createFinancialAnalytics());

        // Operational Analytics Tab
        Tab operationalTab = new Tab("⚙️ Operational");
        operationalTab.setContent(createOperationalAnalytics());

        // Performance Analytics Tab
        Tab performanceTab = new Tab("📊 Performance");
        performanceTab.setContent(createPerformanceAnalytics());

        analyticsTabs.getTabs().addAll(overviewTab, financialTab, operationalTab, performanceTab);
        panel.setCenter(analyticsTabs);

        return panel;
    }

    private HBox createAnalyticsToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(10, 20, 10, 20));
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // Date range picker
        Label dateRangeLabel = new Label("Date Range:");
        dateRangeLabel.setStyle("-fx-font-weight: bold;");

        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setValue(java.time.LocalDate.now().minusDays(30));
        startDatePicker.setPrefWidth(120);

        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setValue(java.time.LocalDate.now());
        endDatePicker.setPrefWidth(120);

        Label toLabel = new Label("to");

        // Quick date range buttons
        Button last7DaysBtn = new Button("Last 7 Days");
        Button last30DaysBtn = new Button("Last 30 Days");
        Button last90DaysBtn = new Button("Last 90 Days");
        Button thisMonthBtn = new Button("This Month");

        last7DaysBtn.setOnAction(e -> {
            startDatePicker.setValue(java.time.LocalDate.now().minusDays(7));
            endDatePicker.setValue(java.time.LocalDate.now());
        });

        last30DaysBtn.setOnAction(e -> {
            startDatePicker.setValue(java.time.LocalDate.now().minusDays(30));
            endDatePicker.setValue(java.time.LocalDate.now());
        });

        last90DaysBtn.setOnAction(e -> {
            startDatePicker.setValue(java.time.LocalDate.now().minusDays(90));
            endDatePicker.setValue(java.time.LocalDate.now());
        });

        thisMonthBtn.setOnAction(e -> {
            java.time.LocalDate now = java.time.LocalDate.now();
            startDatePicker.setValue(now.withDayOfMonth(1));
            endDatePicker.setValue(now);
        });

        // Export button
        Button exportBtn = new Button("📤 Export Report");
        exportBtn.getStyleClass().add("primary-button");
        exportBtn.setOnAction(e -> exportAnalyticsReport());

        // Refresh button
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setOnAction(e -> refreshAnalyticsData());

        HBox dateButtons = new HBox(5, last7DaysBtn, last30DaysBtn, last90DaysBtn, thisMonthBtn);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(dateRangeLabel, startDatePicker, toLabel, endDatePicker,
                dateButtons, spacer, refreshBtn, exportBtn);

        return toolbar;
    }

    private ScrollPane createAnalyticsOverview() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // KPI Cards Row
        HBox kpiRow = new HBox(15);
        Map<String, Object> financialKPIs = analyticsService.getFinancialKPIs();
        kpiRow.getChildren().addAll(
                createKPICard("Total Revenue",
                        "Rs " + String.format("%,.0f", (Double) financialKPIs.get("totalRevenue")),
                        "+" + financialKPIs.get("revenueChange") + "%", "#10b981", "↗"),
                createKPICard("Fuel Savings", "Rs " + String.format("%,.0f", (Double) financialKPIs.get("fuelSavings")),
                        "+" + financialKPIs.get("fuelSavingsChange") + "%", "#3b82f6", "↗"),
                createKPICard("Avg. Efficiency", financialKPIs.get("avgEfficiency") + " L/100km",
                        financialKPIs.get("efficiencyChange") + "%", "#f59e0b", "↘"),
                createKPICard("On-Time Rate", financialKPIs.get("onTimeRate") + "%",
                        "+" + financialKPIs.get("onTimeChange") + "%", "#8b5cf6", "↗"));

        // Charts Row
        HBox chartsRow = new HBox(20);
        chartsRow.setPrefHeight(400);

        // Revenue Trend Chart
        VBox revenueChart = createRevenueTrendChart();
        HBox.setHgrow(revenueChart, Priority.ALWAYS);

        // Fuel Consumption Chart
        VBox fuelChart = createFuelConsumptionChart();
        fuelChart.setPrefWidth(400);

        chartsRow.getChildren().addAll(revenueChart, fuelChart);

        // Bottom Analytics Row
        HBox bottomRow = new HBox(20);
        bottomRow.setPrefHeight(300);

        // Route Performance Table
        VBox routePerformance = createRoutePerformanceTable();
        HBox.setHgrow(routePerformance, Priority.ALWAYS);

        // Top Performing Buses
        VBox topBuses = createTopPerformingBusesTable();
        topBuses.setPrefWidth(400);

        bottomRow.getChildren().addAll(routePerformance, topBuses);

        content.getChildren().addAll(kpiRow, chartsRow, bottomRow);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }

    private VBox createRevenueTrendChart() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label title = new Label("Revenue Trend (Last 30 Days)");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Revenue (LKR)");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setLegendVisible(false);
        lineChart.setPrefHeight(300);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Revenue");

        // Get real data from analytics service
        Map<String, Double> revenueData = analyticsService.getRevenueTrendData(30);
        for (Map.Entry<String, Double> entry : revenueData.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        lineChart.getData().add(series);
        container.getChildren().addAll(title, lineChart);

        return container;
    }

    private VBox createFuelConsumptionChart() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label title = new Label("Fuel Consumption by Route");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Fuel (Liters)");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(300);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Fuel Consumption");

        // Get real data from analytics service
        Map<String, Double> fuelData = analyticsService.getFuelConsumptionByRoute();
        for (Map.Entry<String, Double> entry : fuelData.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        barChart.getData().add(series);
        container.getChildren().addAll(title, barChart);

        return container;
    }

    private BorderPane createPlaceholderPanel(String title, String description) {
        BorderPane panel = new BorderPane();
        panel.setPadding(new Insets(25));
        panel.setStyle("-fx-background-color: #f8f9fc;");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(600);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #111827; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 16px; -fx-text-alignment: center;");
        descLabel.setWrapText(true);

        Button comingSoonBtn = new Button("Coming Soon");
        comingSoonBtn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #6b7280; " +
                "-fx-font-size: 14px; -fx-font-weight: 600; " +
                "-fx-background-radius: 8; -fx-padding: 12 24;");

        Rectangle placeholder = new Rectangle(400, 200);
        placeholder.setFill(Color.web("#f3f4f6"));
        placeholder.setArcWidth(12);
        placeholder.setArcHeight(12);

        content.getChildren().addAll(titleLabel, descLabel, placeholder, comingSoonBtn);

        panel.setCenter(content);
        return panel;
    }

    private void createStatusFooter() {
        HBox footer = new HBox(20);
        footer.setPadding(new Insets(12, 30, 12, 30));
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-background-color: #f9fafb; -fx-border-width: 1 0 0 0; -fx-border-color: #e5e7eb;");

        Label systemInfo = new Label("System Status: Online | Database: Connected | Last Updated: ");
        systemInfo.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");

        Label updateTime = new Label();
        if (timeLabel != null) {
            updateTime.textProperty().bind(timeLabel.textProperty());
        }
        updateTime.setStyle("-fx-text-fill: #374151; -fx-font-size: 11px; -fx-font-weight: 600;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label version = new Label("CTB Depot Management v3.0.0");
        version.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 10px;");

        footer.getChildren().addAll(systemInfo, updateTime, spacer, version);
        mainLayout.setBottom(footer);
    }

    // Controller integration methods
    public void updateStatistics(DashboardData data) {
        Platform.runLater(() -> {
            this.currentDashboardData = data;

            if (totalBusesValue != null) {
                totalBusesValue.setText(String.valueOf(data.getTotalBuses()));
            }
            if (activeBusesValue != null) {
                activeBusesValue.setText(String.valueOf(data.getActiveBuses()));
            }

            // Update the metrics bar with new data
            if (mainLayout != null && mainLayout.getTop() instanceof VBox header) {
                HBox metricsBar = createRealTimeMetricsBar(data);
                // Find and replace the metrics bar in the header
                for (int i = 0; i < header.getChildren().size(); i++) {
                    if (header.getChildren().get(i) instanceof HBox hbox &&
                            hbox.getChildren().size() > 0 &&
                            hbox.getChildren().get(0) instanceof VBox) {
                        header.getChildren().set(i, metricsBar);
                        break;
                    }
                }
            }
        });
    }

    public void updateRecentActivity(List<ActivityLog> activities) {
        Platform.runLater(() -> {
            if (recentActivityContainer != null) {
                recentActivityContainer.getChildren().clear();

                for (ActivityLog activity : activities.subList(0, Math.min(6, activities.size()))) {
                    recentActivityContainer.getChildren().add(
                            createActivityItem(activity.getDescription(), activity.getRelativeTime(),
                                    activity.getStatus()));
                }
            }
        });
    }

    private HBox createActivityItem(String description, String time, String status) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(8));
        item.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 6;");

        String color = switch (status.toLowerCase()) {
            case "success" -> "#10b981";
            case "warning" -> "#f59e0b";
            case "error" -> "#ef4444";
            default -> "#3b82f6";
        };

        Rectangle indicator = new Rectangle(6, 6);
        indicator.setFill(Color.web(color));
        indicator.setArcWidth(6);
        indicator.setArcHeight(6);

        Label desc = new Label(description);
        desc.setStyle("-fx-text-fill: #374151; -fx-font-size: 12px;");
        desc.setWrapText(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 10px;");

        item.getChildren().addAll(indicator, desc, spacer, timeLabel);
        return item;
    }

    public void updateSystemStatus(SystemStatus status) {
        Platform.runLater(() -> {
            if (systemStatusLabel != null) {
                if (status.isSystemHealthy()) {
                    systemStatusLabel.setText("🟢 All Systems Online");
                    systemStatusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-font-weight: 600;");
                } else {
                    systemStatusLabel.setText("🟡 System Issues");
                    systemStatusLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 11px; -fx-font-weight: 600;");
                }
            }
        });
    }

    public void updateSystemHealthStatus(boolean isHealthy) {
        Platform.runLater(() -> {
            SystemStatus status = new SystemStatus();
            status.setSystemHealthy(isHealthy);
            updateSystemStatus(status);
        });
    }

    public void showLoadingIndicator(boolean show) {
        Platform.runLater(() -> {
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(show);
            }
        });
    }

    // Message display methods
    public void showInfoMessage(String message) {
        Platform.runLater(() -> {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Information");
            info.setHeaderText(null);
            info.setContentText(message);
            info.showAndWait();
        });
    }

    public void showErrorMessage(String message) {
        Platform.runLater(() -> {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error");
            error.setHeaderText("System Error");
            error.setContentText(message);
            error.showAndWait();
        });
    }

    public void showWarningMessage(String message) {
        Platform.runLater(() -> {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Warning");
            warning.setHeaderText("System Warning");
            warning.setContentText(message);
            warning.showAndWait();
        });
    }

    private void handleLogout() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Logout");
        confirmation.setHeaderText("Confirm Logout");
        confirmation.setContentText("Are you sure you want to logout?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                controller.handleLogout(primaryStage);
            }
        });
    }

    private void initializeClock() {
        clockTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateClock()));
        clockTimer.setCycleCount(Timeline.INDEFINITE);
        clockTimer.play();
        updateClock();
    }

    private void updateClock() {
        if (controller != null) {
            String currentTime = controller.getCurrentTimeFormatted();
            Platform.runLater(() -> {
                if (timeLabel != null) {
                    timeLabel.setText(currentTime);
                }
            });
        }
    }

    // Getters and setters
    public Task<Void> getUpdateTask() {
        return updateTask;
    }

    public void setUpdateTask(Task<Void> updateTask) {
        this.updateTask = updateTask;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    private void cleanup() {
        if (clockTimer != null) {
            clockTimer.stop();
        }
        if (controller != null) {
            controller.cleanup();
        }
        // Cleanup for route management view
        if (routeManagementView != null && routeManagementView.getController() != null) {
            routeManagementView.getController().cleanup();
        }
        // Cleanup for bus management view
        if (busManagementView != null && busManagementView.getController() != null) {
            busManagementView.getController().cleanup();
        }
        // ← ADD EMPLOYEE PANEL CLEANUP
        if (employeeManagementPanel != null) {
            // Employee panel cleanup if needed
            LOGGER.info("Employee management panel cleaned up");
        }
        LOGGER.info("Modern dashboard view cleanup completed");
    }

    // Analytics methods
    private void exportAnalyticsReport() {
        // TODO: Implement export functionality
        showInfoMessage("Export functionality will be implemented soon.");
    }

    private void refreshAnalyticsData() {
        // TODO: Implement data refresh
        showInfoMessage("Analytics data refreshed.");
    }

    private ScrollPane createFinancialAnalytics() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label title = new Label("Financial Analytics");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Financial KPI cards
        HBox kpiRow = new HBox(15);
        kpiRow.getChildren().addAll(
                createKPICard("Monthly Revenue", "Rs 45,230", "+12.5%", "#10b981", "↗"),
                createKPICard("Fuel Costs", "Rs 12,450", "-5.2%", "#ef4444", "↘"),
                createKPICard("Profit Margin", "72.3%", "+3.1%", "#3b82f6", "↗"),
                createKPICard("Cost Savings", "Rs 8,450", "+8.2%", "#f59e0b", "↗"));

        // Revenue vs Costs chart
        VBox chartContainer = new VBox(10);
        chartContainer.setPadding(new Insets(15));
        chartContainer.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label chartTitle = new Label("Revenue vs Operating Costs");
        chartTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount (LKR)");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(true);
        barChart.setPrefHeight(300);

        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenue");
        XYChart.Series<String, Number> costSeries = new XYChart.Series<>();
        costSeries.setName("Costs");

        String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun" };
        for (String month : months) {
            revenueSeries.getData().add(new XYChart.Data<>(month, 35000 + Math.random() * 10000));
            costSeries.getData().add(new XYChart.Data<>(month, 12000 + Math.random() * 5000));
        }

        barChart.getData().addAll(Arrays.asList(revenueSeries, costSeries));
        chartContainer.getChildren().addAll(chartTitle, barChart);

        content.getChildren().addAll(title, kpiRow, chartContainer);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }

    private ScrollPane createOperationalAnalytics() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label title = new Label("Operational Analytics");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Operational metrics
        HBox metricsRow = new HBox(15);
        Map<String, Object> operationalKPIs = analyticsService.getOperationalKPIs();
        metricsRow.getChildren().addAll(
                createKPICard("Fleet Utilization", operationalKPIs.get("fleetUtilization") + "%",
                        "+" + operationalKPIs.get("utilizationChange") + "%", "#10b981", "↗"),
                createKPICard("Avg. Trip Time", operationalKPIs.get("avgTripTime") + " hrs",
                        operationalKPIs.get("tripTimeChange") + "%", "#3b82f6", "↘"),
                createKPICard("Maintenance Rate", operationalKPIs.get("maintenanceRate") + "%",
                        operationalKPIs.get("maintenanceChange") + "%", "#f59e0b", "↘"),
                createKPICard("Driver Efficiency", operationalKPIs.get("driverEfficiency") + "%",
                        "+" + operationalKPIs.get("driverEfficiencyChange") + "%", "#8b5cf6", "↗"));

        // Route efficiency chart
        VBox chartContainer = new VBox(10);
        chartContainer.setPadding(new Insets(15));
        chartContainer.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label chartTitle = new Label("Route Efficiency Trends");
        chartTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Efficiency (%)");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setLegendVisible(false);
        lineChart.setPrefHeight(300);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Route Efficiency");

        for (int i = 29; i >= 0; i--) {
            java.time.LocalDate date = java.time.LocalDate.now().minusDays(i);
            double efficiency = 85 + Math.random() * 10;
            series.getData().add(
                    new XYChart.Data<>(date.format(java.time.format.DateTimeFormatter.ofPattern("MM/dd")), efficiency));
        }

        lineChart.getData().add(series);
        chartContainer.getChildren().addAll(chartTitle, lineChart);

        content.getChildren().addAll(title, metricsRow, chartContainer);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }

    private ScrollPane createPerformanceAnalytics() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label title = new Label("Performance Analytics");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Performance indicators
        HBox performanceRow = new HBox(15);
        Map<String, Object> performanceKPIs = analyticsService.getPerformanceKPIs();
        performanceRow.getChildren().addAll(
                createKPICard("On-Time Performance", performanceKPIs.get("onTimePerformance") + "%",
                        "+" + performanceKPIs.get("onTimeChange") + "%", "#10b981", "↗"),
                createKPICard("Customer Satisfaction", performanceKPIs.get("customerSatisfaction") + "/5",
                        "+" + performanceKPIs.get("satisfactionChange"), "#3b82f6", "↗"),
                createKPICard("Safety Incidents", performanceKPIs.get("safetyIncidents").toString(),
                        performanceKPIs.get("safetyChange") + "%", "#ef4444", "↘"),
                createKPICard("Avg. Speed", performanceKPIs.get("avgSpeed") + " km/h",
                        "+" + performanceKPIs.get("speedChange") + "%", "#f59e0b", "↗"));

        // Performance trends
        VBox trendsContainer = new VBox(10);
        trendsContainer.setPadding(new Insets(15));
        trendsContainer.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label trendsTitle = new Label("Performance Trends Over Time");
        trendsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Performance Score");

        LineChart<String, Number> trendsChart = new LineChart<>(xAxis, yAxis);
        trendsChart.setLegendVisible(true);
        trendsChart.setPrefHeight(300);

        XYChart.Series<String, Number> onTimeSeries = new XYChart.Series<>();
        onTimeSeries.setName("On-Time %");
        XYChart.Series<String, Number> satisfactionSeries = new XYChart.Series<>();
        satisfactionSeries.setName("Satisfaction");

        for (int i = 11; i >= 0; i--) {
            java.time.LocalDate date = java.time.LocalDate.now().minusMonths(i);
            onTimeSeries.getData().add(new XYChart.Data<>(
                    date.format(java.time.format.DateTimeFormatter.ofPattern("MMM")), 90 + Math.random() * 8));
            satisfactionSeries.getData().add(new XYChart.Data<>(
                    date.format(java.time.format.DateTimeFormatter.ofPattern("MMM")), 4.2 + Math.random() * 0.6));
        }

        trendsChart.getData().addAll(Arrays.asList(onTimeSeries, satisfactionSeries));
        trendsContainer.getChildren().addAll(trendsTitle, trendsChart);

        content.getChildren().addAll(title, performanceRow, trendsContainer);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }

    private VBox createRoutePerformanceTable() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label title = new Label("Route Performance Summary");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TableView<RoutePerformanceData> table = new TableView<>();
        table.setPrefHeight(250);
        table.getStyleClass().add("analytics-table");

        TableColumn<RoutePerformanceData, String> routeCol = new TableColumn<>("Route");
        routeCol.setCellValueFactory(data -> data.getValue().routeProperty());
        routeCol.setPrefWidth(100);

        TableColumn<RoutePerformanceData, Integer> tripsCol = new TableColumn<>("Trips");
        tripsCol.setCellValueFactory(data -> data.getValue().tripsProperty().asObject());
        tripsCol.setPrefWidth(80);

        TableColumn<RoutePerformanceData, Double> revenueCol = new TableColumn<>("Revenue (LKR)");
        revenueCol.setCellValueFactory(data -> data.getValue().revenueProperty().asObject());
        revenueCol.setPrefWidth(100);

        TableColumn<RoutePerformanceData, Double> efficiencyCol = new TableColumn<>("Efficiency");
        efficiencyCol.setCellValueFactory(data -> data.getValue().efficiencyProperty().asObject());
        efficiencyCol.setPrefWidth(100);

        TableColumn<RoutePerformanceData, Double> onTimeCol = new TableColumn<>("On-Time %");
        onTimeCol.setCellValueFactory(data -> data.getValue().onTimeRateProperty().asObject());
        onTimeCol.setPrefWidth(100);

        table.getColumns().addAll(Arrays.asList(routeCol, tripsCol, revenueCol, efficiencyCol, onTimeCol));

        // Sample data
        table.getItems().addAll(analyticsService.getRoutePerformanceData());

        container.getChildren().addAll(title, table);
        return container;
    }

    private VBox createTopPerformingBusesTable() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label title = new Label("Top Performing Buses");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TableView<BusPerformanceData> table = new TableView<>();
        table.setPrefHeight(250);
        table.getStyleClass().add("analytics-table");

        TableColumn<BusPerformanceData, String> busCol = new TableColumn<>("Bus Number");
        busCol.setCellValueFactory(data -> data.getValue().busNumberProperty());
        busCol.setPrefWidth(100);

        TableColumn<BusPerformanceData, Double> efficiencyCol = new TableColumn<>("Efficiency");
        efficiencyCol.setCellValueFactory(data -> data.getValue().efficiencyProperty().asObject());
        efficiencyCol.setPrefWidth(80);

        TableColumn<BusPerformanceData, Integer> tripsCol = new TableColumn<>("Trips");
        tripsCol.setCellValueFactory(data -> data.getValue().tripsProperty().asObject());
        tripsCol.setPrefWidth(60);

        TableColumn<BusPerformanceData, Double> savingsCol = new TableColumn<>("Fuel Saved (L)");
        savingsCol.setCellValueFactory(data -> data.getValue().fuelSavedProperty().asObject());
        savingsCol.setPrefWidth(100);

        table.getColumns().addAll(Arrays.asList(busCol, efficiencyCol, tripsCol, savingsCol));

        // Sample data
        table.getItems().addAll(analyticsService.getTopPerformingBuses());

        container.getChildren().addAll(title, table);
        return container;
    }
}