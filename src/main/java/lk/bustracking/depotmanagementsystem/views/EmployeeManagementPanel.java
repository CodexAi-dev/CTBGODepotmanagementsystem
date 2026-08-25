package lk.bustracking.depotmanagementsystem.views;

import lk.bustracking.depotmanagementsystem.models.Employee;
import lk.bustracking.depotmanagementsystem.models.EmployeeLicense;
import lk.bustracking.depotmanagementsystem.services.EmployeeService;
import lk.bustracking.depotmanagementsystem.utils.AppLogger;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;
import java.util.concurrent.CompletableFuture;

/**
 * Employee Management Panel
 * Comprehensive employee management interface with modern design
 */
public class EmployeeManagementPanel {
    
    private static final Logger LOGGER = AppLogger.getLogger(EmployeeManagementPanel.class);
    
    // Services
    private final EmployeeService employeeService;
    
    // Main UI Components
    private VBox mainContainer;
    private TableView<Employee> employeeTable;
    private ObservableList<Employee> employeeData;
    private FilteredList<Employee> filteredData;
    
    // Search and Filter Components
    private TextField searchField;
    private ComboBox<String> typeFilter;
    private ComboBox<String> statusFilter;
    
    // Action Buttons
    private Button addButton;
    private Button editButton;
    private Button deleteButton;
    private Button viewButton;
    private Button licenseButton;
    private Button refreshButton;
    
    // Statistics Components
    private Label totalEmployeesLabel;
    private Label driversLabel;
    private Label mechanicsLabel;
    private Label conductorsLabel;
    
    public EmployeeManagementPanel() {
        this.employeeService = EmployeeService.getInstance();
        initializeComponents();
        setupEventHandlers();
        loadEmployeeData();
    }
    
    /**
     * Get the main container for the employee management panel
     */
    public VBox getView() {
        return mainContainer;
    }
    
    /**
     * Initialize all UI components
     */
    private void initializeComponents() {
        mainContainer = new VBox(6);
        mainContainer.setPadding(new Insets(8, 20, 8, 20));
        mainContainer.setStyle("-fx-background-color: #f8f9fc;");
        
        // Create header
        VBox header = createHeader();
        
        // Create statistics panel
        HBox statisticsPanel = createStatisticsPanel();
        
        // Create search and filter panel
        HBox searchPanel = createSearchPanel();
        
        // Create employee table
        VBox tableContainer = createEmployeeTable();
        
        // Create action buttons panel
        HBox actionPanel = createActionPanel();
        
        // Add all components to main container
        mainContainer.getChildren().addAll(
            header,
            statisticsPanel,
            searchPanel,
            tableContainer,
            actionPanel
        );
    }
    
    /**
     * Create header section
     */
    private VBox createHeader() {
        VBox header = new VBox(2);
        header.setAlignment(Pos.CENTER_LEFT);

        // Title - compact, no decorative subtitle, so the table below (the
        // important content) gets the space instead.
        Label titleLabel = new Label("Employee Management");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        header.getChildren().add(titleLabel);

        return header;
    }
    
    /**
     * Create statistics panel
     */
    private HBox createStatisticsPanel() {
        HBox statisticsPanel = new HBox(10);
        statisticsPanel.setAlignment(Pos.CENTER_LEFT);
        statisticsPanel.setPadding(new Insets(6));
        statisticsPanel.getStyleClass().add("section-card");
        
        // Total Employees
        VBox totalBox = createStatCard("Total Employees", "0", "#3498db");
        totalEmployeesLabel = (Label) ((VBox) totalBox.getChildren().get(0)).getChildren().get(0);
        
        // Drivers
        VBox driversBox = createStatCard("Drivers", "0", "#e74c3c");
        driversLabel = (Label) ((VBox) driversBox.getChildren().get(0)).getChildren().get(0);
        
        // Mechanics
        VBox mechanicsBox = createStatCard("Mechanics", "0", "#f39c12");
        mechanicsLabel = (Label) ((VBox) mechanicsBox.getChildren().get(0)).getChildren().get(0);
        
        // Conductors
        VBox conductorsBox = createStatCard("Conductors", "0", "#27ae60");
        conductorsLabel = (Label) ((VBox) conductorsBox.getChildren().get(0)).getChildren().get(0);
        
        statisticsPanel.getChildren().addAll(totalBox, driversBox, mechanicsBox, conductorsBox);
        
        return statisticsPanel;
    }
    
