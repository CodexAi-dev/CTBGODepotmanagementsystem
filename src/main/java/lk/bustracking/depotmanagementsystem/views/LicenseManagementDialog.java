package lk.bustracking.depotmanagementsystem.views;

import lk.bustracking.depotmanagementsystem.models.Employee;
import lk.bustracking.depotmanagementsystem.models.EmployeeLicense;
import lk.bustracking.depotmanagementsystem.services.EmployeeService;
import lk.bustracking.depotmanagementsystem.utils.AppLogger;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * License Management Dialog
 * Comprehensive license management for drivers
 */
public class LicenseManagementDialog extends Stage {
    
    private static final Logger LOGGER = AppLogger.getLogger(LicenseManagementDialog.class);
    
    // Services
    private final EmployeeService employeeService;
    
    // Current employee
    private final Employee employee;
    
    // UI Components
    private TableView<EmployeeLicense> licenseTable;
    private ObservableList<EmployeeLicense> licenseData;
    private Button addButton;
    private Button editButton;
    private Button deleteButton;
    private Button renewButton;
    
    public LicenseManagementDialog(Employee employee) {
        this.employeeService = EmployeeService.getInstance();
        this.employee = employee;
        
        initializeDialog();
        createContent();
        loadLicenseData();
    }
    
    /**
     * Initialize dialog properties
     */
    private void initializeDialog() {
        setTitle("License Management - " + employee.getFullName());
        initModality(Modality.APPLICATION_MODAL);
        setResizable(true);
        
        // Set size
        setWidth(900);
        setHeight(600);
        setMinWidth(800);
        setMinHeight(500);
        
        // Remove icon loading code that causes the error
        // Icon loading is optional and causing issues
    }
    
    /**
     * Create the main content
     */
    private void createContent() {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(25));
        mainContainer.setStyle("""
            -fx-background-color: linear-gradient(to bottom, #f8f9fa, #ffffff);
            """);
        
        // Create header
        VBox header = createHeader();
        
        // Create license table
        VBox tableContainer = createLicenseTable();
        
        // Create action buttons
        HBox buttonPanel = createButtonPanel();
        
        mainContainer.getChildren().addAll(header, tableContainer, buttonPanel);
        
