package lk.bustracking.depotmanagementsystem.views;

import lk.bustracking.depotmanagementsystem.models.Bus;
import lk.bustracking.depotmanagementsystem.models.Route;
import lk.bustracking.depotmanagementsystem.models.User;
import lk.bustracking.depotmanagementsystem.controllers.BusController;
import lk.bustracking.depotmanagementsystem.services.BusService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

/**
 * Professional Bus Management View with complete CRUD operations
 */
public class BusManagementView {
    
    private static final Logger LOGGER = Logger.getLogger(BusManagementView.class.getName());
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    
    private BusController controller;
    private Stage primaryStage;
    private BorderPane mainLayout;
    
    // Main UI components
    private TableView<Bus> busTable;
    private ObservableList<Bus> busData;
    private TextField searchField;
    private ComboBox<Bus.OperationalStatus> statusFilterCombo;
    private ProgressIndicator loadingIndicator;
    
    // Summary cards
    private Label totalBusesLabel, activeBusesLabel, maintenanceBusesLabel, offlineBusesLabel;
    private Label utilizationLabel, needsAttentionLabel;
    
    // Dialogs
    private Stage busEditDialog;
    private Stage routeAssignmentDialog;
    private Stage busDetailsDialog;
    
    // Route data
    private ObservableList<Route> routeData;
    
    // Current user context
    private User currentUser;
    
    // Constructor
    public BusManagementView(User user) {
        this.currentUser = user;
        this.controller = new BusController(this, user);
        this.busData = FXCollections.observableArrayList();
        this.routeData = FXCollections.observableArrayList();
    }
    
    public void start(Stage stage) {
        this.primaryStage = stage;
        initializeUI();
        
        Scene scene = new Scene(mainLayout, 1400, 900);
        stage.setTitle("Bus Fleet Management - CTB Depot System");
        stage.setScene(scene);
        stage.show();
        
        // Initialize controller
        controller.initializeController(currentUser);
        
        LOGGER.info("Bus Management View initialized successfully");
    }
    
    private void initializeUI() {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f8f9fc;");
        
        createHeader();
        createCenterContent();
        createToolbar();
    }
    
    /**
     * Get the main layout for embedding in other views
     * Call initializeUI() first to ensure UI is created
     */
    public BorderPane getMainLayoutForEmbedding() {
        if (mainLayout == null) {
            initializeUI();
            // Initialize controller without showing stage
            if (controller != null && currentUser != null) {
                controller.initializeController(currentUser);
            }
        }
        return mainLayout;
    }

    /**
     * Initialize the view for embedding in other containers
     */
    public void initializeForEmbedding(User user) {
        this.currentUser = user;
        if (mainLayout == null) {
            initializeUI();
        }
        if (controller != null) {
            controller.initializeController(currentUser);
        }
    }
    
    // =====================================================================================
    // HEADER SECTION
    // =====================================================================================
    
    private void createHeader() {
        VBox header = new VBox(8);
        header.setPadding(new Insets(10, 20, 8, 20));
        header.setStyle("-fx-background-color: white; -fx-border-width: 0 0 1 0; -fx-border-color: #e3e6f0;");

        // Title section - compact single line (icon + name), no decorative
        // subtitle, so this row stays small and the table below gets the space.
        HBox titleSection = new HBox(10);
        titleSection.setAlignment(Pos.CENTER_LEFT);

        Rectangle icon = new Rectangle(22, 22);
        icon.setFill(Color.web("#3b82f6"));
        icon.setArcWidth(6);
        icon.setArcHeight(6);

        Label title = new Label("Fleet Management");
        title.setStyle("-fx-text-fill: #111827; -fx-font-size: 16px; -fx-font-weight: bold;");

        titleSection.getChildren().addAll(icon, title);

        // Fleet summary cards
        HBox summaryCards = createFleetSummaryCards();

        header.getChildren().addAll(titleSection, summaryCards);
        mainLayout.setTop(header);
    }

    private HBox createFleetSummaryCards() {
        HBox summary = new HBox(10);
        summary.setAlignment(Pos.CENTER_LEFT);
        
        // Create summary cards
        VBox totalCard = createSummaryCard("Total Fleet", "0", "#6b7280", "🚌");
        VBox activeCard = createSummaryCard("Active", "0", "#10b981", "✓");
        VBox maintenanceCard = createSummaryCard("Maintenance", "0", "#f59e0b", "🔧");
        VBox offlineCard = createSummaryCard("Offline", "0", "#ef4444", "⚠");
        VBox utilizationCard = createSummaryCard("Utilization", "0%", "#8b5cf6", "📊");
        // "Needs Attention" counts buses that are overdue for maintenance, in
        // poor condition, or reporting GPS problems (Bus.needsAttention()) -
        // this replaces a card that used to show a permanently hardcoded 85.2%.
        VBox attentionCard = createSummaryCard("Needs Attention", "0", "#06b6d4", "⚠️");

        // Store labels for updates
        totalBusesLabel = (Label) totalCard.getChildren().get(1);
        activeBusesLabel = (Label) activeCard.getChildren().get(1);
        maintenanceBusesLabel = (Label) maintenanceCard.getChildren().get(1);
        offlineBusesLabel = (Label) offlineCard.getChildren().get(1);
        utilizationLabel = (Label) utilizationCard.getChildren().get(1);
        needsAttentionLabel = (Label) attentionCard.getChildren().get(1);

        summary.getChildren().addAll(totalCard, activeCard, maintenanceCard,
                                     offlineCard, utilizationCard, attentionCard);

        return summary;
    }