    /**
     * Create individual stat card
     */
    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(2);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(6, 10, 6, 10));
        card.setPrefWidth(105);
        card.getStyleClass().add("stat-card");
        card.setStyle("-fx-border-color: " + color + "; -fx-border-width: 0 0 2 0;");

        VBox valueContainer = new VBox();
        valueContainer.setAlignment(Pos.CENTER);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        valueLabel.setTextFill(Color.web(color));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", 9));
        titleLabel.setTextFill(Color.web("#7f8c8d"));

        valueContainer.getChildren().add(valueLabel);
        card.getChildren().addAll(valueContainer, titleLabel);

        return card;
    }
    
    /**
     * Create search and filter panel
     */
    private HBox createSearchPanel() {
        HBox searchPanel = new HBox(8);
        searchPanel.setAlignment(Pos.CENTER_LEFT);
        searchPanel.setPadding(new Insets(8, 12, 8, 12));
        searchPanel.getStyleClass().add("section-card");

        searchField = new TextField();
        searchField.setPromptText("Search employees...");
        searchField.setPrefWidth(180);
        searchField.setStyle("-fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");

        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("All Types", "Driver", "Conductor", "Mechanic", "Supervisor", "Office Staff");
        typeFilter.setValue("All Types");
        typeFilter.setPrefWidth(110);

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Status", "Active", "Inactive", "Terminated", "Suspended");
        statusFilter.setValue("All Status");
        statusFilter.setPrefWidth(110);

        searchPanel.getChildren().addAll(searchField, typeFilter, statusFilter);

        return searchPanel;
    }
    
    /**
     * Create employee table
     */
    private VBox createEmployeeTable() {
        VBox tableContainer = new VBox(6);
        tableContainer.getStyleClass().add("section-card");
        tableContainer.setPadding(new Insets(8));
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        // Table header
        Label tableTitle = new Label("Employee List");
        tableTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        tableTitle.setTextFill(Color.web("#2c3e50"));

        // Create table
        employeeTable = new TableView<>();
        employeeTable.getStyleClass().add("table-view");
        employeeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        employeeTable.setStyle("-fx-selection-bar: #3498db; -fx-selection-bar-non-focused: #bdc3c7;");

        // Employee Code Column
        TableColumn<Employee, String> codeColumn = new TableColumn<>("Code");
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("employeeCode"));
        codeColumn.setMinWidth(70);
        codeColumn.setStyle("-fx-alignment: CENTER;");

        // Name Column
        TableColumn<Employee, String> nameColumn = new TableColumn<>("Full Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameColumn.setMinWidth(140);

        // Type Column
        TableColumn<Employee, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("employeeType"));
        typeColumn.setMinWidth(90);
        typeColumn.setCellFactory(createTypeColumnCellFactory());

        // Position Column
        TableColumn<Employee, String> positionColumn = new TableColumn<>("Position");
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));
        positionColumn.setMinWidth(110);

        // Phone Column
        TableColumn<Employee, String> phoneColumn = new TableColumn<>("Phone");
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneColumn.setMinWidth(100);

        // Hire Date Column
        TableColumn<Employee, String> hireDateColumn = new TableColumn<>("Hire Date");
        hireDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getHireDate() != null) {
                return new ReadOnlyStringWrapper(
                    cellData.getValue().getHireDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                );
            }
            return new ReadOnlyStringWrapper("");
        });
        hireDateColumn.setMinWidth(90);
        hireDateColumn.setStyle("-fx-alignment: CENTER;");

        // Status Column
        TableColumn<Employee, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("employmentStatus"));
        statusColumn.setMinWidth(80);
        statusColumn.setCellFactory(createStatusColumnCellFactory());

        // Salary Column
        TableColumn<Employee, String> salaryColumn = new TableColumn<>("Salary");
        salaryColumn.setCellValueFactory(cellData -> {
            double salary = cellData.getValue().getSalary();
            return new ReadOnlyStringWrapper(String.format("Rs. %.2f", salary));
        });
        salaryColumn.setMinWidth(100);
        salaryColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        
        // Add columns to table
        employeeTable.getColumns().addAll(
            codeColumn, nameColumn, typeColumn, positionColumn, 
            phoneColumn, hireDateColumn, statusColumn, salaryColumn
        );
        
        // Initialize data
        employeeData = FXCollections.observableArrayList();
        filteredData = new FilteredList<>(employeeData, p -> true);
        SortedList<Employee> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(employeeTable.comparatorProperty());
        employeeTable.setItems(sortedData);
        
        // Table styling
        employeeTable.setRowFactory(tv -> {
            TableRow<Employee> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    viewEmployeeDetails(row.getItem());
                }
            });
            return row;
        });
        
        tableContainer.getChildren().addAll(tableTitle, employeeTable);
        VBox.setVgrow(employeeTable, Priority.ALWAYS);
        
        return tableContainer;
    }
    
    /**
     * Create action panel
     */
    private HBox createActionPanel() {
        HBox actionPanel = new HBox(8);
        actionPanel.setAlignment(Pos.CENTER_LEFT);
        actionPanel.setPadding(new Insets(8, 12, 8, 12));
        actionPanel.getStyleClass().add("section-card");

        // Short labels, no emoji prefixes - the shared button color already
        // signals what each action does, so the extra icon just adds width.
        addButton = new Button("+ Add");
        addButton.getStyleClass().add("btn-success");

        editButton = new Button("Edit");
        editButton.getStyleClass().add("btn-primary");
        editButton.setDisable(true);

        deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("btn-danger");
        deleteButton.setDisable(true);

        viewButton = new Button("View");
        viewButton.getStyleClass().add("btn-secondary");
        viewButton.setDisable(true);

        licenseButton = new Button("Licenses");
        licenseButton.getStyleClass().add("btn-warning");
        licenseButton.setDisable(true);

        refreshButton = new Button("↻");
        refreshButton.getStyleClass().add("btn-secondary");

        actionPanel.getChildren().addAll(
            addButton, editButton, deleteButton, viewButton, licenseButton,
            new Region(), // Spacer
            refreshButton
        );
        
        HBox.setHgrow(actionPanel.getChildren().get(actionPanel.getChildren().size() - 2), Priority.ALWAYS);
        
        return actionPanel;
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });
        
        // Filter functionality
        typeFilter.setOnAction(e -> updateFilters());
        statusFilter.setOnAction(e -> updateFilters());
        
        // Table selection handler
        employeeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
            viewButton.setDisable(!hasSelection);
            licenseButton.setDisable(!hasSelection || !newSelection.isDriver());
        });
        
        // Button actions
        addButton.setOnAction(e -> showAddEmployeeDialog());
        editButton.setOnAction(e -> editSelectedEmployee());
        deleteButton.setOnAction(e -> deleteSelectedEmployee());
        viewButton.setOnAction(e -> viewEmployeeDetails(employeeTable.getSelectionModel().getSelectedItem()));
        licenseButton.setOnAction(e -> manageLicenses(employeeTable.getSelectionModel().getSelectedItem()));
        refreshButton.setOnAction(e -> loadEmployeeData());
    }
    
    /**
     * Update table filters
     */
    private void updateFilters() {
        filteredData.setPredicate(employee -> {
            // Search filter
            String searchText = searchField.getText().toLowerCase().trim();
            if (!searchText.isEmpty()) {
                if (!employee.getEmployeeCode().toLowerCase().contains(searchText) &&
                    !employee.getFullName().toLowerCase().contains(searchText) &&
                    !employee.getEmail().toLowerCase().contains(searchText) &&
                    !employee.getPhone().toLowerCase().contains(searchText) &&
                    !employee.getNationalId().toLowerCase().contains(searchText)) {
                    return false;
                }
            }
            
            // Type filter
            String selectedType = typeFilter.getValue();
            if (!"All Types".equals(selectedType) && !employee.getEmployeeType().equals(selectedType)) {
                return false;
            }
            
            // Status filter
            String selectedStatus = statusFilter.getValue();
            if (!"All Status".equals(selectedStatus) && !employee.getEmploymentStatus().equals(selectedStatus)) {
                return false;
            }
            
            return true;
        });
    }
    
    /**
     * Load employee data from database
     */
    private void loadEmployeeData() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return employeeService.getAllEmployees();
            } catch (Exception e) {
                LOGGER.severe("Error loading employee data: " + e.getMessage());
                Platform.runLater(() -> showErrorAlert("Error", "Failed to load employee data: " + e.getMessage()));
                return List.<Employee>of();
            }
        }).thenAccept(employees -> {
            Platform.runLater(() -> {
                employeeData.clear();
                employeeData.addAll(employees);
                updateStatistics();
                LOGGER.info("Loaded " + employees.size() + " employees");
            });
        });
    }
    
    /**
     * Update statistics display
     */
    private void updateStatistics() {
        int totalEmployees = employeeData.size();
        int drivers = (int) employeeData.stream().filter(Employee::isDriver).count();
        int mechanics = (int) employeeData.stream().filter(Employee::isMechanic).count();
        int conductors = (int) employeeData.stream().filter(Employee::isConductor).count();
        
        totalEmployeesLabel.setText(String.valueOf(totalEmployees));
        driversLabel.setText(String.valueOf(drivers));
        mechanicsLabel.setText(String.valueOf(mechanics));
        conductorsLabel.setText(String.valueOf(conductors));
    }
    
    /**
     * Show add employee dialog with error handling
     */
    private void showAddEmployeeDialog() {
        try {
            LOGGER.info("Attempting to create Add Employee dialog...");
            
            // Create a simple inline dialog instead of using the complex EmployeeFormDialog
            Stage dialog = new Stage();
            dialog.setTitle("Add New Employee");
            dialog.initModality(Modality.APPLICATION_MODAL);
            
            VBox mainContainer = new VBox(20);
            mainContainer.setPadding(new Insets(30));
            mainContainer.setStyle("-fx-background-color: #f8f9fa;");
            
            // Header
            Label titleLabel = new Label("Add New Employee");
            titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            
            // Form Grid
            GridPane formGrid = new GridPane();
            formGrid.setHgap(15);
            formGrid.setVgap(15);
            formGrid.setPadding(new Insets(20));
            formGrid.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
            
            // Create form fields
            TextField employeeCodeField = new TextField(employeeService.generateEmployeeCode());
            TextField firstNameField = new TextField();
            firstNameField.setPromptText("Enter first name");
            TextField lastNameField = new TextField();
            lastNameField.setPromptText("Enter last name");
            TextField emailField = new TextField();
            emailField.setPromptText("john.doe@example.com");
            TextField phoneField = new TextField();
            phoneField.setPromptText("0771234567");
            TextField nationalIdField = new TextField();
            nationalIdField.setPromptText("199012345678V");
            ComboBox<String> employeeTypeCombo = new ComboBox<>();
            employeeTypeCombo.getItems().addAll("Driver", "Conductor", "Mechanic", "Supervisor", "Office Staff");
            employeeTypeCombo.setValue("Driver");
            TextField positionField = new TextField();
            positionField.setPromptText("Senior Driver");
            TextField salaryField = new TextField();
            salaryField.setPromptText("45000.00");
            
            // Add labels and fields to grid
            formGrid.add(new Label("Employee Code:"), 0, 0);
            formGrid.add(employeeCodeField, 1, 0);
            formGrid.add(new Label("First Name:"), 0, 1);
            formGrid.add(firstNameField, 1, 1);
            formGrid.add(new Label("Last Name:"), 0, 2);
            formGrid.add(lastNameField, 1, 2);
            formGrid.add(new Label("Email:"), 0, 3);
            formGrid.add(emailField, 1, 3);
            formGrid.add(new Label("Phone:"), 0, 4);
            formGrid.add(phoneField, 1, 4);
            formGrid.add(new Label("National ID:"), 0, 5);
            formGrid.add(nationalIdField, 1, 5);
            formGrid.add(new Label("Employee Type:"), 0, 6);
            formGrid.add(employeeTypeCombo, 1, 6);
            formGrid.add(new Label("Position:"), 0, 7);
            formGrid.add(positionField, 1, 7);
            formGrid.add(new Label("Salary:"), 0, 8);
            formGrid.add(salaryField, 1, 8);
            
            // Buttons
            HBox buttonPanel = new HBox(15);
            buttonPanel.setAlignment(Pos.CENTER);
            buttonPanel.setPadding(new Insets(20));
            
            Button saveButton = new Button("Save Employee");
            saveButton.getStyleClass().add("btn-success");

            Button cancelButton = new Button("Cancel");
            cancelButton.getStyleClass().add("btn-secondary");
            cancelButton.setOnAction(e -> dialog.close());

            saveButton.setOnAction(e -> {
                try {
                    // Validation
                    if (firstNameField.getText().trim().isEmpty() || 
                        lastNameField.getText().trim().isEmpty() ||
                        phoneField.getText().trim().isEmpty() ||
                        nationalIdField.getText().trim().isEmpty()) {
                        showErrorAlert("Validation Error", "Please fill in all required fields (Name, Phone, National ID)");
                        return;
                    }
                    
                    // Create employee
                    Employee employee = new Employee();
                    employee.setEmployeeCode(employeeCodeField.getText().trim());
                    employee.setFirstName(firstNameField.getText().trim());
                    employee.setLastName(lastNameField.getText().trim());
                    employee.setEmail(emailField.getText().trim());
                    employee.setPhone(phoneField.getText().trim());
                    employee.setNationalId(nationalIdField.getText().trim());
                    employee.setEmployeeType(employeeTypeCombo.getValue());
                    employee.setPosition(positionField.getText().trim());
                    employee.setHireDate(java.time.LocalDate.now());
                    employee.setEmploymentStatus("Active");
                    
                    try {
                        employee.setSalary(Double.parseDouble(salaryField.getText().trim()));
                    } catch (NumberFormatException ex) {
                        employee.setSalary(0.0);
                    }
                    
                    // Save employee
                    if (employeeService.addEmployee(employee)) {
                        dialog.close();
                        loadEmployeeData();
                        showSuccessAlert("Success", "Employee added successfully!");
                    } else {
                        showErrorAlert("Error", "Failed to add employee. Please try again.");
                    }
                    
                } catch (Exception ex) {
                    LOGGER.severe("Error saving employee: " + ex.getMessage());
                    ex.printStackTrace();
                    showErrorAlert("Error", "Failed to save employee: " + ex.getMessage());
                }
            });
            
            buttonPanel.getChildren().addAll(saveButton, cancelButton);
            mainContainer.getChildren().addAll(titleLabel, formGrid, buttonPanel);
            
            Scene scene = new Scene(mainContainer, 500, 650);
            dialog.setScene(scene);
            dialog.showAndWait();
            
            LOGGER.info("Add Employee dialog completed successfully");
            
        } catch (Exception e) {
            LOGGER.severe("Error creating Add Employee dialog: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Dialog Error", "Failed to open Add Employee dialog: " + e.getMessage());
        }
    }
    
    /**
     * Edit selected employee with error handling
     */
    private void editSelectedEmployee() {
        Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();
        if (selectedEmployee == null) {
            showErrorAlert("Selection Error", "Please select an employee to edit.");
            return;
        }
        
        try {
            LOGGER.info("Attempting to create Edit Employee dialog...");
            
            // Create a simple inline dialog for editing
            Stage dialog = new Stage();
            dialog.setTitle("Edit Employee - " + selectedEmployee.getFullName());
            dialog.initModality(Modality.APPLICATION_MODAL);
            
            VBox mainContainer = new VBox(20);
            mainContainer.setPadding(new Insets(30));
            mainContainer.setStyle("-fx-background-color: #f8f9fa;");
            
            // Header
            Label titleLabel = new Label("Edit Employee - " + selectedEmployee.getFullName());
            titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            
            // Form Grid
            GridPane formGrid = new GridPane();
            formGrid.setHgap(15);
            formGrid.setVgap(15);
            formGrid.setPadding(new Insets(20));
            formGrid.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
            
            // Create form fields with existing data
            TextField employeeCodeField = new TextField(selectedEmployee.getEmployeeCode());
            employeeCodeField.setDisable(true); // Can't change employee code
            TextField firstNameField = new TextField(selectedEmployee.getFirstName());
            TextField lastNameField = new TextField(selectedEmployee.getLastName());
            TextField emailField = new TextField(selectedEmployee.getEmail());
            TextField phoneField = new TextField(selectedEmployee.getPhone());
            TextField nationalIdField = new TextField(selectedEmployee.getNationalId());
            ComboBox<String> employeeTypeCombo = new ComboBox<>();
            employeeTypeCombo.getItems().addAll("Driver", "Conductor", "Mechanic", "Supervisor", "Office Staff");
            employeeTypeCombo.setValue(selectedEmployee.getEmployeeType());
            TextField positionField = new TextField(selectedEmployee.getPosition());
            TextField salaryField = new TextField(String.valueOf(selectedEmployee.getSalary()));
            ComboBox<String> statusCombo = new ComboBox<>();
            statusCombo.getItems().addAll("Active", "Inactive", "Terminated", "Suspended");
            statusCombo.setValue(selectedEmployee.getEmploymentStatus());
            
            // Add labels and fields to grid
            formGrid.add(new Label("Employee Code:"), 0, 0);
            formGrid.add(employeeCodeField, 1, 0);
            formGrid.add(new Label("First Name:"), 0, 1);
            formGrid.add(firstNameField, 1, 1);
            formGrid.add(new Label("Last Name:"), 0, 2);
            formGrid.add(lastNameField, 1, 2);
            formGrid.add(new Label("Email:"), 0, 3);
            formGrid.add(emailField, 1, 3);
            formGrid.add(new Label("Phone:"), 0, 4);
            formGrid.add(phoneField, 1, 4);
            formGrid.add(new Label("National ID:"), 0, 5);
            formGrid.add(nationalIdField, 1, 5);
            formGrid.add(new Label("Employee Type:"), 0, 6);
            formGrid.add(employeeTypeCombo, 1, 6);
            formGrid.add(new Label("Position:"), 0, 7);
            formGrid.add(positionField, 1, 7);
            formGrid.add(new Label("Salary:"), 0, 8);
            formGrid.add(salaryField, 1, 8);
            formGrid.add(new Label("Status:"), 0, 9);
            formGrid.add(statusCombo, 1, 9);
            
            // Buttons
            HBox buttonPanel = new HBox(15);
            buttonPanel.setAlignment(Pos.CENTER);
            buttonPanel.setPadding(new Insets(20));
            
            Button saveButton = new Button("Update Employee");
            saveButton.getStyleClass().add("btn-primary");

            Button cancelButton = new Button("Cancel");
            cancelButton.getStyleClass().add("btn-secondary");
            cancelButton.setOnAction(e -> dialog.close());

            saveButton.setOnAction(e -> {
                try {
                    // Validation
                    if (firstNameField.getText().trim().isEmpty() || 
                        lastNameField.getText().trim().isEmpty() ||
                        phoneField.getText().trim().isEmpty() ||
                        nationalIdField.getText().trim().isEmpty()) {
                        showErrorAlert("Validation Error", "Please fill in all required fields (Name, Phone, National ID)");
                        return;
                    }
                    
                    // Update employee
                    selectedEmployee.setFirstName(firstNameField.getText().trim());
                    selectedEmployee.setLastName(lastNameField.getText().trim());
                    selectedEmployee.setEmail(emailField.getText().trim());
                    selectedEmployee.setPhone(phoneField.getText().trim());
                    selectedEmployee.setNationalId(nationalIdField.getText().trim());
                    selectedEmployee.setEmployeeType(employeeTypeCombo.getValue());
                    selectedEmployee.setPosition(positionField.getText().trim());
                    selectedEmployee.setEmploymentStatus(statusCombo.getValue());
                    
                    try {
                        selectedEmployee.setSalary(Double.parseDouble(salaryField.getText().trim()));
                    } catch (NumberFormatException ex) {
                        showErrorAlert("Validation Error", "Please enter a valid salary amount");
                        return;
                    }
                    
                    // Save employee
                    if (employeeService.updateEmployee(selectedEmployee)) {
                        dialog.close();
                        loadEmployeeData();
                        showSuccessAlert("Success", "Employee updated successfully!");
                    } else {
                        showErrorAlert("Error", "Failed to update employee. Please try again.");
                    }
                    
                } catch (Exception ex) {
                    LOGGER.severe("Error updating employee: " + ex.getMessage());
                    ex.printStackTrace();
                    showErrorAlert("Error", "Failed to update employee: " + ex.getMessage());
                }
            });
            
            buttonPanel.getChildren().addAll(saveButton, cancelButton);
            mainContainer.getChildren().addAll(titleLabel, formGrid, buttonPanel);
            
            Scene scene = new Scene(mainContainer, 500, 700);
            dialog.setScene(scene);
            dialog.showAndWait();
            
            LOGGER.info("Edit Employee dialog completed successfully");
            
        } catch (Exception e) {
            LOGGER.severe("Error creating Edit Employee dialog: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Dialog Error", "Failed to open Edit Employee dialog: " + e.getMessage());
        }
    }
    
    /**
     * Delete selected employee
     */
    private void deleteSelectedEmployee() {
        Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();
        if (selectedEmployee != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Deletion");
            confirmAlert.setHeaderText("Delete Employee");
            confirmAlert.setContentText("Are you sure you want to delete employee: " + selectedEmployee.getFullName() + "?");
            
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    if (employeeService.deleteEmployee(selectedEmployee.getEmployeeId())) {
                        loadEmployeeData();
                        showSuccessAlert("Success", "Employee deleted successfully!");
                    } else {
                        showErrorAlert("Error", "Failed to delete employee. Please try again.");
                    }
                }
            });
        }
    }
    
    /**
     * View employee details
     */
    private void viewEmployeeDetails(Employee employee) {
        if (employee != null) {
            EmployeeDetailsDialog dialog = new EmployeeDetailsDialog(employee);
            dialog.show();
        }
    }
    
    /**
     * Manage employee licenses
     */
    private void manageLicenses(Employee employee) {
        if (employee == null) {
            showErrorAlert("Error", "Please select an employee first.");
            return;
        }
        
        if (!employee.isDriver()) {
            showErrorAlert("License Management", 
                "License management is only available for drivers.\n" +
                "Employee Type: " + employee.getEmployeeType());
            return;
        }
        
        try {
            LicenseManagementDialog dialog = new LicenseManagementDialog(employee);
            dialog.show();
            
            // Optional: Refresh data when dialog closes
            dialog.setOnHidden(e -> {
                // Refresh employee data if licenses were updated
                loadEmployeeData();
            });
            
        } catch (Exception e) {
            LOGGER.severe("Error opening license management dialog: " + e.getMessage());
            showErrorAlert("Error", "Failed to open license management dialog: " + e.getMessage());
        }
    }
    
    /**
     * Create cell factory for employee type column
     */
    private Callback<TableColumn<Employee, String>, TableCell<Employee, String>> createTypeColumnCellFactory() {
        return column -> new TableCell<Employee, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String style = switch (item) {
                        case "Driver" -> "-fx-background-color: #e8f5e8; -fx-text-fill: #27ae60;";
                        case "Conductor" -> "-fx-background-color: #e3f2fd; -fx-text-fill: #2196f3;";
                        case "Mechanic" -> "-fx-background-color: #fff3e0; -fx-text-fill: #ff9800;";
                        case "Supervisor" -> "-fx-background-color: #f3e5f5; -fx-text-fill: #9c27b0;";
                        default -> "-fx-background-color: #f5f5f5; -fx-text-fill: #666666;";
                    };
                    setStyle(style + " -fx-background-radius: 3; -fx-padding: 2 8;");
                }
            }
        };
    }
    
    /**
     * Create cell factory for status column
     */
    private Callback<TableColumn<Employee, String>, TableCell<Employee, String>> createStatusColumnCellFactory() {
        return column -> new TableCell<Employee, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String style = switch (item) {
                        case "Active" -> "-fx-background-color: #e8f5e8; -fx-text-fill: #27ae60;";
                        case "Inactive" -> "-fx-background-color: #fff3e0; -fx-text-fill: #ff9800;";
                        case "Terminated" -> "-fx-background-color: #ffebee; -fx-text-fill: #f44336;";
                        case "Suspended" -> "-fx-background-color: #fff8e1; -fx-text-fill: #ffc107;";
                        default -> "-fx-background-color: #f5f5f5; -fx-text-fill: #666666;";
                    };
                    setStyle(style + " -fx-background-radius: 3; -fx-padding: 2 8;");
                }
            }
        };
    }
    
    /**
     * Show success alert
     */
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show error alert
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}