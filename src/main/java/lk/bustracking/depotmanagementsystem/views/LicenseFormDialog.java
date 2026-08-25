package lk.bustracking.depotmanagementsystem.views;

import lk.bustracking.depotmanagementsystem.models.Employee;
import lk.bustracking.depotmanagementsystem.models.EmployeeLicense;
import lk.bustracking.depotmanagementsystem.utils.AppLogger;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * License Form Dialog
 * Add/Edit driver license information
 */
public class LicenseFormDialog extends Stage {
    
    private static final Logger LOGGER = AppLogger.getLogger(LicenseFormDialog.class);
    
    // Form Fields
    private TextField licenseNumberField;
    private ComboBox<String> licenseTypeCombo;
    private ComboBox<String> licenseClassCombo;
    private DatePicker issueDatePicker;
    private DatePicker expiryDatePicker;
    private TextField issuingAuthorityField;
    private ComboBox<String> statusCombo;
    
    // Current license (null for add mode)
    private EmployeeLicense currentLicense;
    private Employee employee;
    private EmployeeLicense resultLicense;
    
    public LicenseFormDialog(Employee employee, EmployeeLicense license) {
        this.employee = employee;
        this.currentLicense = license;
        
        initializeDialog();
        createForm();
        
        if (license != null) {
            populateForm(license);
        } else {
            setDefaults();
        }
    }
    
    /**
     * Initialize dialog properties
     */
    private void initializeDialog() {
        setTitle(currentLicense == null ? "Add New License" : "Edit License");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);
        
