package lk.bustracking.depotmanagementsystem.views;

import lk.bustracking.depotmanagementsystem.controllers.RouteController;
import lk.bustracking.depotmanagementsystem.models.Route;
import lk.bustracking.depotmanagementsystem.models.User;
import lk.bustracking.depotmanagementsystem.services.RouteService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Route Management View - Professional UI for route CRUD operations
 */
public class RouteManagementView {
    
    private static final Logger LOGGER = Logger.getLogger(RouteManagementView.class.getName());
    
    private final RouteController controller;
    private final User currentUser;
    private Stage primaryStage;
    
    // UI Components
    private BorderPane mainPanel;
    private TableView<Route> routeTable;
    private ObservableList<Route> routeData;
    private TextField searchField;
    private ComboBox<Route.RouteType> typeFilterCombo;
    private CheckBox activeOnlyCheckBox;
    private Label totalRoutesLabel;
    private Label activeRoutesLabel;
    private Label expressRoutesLabel;
    private Label nightRoutesLabel;
    private ProgressIndicator loadingIndicator;
    
    public RouteManagementView(User currentUser) {
        this.currentUser = currentUser;
        this.controller = new RouteController(this, currentUser);
        this.routeData = FXCollections.observableArrayList();
        initializeUI();
        
        // Load initial data
        controller.loadRoutes();
    }

    public RouteManagementView() {
        this.currentUser = null;
        this.controller = new RouteController(this, currentUser);
        this.routeData = FXCollections.observableArrayList();
        initializeUI();
    }

    /**
     * Start the view in a separate stage (standalone mode)
     */
    public void start(Stage stage) {
        this.primaryStage = stage;
        if (mainPanel == null) {
            initializeUI();
        }
        
        Scene scene = new Scene(mainPanel, 1400, 900);
        stage.setTitle("Route Management - CTB Depot System");
        stage.setScene(scene);
        stage.show();
        
        // Initialize controller if not already done
        if (currentUser != null) {
            controller.loadRoutes();
        }
        
        LOGGER.info("Route Management View started in standalone mode");
    }

    /**
     * Get the main layout for embedding in other views (like dashboard tabs)
     */
    public BorderPane getMainLayoutForEmbedding() {
        if (mainPanel == null) {
            initializeUI();
        }
        
        // Initialize controller if not already done and user is available
        if (currentUser != null) {
            controller.loadRoutes();
        }
        
        return mainPanel;
    }

    /**
     * Initialize the view for embedding in other containers
     */
    public void initializeForEmbedding(User user) {
        // Update current user if provided
        if (user != null && this.currentUser == null) {
            // Note: we can't change the final currentUser field, but we can work with the provided user
            // The controller should handle this appropriately
        }
        
        if (mainPanel == null) {
            initializeUI();
        }
        
        // Load data with the provided user context
        if (controller != null) {
            controller.loadRoutes();
        }
        
        LOGGER.info("Route Management View initialized for embedding");
    }
    
    private void initializeUI() {
        mainPanel = new BorderPane();
        mainPanel.setStyle("-fx-background-color: #f8fafc;");
        
        // Create header
        VBox header = createHeader();
        mainPanel.setTop(header);
        
        // Create main content
        VBox content = createMainContent();
        mainPanel.setCenter(content);
        
        LOGGER.info("Route Management View initialized");
    }
    
    private VBox createHeader() {
        VBox header = new VBox(8);
        header.setPadding(new Insets(10, 20, 8, 20));
        header.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");

        // Title - compact single line, no decorative subtitle, so the table
        // below (the important content) gets the space instead.
        Label titleLabel = new Label("Route Management");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        // Statistics cards
        HBox statsContainer = createStatsContainer();

        header.getChildren().addAll(titleLabel, statsContainer);
        return header;
    }

    private HBox createStatsContainer() {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);
        
        // Total Routes Card
        VBox totalCard = createStatCard("Total Routes", "0", "#3b82f6", "📍");
        totalRoutesLabel = (Label) ((VBox) totalCard.getChildren().get(1)).getChildren().get(0);
        
        // Active Routes Card
        VBox activeCard = createStatCard("Active Routes", "0", "#10b981", "✅");
        activeRoutesLabel = (Label) ((VBox) activeCard.getChildren().get(1)).getChildren().get(0);
        
