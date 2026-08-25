package lk.bustracking.depotmanagementsystem.views;

import lk.bustracking.depotmanagementsystem.models.Employee;
import lk.bustracking.depotmanagementsystem.models.EmployeeLicense;
import lk.bustracking.depotmanagementsystem.services.EmployeeService;
import lk.bustracking.depotmanagementsystem.utils.AppLogger;

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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

/**
 * Employee Details Dialog
 * Read-only view of complete employee information including licenses
 */
public class EmployeeDetailsDialog extends Stage {
    
    private static final Logger LOGGER = AppLogger.getLogger(EmployeeDetailsDialog.class);
    
    // Services
    private final EmployeeService employeeService;
    
    // Current employee
    private final Employee employee;
    
    public EmployeeDetailsDialog(Employee employee) {
        this.employeeService = EmployeeService.getInstance();
        this.employee = employee;
        
        initializeDialog();
        createContent();
    }
    
    /**
     * Initialize dialog properties
     */
    private void initializeDialog() {
        setTitle("Employee Details - " + employee.getFullName());
        initModality(Modality.NONE);
        setResizable(true);
        
        // Set minimum size
        setMinWidth(900);
        setMinHeight(700);
        
        // Remove icon loading code that causes the error
        // Icon loading is optional and causing issues
    }
    
    /**
     * Create the main content
     */
    private void createContent() {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("""
            -fx-background-color: linear-gradient(to bottom, #f8f9fa, #ffffff);
            """);
        
        // Create header
        VBox header = createHeader();
        
        // Create content sections
        ScrollPane contentScrollPane = createContentSections();
        
        // Create button panel
        HBox buttonPanel = createButtonPanel();
        
        mainContainer.getChildren().addAll(header, contentScrollPane, buttonPanel);
        
        Scene scene = new Scene(mainContainer, 950, 750);
        setScene(scene);
    }
    