        // Set icon
        try {
            getIcons().add(new javafx.scene.image.Image(
                getClass().getResourceAsStream("/icons/license-form.png")
            ));
        } catch (Exception e) {
            // Icon not found, continue without it
        }
    }
    
    /**
     * Create the main form
     */
    private void createForm() {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("""
            -fx-background-color: linear-gradient(to bottom, #f8f9fa, #ffffff);
            """);
        
        // Create header
        VBox header = createHeader();
        
        // Create form content
        VBox formContent = createFormContent();
        
        // Create button panel
        HBox buttonPanel = createButtonPanel();
        
        mainContainer.getChildren().addAll(header, formContent, buttonPanel);
        
        Scene scene = new Scene(mainContainer, 600, 550);
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
            -fx-background-color: linear-gradient(to right, #f39c12, #e67e22);
            -fx-background-radius: 10;
            """);
        
        Label titleLabel = new Label(currentLicense == null ? "Add New License" : "Edit License");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.WHITE);
        
        Label employeeLabel = new Label("Driver: " + employee.getFullName() + " (" + employee.getEmployeeCode() + ")");
        employeeLabel.setFont(Font.font("Segoe UI", 14));
        employeeLabel.setTextFill(Color.web("#ecf0f1"));
        
        header.getChildren().addAll(titleLabel, employeeLabel);
        
        return header;
    }
    
    /**
     * Create form content
     */
    private VBox createFormContent() {
        VBox formContainer = new VBox(20);
        formContainer.setPadding(new Insets(25));
        formContainer.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
            """);
        
        Label sectionTitle = new Label("License Information");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        sectionTitle.setTextFill(Color.web("#2c3e50"));
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        
        // License Number
        Label numberLabel = new Label("License Number:");
        numberLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        licenseNumberField = new TextField();
        licenseNumberField.setPromptText("B1234567");
        styleTextField(licenseNumberField);
        
        // License Type
        Label typeLabel = new Label("License Type:");
        typeLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        licenseTypeCombo = new ComboBox<>();
        licenseTypeCombo.getItems().addAll(EmployeeLicense.getLicenseTypes());
        licenseTypeCombo.setPromptText("Select license type");
        styleComboBox(licenseTypeCombo);
        
        // License Class
        Label classLabel = new Label("License Class:");
        classLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        licenseClassCombo = new ComboBox<>();
        licenseClassCombo.getItems().addAll(EmployeeLicense.getLicenseClasses());
        licenseClassCombo.setPromptText("Select license class");
        styleComboBox(licenseClassCombo);
        
        // Issue Date
        Label issueDateLabel = new Label("Issue Date:");
        issueDateLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        issueDatePicker = new DatePicker();
        issueDatePicker.setPromptText("Select issue date");
        styleTextField(issueDatePicker);
        
        // Expiry Date
        Label expiryDateLabel = new Label("Expiry Date:");
        expiryDateLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        expiryDatePicker = new DatePicker();
        expiryDatePicker.setPromptText("Select expiry date");
        styleTextField(expiryDatePicker);
        
        // Issuing Authority
        Label authorityLabel = new Label("Issuing Authority:");
        authorityLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        issuingAuthorityField = new TextField();
        issuingAuthorityField.setPromptText("Department of Motor Traffic");
        styleTextField(issuingAuthorityField);
        
        // License Status
        Label statusLabel = new Label("Status:");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(EmployeeLicense.getLicenseStatuses());
        statusCombo.setValue("Valid");
        styleComboBox(statusCombo);
        
        // Add fields to grid
        grid.add(numberLabel, 0, 0);
        grid.add(licenseNumberField, 1, 0);
        
        grid.add(typeLabel, 0, 1);
        grid.add(licenseTypeCombo, 1, 1);
        
        grid.add(classLabel, 0, 2);
        grid.add(licenseClassCombo, 1, 2);
        
        grid.add(issueDateLabel, 0, 3);
        grid.add(issueDatePicker, 1, 3);
        
        grid.add(expiryDateLabel, 0, 4);
        grid.add(expiryDatePicker, 1, 4);
        
        grid.add(authorityLabel, 0, 5);
        grid.add(issuingAuthorityField, 1, 5);
        
        grid.add(statusLabel, 0, 6);
        grid.add(statusCombo, 1, 6);
        
        // Auto-calculate expiry date when issue date is selected
        issueDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null && expiryDatePicker.getValue() == null) {
                expiryDatePicker.setValue(newDate.plusYears(5)); // Default 5 years validity
            }
        });
        
        formContainer.getChildren().addAll(sectionTitle, grid);
        
        return formContainer;
    }
    
    /**
     * Create button panel
     */
    private HBox createButtonPanel() {
        HBox buttonPanel = new HBox(15);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setPadding(new Insets(20));
        
        Button saveButton = new Button(currentLicense == null ? "Add License" : "Update License");
        saveButton.setStyle("""
            -fx-background-color: #27ae60;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 12 30;
            -fx-background-radius: 5;
            -fx-cursor: hand;
            -fx-font-size: 14;
            """);
        saveButton.setOnAction(e -> handleSave());
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("""
            -fx-background-color: #95a5a6;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 12 30;
            -fx-background-radius: 5;
            -fx-cursor: hand;
            -fx-font-size: 14;
            """);
        cancelButton.setOnAction(e -> close());
        
        buttonPanel.getChildren().addAll(saveButton, cancelButton);
        
        return buttonPanel;
    }
    
    /**
     * Populate form with existing license data
     */
    private void populateForm(EmployeeLicense license) {
        licenseNumberField.setText(license.getLicenseNumber());
        licenseTypeCombo.setValue(license.getLicenseType());
        licenseClassCombo.setValue(license.getLicenseClass());
        
        if (license.getIssueDate() != null) {
            issueDatePicker.setValue(license.getIssueDate());
        }
        
        if (license.getExpiryDate() != null) {
            expiryDatePicker.setValue(license.getExpiryDate());
        }
        
        issuingAuthorityField.setText(license.getIssuingAuthority());
        statusCombo.setValue(license.getLicenseStatus());
    }
    
    /**
     * Set default values for new license
     */
    private void setDefaults() {
        issueDatePicker.setValue(LocalDate.now());
        expiryDatePicker.setValue(LocalDate.now().plusYears(5));
        statusCombo.setValue("Valid");
        licenseTypeCombo.setValue("Bus"); // Default for bus drivers
        licenseClassCombo.setValue("Class B");
        issuingAuthorityField.setText("Department of Motor Traffic");
    }
    
    /**
     * Handle save button action
     */
    private void handleSave() {
        if (validateForm()) {
            EmployeeLicense license = createLicenseFromForm();
            resultLicense = license;
            close();
        }
    }
    
    /**
     * Validate form inputs
     */
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        
        // Required field validations
        if (licenseNumberField.getText().trim().isEmpty()) {
            errors.append("• License Number is required\n");
        }
        
        if (licenseTypeCombo.getValue() == null) {
            errors.append("• License Type is required\n");
        }
        
        if (licenseClassCombo.getValue() == null) {
            errors.append("• License Class is required\n");
        }
        
        if (issueDatePicker.getValue() == null) {
            errors.append("• Issue Date is required\n");
        }
        
        if (expiryDatePicker.getValue() == null) {
            errors.append("• Expiry Date is required\n");
        }
        
        if (issuingAuthorityField.getText().trim().isEmpty()) {
            errors.append("• Issuing Authority is required\n");
        }
        
        if (statusCombo.getValue() == null) {
            errors.append("• Status is required\n");
        }
        
        // Date validations
        if (issueDatePicker.getValue() != null && expiryDatePicker.getValue() != null) {
            if (expiryDatePicker.getValue().isBefore(issueDatePicker.getValue())) {
                errors.append("• Expiry Date must be after Issue Date\n");
            }
            
            if (issueDatePicker.getValue().isAfter(LocalDate.now())) {
                errors.append("• Issue Date cannot be in the future\n");
            }
        }
        
        // License number format validation
        String licenseNumber = licenseNumberField.getText().trim();
        if (!licenseNumber.isEmpty() && !licenseNumber.matches("^[A-Z][0-9]{7}$")) {
            errors.append("• License Number format should be: A1234567 (1 letter + 7 digits)\n");
        }
        
        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }
        
        return true;
    }
    
    /**
     * Create license object from form data
     */
    private EmployeeLicense createLicenseFromForm() {
        EmployeeLicense license = currentLicense != null ? currentLicense : new EmployeeLicense();
        
        license.setEmployeeId(employee.getEmployeeId());
        license.setLicenseNumber(licenseNumberField.getText().trim());
        license.setLicenseType(licenseTypeCombo.getValue());
        license.setLicenseClass(licenseClassCombo.getValue());
        license.setIssueDate(issueDatePicker.getValue());
        license.setExpiryDate(expiryDatePicker.getValue());
        license.setIssuingAuthority(issuingAuthorityField.getText().trim());
        license.setLicenseStatus(statusCombo.getValue());
        
        return license;
    }
    
    /**
     * Show dialog and wait for result
     */
    public Optional<EmployeeLicense> showDialogAndWait() {
        super.showAndWait();
        return Optional.ofNullable(resultLicense);
    }
    
    /**
     * Style text field
     */
    private void styleTextField(Control field) {
        field.setStyle("""
            -fx-padding: 10;
            -fx-background-radius: 5;
            -fx-border-color: #bdc3c7;
            -fx-border-radius: 5;
            -fx-font-size: 12;
            """);
        field.setPrefWidth(250);
    }
    
    /**
     * Style combo box
     */
    private void styleComboBox(ComboBox<?> combo) {
        combo.setStyle("""
            -fx-padding: 10;
            -fx-background-radius: 5;
            -fx-border-color: #bdc3c7;
            -fx-border-radius: 5;
            -fx-font-size: 12;
            """);
        combo.setPrefWidth(250);
    }
}