        // Express Routes Card
        VBox expressCard = createStatCard("Express Routes", "0", "#8b5cf6", "⚡");
        expressRoutesLabel = (Label) ((VBox) expressCard.getChildren().get(1)).getChildren().get(0);
        
        // Night Routes Card
        VBox nightCard = createStatCard("Night Service", "0", "#f59e0b", "🌙");
        nightRoutesLabel = (Label) ((VBox) nightCard.getChildren().get(1)).getChildren().get(0);
        
        container.getChildren().addAll(totalCard, activeCard, expressCard, nightCard);
        return container;
    }
    
    private VBox createStatCard(String title, String value, String color, String icon) {
        VBox card = new VBox(2);
        card.setPadding(new Insets(6, 10, 6, 10));
        card.getStyleClass().add("stat-card");
        card.setPrefWidth(110);

        HBox iconRow = new HBox(4);
        iconRow.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 11px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #6b7280; -fx-font-weight: 500;");

        iconRow.getChildren().addAll(iconLabel, titleLabel);

        VBox valueContainer = new VBox();
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        valueContainer.getChildren().add(valueLabel);
        card.getChildren().addAll(iconRow, valueContainer);

        return card;
    }
    
    private VBox createMainContent() {
        VBox content = new VBox(8);
        content.setPadding(new Insets(8, 20, 8, 20));

        // Controls section
        HBox controlsSection = createControlsSection();

        // Table section
        VBox tableSection = createTableSection();

        content.getChildren().addAll(controlsSection, tableSection);
        VBox.setVgrow(tableSection, Priority.ALWAYS);
        return content;
    }

    private HBox createControlsSection() {
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(8, 12, 8, 12));
        controls.getStyleClass().add("section-card");

        searchField = new TextField();
        searchField.setPromptText("Search routes...");
        searchField.setPrefWidth(160);
        searchField.setStyle("-fx-background-radius: 6; -fx-border-color: #d1d5db; -fx-border-radius: 6;");
        searchField.textProperty().addListener((obs, old, text) -> controller.performSearch());

        typeFilterCombo = new ComboBox<>();
        typeFilterCombo.getItems().add(null); // All types option
        typeFilterCombo.getItems().addAll(Route.RouteType.values());
        typeFilterCombo.setValue(null);
        typeFilterCombo.setPromptText("Type");
        typeFilterCombo.setPrefWidth(100);
        typeFilterCombo.valueProperty().addListener((obs, old, value) -> controller.performSearch());

        activeOnlyCheckBox = new CheckBox("Active only");
        activeOnlyCheckBox.setStyle("-fx-text-fill: #374151; -fx-font-size: 12px;");
        activeOnlyCheckBox.selectedProperty().addListener((obs, old, value) -> controller.performSearch());

        // Action buttons - short labels so nothing gets clipped in the tab.
        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button addButton = new Button("+ Add");
        addButton.getStyleClass().add("btn-primary");
        addButton.setOnAction(e -> showAddEditDialog(null));

        Button refreshButton = new Button("↻");
        refreshButton.getStyleClass().add("btn-secondary");
        refreshButton.setOnAction(e -> controller.loadRoutes());

        Button exportButton = new Button("Export");
        exportButton.getStyleClass().add("btn-success");
        exportButton.setOnAction(e -> controller.exportToCSV());

        buttonBox.getChildren().addAll(addButton, refreshButton, exportButton);

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        controls.getChildren().addAll(searchField, typeFilterCombo, activeOnlyCheckBox, spacer, buttonBox);
        return controls;
    }

    private VBox createTableSection() {
        VBox tableSection = new VBox(6);
        VBox.setVgrow(tableSection, Priority.ALWAYS);

        // Table container
        VBox tableContainer = new VBox();
        tableContainer.getStyleClass().add("section-card");
        tableContainer.setPadding(new Insets(8));
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(40, 40);
        loadingIndicator.setVisible(false);

        StackPane loadingPane = new StackPane(loadingIndicator);
        loadingPane.setPrefHeight(60);

        // Create table
        createRouteTable();
        VBox.setVgrow(routeTable, Priority.ALWAYS);

        VBox tableContent = new VBox();
        VBox.setVgrow(tableContent, Priority.ALWAYS);
        tableContent.getChildren().addAll(loadingPane, routeTable);

        tableContainer.getChildren().add(tableContent);
        tableSection.getChildren().add(tableContainer);

        return tableSection;
    }
    
    private void createRouteTable() {
        routeTable = new TableView<>();
        routeTable.setItems(routeData);
        routeTable.getStyleClass().add("table-view");
        routeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Route Number Column
        TableColumn<Route, String> routeNumberCol = new TableColumn<>("Route #");
        routeNumberCol.setCellValueFactory(new PropertyValueFactory<>("routeNumber"));
        routeNumberCol.setMinWidth(70);
        routeNumberCol.setStyle("-fx-alignment: CENTER;");

        // Route Name Column
        TableColumn<Route, String> routeNameCol = new TableColumn<>("Route Name");
        routeNameCol.setCellValueFactory(new PropertyValueFactory<>("routeName"));
        routeNameCol.setMinWidth(140);

        // Start Location Column
        TableColumn<Route, String> startLocationCol = new TableColumn<>("From");
        startLocationCol.setCellValueFactory(new PropertyValueFactory<>("startLocation"));
        startLocationCol.setMinWidth(100);

        // End Location Column
        TableColumn<Route, String> endLocationCol = new TableColumn<>("To");
        endLocationCol.setCellValueFactory(new PropertyValueFactory<>("endLocation"));
        endLocationCol.setMinWidth(100);

        // Distance Column
        TableColumn<Route, String> distanceCol = new TableColumn<>("Distance");
        distanceCol.setCellValueFactory(cellData -> {
            BigDecimal distance = cellData.getValue().getTotalDistanceKm();
            return new SimpleStringProperty(distance != null ? distance + " km" : "N/A");
        });
        distanceCol.setMinWidth(75);
        distanceCol.setStyle("-fx-alignment: CENTER;");

        // Duration Column
        TableColumn<Route, String> durationCol = new TableColumn<>("Duration");
        durationCol.setCellValueFactory(cellData -> {
            int minutes = cellData.getValue().getEstimatedDurationMinutes();
            int hours = minutes / 60;
            int mins = minutes % 60;
            return new SimpleStringProperty(hours + "h " + mins + "m");
        });
        durationCol.setMinWidth(75);
        durationCol.setStyle("-fx-alignment: CENTER;");

        // Type Column
        TableColumn<Route, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> {
            Route.RouteType type = cellData.getValue().getRouteType();
            return new SimpleStringProperty(type != null ? type.getDisplayName() : "Regular");
        });
        typeCol.setMinWidth(90);
        typeCol.setStyle("-fx-alignment: CENTER;");

        // Fare Column
        TableColumn<Route, String> fareCol = new TableColumn<>("Fare");
        fareCol.setCellValueFactory(cellData -> {
            BigDecimal fare = cellData.getValue().getFarePrice();
            return new SimpleStringProperty(fare != null ? "Rs. " + fare : "N/A");
        });
        fareCol.setMinWidth(80);
        fareCol.setStyle("-fx-alignment: CENTER;");

        // Operating Hours Column
        TableColumn<Route, String> hoursCol = new TableColumn<>("Hours");
        hoursCol.setCellValueFactory(cellData -> {
            Route route = cellData.getValue();
            LocalTime start = route.getOperatingHoursStart();
            LocalTime end = route.getOperatingHoursEnd();
            if (start != null && end != null) {
                return new SimpleStringProperty(
                    start.format(DateTimeFormatter.ofPattern("HH:mm")) + " - " +
                    end.format(DateTimeFormatter.ofPattern("HH:mm"))
                );
            }
            return new SimpleStringProperty("24/7");
        });
        hoursCol.setMinWidth(100);
        hoursCol.setStyle("-fx-alignment: CENTER;");

        // Status Column
        TableColumn<Route, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> {
            boolean isActive = cellData.getValue().isActive();
            return new SimpleStringProperty(isActive ? "Active" : "Inactive");
        });
        statusCol.setMinWidth(80);
        statusCol.setStyle("-fx-alignment: CENTER;");

        // Custom cell factory for status column coloring
        statusCol.setCellFactory(column -> new TableCell<Route, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("status-badge", "status-badge-success", "status-badge-danger");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    getStyleClass().addAll("status-badge", "Active".equals(item) ? "status-badge-success" : "status-badge-danger");
                }
            }
        });

        // Actions Column
        TableColumn<Route, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setMinWidth(110);
        actionsCol.setCellFactory(column -> new TableCell<Route, Void>() {
            private final HBox actionBox = new HBox(5);
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                editButton.getStyleClass().addAll("btn-small", "btn-primary");
                deleteButton.getStyleClass().addAll("btn-small", "btn-danger");

                editButton.setOnAction(e -> {
                    Route route = getTableView().getItems().get(getIndex());
                    showAddEditDialog(route);
                });
                
                deleteButton.setOnAction(e -> {
                    Route route = getTableView().getItems().get(getIndex());
                    confirmDelete(route);
                });
                
                actionBox.getChildren().addAll(editButton, deleteButton);
                actionBox.setAlignment(Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionBox);
                }
            }
        });
        
        routeTable.getColumns().addAll(
            routeNumberCol, routeNameCol, startLocationCol, endLocationCol,
            distanceCol, durationCol, typeCol, fareCol, hoursCol, statusCol, actionsCol
        );
        
        // Context menu for right-click
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit Route");
        MenuItem deleteItem = new MenuItem("Delete Route");
        MenuItem viewDetailsItem = new MenuItem("View Details");
        
        editItem.setOnAction(e -> {
            Route selected = routeTable.getSelectionModel().getSelectedItem();
            if (selected != null) showAddEditDialog(selected);
        });
        
        deleteItem.setOnAction(e -> {
            Route selected = routeTable.getSelectionModel().getSelectedItem();
            if (selected != null) confirmDelete(selected);
        });
        
        viewDetailsItem.setOnAction(e -> {
            Route selected = routeTable.getSelectionModel().getSelectedItem();
            if (selected != null) showRouteDetails(selected);
        });
        
        contextMenu.getItems().addAll(editItem, deleteItem, new SeparatorMenuItem(), viewDetailsItem);
        routeTable.setContextMenu(contextMenu);
        
        // Row factory for styling
        routeTable.setRowFactory(tv -> {
            TableRow<Route> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldRoute, newRoute) -> {
                if (newRoute == null) {
                    row.setStyle("");
                } else if (!newRoute.isActive()) {
                    row.setStyle("-fx-background-color: #fef2f2;");
                } else {
                    row.setStyle("");
                }
            });
            return row;
        });
    }
    
    private void showAddEditDialog(Route route) {
        boolean isEdit = route != null;
        
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(isEdit ? "Edit Route" : "Add New Route");
        dialog.setResizable(false);
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #f9fafb;");
        
        // Form
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setStyle("-fx-background-color: white; -fx-padding: 25; -fx-background-radius: 12; " +
                         "-fx-border-color: #e5e7eb; -fx-border-radius: 12; -fx-border-width: 1;");
        
        // Form fields
        TextField routeNumberField = new TextField();
        TextField routeNameField = new TextField();
        TextField startLocationField = new TextField();
        TextField endLocationField = new TextField();
        TextField distanceField = new TextField();
        Spinner<Integer> durationSpinner = new Spinner<>(30, 600, 60, 15);
        ComboBox<Route.RouteType> typeCombo = new ComboBox<>();
        TextField fareField = new TextField();
        Spinner<Integer> frequencySpinner = new Spinner<>(5, 120, 15, 5);
        TextField startTimeField = new TextField();
        TextField endTimeField = new TextField();
        CheckBox activeCheckBox = new CheckBox("Active Route");
        
        // Setup components
        typeCombo.getItems().addAll(Route.RouteType.values());
        typeCombo.setValue(Route.RouteType.REGULAR);
        
        routeNumberField.setPromptText("e.g., R001, EX01");
        routeNameField.setPromptText("e.g., Colombo - Kandy Express");
        startLocationField.setPromptText("e.g., Colombo Fort");
        endLocationField.setPromptText("e.g., Kandy Bus Stand");
        distanceField.setPromptText("e.g., 115.5");
        fareField.setPromptText("e.g., 150.00");
        startTimeField.setPromptText("e.g., 05:30");
        endTimeField.setPromptText("e.g., 22:00");
        
        activeCheckBox.setSelected(true);
        
        // Style form fields
        String fieldStyle = "-fx-background-radius: 6; -fx-border-color: #d1d5db; -fx-border-radius: 6; -fx-padding: 8;";
        routeNumberField.setStyle(fieldStyle);
        routeNameField.setStyle(fieldStyle);
        startLocationField.setStyle(fieldStyle);
        endLocationField.setStyle(fieldStyle);
        distanceField.setStyle(fieldStyle);
        fareField.setStyle(fieldStyle);
        startTimeField.setStyle(fieldStyle);
        endTimeField.setStyle(fieldStyle);
        
        // Pre-fill for edit
        if (isEdit) {
            routeNumberField.setText(route.getRouteNumber());
            routeNameField.setText(route.getRouteName());
            startLocationField.setText(route.getStartLocation());
            endLocationField.setText(route.getEndLocation());
            if (route.getTotalDistanceKm() != null) {
                distanceField.setText(route.getTotalDistanceKm().toString());
            }
            durationSpinner.getValueFactory().setValue(route.getEstimatedDurationMinutes());
            typeCombo.setValue(route.getRouteType());
            if (route.getFarePrice() != null) {
                fareField.setText(route.getFarePrice().toString());
            }
            frequencySpinner.getValueFactory().setValue(route.getFrequencyMinutes());
            if (route.getOperatingHoursStart() != null) {
                startTimeField.setText(route.getOperatingHoursStart().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
            if (route.getOperatingHoursEnd() != null) {
                endTimeField.setText(route.getOperatingHoursEnd().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
            activeCheckBox.setSelected(route.isActive());
        }
        
        // Add fields to grid
        int row = 0;
        formGrid.add(new Label("Route Number:"), 0, row);
        formGrid.add(routeNumberField, 1, row++);
        formGrid.add(new Label("Route Name:"), 0, row);
        formGrid.add(routeNameField, 1, row++);
        formGrid.add(new Label("Start Location:"), 0, row);
        formGrid.add(startLocationField, 1, row++);
        formGrid.add(new Label("End Location:"), 0, row);
        formGrid.add(endLocationField, 1, row++);
        formGrid.add(new Label("Distance (km):"), 0, row);
        formGrid.add(distanceField, 1, row++);
        formGrid.add(new Label("Duration (minutes):"), 0, row);
        formGrid.add(durationSpinner, 1, row++);
        formGrid.add(new Label("Route Type:"), 0, row);
        formGrid.add(typeCombo, 1, row++);
        formGrid.add(new Label("Fare (Rs):"), 0, row);
        formGrid.add(fareField, 1, row++);
        formGrid.add(new Label("Frequency (minutes):"), 0, row);
        formGrid.add(frequencySpinner, 1, row++);
        formGrid.add(new Label("Start Time:"), 0, row);
        formGrid.add(startTimeField, 1, row++);
        formGrid.add(new Label("End Time:"), 0, row);
        formGrid.add(endTimeField, 1, row++);
        formGrid.add(new Label("Status:"), 0, row);
        formGrid.add(activeCheckBox, 1, row++);
        
        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("btn-secondary");
        cancelButton.setOnAction(e -> dialog.close());

        Button saveButton = new Button(isEdit ? "Update Route" : "Save Route");
        saveButton.getStyleClass().add("btn-primary");

        saveButton.setOnAction(e -> {
            try {
                Route routeToSave = isEdit ? route : new Route();
                
                // Validate and set data
                if (routeNumberField.getText().trim().isEmpty()) {
                    showError("Route number is required");
                    return;
                }
                
                routeToSave.setRouteNumber(routeNumberField.getText().trim());
                routeToSave.setRouteName(routeNameField.getText().trim());
                routeToSave.setStartLocation(startLocationField.getText().trim());
                routeToSave.setEndLocation(endLocationField.getText().trim());
                
                if (!distanceField.getText().trim().isEmpty()) {
                    routeToSave.setTotalDistanceKm(new BigDecimal(distanceField.getText().trim()));
                }
                
                routeToSave.setEstimatedDurationMinutes(durationSpinner.getValue());
                routeToSave.setRouteType(typeCombo.getValue());
                
                if (!fareField.getText().trim().isEmpty()) {
                    routeToSave.setFarePrice(new BigDecimal(fareField.getText().trim()));
                }
                
                routeToSave.setFrequencyMinutes(frequencySpinner.getValue());
                
                if (!startTimeField.getText().trim().isEmpty()) {
                    routeToSave.setOperatingHoursStart(LocalTime.parse(startTimeField.getText().trim()));
                }
                
                if (!endTimeField.getText().trim().isEmpty()) {
                    routeToSave.setOperatingHoursEnd(LocalTime.parse(endTimeField.getText().trim()));
                }
                
                routeToSave.setActive(activeCheckBox.isSelected());
                
                // Save via controller
                if (isEdit) {
                    controller.updateRoute(routeToSave);
                } else {
                    controller.saveRoute(routeToSave);
                }
                
                dialog.close();
                
            } catch (NumberFormatException ex) {
                showError("Please enter valid numbers for distance and fare");
            } catch (Exception ex) {
                showError("Error saving route: " + ex.getMessage());
            }
        });
        
        buttonBox.getChildren().addAll(cancelButton, saveButton);
        
        content.getChildren().addAll(formGrid, buttonBox);
        
        Scene scene = new Scene(content, 500, 700);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private void confirmDelete(Route route) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Route: " + route.getRouteNumber());
        alert.setContentText("Are you sure you want to delete this route?\n\n" +
                            "Route: " + route.getRouteName() + "\n" +
                            "From: " + route.getStartLocation() + "\n" +
                            "To: " + route.getEndLocation() + "\n\n" +
                            "This action cannot be undone.");
        
        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(deleteButtonType, cancelButtonType);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == deleteButtonType) {
            controller.deleteRoute(route.getRouteId());
        }
    }
    
    private void showRouteDetails(Route route) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Route Details");
        alert.setHeaderText("Route: " + route.getRouteNumber() + " - " + route.getRouteName());
        
        StringBuilder details = new StringBuilder();
        details.append("Start Location: ").append(route.getStartLocation()).append("\n");
        details.append("End Location: ").append(route.getEndLocation()).append("\n");
        details.append("Distance: ").append(route.getTotalDistanceKm()).append(" km\n");
        details.append("Duration: ").append(route.getEstimatedDurationMinutes()).append(" minutes\n");
        details.append("Type: ").append(route.getRouteType().getDisplayName()).append("\n");
        details.append("Fare: Rs. ").append(route.getFarePrice()).append("\n");
        details.append("Frequency: ").append(route.getFrequencyMinutes()).append(" minutes\n");
        
        if (route.getOperatingHoursStart() != null && route.getOperatingHoursEnd() != null) {
            details.append("Operating Hours: ")
                   .append(route.getOperatingHoursStart().format(DateTimeFormatter.ofPattern("HH:mm")))
                   .append(" - ")
                   .append(route.getOperatingHoursEnd().format(DateTimeFormatter.ofPattern("HH:mm")))
                   .append("\n");
        }
        
        details.append("Status: ").append(route.isActive() ? "Active" : "Inactive").append("\n");
        details.append("Created: ").append(route.getCreatedAt() != null ? 
                      route.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A");
        
        alert.setContentText(details.toString());
        alert.showAndWait();
    }
    
    // =====================================================================================
    // PUBLIC METHODS FOR CONTROLLER CALLBACKS
    // =====================================================================================
    
    public void updateRouteData(List<Route> routes) {
        Platform.runLater(() -> {
            routeData.clear();
            routeData.addAll(routes);
            hideLoading();
            updateStatsFromData();
        });
    }
    
    public void updateStats(RouteService.RouteStats stats) {
        Platform.runLater(() -> {
            if (totalRoutesLabel != null) {
                totalRoutesLabel.setText(String.valueOf(stats.getTotalRoutes()));
            }
            if (activeRoutesLabel != null) {
                activeRoutesLabel.setText(String.valueOf(stats.getActiveRoutes()));
            }
            if (expressRoutesLabel != null) {
                expressRoutesLabel.setText(String.valueOf(stats.getExpressRoutes()));
            }
            if (nightRoutesLabel != null) {
                nightRoutesLabel.setText(String.valueOf(stats.getNightRoutes()));
            }
        });
    }
    
    private void updateStatsFromData() {
        if (routeData == null || routeData.isEmpty()) {
            return;
        }
        
        int total = routeData.size();
        long active = routeData.stream().filter(Route::isActive).count();
        long express = routeData.stream()
                .filter(r -> r.getRouteType() == Route.RouteType.EXPRESS)
                .count();
        long night = routeData.stream()
                .filter(r -> r.getRouteType() == Route.RouteType.NIGHT_SERVICE)
                .count();
        
        if (totalRoutesLabel != null) {
            totalRoutesLabel.setText(String.valueOf(total));
        }
        if (activeRoutesLabel != null) {
            activeRoutesLabel.setText(String.valueOf(active));
        }
        if (expressRoutesLabel != null) {
            expressRoutesLabel.setText(String.valueOf(express));
        }
        if (nightRoutesLabel != null) {
            nightRoutesLabel.setText(String.valueOf(night));
        }
    }
    
    public void showLoading() {
        Platform.runLater(() -> {
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(true);
            }
            if (routeTable != null) {
                routeTable.setDisable(true);
            }
        });
    }
    
    public void hideLoading() {
        Platform.runLater(() -> {
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(false);
            }
            if (routeTable != null) {
                routeTable.setDisable(false);
            }
        });
    }
    
    public void showSuccessMessage(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    public void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void showWarningMessage(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    public void refreshTable() {
        Platform.runLater(() -> {
            if (routeTable != null) {
                routeTable.refresh();
            }
        });
    }
    
    public void addRouteToTable(Route route) {
        Platform.runLater(() -> {
            if (!routeData.contains(route)) {
                routeData.add(route);
                updateStatsFromData();
            }
        });
    }
    
    public void updateRouteInTable(Route updatedRoute) {
        Platform.runLater(() -> {
            if (updatedRoute == null) return;
            
            for (int i = 0; i < routeData.size(); i++) {
                Route route = routeData.get(i);
                if (route != null && route.getRouteId() == updatedRoute.getRouteId()) {
                    routeData.set(i, updatedRoute);
                    updateStatsFromData();
                    break;
                }
            }
        });
    }
    
    public void removeRouteFromTable(int routeId) {
        Platform.runLater(() -> {
            routeData.removeIf(route -> route != null && route.getRouteId() == routeId);
            updateStatsFromData();
        });
    }
    
    public void clearRouteSelection() {
        Platform.runLater(() -> {
            if (routeTable != null) {
                routeTable.getSelectionModel().clearSelection();
            }
        });
    }
    
    public Route getSelectedRoute() {
        if (routeTable != null) {
            return routeTable.getSelectionModel().getSelectedItem();
        }
        return null;
    }
    
    public String getSearchText() {
        return searchField != null ? searchField.getText() : "";
    }
    
    public Route.RouteType getSelectedRouteType() {
        return typeFilterCombo != null ? typeFilterCombo.getValue() : null;
    }
    
    public boolean isActiveOnlySelected() {
        return activeOnlyCheckBox != null && activeOnlyCheckBox.isSelected();
    }
    
    public void clearFilters() {
        Platform.runLater(() -> {
            if (searchField != null) {
                searchField.clear();
            }
            if (typeFilterCombo != null) {
                typeFilterCombo.setValue(null);
            }
            if (activeOnlyCheckBox != null) {
                activeOnlyCheckBox.setSelected(false);
            }
        });
    }
    
    // =====================================================================================
    // GETTERS FOR CONTROLLER ACCESS
    // =====================================================================================
    
    public BorderPane getMainPanel() { 
        return mainPanel; 
    }
    
    public TableView<Route> getRouteTable() { 
        return routeTable; 
    }
    
    public ObservableList<Route> getRouteData() { 
        return routeData; 
    }
    
    public TextField getSearchField() { 
        return searchField; 
    }
    
    public ComboBox<Route.RouteType> getTypeFilterCombo() { 
        return typeFilterCombo; 
    }
    
    public CheckBox getActiveOnlyCheckBox() { 
        return activeOnlyCheckBox; 
    }
    
    public User getCurrentUser() { 
        return currentUser; 
    }
    
    public RouteController getController() { 
        return controller; 
    }
    
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public ProgressIndicator getLoadingIndicator() {
        return loadingIndicator;
    }
    
    public Label getTotalRoutesLabel() {
        return totalRoutesLabel;
    }
    
    public Label getActiveRoutesLabel() {
        return activeRoutesLabel;
    }
    
    public Label getExpressRoutesLabel() {
        return expressRoutesLabel;
    }
    
    public Label getNightRoutesLabel() {
        return nightRoutesLabel;
    }
}