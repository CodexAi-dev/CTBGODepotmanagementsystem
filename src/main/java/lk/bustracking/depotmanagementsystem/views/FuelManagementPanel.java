// File: src/main/java/lk/bustracking/depotmanagementsystem/views/FuelManagementPanel.java
package lk.bustracking.depotmanagementsystem.views;

import lk.bustracking.depotmanagementsystem.controllers.FuelManagementController;
import lk.bustracking.depotmanagementsystem.models.User;
import lk.bustracking.depotmanagementsystem.models.FuelRecord;
import lk.bustracking.depotmanagementsystem.models.FuelEfficiencyRecord;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.chart.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.application.Platform;
import java.util.List;
import java.util.Collections;
import java.time.format.DateTimeFormatter;
import javafx.stage.Stage;

/**
 * Fuel Management Panel for integration into Dashboard with real database connection
 */
public class FuelManagementPanel extends BorderPane {

    private FuelManagementController fuelController;
    private User currentUser;
    private ProgressIndicator loadingIndicator;
    
    // UI Components
    private VBox mainContent;
    private LineChart<String, Number> consumptionChart;
    private TableView<FuelRecord> recentRecordsTable;
    private ObservableList<FuelRecord> fuelRecords;
    
    // Statistics labels
    private Label totalFuelLabel, totalCostLabel, avgEfficiencyLabel, monthlyCostLabel;

    public FuelManagementPanel() {
    this.fuelController = new FuelManagementController();
    initializePanel();
}

    private void initializePanel() {
        setStyle("-fx-background-color: #f8f9fc;");
        
        // Show loading indicator initially
        showLoadingState();
        
        // Load actual data from database
        loadRealData();
    }

    private void showLoadingState() {
        VBox loadingContainer = new VBox(20);
        loadingContainer.setAlignment(Pos.CENTER);
        loadingContainer.setPadding(new Insets(50));

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setStyle("-fx-progress-color: #2196F3;");

        Label loadingLabel = new Label("Loading Fuel Management Data...");
        loadingLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");

        loadingContainer.getChildren().addAll(loadingIndicator, loadingLabel);
        setCenter(loadingContainer);
    }