    /**
     * Create header section
     */
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20));
        header.setStyle("""
            -fx-background-color: linear-gradient(to right, #3498db, #2980b9);
            -fx-background-radius: 10;
            """);
        
        // Employee photo placeholder
        Label photoLabel = new Label("📷");
        photoLabel.setFont(Font.font(48));
        photoLabel.setTextFill(Color.WHITE);
        
        // Employee name
        Label nameLabel = new Label(employee.getFullName());
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        nameLabel.setTextFill(Color.WHITE);
        
        // Employee code and type
        Label detailsLabel = new Label(String.format("%s • %s • %s", 
            employee.getEmployeeCode(), 
            employee.getEmployeeType(), 
            employee.getEmploymentStatus()));
        detailsLabel.setFont(Font.font("Segoe UI", 14));
        detailsLabel.setTextFill(Color.web("#ecf0f1"));
        
        header.getChildren().addAll(photoLabel, nameLabel, detailsLabel);
        
        return header;
    }
    
    /**
     * Create content sections
     */
    private ScrollPane createContentSections() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        VBox contentContainer = new VBox(20);
        contentContainer.setPadding(new Insets(20));
        
        // Create information sections
        HBox topSections = new HBox(20);
        topSections.setAlignment(Pos.TOP_CENTER);
        
        VBox personalSection = createPersonalInfoSection();
        VBox employmentSection = createEmploymentInfoSection();
        
        topSections.getChildren().addAll(personalSection, employmentSection);
        
        // Emergency contact section
        VBox emergencySection = createEmergencyContactSection();
        
        // Statistics section
        VBox statsSection = createStatisticsSection();
        
        // License section (only for drivers)
        VBox licenseSection = null;
        if (employee.isDriver()) {
            licenseSection = createLicenseSection();
        }
        
        contentContainer.getChildren().addAll(topSections, emergencySection, statsSection);
        
        if (licenseSection != null) {
            contentContainer.getChildren().add(licenseSection);
        }
        
        scrollPane.setContent(contentContainer);
        
        return scrollPane;
    }
    
    /**
     * Create personal information section
     */
    private VBox createPersonalInfoSection() {
        VBox section = new VBox(15);
        section.setPrefWidth(400);
        section.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-padding: 20;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
            """);
        
        Label sectionTitle = new Label("Personal Information");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web("#2c3e50"));
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        
        // Add personal details
        addDetailRow(grid, 0, "Full Name:", employee.getFullName());
        addDetailRow(grid, 1, "Email:", employee.getEmail());
        addDetailRow(grid, 2, "Phone:", employee.getPhone());
        addDetailRow(grid, 3, "National ID:", employee.getNationalId());
        addDetailRow(grid, 4, "Gender:", employee.getGender());
        
        if (employee.getDateOfBirth() != null) {
            addDetailRow(grid, 5, "Date of Birth:", 
                employee.getDateOfBirth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                " (Age: " + employee.getAge() + ")");
        }
        
        if (employee.getAddress() != null && !employee.getAddress().trim().isEmpty()) {
            Label addressLabel = createDetailLabel("Address:");
            Label addressValue = createDetailValue(employee.getAddress());
            addressValue.setWrapText(true);
            addressValue.setMaxWidth(250);
            
            grid.add(addressLabel, 0, 6);
            grid.add(addressValue, 1, 6);
        }
        
        section.getChildren().addAll(sectionTitle, grid);
        
        return section;
    }
    
    /**
     * Create employment information section
     */
    private VBox createEmploymentInfoSection() {
        VBox section = new VBox(15);
        section.setPrefWidth(400);
        section.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-padding: 20;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
            """);
        
        Label sectionTitle = new Label("Employment Information");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web("#2c3e50"));
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        
        // Add employment details
        addDetailRow(grid, 0, "Employee Code:", employee.getEmployeeCode());
        addDetailRow(grid, 1, "Employee Type:", employee.getEmployeeType());
        addDetailRow(grid, 2, "Position:", employee.getPosition());
        addDetailRow(grid, 3, "Department:", employee.getDepartment());
        
        if (employee.getHireDate() != null) {
            addDetailRow(grid, 4, "Hire Date:", 
                employee.getHireDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                " (" + employee.getYearsOfService() + " years)");
        }
        
        addDetailRow(grid, 5, "Salary:", String.format("Rs. %.2f", employee.getSalary()));
        
        // Status with color coding
        Label statusLabel = createDetailLabel("Status:");
        Label statusValue = createDetailValue(employee.getEmploymentStatus());
        statusValue.setStyle(getStatusStyle(employee.getEmploymentStatus()));
        
        grid.add(statusLabel, 0, 6);
        grid.add(statusValue, 1, 6);
        
        section.getChildren().addAll(sectionTitle, grid);
        
        return section;
    }
    
    /**
     * Create emergency contact section
     */
    private VBox createEmergencyContactSection() {
        VBox section = new VBox(15);
        section.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-padding: 20;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
            """);
        
        Label sectionTitle = new Label("Emergency Contact");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web("#2c3e50"));
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        
        addDetailRow(grid, 0, "Contact Name:", employee.getEmergencyContactName());
        addDetailRow(grid, 1, "Contact Phone:", employee.getEmergencyContactPhone());
        
        section.getChildren().addAll(sectionTitle, grid);
        
        return section;
    }
    
    /**
     * Create statistics section
     */
    private VBox createStatisticsSection() {
        VBox section = new VBox(15);
        section.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-padding: 20;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
            """);
        
        Label sectionTitle = new Label("Employee Statistics");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web("#2c3e50"));
        
        HBox statsContainer = new HBox(20);
        statsContainer.setAlignment(Pos.CENTER);
        
        // Years of service
        VBox yearsBox = createStatBox("Years of Service", 
            String.valueOf(employee.getYearsOfService()), "#3498db");
        
        // Age
        VBox ageBox = createStatBox("Age", 
            String.valueOf(employee.getAge()), "#27ae60");
        
        // Status indicator
        VBox statusBox = createStatBox("Status", 
            employee.getEmploymentStatus(), getStatusColor(employee.getEmploymentStatus()));
        
        statsContainer.getChildren().addAll(yearsBox, ageBox, statusBox);
        
        section.getChildren().addAll(sectionTitle, statsContainer);
        
        return section;
    }
    
    /**
     * Create license section (for drivers only)
     */
    private VBox createLicenseSection() {
        VBox section = new VBox(15);
        section.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-padding: 20;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
            """);
        
        Label sectionTitle = new Label("Driver Licenses");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web("#2c3e50"));
        
        // Create license table
        TableView<EmployeeLicense> licenseTable = new TableView<>();
        licenseTable.setPrefHeight(200);
        licenseTable.setStyle("""
            -fx-background-color: transparent;
            -fx-table-cell-border-color: #ecf0f1;
            """);
        
        // License Number Column
        TableColumn<EmployeeLicense, String> numberColumn = new TableColumn<>("License Number");
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("licenseNumber"));
        numberColumn.setPrefWidth(150);
        
        // Type Column
        TableColumn<EmployeeLicense, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("licenseType"));
        typeColumn.setPrefWidth(120);
        
        // Class Column
        TableColumn<EmployeeLicense, String> classColumn = new TableColumn<>("Class");
        classColumn.setCellValueFactory(new PropertyValueFactory<>("licenseClass"));
        classColumn.setPrefWidth(80);
        
        // Expiry Date Column
        TableColumn<EmployeeLicense, String> expiryColumn = new TableColumn<>("Expiry Date");
        expiryColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getExpiryDate() != null) {
                return new javafx.beans.property.ReadOnlyStringWrapper(
                    cellData.getValue().getExpiryDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                );
            }
            return new javafx.beans.property.ReadOnlyStringWrapper("");
        });
        expiryColumn.setPrefWidth(100);
        
        // Status Column
        TableColumn<EmployeeLicense, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.ReadOnlyStringWrapper(cellData.getValue().getStatusDescription())
        );
        statusColumn.setPrefWidth(120);
        statusColumn.setCellFactory(column -> new TableCell<EmployeeLicense, String>() {
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
        });
        
        licenseTable.getColumns().addAll(numberColumn, typeColumn, classColumn, expiryColumn, statusColumn);
        
        // Load license data
        try {
            List<EmployeeLicense> licenses = employeeService.getEmployeeLicenses(employee.getEmployeeId());
            licenseTable.getItems().addAll(licenses);
            
            if (licenses.isEmpty()) {
                Label noLicensesLabel = new Label("No licenses found for this driver.");
                noLicensesLabel.setFont(Font.font("Segoe UI", 12));
                noLicensesLabel.setTextFill(Color.web("#7f8c8d"));
                section.getChildren().addAll(sectionTitle, noLicensesLabel);
                return section;
            }
            
        } catch (Exception e) {
            LOGGER.severe("Error loading employee licenses: " + e.getMessage());
            Label errorLabel = new Label("Error loading license data.");
            errorLabel.setFont(Font.font("Segoe UI", 12));
            errorLabel.setTextFill(Color.web("#e74c3c"));
            section.getChildren().addAll(sectionTitle, errorLabel);
            return section;
        }
        
        section.getChildren().addAll(sectionTitle, licenseTable);
        
        return section;
    }
    
    /**
     * Create button panel
     */
    private HBox createButtonPanel() {
        HBox buttonPanel = new HBox(15);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setPadding(new Insets(20));
        
        Button editButton = new Button("✏️ Edit Employee");
        editButton.setStyle("""
            -fx-background-color: #3498db;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 12 30;
            -fx-background-radius: 5;
            -fx-cursor: hand;
            -fx-font-size: 14;
            """);
        editButton.setOnAction(e -> openEditDialog());
        
        Button closeButton = new Button("Close");
        closeButton.setStyle("""
            -fx-background-color: #95a5a6;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 12 30;
            -fx-background-radius: 5;
            -fx-cursor: hand;
            -fx-font-size: 14;
            """);
        closeButton.setOnAction(e -> close());
        
        buttonPanel.getChildren().addAll(editButton, closeButton);
        
        return buttonPanel;
    }
    
    /**
     * Helper method to add detail row to grid
     */
    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label labelControl = createDetailLabel(label);
        Label valueControl = createDetailValue(value != null ? value : "Not specified");
        
        grid.add(labelControl, 0, row);
        grid.add(valueControl, 1, row);
    }
    
    /**
     * Create detail label
     */
    private Label createDetailLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        label.setTextFill(Color.web("#7f8c8d"));
        return label;
    }
    
    /**
     * Create detail value
     */
    private Label createDetailValue(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", 12));
        label.setTextFill(Color.web("#2c3e50"));
        return label;
    }
    
    /**
     * Create stat box
     */
    private VBox createStatBox(String title, String value, String color) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15));
        box.setPrefWidth(150);
        box.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 8;
            """, color));
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        valueLabel.setTextFill(Color.WHITE);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", 11));
        titleLabel.setTextFill(Color.WHITE);
        
        box.getChildren().addAll(valueLabel, titleLabel);
        
        return box;
    }
    
    /**
     * Get status style
     */
    private String getStatusStyle(String status) {
        return switch (status) {
            case "Active" -> "-fx-background-color: #e8f5e8; -fx-text-fill: #27ae60; -fx-background-radius: 3; -fx-padding: 2 8;";
            case "Inactive" -> "-fx-background-color: #fff3e0; -fx-text-fill: #ff9800; -fx-background-radius: 3; -fx-padding: 2 8;";
            case "Terminated" -> "-fx-background-color: #ffebee; -fx-text-fill: #f44336; -fx-background-radius: 3; -fx-padding: 2 8;";
            case "Suspended" -> "-fx-background-color: #fff8e1; -fx-text-fill: #ffc107; -fx-background-radius: 3; -fx-padding: 2 8;";
            default -> "-fx-background-color: #f5f5f5; -fx-text-fill: #666666; -fx-background-radius: 3; -fx-padding: 2 8;";
        };
    }
    
    /**
     * Get status color
     */
    private String getStatusColor(String status) {
        return switch (status) {
            case "Active" -> "#27ae60";
            case "Inactive" -> "#ff9800";
            case "Terminated" -> "#f44336";
            case "Suspended" -> "#ffc107";
            default -> "#666666";
        };
    }
    
    /**
     * Open edit dialog
     */
    private void openEditDialog() {
        EmployeeFormDialog dialog = new EmployeeFormDialog(employee);
        dialog.showDialogAndWait().ifPresent(updatedEmployee -> {
            // Close this dialog and let parent refresh
            close();
        });
    }
}