        Scene scene = new Scene(mainContainer);
        setScene(scene);
    }
    
    /**
     * Create header section
     */
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20));
        header.setStyle("""
            -fx-background-color: linear-gradient(to right, #f39c12, #e67e22);
            -fx-background-radius: 10;
            """);
        
        // Title
        Label titleLabel = new Label("Driver License Management");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);
        
        // Employee info
        Label employeeLabel = new Label(String.format("%s (%s)", 
            employee.getFullName(), employee.getEmployeeCode()));
        employeeLabel.setFont(Font.font("Segoe UI", 16));
        employeeLabel.setTextFill(Color.web("#ecf0f1"));
        
        // Warning for expiring licenses
        Label warningLabel = new Label("⚠️ Monitor license expiry dates and renew before expiration");
        warningLabel.setFont(Font.font("Segoe UI", 12));
        warningLabel.setTextFill(Color.web("#fff3cd"));
        
        header.getChildren().addAll(titleLabel, employeeLabel, warningLabel);
        
        return header;
    }
    
    /**
     * Create license table
     */
    private VBox createLicenseTable() {
        VBox tableContainer = new VBox(15);
        
        // Table title
        Label tableTitle = new Label("Driver Licenses");
        tableTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        tableTitle.setTextFill(Color.web("#2c3e50"));
        
        // Create table
        licenseTable = new TableView<>();
        licenseTable.setStyle("""
            -fx-background-color: white;
            -fx-table-cell-border-color: #ecf0f1;
            -fx-selection-bar: #f39c12;
            -fx-selection-bar-non-focused: #bdc3c7;
            """);
        
        // License Number Column
        TableColumn<EmployeeLicense, String> numberColumn = new TableColumn<>("License Number");
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("licenseNumber"));
        numberColumn.setPrefWidth(150);
        numberColumn.setStyle("-fx-alignment: CENTER;");
        
        // License Type Column
        TableColumn<EmployeeLicense, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("licenseType"));
        typeColumn.setPrefWidth(120);
        typeColumn.setCellFactory(createTypeColumnCellFactory());
        
        // License Class Column
        TableColumn<EmployeeLicense, String> classColumn = new TableColumn<>("Class");
        classColumn.setCellValueFactory(new PropertyValueFactory<>("licenseClass"));
        classColumn.setPrefWidth(80);
        classColumn.setStyle("-fx-alignment: CENTER;");
        
        // Issue Date Column
        TableColumn<EmployeeLicense, String> issueDateColumn = new TableColumn<>("Issue Date");
        issueDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getIssueDate() != null) {
                return new ReadOnlyStringWrapper(
                    cellData.getValue().getIssueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                );
            }
            return new ReadOnlyStringWrapper("");
        });
        issueDateColumn.setPrefWidth(100);
        issueDateColumn.setStyle("-fx-alignment: CENTER;");
        
        // Expiry Date Column
        TableColumn<EmployeeLicense, String> expiryColumn = new TableColumn<>("Expiry Date");
        expiryColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getExpiryDate() != null) {
                return new ReadOnlyStringWrapper(
                    cellData.getValue().getExpiryDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                );
            }
            return new ReadOnlyStringWrapper("");
        });
        expiryColumn.setPrefWidth(100);
        expiryColumn.setStyle("-fx-alignment: CENTER;");
        
        // Days Until Expiry Column
        TableColumn<EmployeeLicense, String> daysColumn = new TableColumn<>("Days Until Expiry");
        daysColumn.setCellValueFactory(cellData -> {
            long days = cellData.getValue().getDaysUntilExpiry();
            if (days >= 0) {
                return new ReadOnlyStringWrapper(String.valueOf(days));
            } else {
                return new ReadOnlyStringWrapper("Expired");
            }
        });
        daysColumn.setPrefWidth(120);
        daysColumn.setCellFactory(createDaysColumnCellFactory());
        
        // Status Column
        TableColumn<EmployeeLicense, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().getStatusDescription())
        );
        statusColumn.setPrefWidth(120);
        statusColumn.setCellFactory(createStatusColumnCellFactory());
        
        // Issuing Authority Column
        TableColumn<EmployeeLicense, String> authorityColumn = new TableColumn<>("Issuing Authority");
        authorityColumn.setCellValueFactory(new PropertyValueFactory<>("issuingAuthority"));
        authorityColumn.setPrefWidth(180);
        
        // Add columns to table
        licenseTable.getColumns().addAll(
            numberColumn, typeColumn, classColumn, issueDateColumn, 
            expiryColumn, daysColumn, statusColumn, authorityColumn
        );
        
        // Initialize data
        licenseData = FXCollections.observableArrayList();
        licenseTable.setItems(licenseData);
        
        // Selection handler
        licenseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
            renewButton.setDisable(!hasSelection);
        });
        
        // Double-click to edit
        licenseTable.setRowFactory(tv -> {
            TableRow<EmployeeLicense> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editSelectedLicense();
                }
            });
            return row;
        });
        
        tableContainer.getChildren().addAll(tableTitle, licenseTable);
        VBox.setVgrow(licenseTable, Priority.ALWAYS);
        
        return tableContainer;
    }
    
    /**
     * Create button panel
     */
    private HBox createButtonPanel() {
        HBox buttonPanel = new HBox(15);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setPadding(new Insets(20));
        buttonPanel.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
            """);
        
        // Add License Button
        addButton = new Button("➕ Add License");
        addButton.setStyle("""
            -fx-background-color: #27ae60;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 12 25;
            -fx-background-radius: 5;
            -fx-cursor: hand;
            -fx-font-size: 14;
            """);
        addButton.setOnAction(e -> addNewLicense());
        
        // Edit License Button
        editButton = new Button("✏️ Edit");
        editButton.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 12 25;
            -fx-background-radius: 5;
            -fx-cursor: hand;
            -fx-font-size: 14;
            """);
        editButton.setDisable(true);
        editButton.setOnAction(e -> editSelectedLicense());
        
        // Delete License Button
        deleteButton = new Button("🗑️ Delete");
        deleteButton.setStyle("""
            -fx-background-color: #e74c3c;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 12 25;
            -fx-background-radius: 5;
            -fx-cursor: hand;
            -fx-font-size: 14;
            """);
        deleteButton.setDisable(true);
        deleteButton.setOnAction(e -> deleteSelectedLicense());
        
        // Renew License Button
        renewButton = new Button("🔄 Renew");
        renewButton.setStyle("""
            -fx-background-color: #f39c12;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 12 25;
            -fx-background-radius: 5;
            -fx-cursor: hand;
            -fx-font-size: 14;
            """);
        renewButton.setDisable(true);
        renewButton.setOnAction(e -> renewSelectedLicense());
        
        // Close Button
        Button closeButton = new Button("Close");
        closeButton.setStyle("""
            -fx-background-color: #95a5a6;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 12 25;
            -fx-background-radius: 5;
            -fx-cursor: hand;
            -fx-font-size: 14;
            """);
        closeButton.setOnAction(e -> close());
        
        buttonPanel.getChildren().addAll(
            addButton, editButton, deleteButton, renewButton,
            new Region(), // Spacer
            closeButton
        );
        
        HBox.setHgrow(buttonPanel.getChildren().get(buttonPanel.getChildren().size() - 2), Priority.ALWAYS);
        
        return buttonPanel;
    }
    
    /**
     * Load license data from database
     */
    private void loadLicenseData() {
        try {
            List<EmployeeLicense> licenses = employeeService.getEmployeeLicenses(employee.getEmployeeId());
            licenseData.clear();
            licenseData.addAll(licenses);
            LOGGER.info("Loaded " + licenses.size() + " licenses for employee: " + employee.getEmployeeCode());
        } catch (Exception e) {
            LOGGER.severe("Error loading license data: " + e.getMessage());
            showErrorAlert("Error", "Failed to load license data: " + e.getMessage());
        }
    }
    
    /**
     * Add new license
     */
    private void addNewLicense() {
        LicenseFormDialog dialog = new LicenseFormDialog(employee, null);
        dialog.showDialogAndWait().ifPresent(license -> {
            if (addLicenseToDatabase(license)) {
                loadLicenseData();
                showSuccessAlert("Success", "License added successfully!");
            } else {
                showErrorAlert("Error", "Failed to add license. Please try again.");
            }
        });
    }
    
    /**
     * Edit selected license
     */
    private void editSelectedLicense() {
        EmployeeLicense selectedLicense = licenseTable.getSelectionModel().getSelectedItem();
        if (selectedLicense != null) {
            LicenseFormDialog dialog = new LicenseFormDialog(employee, selectedLicense);
            dialog.showDialogAndWait().ifPresent(license -> {
                if (updateLicenseInDatabase(license)) {
                    loadLicenseData();
                    showSuccessAlert("Success", "License updated successfully!");
                } else {
                    showErrorAlert("Error", "Failed to update license. Please try again.");
                }
            });
        }
    }
    
    /**
     * Delete selected license
     */
    private void deleteSelectedLicense() {
        EmployeeLicense selectedLicense = licenseTable.getSelectionModel().getSelectedItem();
        if (selectedLicense != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Deletion");
            confirmAlert.setHeaderText("Delete License");
            confirmAlert.setContentText("Are you sure you want to delete this license?\n\n" +
                "License Number: " + selectedLicense.getLicenseNumber() + "\n" +
                "Type: " + selectedLicense.getLicenseType());
            
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    if (deleteLicenseFromDatabase(selectedLicense)) {
                        loadLicenseData();
                        showSuccessAlert("Success", "License deleted successfully!");
                    } else {
                        showErrorAlert("Error", "Failed to delete license. Please try again.");
                    }
                }
            });
        }
    }
    
    /**
     * Renew selected license
     */
    private void renewSelectedLicense() {
        EmployeeLicense selectedLicense = licenseTable.getSelectionModel().getSelectedItem();
        if (selectedLicense != null) {
            // Create a new license based on the existing one with extended expiry
            EmployeeLicense renewedLicense = new EmployeeLicense();
            renewedLicense.setEmployeeId(selectedLicense.getEmployeeId());
            renewedLicense.setLicenseNumber(selectedLicense.getLicenseNumber());
            renewedLicense.setLicenseType(selectedLicense.getLicenseType());
            renewedLicense.setLicenseClass(selectedLicense.getLicenseClass());
            renewedLicense.setIssueDate(LocalDate.now());
            renewedLicense.setExpiryDate(LocalDate.now().plusYears(5)); // 5 years validity
            renewedLicense.setIssuingAuthority(selectedLicense.getIssuingAuthority());
            renewedLicense.setLicenseStatus("Valid");
            
            LicenseFormDialog dialog = new LicenseFormDialog(employee, renewedLicense);
            dialog.setTitle("Renew License - " + selectedLicense.getLicenseNumber());
            
            dialog.showDialogAndWait().ifPresent(license -> {
                if (updateLicenseInDatabase(license)) {
                    loadLicenseData();
                    showSuccessAlert("Success", "License renewed successfully!");
                } else {
                    showErrorAlert("Error", "Failed to renew license. Please try again.");
                }
            });
        }
    }
    
    /**
     * Add license to database
     */
    private boolean addLicenseToDatabase(EmployeeLicense license) {
        try {
            return employeeService.addEmployeeLicense(license);
        } catch (Exception e) {
            LOGGER.severe("Error adding license to database: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update license in database
     */
    private boolean updateLicenseInDatabase(EmployeeLicense license) {
        try {
            return employeeService.updateEmployeeLicense(license);
        } catch (Exception e) {
            LOGGER.severe("Error updating license in database: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete license from database
     */
    private boolean deleteLicenseFromDatabase(EmployeeLicense license) {
        try {
            return employeeService.deleteEmployeeLicense(license.getLicenseId());
        } catch (Exception e) {
            LOGGER.severe("Error deleting license from database: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Create cell factory for license type column
     */
    private Callback<TableColumn<EmployeeLicense, String>, TableCell<EmployeeLicense, String>> createTypeColumnCellFactory() {
        return column -> new TableCell<EmployeeLicense, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String style = switch (item) {
                        case "Bus" -> "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2;";
                        case "Heavy Vehicle" -> "-fx-background-color: #fff3e0; -fx-text-fill: #f57c00;";
                        case "Light Vehicle" -> "-fx-background-color: #e8f5e8; -fx-text-fill: #388e3c;";
                        case "Motorcycle" -> "-fx-background-color: #f3e5f5; -fx-text-fill: #7b1fa2;";
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
    private Callback<TableColumn<EmployeeLicense, String>, TableCell<EmployeeLicense, String>> createStatusColumnCellFactory() {
        return column -> new TableCell<EmployeeLicense, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    EmployeeLicense license = getTableView().getItems().get(getIndex());
                    setStyle(String.format(
                        "-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 3; -fx-padding: 2 8;",
                        license.getStatusColor()
                    ));
                }
            }
        };
    }
    
    /**
     * Create cell factory for days until expiry column
     */
    private Callback<TableColumn<EmployeeLicense, String>, TableCell<EmployeeLicense, String>> createDaysColumnCellFactory() {
        return column -> new TableCell<EmployeeLicense, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Expired".equals(item)) {
                        setStyle("-fx-background-color: #ffebee; -fx-text-fill: #d32f2f; -fx-font-weight: bold; -fx-background-radius: 3; -fx-padding: 2 8;");
                    } else {
                        try {
                            int days = Integer.parseInt(item);
                            if (days <= 30) {
                                setStyle("-fx-background-color: #fff8e1; -fx-text-fill: #f57c00; -fx-font-weight: bold; -fx-background-radius: 3; -fx-padding: 2 8;");
                            } else if (days <= 90) {
                                setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #ff9800; -fx-background-radius: 3; -fx-padding: 2 8;");
                            } else {
                                setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #388e3c; -fx-background-radius: 3; -fx-padding: 2 8;");
                            }
                        } catch (NumberFormatException e) {
                            setStyle("");
                        }
                    }
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