    /**
     * Children stay in [icon, value, title] order - createFleetSummaryCards()
     * grabs the value label back out by index (getChildren().get(1)) to wire
     * up live updates, so keep this a flat 3-child VBox.
     */
    private VBox createSummaryCard(String title, String value, String color, String icon) {
        VBox card = new VBox(1);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(6, 10, 6, 10));
        card.setPrefWidth(105);
        card.getStyleClass().add("stat-card");

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 12px;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 16px; -fx-font-weight: bold;", color));

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 9px; -fx-font-weight: 600;");

        card.getChildren().addAll(iconLabel, valueLabel, titleLabel);

        return card;
    }
    
    // =====================================================================================
    // TOOLBAR SECTION
    // =====================================================================================
    
    private void createToolbar() {
        HBox toolbar = new HBox(12);
        toolbar.setPadding(new Insets(8, 20, 8, 20));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: white; -fx-border-width: 0 0 1 0; -fx-border-color: #e5e7eb;");

        // Search and filter section
        HBox searchSection = new HBox(8);
        searchSection.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search buses...");
        searchField.setPrefWidth(180);
        searchField.setStyle("-fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #d1d5db;");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> controller.handleSearchBuses(newVal));

        statusFilterCombo = new ComboBox<>();
        statusFilterCombo.getItems().addAll(Bus.OperationalStatus.values());
        statusFilterCombo.setPromptText("Status");
        statusFilterCombo.setPrefWidth(110);
        statusFilterCombo.setOnAction(e -> controller.handleFilterByStatus(statusFilterCombo.getValue()));

        Button clearFiltersBtn = createToolbarButton("Clear", "btn-secondary");
        clearFiltersBtn.setOnAction(e -> {
            searchField.clear();
            statusFilterCombo.setValue(null);
            controller.refreshData();
        });

        searchSection.getChildren().addAll(searchField, statusFilterCombo, clearFiltersBtn);

        // Action buttons section - short labels so nothing gets clipped
        // when this toolbar shares the window with the sidebar and header.
        HBox actionSection = new HBox(8);
        actionSection.setAlignment(Pos.CENTER_RIGHT);

        Button addBusBtn = createActionButton("+ Add", "btn-success");
        addBusBtn.setOnAction(e -> showAddBusDialog());

        Button assignRouteBtn = createActionButton("Assign", "btn-primary");
        assignRouteBtn.setOnAction(e -> {
            Bus selectedBus = busTable.getSelectionModel().getSelectedItem();
            if (selectedBus != null) {
                showRouteAssignmentDialog(selectedBus);
            } else {
                showWarningMessage("Please select a bus to assign a route");
            }
        });

        Button maintenanceBtn = createActionButton("Attention", "btn-warning");
        maintenanceBtn.setOnAction(e -> controller.handleShowBusesNeedingAttention());

        Button refreshBtn = createActionButton("↻", "btn-secondary");
        refreshBtn.setOnAction(e -> controller.refreshData());

        Button exportBtn = createActionButton("Export", "btn-secondary");
        exportBtn.setOnAction(e -> showExportDialog());

        actionSection.getChildren().addAll(addBusBtn, assignRouteBtn, maintenanceBtn, refreshBtn, exportBtn);

        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(16, 16);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(searchSection, spacer, actionSection, loadingIndicator);
        
        // Create container for toolbar
        VBox toolbarContainer = new VBox();
        toolbarContainer.getChildren().add(toolbar);
        
        // Add toolbar to main layout
        mainLayout.setBottom(toolbarContainer);
    }
    
    /**
     * Create a toolbar button using a shared CSS class (e.g. "btn-secondary")
     * defined in modern-dashboard.css, instead of a one-off inline style.
     */
    private Button createToolbarButton(String text, String styleClass) {
        Button btn = new Button(text);
        btn.getStyleClass().add(styleClass);
        return btn;
    }

    private Button createActionButton(String text, String styleClass) {
        Button btn = new Button(text);
        btn.getStyleClass().add(styleClass);
        return btn;
    }
    
    // =====================================================================================
    // MAIN CONTENT - BUS TABLE
    // =====================================================================================
    
    private void createCenterContent() {
        VBox centerContent = new VBox();
        centerContent.setPadding(new Insets(8, 20, 8, 20));
        centerContent.setStyle("-fx-background-color: #f8f9fc;");

        // Table container - the table is the important content here, so this
        // wrapper stays thin (small padding, small title) to leave the table
        // as much vertical room as possible.
        VBox tableContainer = new VBox(6);
        tableContainer.getStyleClass().add("section-card");
        tableContainer.setPadding(new Insets(10));

        Label tableTitle = new Label("Fleet Overview");
        tableTitle.setStyle("-fx-text-fill: #111827; -fx-font-size: 13px; -fx-font-weight: bold;");

        busTable = createBusTable();
        
        tableContainer.getChildren().addAll(tableTitle, busTable);
        centerContent.getChildren().add(tableContainer);
        
        VBox.setVgrow(tableContainer, Priority.ALWAYS);
        VBox.setVgrow(busTable, Priority.ALWAYS);
        
        mainLayout.setCenter(centerContent);
    }
    
    @SuppressWarnings("unchecked")
    private TableView<Bus> createBusTable() {
        TableView<Bus> table = new TableView<>();
        table.setItems(busData);
        table.getStyleClass().add("table-view");
        // Let columns stretch to fill the available width instead of leaving dead space
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Bus Number Column
        TableColumn<Bus, String> busNumberCol = new TableColumn<>("Bus Number");
        busNumberCol.setCellValueFactory(new PropertyValueFactory<>("busNumber"));
        busNumberCol.setMinWidth(90);
        busNumberCol.setCellFactory(col -> new TableCell<Bus, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #3b82f6;");
                }
            }
        });

        // Registration Column
        TableColumn<Bus, String> registrationCol = new TableColumn<>("Registration");
        registrationCol.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        registrationCol.setMinWidth(100);

        // Make & Model Column
        TableColumn<Bus, String> makeModelCol = new TableColumn<>("Make & Model");
        makeModelCol.setCellValueFactory(cellData -> {
            Bus bus = cellData.getValue();
            String makeModel = (bus.getMake() != null ? bus.getMake() : "") + " " +
                              (bus.getModel() != null ? bus.getModel() : "");
            return new javafx.beans.property.SimpleStringProperty(makeModel.trim());
        });
        makeModelCol.setMinWidth(130);

        // Year Column
        TableColumn<Bus, Number> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getYearManufactured()));
        yearCol.setMinWidth(60);

        // Capacity Column
        TableColumn<Bus, Number> capacityCol = new TableColumn<>("Capacity");
        capacityCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getSeatingCapacity()));
        capacityCol.setMinWidth(70);

        // Mileage Column
        TableColumn<Bus, String> mileageCol = new TableColumn<>("Mileage");
        mileageCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedMileage()));
        mileageCol.setMinWidth(90);

        // Status Column with colored indicators
        TableColumn<Bus, Bus.OperationalStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("operationalStatus"));
        statusCol.setMinWidth(100);
        statusCol.setCellFactory(col -> new TableCell<Bus, Bus.OperationalStatus>() {
            @Override
            protected void updateItem(Bus.OperationalStatus status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox statusBox = new HBox(8);
                    statusBox.setAlignment(Pos.CENTER_LEFT);

                    Circle indicator = new Circle(5);
                    String color = switch(status) {
                        case ACTIVE -> "#10b981";
                        case MAINTENANCE -> "#f59e0b";
                        case OUT_OF_SERVICE -> "#ef4444";
                        case RETIRED -> "#6b7280";
                    };
                    indicator.setFill(Color.web(color));

                    Label statusLabel = new Label(status.getDisplayName());
                    statusLabel.setStyle("-fx-font-weight: 500;");

                    statusBox.getChildren().addAll(indicator, statusLabel);
                    setGraphic(statusBox);
                    setText(null);
                }
            }
        });

        // Current Route Column
        TableColumn<Bus, String> routeCol = new TableColumn<>("Current Route");
        routeCol.setCellValueFactory(cellData -> {
            String routeDisplay = cellData.getValue().getCurrentRouteDisplay();
            return new javafx.beans.property.SimpleStringProperty(routeDisplay);
        });
        routeCol.setMinWidth(130);

        // Condition Column
        TableColumn<Bus, Bus.ConditionRating> conditionCol = new TableColumn<>("Condition");
        conditionCol.setCellValueFactory(new PropertyValueFactory<>("conditionRating"));
        conditionCol.setMinWidth(90);
        conditionCol.setCellFactory(col -> new TableCell<Bus, Bus.ConditionRating>() {
            @Override
            protected void updateItem(Bus.ConditionRating condition, boolean empty) {
                super.updateItem(condition, empty);
                getStyleClass().removeAll("status-badge", "status-badge-success", "status-badge-warning", "status-badge-danger");
                if (empty || condition == null) {
                    setText(null);
                } else {
                    setText(condition.getDisplayName());
                    String styleClass = switch(condition) {
                        case EXCELLENT, GOOD -> "status-badge-success";
                        case FAIR -> "status-badge-warning";
                        case POOR -> "status-badge-danger";
                    };
                    getStyleClass().addAll("status-badge", styleClass);
                }
            }
        });

        // Next Service Column
        TableColumn<Bus, Bus> nextServiceCol = new TableColumn<>("Next Service");
        nextServiceCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue()));
        nextServiceCol.setMinWidth(110);
        nextServiceCol.setCellFactory(col -> new TableCell<Bus, Bus>() {
            @Override
            protected void updateItem(Bus bus, boolean empty) {
                super.updateItem(bus, empty);
                getStyleClass().removeAll("status-badge", "status-badge-warning", "status-badge-neutral");
                if (empty || bus == null) {
                    setText(null);
                } else if (bus.getNextServiceDueDate() == null) {
                    setText("Not scheduled");
                    getStyleClass().addAll("status-badge", "status-badge-neutral");
                } else {
                    setText(bus.getNextServiceDueDate().format(DATE_FORMAT));
                    if (bus.isMaintenanceDue()) {
                        getStyleClass().addAll("status-badge", "status-badge-warning");
                    }
                }
            }
        });

        // GPS Status Column
        TableColumn<Bus, Boolean> gpsCol = new TableColumn<>("GPS");
        gpsCol.setCellValueFactory(cellData -> {
            boolean isOnline = cellData.getValue().isOnline();
            return new javafx.beans.property.SimpleBooleanProperty(isOnline);
        });
        gpsCol.setMinWidth(80);
        gpsCol.setCellFactory(col -> new TableCell<Bus, Boolean>() {
            @Override
            protected void updateItem(Boolean online, boolean empty) {
                super.updateItem(online, empty);
                if (empty || online == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Circle indicator = new Circle(6);
                    indicator.setFill(online ? Color.web("#10b981") : Color.web("#ef4444"));

                    Label statusText = new Label(online ? "Online" : "Offline");
                    statusText.getStyleClass().addAll("status-badge", online ? "status-badge-success" : "status-badge-danger");

                    VBox statusBox = new VBox(2);
                    statusBox.setAlignment(Pos.CENTER);
                    statusBox.getChildren().addAll(indicator, statusText);

                    setGraphic(statusBox);
                    setText(null);
                }
            }
        });

        // Actions Column
        TableColumn<Bus, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setMinWidth(120);
        actionsCol.setCellFactory(col -> new TableCell<Bus, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button detailsBtn = new Button("Details");

            {
                editBtn.getStyleClass().addAll("btn-small", "btn-primary");
                detailsBtn.getStyleClass().addAll("btn-small", "btn-secondary");

                editBtn.setOnAction(e -> {
                    Bus bus = getTableView().getItems().get(getIndex());
                    showEditBusDialog(bus);
                });

                detailsBtn.setOnAction(e -> {
                    Bus bus = getTableView().getItems().get(getIndex());
                    showBusDetails(bus);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox actionBox = new HBox(5);
                    actionBox.setAlignment(Pos.CENTER);
                    actionBox.getChildren().addAll(editBtn, detailsBtn);
                    setGraphic(actionBox);
                }
            }
        });

        table.getColumns().addAll(busNumberCol, registrationCol, makeModelCol, yearCol, capacityCol,
                                 mileageCol, statusCol, routeCol, conditionCol, nextServiceCol, gpsCol, actionsCol);
        
        // Row double-click to show details
        table.setRowFactory(tv -> {
            TableRow<Bus> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showBusDetails(row.getItem());
                }
            });
            return row;
        });
        
        return table;
    }
    
    // =====================================================================================
    // DIALOG WINDOWS
    // =====================================================================================
    
    public void showAddBusDialog() {
        showBusEditDialog(null); // null means create new bus
    }
    
    public void showEditBusDialog(Bus bus) {
        showBusEditDialog(bus);
    }
    
    private void showBusEditDialog(Bus bus) {
        boolean isEdit = (bus != null);
        
        busEditDialog = new Stage();
        busEditDialog.initModality(Modality.APPLICATION_MODAL);
        busEditDialog.setTitle(isEdit ? "Edit Bus - " + bus.getBusNumber() : "Add New Bus");
        busEditDialog.setResizable(false);
        
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setStyle("-fx-background-color: white;");
        
        // Form fields
        GridPane formGrid = new GridPane();
        formGrid.setHgap(20);
        formGrid.setVgap(15);
        formGrid.setAlignment(Pos.TOP_LEFT);
        
        // Bus Number
        Label busNumberLabel = new Label("Bus Number*:");
        busNumberLabel.setStyle("-fx-font-weight: 600;");
        TextField busNumberField = new TextField();
        busNumberField.setPromptText("e.g., CTB-245");
        
        // Registration Number
        Label regLabel = new Label("Registration Number*:");
        regLabel.setStyle("-fx-font-weight: 600;");
        TextField regField = new TextField();
        regField.setPromptText("e.g., WP CAB-2458");
        
        // Make
        Label makeLabel = new Label("Make:");
        makeLabel.setStyle("-fx-font-weight: 600;");
        ComboBox<String> makeCombo = new ComboBox<>();
        makeCombo.getItems().addAll("Tata", "Ashok Leyland", "Mahindra", "Eicher", "BharatBenz");
        makeCombo.setEditable(true);
        
        // Model
        Label modelLabel = new Label("Model:");
        modelLabel.setStyle("-fx-font-weight: 600;");
        TextField modelField = new TextField();
        modelField.setPromptText("e.g., LP 909/42");
        
        // Year
        Label yearLabel = new Label("Year:");
        yearLabel.setStyle("-fx-font-weight: 600;");
        Spinner<Integer> yearSpinner = new Spinner<>(1990, LocalDate.now().getYear() + 1, LocalDate.now().getYear());
        
        // Seating Capacity
        Label capacityLabel = new Label("Seating Capacity*:");
        capacityLabel.setStyle("-fx-font-weight: 600;");
        Spinner<Integer> capacitySpinner = new Spinner<>(20, 100, 50);
        
        // Fuel Tank Capacity
        Label fuelLabel = new Label("Fuel Tank (Liters):");
        fuelLabel.setStyle("-fx-font-weight: 600;");
        TextField fuelField = new TextField();
        fuelField.setPromptText("e.g., 200.0");
        
        // GPS Device ID
        Label gpsLabel = new Label("GPS Device ID*:");
        gpsLabel.setStyle("-fx-font-weight: 600;");
        TextField gpsField = new TextField();
        gpsField.setPromptText("e.g., GPS001245");
        
        // Status
        Label statusLabel = new Label("Status:");
        statusLabel.setStyle("-fx-font-weight: 600;");
        ComboBox<Bus.OperationalStatus> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(Bus.OperationalStatus.values());
        statusCombo.setValue(Bus.OperationalStatus.ACTIVE);
        
        // Condition
        Label conditionLabel = new Label("Condition:");
        conditionLabel.setStyle("-fx-font-weight: 600;");
        ComboBox<Bus.ConditionRating> conditionCombo = new ComboBox<>();
        conditionCombo.getItems().addAll(Bus.ConditionRating.values());
        conditionCombo.setValue(Bus.ConditionRating.GOOD);
        
        // Add fields to grid
        formGrid.add(busNumberLabel, 0, 0);
        formGrid.add(busNumberField, 1, 0);
        formGrid.add(regLabel, 0, 1);
        formGrid.add(regField, 1, 1);
        formGrid.add(makeLabel, 0, 2);
        formGrid.add(makeCombo, 1, 2);
        formGrid.add(modelLabel, 0, 3);
        formGrid.add(modelField, 1, 3);
        formGrid.add(yearLabel, 0, 4);
        formGrid.add(yearSpinner, 1, 4);
        formGrid.add(capacityLabel, 0, 5);
        formGrid.add(capacitySpinner, 1, 5);
        formGrid.add(fuelLabel, 0, 6);
        formGrid.add(fuelField, 1, 6);
        formGrid.add(gpsLabel, 0, 7);
        formGrid.add(gpsField, 1, 7);
        formGrid.add(statusLabel, 0, 8);
        formGrid.add(statusCombo, 1, 8);
        formGrid.add(conditionLabel, 0, 9);
        formGrid.add(conditionCombo, 1, 9);
        
        // Set column constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(150);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setMinWidth(200);
        formGrid.getColumnConstraints().addAll(col1, col2);
        
        // Pre-fill data for edit mode
        if (isEdit) {
            busNumberField.setText(bus.getBusNumber());
            regField.setText(bus.getRegistrationNumber());
            makeCombo.setValue(bus.getMake());
            modelField.setText(bus.getModel());
            yearSpinner.getValueFactory().setValue(bus.getYearManufactured());
            capacitySpinner.getValueFactory().setValue(bus.getSeatingCapacity());
            if (bus.getFuelTankCapacity() != null) {
                fuelField.setText(bus.getFuelTankCapacity().toString());
            }
            gpsField.setText(bus.getGpsDeviceId());
            statusCombo.setValue(bus.getOperationalStatus());
            conditionCombo.setValue(bus.getConditionRating());
        }
        
        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-secondary");
        cancelBtn.setOnAction(e -> busEditDialog.close());

        Button saveBtn = new Button(isEdit ? "Update Bus" : "Create Bus");
        saveBtn.getStyleClass().add("btn-success");
        saveBtn.setOnAction(e -> {
            // Validation
            if (busNumberField.getText().trim().isEmpty() || 
                regField.getText().trim().isEmpty() || 
                gpsField.getText().trim().isEmpty()) {
                showErrorMessage("Please fill in all required fields (marked with *)");
                return;
            }
            
            // Create or update bus object
            try {
                Bus busToSave = isEdit ? bus : new Bus();
                
                busToSave.setBusNumber(busNumberField.getText().trim());
                busToSave.setRegistrationNumber(regField.getText().trim());
                busToSave.setMake(makeCombo.getValue());
                busToSave.setModel(modelField.getText().trim());
                busToSave.setYearManufactured(yearSpinner.getValue());
                busToSave.setSeatingCapacity(capacitySpinner.getValue());
                
                if (!fuelField.getText().trim().isEmpty()) {
                    busToSave.setFuelTankCapacity(new BigDecimal(fuelField.getText().trim()));
                }
                
                busToSave.setGpsDeviceId(gpsField.getText().trim());
                busToSave.setOperationalStatus(statusCombo.getValue());
                busToSave.setConditionRating(conditionCombo.getValue());
                
                // Call controller to save
                if (isEdit) {
                    controller.handleUpdateBus(busToSave);
                } else {
                    controller.handleCreateBus(busToSave);
                }
                
                busEditDialog.close();
                
            } catch (NumberFormatException ex) {
                showErrorMessage("Please enter valid numeric values for fuel tank capacity");
            } catch (Exception ex) {
                showErrorMessage("Error saving bus: " + ex.getMessage());
                LOGGER.severe("Error saving bus: " + ex.getMessage());
            }
        });
        
        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        
        // Add required field note
        Label requiredNote = new Label("* Required fields");
        requiredNote.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px; -fx-font-style: italic;");
        
        dialogContent.getChildren().addAll(
            new Label(isEdit ? "Edit Bus Information" : "Add New Bus to Fleet"),
            formGrid,
            requiredNote,
            buttonBox
        );
        
        Scene dialogScene = new Scene(dialogContent, 500, 600);
        busEditDialog.setScene(dialogScene);
        busEditDialog.showAndWait();
    }
    
    // =====================================================================================
    // ROUTE ASSIGNMENT DIALOG
    // =====================================================================================
    
    private void showRouteAssignmentDialog(Bus selectedBus) {
        routeAssignmentDialog = new Stage();
        routeAssignmentDialog.initModality(Modality.APPLICATION_MODAL);
        routeAssignmentDialog.setTitle("Assign Route - " + selectedBus.getBusNumber());
        
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setStyle("-fx-background-color: white;");
        
        // Current assignment info
        VBox currentInfo = new VBox(10);
        Label currentTitle = new Label("Current Assignment:");
        currentTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label currentRoute = new Label("Route: " + selectedBus.getCurrentRouteDisplay());
        Label busInfo = new Label("Bus: " + selectedBus.getBusNumber() + " (" + selectedBus.getRegistrationNumber() + ")");
        
        currentInfo.getChildren().addAll(currentTitle, currentRoute, busInfo);
        
        // Route selection
        VBox routeSelection = new VBox(10);
        Label routeTitle = new Label("Select New Route:");
        routeTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        ComboBox<Route> routeCombo = new ComboBox<>();
        routeCombo.setItems(routeData);
        routeCombo.setPrefWidth(400);
        routeCombo.setPromptText("Choose a route...");
        
        // Route details area
        TextArea routeDetails = new TextArea();
        routeDetails.setEditable(false);
        routeDetails.setPrefHeight(100);
        routeDetails.setPromptText("Route details will appear here...");
        
        routeSelection.getChildren().addAll(routeTitle, routeCombo, routeDetails);
        
        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-secondary");
        cancelBtn.setOnAction(e -> routeAssignmentDialog.close());

        Button assignBtn = new Button("Assign Route");
        assignBtn.getStyleClass().add("btn-primary");
        assignBtn.setOnAction(e -> {
            Route selectedRoute = routeCombo.getValue();
            if (selectedRoute != null) {
                controller.handleAssignRoute(selectedBus, selectedRoute);
                routeAssignmentDialog.close();
            } else {
                showWarningMessage("Please select a route to assign");
            }
        });
        
        buttonBox.getChildren().addAll(cancelBtn, assignBtn);
        
        dialogContent.getChildren().addAll(currentInfo, new Separator(), routeSelection, buttonBox);
        
        Scene dialogScene = new Scene(dialogContent, 500, 400);
        routeAssignmentDialog.setScene(dialogScene);
        routeAssignmentDialog.showAndWait();
    }
    
    // =====================================================================================
    // BUS DETAILS DIALOG
    // =====================================================================================
    
    private void showBusDetails(Bus bus) {
        busDetailsDialog = new Stage();
        busDetailsDialog.initModality(Modality.APPLICATION_MODAL);
        busDetailsDialog.setTitle("Bus Details - " + bus.getBusNumber());
        busDetailsDialog.setResizable(false);
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(600, 500);
        
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setStyle("-fx-background-color: white;");
        
        // Header with bus info
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("section-card");
        
        Circle statusIndicator = new Circle(12);
        String statusColor = switch(bus.getOperationalStatus()) {
            case ACTIVE -> "#10b981";
            case MAINTENANCE -> "#f59e0b";
            case OUT_OF_SERVICE -> "#ef4444";
            case RETIRED -> "#6b7280";
        };
        statusIndicator.setFill(Color.web(statusColor));
        
        VBox headerText = new VBox(5);
        Label busNumber = new Label("Bus " + bus.getBusNumber());
        busNumber.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        
        Label registrationNum = new Label(bus.getRegistrationNumber());
        registrationNum.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280;");
        
        headerText.getChildren().addAll(busNumber, registrationNum);
        header.getChildren().addAll(statusIndicator, headerText);
        
        // Details sections
        VBox detailsContainer = new VBox(20);
        
        // Basic Information
        VBox basicInfo = createDetailsSection("Basic Information", 
            "Make", bus.getMake() != null ? bus.getMake() : "N/A",
            "Model", bus.getModel() != null ? bus.getModel() : "N/A",
            "Year", String.valueOf(bus.getYearManufactured()),
            "Seating Capacity", String.valueOf(bus.getSeatingCapacity()),
            "Fuel Tank Capacity", bus.getFuelTankCapacity() != null ? bus.getFuelTankCapacity() + " L" : "N/A"
        );
        
        // Operational Status
        VBox operationalInfo = createDetailsSection("Operational Status",
            "Status", bus.getOperationalStatus().getDisplayName(),
            "Condition", bus.getConditionRating().getDisplayName(),
            "Current Route", bus.getCurrentRouteDisplay(),
            "GPS Device", bus.getGpsDeviceId() != null ? bus.getGpsDeviceId() : "N/A",
            "GPS Status", bus.isOnline() ? "Online" : "Offline"
        );
        
        // Maintenance & Usage - all real fields already on the Bus object
        VBox maintenanceInfo = createDetailsSection("Maintenance & Usage",
            "Total Mileage", bus.getFormattedMileage(),
            "Last Service", bus.getLastServiceDate() != null ? bus.getLastServiceDate().format(DATE_FORMAT) : "Not recorded",
            "Next Service Due", bus.getNextServiceDueDate() != null ? bus.getNextServiceDueDate().format(DATE_FORMAT) : "Not scheduled",
            "Purchase Date", bus.getPurchaseDate() != null ? bus.getPurchaseDate().format(DATE_FORMAT) : "N/A",
            "Purchase Cost", bus.getPurchaseCost() != null ? "Rs. " + bus.getPurchaseCost() : "N/A"
        );

        // Performance Metrics - honestly empty until real trip data exists
        // (Trip_Logs has no rows yet), instead of showing fabricated numbers.
        VBox performanceInfo = createDetailsSection("Performance Metrics",
            "On-Time Performance", "No trip data yet",
            "Fuel Efficiency", "No trip data yet"
        );

        detailsContainer.getChildren().addAll(basicInfo, operationalInfo, maintenanceInfo, performanceInfo);
        
        // Action buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button editBtn = new Button("Edit Bus");
        editBtn.getStyleClass().add("btn-primary");
        editBtn.setOnAction(e -> {
            busDetailsDialog.close();
            showEditBusDialog(bus);
        });

        Button assignRouteBtn = new Button("Assign Route");
        assignRouteBtn.getStyleClass().add("btn-success");
        assignRouteBtn.setOnAction(e -> {
            busDetailsDialog.close();
            showRouteAssignmentDialog(bus);
        });

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("btn-secondary");
        closeBtn.setOnAction(e -> busDetailsDialog.close());
        
        buttonBox.getChildren().addAll(editBtn, assignRouteBtn, closeBtn);
        
        dialogContent.getChildren().addAll(header, detailsContainer, buttonBox);
        scrollPane.setContent(dialogContent);
        
        Scene dialogScene = new Scene(scrollPane, 600, 500);
        busDetailsDialog.setScene(dialogScene);
        busDetailsDialog.showAndWait();
    }
    
    private VBox createDetailsSection(String sectionTitle, String... keyValuePairs) {
        VBox section = new VBox(10);
        section.getStyleClass().add("section-card");
        
        Label title = new Label(sectionTitle);
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(8);
        
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            int row = i / 2;
            
            Label keyLabel = new Label(keyValuePairs[i] + ":");
            keyLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #374151;");
            
            Label valueLabel = new Label(keyValuePairs[i + 1]);
            valueLabel.setStyle("-fx-text-fill: #6b7280;");
            
            grid.add(keyLabel, 0, row);
            grid.add(valueLabel, 1, row);
        }
        
        section.getChildren().addAll(title, grid);
        return section;
    }
    
    // =====================================================================================
    // EXPORT DIALOG
    // =====================================================================================
    
    private void showExportDialog() {
        Stage exportDialog = new Stage();
        exportDialog.initModality(Modality.APPLICATION_MODAL);
        exportDialog.setTitle("Export Fleet Data");
        
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setStyle("-fx-background-color: white;");
        
        Label title = new Label("Export Options");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Export format selection
        VBox formatSection = new VBox(10);
        Label formatLabel = new Label("Select Format:");
        formatLabel.setStyle("-fx-font-weight: 600;");
        
        ToggleGroup formatGroup = new ToggleGroup();
        RadioButton csvRadio = new RadioButton("CSV (Comma Separated Values)");
        RadioButton excelRadio = new RadioButton("Excel Spreadsheet");
        RadioButton pdfRadio = new RadioButton("PDF Report");
        
        csvRadio.setToggleGroup(formatGroup);
        excelRadio.setToggleGroup(formatGroup);
        pdfRadio.setToggleGroup(formatGroup);
        csvRadio.setSelected(true);
        
        formatSection.getChildren().addAll(formatLabel, csvRadio, excelRadio, pdfRadio);
        
        // Export options
        VBox optionsSection = new VBox(10);
        Label optionsLabel = new Label("Export Options:");
        optionsLabel.setStyle("-fx-font-weight: 600;");
        
        CheckBox includeInactive = new CheckBox("Include inactive buses");
        CheckBox includeDetails = new CheckBox("Include detailed information");
        CheckBox includeHistory = new CheckBox("Include maintenance history");
        
        includeDetails.setSelected(true);
        
        optionsSection.getChildren().addAll(optionsLabel, includeInactive, includeDetails, includeHistory);
        
        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-secondary");
        cancelBtn.setOnAction(e -> exportDialog.close());

        Button exportBtn = new Button("Export");
        exportBtn.getStyleClass().add("btn-primary");
        exportBtn.setOnAction(e -> {
            // Get selected format
            RadioButton selectedFormat = (RadioButton) formatGroup.getSelectedToggle();
            String format = selectedFormat.getText();
            
            // Handle export
            controller.handleExportData(format, includeInactive.isSelected(), 
                                       includeDetails.isSelected(), includeHistory.isSelected());
            exportDialog.close();
        });
        
        buttonBox.getChildren().addAll(cancelBtn, exportBtn);
        
        dialogContent.getChildren().addAll(title, formatSection, new Separator(), 
                                          optionsSection, buttonBox);
        
        Scene dialogScene = new Scene(dialogContent, 400, 350);
        exportDialog.setScene(dialogScene);
        exportDialog.showAndWait();
    }
    
    // =====================================================================================
    // PUBLIC METHODS FOR CONTROLLER
    // =====================================================================================
    
    public void updateBusData(List<Bus> buses) {
        Platform.runLater(() -> {
            busData.clear();
            busData.addAll(buses);
            updateSummaryCards();
        });
    }
    
    public void updateRouteData(List<Route> routes) {
        Platform.runLater(() -> {
            routeData.clear();
            routeData.addAll(routes);
        });
    }
    
    private void updateSummaryCards() {
        int total = busData.size();
        long active = busData.stream().filter(b -> b.getOperationalStatus() == Bus.OperationalStatus.ACTIVE).count();
        long maintenance = busData.stream().filter(b -> b.getOperationalStatus() == Bus.OperationalStatus.MAINTENANCE).count();
        long offline = busData.stream().filter(b -> !b.isOnline()).count();
        
        totalBusesLabel.setText(String.valueOf(total));
        activeBusesLabel.setText(String.valueOf(active));
        maintenanceBusesLabel.setText(String.valueOf(maintenance));
        offlineBusesLabel.setText(String.valueOf(offline));
        
        // Utilization = share of the fleet that's currently active
        double utilization = total > 0 ? ((double)active / total) * 100 : 0;
        utilizationLabel.setText(String.format("%.1f%%", utilization));

        // Needs Attention = real count from Bus.needsAttention() (maintenance
        // overdue, poor condition, or GPS issues) - not a fabricated number.
        long needsAttention = busData.stream().filter(Bus::needsAttention).count();
        needsAttentionLabel.setText(String.valueOf(needsAttention));
    }
    
    public void setLoading(boolean loading) {
        Platform.runLater(() -> loadingIndicator.setVisible(loading));
    }
    
    public void showErrorMessage(String message) {
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
        Platform.runLater(() -> busTable.refresh());
    }
    
    public void showLoading() {
        Platform.runLater(() -> {
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(true);
            }
        });
    }

    public void hideLoading() {
        Platform.runLater(() -> {
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(false);
            }
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

    public void showSuccessMessage(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void updateAvailableRoutes(List<Route> routes) {
        Platform.runLater(() -> {
            routeData.clear();
            routeData.addAll(routes);
        });
    }
    
    // Getters for controller access
    public TableView<Bus> getBusTable() { return busTable; }
    public ObservableList<Bus> getBusData() { return busData; }
    public TextField getSearchField() { return searchField; }
    public ComboBox<Bus.OperationalStatus> getStatusFilterCombo() { return statusFilterCombo; }
    public User getCurrentUser() { return currentUser; }
    public BusController getController() { return controller; }
}