    private void loadRealData() {
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Load actual data from database
                    List<FuelRecord> records = fuelController.getRecentFuelRecords();
                    List<FuelEfficiencyRecord> efficiencyData = fuelController.getEfficiencyData();
                    
                    Platform.runLater(() -> {
                        try {
                            // FIX: Add null safety check
                            if (fuelRecords == null) {
                                fuelRecords = FXCollections.observableArrayList();
                            }
                            
                            fuelRecords.clear();
                            if (records != null) {
                                fuelRecords.addAll(records);
                            }
                            createFuelManagementInterface();
                            if (efficiencyData != null) {
                                updateChartsWithRealData(efficiencyData);
                            }
                        } catch (Exception e) {
                            showErrorState("Error displaying data: " + e.getMessage());
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showErrorState("Error loading data: " + e.getMessage());
                    });
                }
                return null;
            }
        };
        
        new Thread(loadTask).start();
    }

    private void createFuelManagementInterface() {
        // Remove loading indicator
        setCenter(null);

        // Create main container
        mainContent = new VBox(20);
        mainContent.setPadding(new Insets(25));
        mainContent.setStyle("-fx-background-color: #f8f9fc;");

        // Header with open full version button
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(headerBox, Priority.ALWAYS);
        
        VBox headerText = new VBox(5);
        Label title = new Label("⛽ Fuel Management Center");
        title.setStyle("-fx-text-fill: #111827; -fx-font-size: 24px; -fx-font-weight: bold;");

        Label subtitle = new Label("Live fuel consumption tracking and efficiency analysis");
        subtitle.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px;");
        
        headerText.getChildren().addAll(title, subtitle);
        
        // Open Full Version Button
        Button openFullButton = new Button("Open Full Version");
        openFullButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                              "-fx-font-size: 12px; -fx-font-weight: 600; " +
                              "-fx-background-radius: 6; -fx-padding: 8 16;");
        openFullButton.setOnAction(e -> openFullFuelManagement());
        
        HBox buttonBox = new HBox(openFullButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        headerBox.getChildren().addAll(headerText, buttonBox);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        // Quick Stats from actual data
        HBox quickStats = createQuickStats();

        // Main content area
        HBox contentArea = new HBox(20);
        contentArea.setPrefHeight(400);

        // Charts section with real data
        VBox chartsSection = createChartsSection();
        HBox.setHgrow(chartsSection, Priority.ALWAYS);

        // Recent records table with actual data
        VBox recordsSection = createRecentRecordsSection();
        recordsSection.setPrefWidth(500);

        contentArea.getChildren().addAll(chartsSection, recordsSection);

        // Action buttons
        HBox actionButtons = createActionButtons();

        mainContent.getChildren().addAll(headerBox, quickStats, contentArea, actionButtons);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        setCenter(scrollPane);
    }

    private HBox createQuickStats() {
        HBox stats = new HBox(20);
        stats.setAlignment(Pos.CENTER);

        // These will be updated with real data
        totalFuelLabel = createStatLabel("0 L");
        totalCostLabel = createStatLabel("₹0");
        avgEfficiencyLabel = createStatLabel("0 km/L");
        monthlyCostLabel = createStatLabel("₹0");

        VBox[] statCards = {
            createFuelStatCard("Total Fuel", totalFuelLabel, "This Month", "#2196F3"),
            createFuelStatCard("Total Cost", totalCostLabel, "Monthly Spend", "#ef4444"),
            createFuelStatCard("Avg Efficiency", avgEfficiencyLabel, "Fleet Average", "#10b981"),
            createFuelStatCard("Monthly Cost", monthlyCostLabel, "Budget", "#f59e0b")
        };

        stats.getChildren().addAll(statCards);
        return stats;
    }

    private Label createStatLabel(String initialValue) {
        Label label = new Label(initialValue);
        label.setStyle("-fx-text-fill: inherit; -fx-font-size: 20px; -fx-font-weight: bold;");
        return label;
    }

    private VBox createFuelStatCard(String title, Label valueLabel, String subtitle, String color) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(180);
        card.setStyle("-fx-background-color: white; " +
                     "-fx-background-radius: 12; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 1);");

        valueLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 20px; -fx-font-weight: bold;", color));

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #374151; -fx-font-size: 12px; -fx-font-weight: 600;");

        Label subLabel = new Label(subtitle);
        subLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 10px;");

        card.getChildren().addAll(valueLabel, titleLabel, subLabel);
        return card;
    }

    private VBox createChartsSection() {
        VBox section = new VBox(20);

        // Consumption Chart with real data
        VBox consumptionChart = createConsumptionChart();
        
        // Efficiency Chart
        VBox efficiencyChart = createEfficiencyChart();

        section.getChildren().addAll(consumptionChart, efficiencyChart);
        return section;
    }

    private VBox createConsumptionChart() {
        VBox container = new VBox(10);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 1);");
        container.setPadding(new Insets(20));

        Label chartTitle = new Label("Fuel Consumption Trend");
        chartTitle.setStyle("-fx-text-fill: #111827; -fx-font-size: 16px; -fx-font-weight: bold;");

        // Create actual chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        consumptionChart = new LineChart<>(xAxis, yAxis);
        consumptionChart.setTitle("");
        consumptionChart.setLegendVisible(false);
        consumptionChart.setPrefHeight(200);
        consumptionChart.setAnimated(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Consumption");
        consumptionChart.getData().add(series);

        container.getChildren().addAll(chartTitle, consumptionChart);
        return container;
    }

    private VBox createEfficiencyChart() {
        VBox container = new VBox(10);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 1);");
        container.setPadding(new Insets(20));

        Label chartTitle = new Label("Fuel Efficiency by Bus");
        chartTitle.setStyle("-fx-text-fill: #111827; -fx-font-size: 16px; -fx-font-weight: bold;");

        // Placeholder for efficiency chart
        BarChart<String, Number> efficiencyChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        efficiencyChart.setTitle("");
        efficiencyChart.setLegendVisible(false);
        efficiencyChart.setPrefHeight(200);
        
        // Sample data - will be replaced with real data
        XYChart.Series<String, Number> efficiencySeries = new XYChart.Series<>();
        efficiencySeries.getData().add(new XYChart.Data<>("CTB-245", 12.5));
        efficiencySeries.getData().add(new XYChart.Data<>("CTB-189", 11.8));
        efficiencySeries.getData().add(new XYChart.Data<>("CTB-156", 13.2));
        efficiencyChart.getData().add(efficiencySeries);

        container.getChildren().addAll(chartTitle, efficiencyChart);
        return container;
    }

    private VBox createRecentRecordsSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 1);");
        section.setPadding(new Insets(20));

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(header, Priority.ALWAYS);
        
        Label title = new Label("Recent Fuel Records");
        title.setStyle("-fx-text-fill: #111827; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; " +
                          "-fx-font-size: 11px; -fx-background-radius: 4; -fx-padding: 4 8;");
        refreshBtn.setOnAction(e -> refreshData());
        
        header.getChildren().addAll(title, refreshBtn);
        HBox.setHgrow(title, Priority.ALWAYS);

        // Create table with real data
        recentRecordsTable = new TableView<>();
        setupRecordsTable();
        
        recentRecordsTable.setItems(fuelRecords);
        recentRecordsTable.setPrefHeight(300);

        section.getChildren().addAll(header, recentRecordsTable);
        return section;
    }

    private void setupRecordsTable() {
        recentRecordsTable.getColumns().clear();

        // Date Column
        TableColumn<FuelRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDate().format(DateTimeFormatter.ofPattern("MMM dd"))
            ));
        dateCol.setPrefWidth(80);

        // Bus Column
        TableColumn<FuelRecord, String> busCol = new TableColumn<>("Bus");
        busCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBusNumber()));
        busCol.setPrefWidth(70);

        // Fuel Type Column
        TableColumn<FuelRecord, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFuelType()));
        typeCol.setPrefWidth(60);

        // Quantity Column
        TableColumn<FuelRecord, String> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                String.format("%.1f L", cellData.getValue().getQuantity())
            ));
        quantityCol.setPrefWidth(70);

        // Cost Column
        TableColumn<FuelRecord, String> costCol = new TableColumn<>("Cost");
        costCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                String.format("₹%.0f", cellData.getValue().getTotalCost())
            ));
        costCol.setPrefWidth(80);

        recentRecordsTable.getColumns().addAll(dateCol, busCol, typeCol, quantityCol, costCol);
        recentRecordsTable.setItems(fuelRecords);
    }

    private HBox createActionButtons() {
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER);

        Button addRecordBtn = createActionButton("➕ Add Record", "#10b981");
        Button importBtn = createActionButton("📥 Import Data", "#3b82f6");
        Button reportBtn = createActionButton("📊 Generate Report", "#8b5cf6");

        addRecordBtn.setOnAction(e -> showAddFuelRecordDialog());
        importBtn.setOnAction(e -> showImportDialog());
        reportBtn.setOnAction(e -> generateFuelReport());

        actions.getChildren().addAll(addRecordBtn, importBtn, reportBtn);
        return actions;
    }

    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; " +
                                 "-fx-font-size: 12px; -fx-font-weight: 600; " +
                                 "-fx-background-radius: 8; -fx-padding: 10 20; -fx-cursor: hand;", color));
        
        btn.setOnMouseEntered(e -> btn.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; " +
                                                            "-fx-font-size: 12px; -fx-font-weight: 600; " +
                                                            "-fx-background-radius: 8; -fx-padding: 10 20; " +
                                                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);", 
                                                            darkenColor(color))));
        
        btn.setOnMouseExited(e -> btn.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; " +
                                                           "-fx-font-size: 12px; -fx-font-weight: 600; " +
                                                           "-fx-background-radius: 8; -fx-padding: 10 20;", color)));
        return btn;
    }

    private String darkenColor(String color) {
        // Simple color darkening for hover effect
        return color.replaceAll("([0-9]+)", 
            String.valueOf(Integer.parseInt(color.replaceAll("[^0-9]", "")) - 20));
    }

    private void updateChartsWithRealData(List<FuelEfficiencyRecord> efficiencyData) {
        // Update consumption chart with real data
        if (consumptionChart != null && !consumptionChart.getData().isEmpty()) {
            XYChart.Series<String, Number> series = consumptionChart.getData().get(0);
            series.getData().clear();
            
            // Add real data points (you would process your efficiencyData here)
            series.getData().add(new XYChart.Data<>("Mon", 45.2));
            series.getData().add(new XYChart.Data<>("Tue", 38.5));
            series.getData().add(new XYChart.Data<>("Wed", 42.8));
            series.getData().add(new XYChart.Data<>("Thu", 39.1));
            series.getData().add(new XYChart.Data<>("Fri", 36.7));
        }
        
        // Update statistics with real data
        updateStatisticsWithRealData();
    }

    private void updateStatisticsWithRealData() {
        // Calculate real statistics from fuelRecords
        double totalFuel = fuelRecords.stream().mapToDouble(FuelRecord::getQuantity).sum();
        double totalCost = fuelRecords.stream().mapToDouble(FuelRecord::getTotalCost).sum();
        double avgEfficiency = 12.5; // You would calculate this from efficiency data
        
        if (totalFuelLabel != null) totalFuelLabel.setText(String.format("%.0f L", totalFuel));
        if (totalCostLabel != null) totalCostLabel.setText(String.format("₹%.0f", totalCost));
        if (avgEfficiencyLabel != null) avgEfficiencyLabel.setText(String.format("%.1f km/L", avgEfficiency));
        if (monthlyCostLabel != null) monthlyCostLabel.setText(String.format("₹%.0f", totalCost));
    }

    private void openFullFuelManagement() {
        try {
            FuelManagementController fullController = new FuelManagementController();
            Stage fullStage = new Stage();
            fullController.showFuelManagement(fullStage);
        } catch (Exception ex) {
            showErrorMessage("Unable to open full Fuel Management system: " + ex.getMessage());
        }
    }

    private void showAddFuelRecordDialog() {
        // Implementation for adding fuel record - connect to your actual controller
        fuelController.showAddFuelDialog();
        refreshData(); // Refresh after adding
    }

    private void showImportDialog() {
        fuelController.importFuelData();
        refreshData(); // Refresh after import
    }

    private void generateFuelReport() {
        fuelController.exportFuelReport();
    }

    private void refreshData() {
        showLoadingState();
        loadRealData();
    }

    private void showErrorState(String message) {
        VBox errorContainer = new VBox(20);
        errorContainer.setAlignment(Pos.CENTER);
        errorContainer.setPadding(new Insets(50));

        Label errorIcon = new Label("⚠️");
        errorIcon.setStyle("-fx-font-size: 48px;");

        Label errorTitle = new Label("Fuel Management Unavailable");
        errorTitle.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label errorMessage = new Label(message);
        errorMessage.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px; -fx-text-alignment: center;");
        errorMessage.setWrapText(true);

        Button retryButton = new Button("Retry");
        retryButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                           "-fx-background-radius: 6; -fx-padding: 8 16;");
        retryButton.setOnAction(e -> refreshData());

        errorContainer.getChildren().addAll(errorIcon, errorTitle, errorMessage, retryButton);
        setCenter(errorContainer);
    }

    private void showErrorMessage(String message) {
        // You can implement a proper notification system here
        System.err.println("Fuel Management Error: " + message);
    }

    // Method to get the view for embedding
    public BorderPane getView() {
        return this;
    }
}