package lk.bustracking.depotmanagementsystem.views;

import lk.bustracking.depotmanagementsystem.models.Employee;
import lk.bustracking.depotmanagementsystem.services.EmployeeService;
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
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Employee Form Dialog
 * Add/Edit employee information with comprehensive form validation
 */
public class EmployeeFormDialog extends Stage {
    
    private static final Logger LOGGER = AppLogger.getLogger(EmployeeFormDialog.class);
    
    // Services
    private final EmployeeService employeeService;
    
    // Form Fields
    private TextField employeeCodeField;
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField emailField;
    private TextField phoneField;
    private TextField nationalIdField;
    private TextArea addressArea;
    private DatePicker dateOfBirthPicker;
    private ComboBox<String> genderCombo;
    private ComboBox<String> employeeTypeCombo;
    private TextField departmentField;
    private TextField positionField;
    private DatePicker hireDatePicker;
    private TextField salaryField;
    private ComboBox<String> statusCombo;
    private TextField emergencyContactNameField;
    private TextField emergencyContactPhoneField;
    
    // Current employee (null for add mode)
    private Employee currentEmployee;
    private Employee resultEmployee;
    
    public EmployeeFormDialog(Employee employee) {
        this.employeeService = EmployeeService.getInstance();
        this.currentEmployee = employee;
        
        initializeDialog();
        createForm();
        
        if (employee != null) {
            populateForm(employee);
        } else {
            setDefaults();
        }
    }
    
    /**
     * Initialize dialog properties
     */
    private void initializeDialog() {
        setTitle(currentEmployee == null ? "Add New Employee" : "Edit Employee");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);
        
        // Set icon and styling
        getIcons().add(new javafx.scene.image.Image(
            getClass().getResourceAsStream("/icons/employee.png")
        ));
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
        ScrollPane formScrollPane = createFormContent();
        
        // Create button panel
        HBox buttonPanel = createButtonPanel();
        
        mainContainer.getChildren().addAll(header, formScrollPane, buttonPanel);
        
        Scene scene = new Scene(mainContainer, 800, 700);
        setScene(scene);
    }
    
    /**
     * Create header section
     */
    private VBox createHeader() {
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label(currentEmployee == null ? "Add New Employee" : "Edit Employee");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        Label subtitleLabel = new Label("Enter employee information below");
        subtitleLabel.setFont(Font.font("Segoe UI", 14));
        subtitleLabel.setTextFill(Color.web("#7f8c8d"));
        
        header.getChildren().addAll(titleLabel, subtitleLabel);
        
        return header;
    }
    
    /**
     * Create form content
     */
    private ScrollPane createFormContent() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefHeight(500);
        
        VBox formContainer = new VBox(20);
        formContainer.setPadding(new Insets(20));
        
        // Personal Information Section
        VBox personalSection = createPersonalInformationSection();
        
        // Employment Information Section
        VBox employmentSection = createEmploymentInformationSection();
        
        // Emergency Contact Section
        VBox emergencySection = createEmergencyContactSection();
        
        formContainer.getChildren().addAll(personalSection, employmentSection, emergencySection);
        scrollPane.setContent(formContainer);
        
        return scrollPane;
    }
    
    /**
     * Create personal information section
     */
    private VBox createPersonalInformationSection() {
        VBox section = new VBox(15);
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
        grid.setVgap(15);
        
        // Employee Code
        Label codeLabel = new Label("Employee Code:");
        codeLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        employeeCodeField = new TextField();
        employeeCodeField.setPromptText("EMP001");
        employeeCodeField.setDisable(currentEmployee != null); // Disable for edit mode
        styleTextField(employeeCodeField);
        
        // First Name
        Label firstNameLabel = new Label("First Name:");
        firstNameLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        firstNameField = new TextField();
        firstNameField.setPromptText("Enter first name");
        styleTextField(firstNameField);
        
        // Last Name
        Label lastNameLabel = new Label("Last Name:");
        lastNameLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        lastNameField = new TextField();
        lastNameField.setPromptText("Enter last name");
        styleTextField(lastNameField);
        
        // Email
        Label emailLabel = new Label("Email:");
        emailLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        emailField = new TextField();
        emailField.setPromptText("john.doe@example.com");
        styleTextField(emailField);
        
        // Phone
        Label phoneLabel = new Label("Phone:");
        phoneLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        phoneField = new TextField();
        phoneField.setPromptText("0771234567");
        styleTextField(phoneField);
        
        // National ID
        Label nationalIdLabel = new Label("National ID:");
        nationalIdLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        nationalIdField = new TextField();
        nationalIdField.setPromptText("199012345678V");
        styleTextField(nationalIdField);
        
        // Date of Birth
        Label dobLabel = new Label("Date of Birth:");
        dobLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        dateOfBirthPicker = new DatePicker();
        dateOfBirthPicker.setPromptText("Select date of birth");
        styleTextField(dateOfBirthPicker);
        
        // Gender
        Label genderLabel = new Label("Gender:");
        genderLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll(Employee.getGenders());
        genderCombo.setPromptText("Select gender");
        styleComboBox(genderCombo);
        
        // Address
        Label addressLabel = new Label("Address:");
        addressLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        addressArea = new TextArea();
        addressArea.setPromptText("Enter full address");
        addressArea.setPrefRowCount(3);
        styleTextArea(addressArea);
        
        // Add fields to grid
        grid.add(codeLabel, 0, 0);
        grid.add(employeeCodeField, 1, 0);
        grid.add(firstNameLabel, 2, 0);
        grid.add(firstNameField, 3, 0);
        
        grid.add(lastNameLabel, 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(emailLabel, 2, 1);
        grid.add(emailField, 3, 1);
        
        grid.add(phoneLabel, 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(nationalIdLabel, 2, 2);
        grid.add(nationalIdField, 3, 2);
        
        grid.add(dobLabel, 0, 3);
        grid.add(dateOfBirthPicker, 1, 3);
        grid.add(genderLabel, 2, 3);
        grid.add(genderCombo, 3, 3);
        
        grid.add(addressLabel, 0, 4);
        grid.add(addressArea, 1, 4, 3, 1);
        
        section.getChildren().addAll(sectionTitle, grid);
        
        return section;
    }
    
    /**
     * Create employment information section
     */
    private VBox createEmploymentInformationSection() {
        VBox section = new VBox(15);
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
        grid.setVgap(15);
        
        // Employee Type
        Label typeLabel = new Label("Employee Type:");
        typeLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        employeeTypeCombo = new ComboBox<>();
        employeeTypeCombo.getItems().addAll(Employee.getEmployeeTypes());
        employeeTypeCombo.setPromptText("Select employee type");
        styleComboBox(employeeTypeCombo);
        
        // Department
        Label departmentLabel = new Label("Department:");
        departmentLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        departmentField = new TextField();
        departmentField.setPromptText("Operations");
        styleTextField(departmentField);
        
        // Position
        Label positionLabel = new Label("Position:");
        positionLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        positionField = new TextField();
        positionField.setPromptText("Senior Driver");
        styleTextField(positionField);
        
        // Hire Date
        Label hireDateLabel = new Label("Hire Date:");
        hireDateLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        hireDatePicker = new DatePicker();
        hireDatePicker.setValue(LocalDate.now());
        hireDatePicker.setPromptText("Select hire date");
        styleTextField(hireDatePicker);
        
        // Salary
        Label salaryLabel = new Label("Salary (Rs.):");
        salaryLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        salaryField = new TextField();
        salaryField.setPromptText("45000.00");
        styleTextField(salaryField);
        
        // Employment Status
        Label statusLabel = new Label("Employment Status:");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(Employee.getEmploymentStatuses());
        statusCombo.setValue("Active");
        styleComboBox(statusCombo);
        
        // Add fields to grid
        grid.add(typeLabel, 0, 0);
        grid.add(employeeTypeCombo, 1, 0);
        grid.add(departmentLabel, 2, 0);
        grid.add(departmentField, 3, 0);
        
        grid.add(positionLabel, 0, 1);
        grid.add(positionField, 1, 1);
        grid.add(hireDateLabel, 2, 1);
        grid.add(hireDatePicker, 3, 1);
        
        grid.add(salaryLabel, 0, 2);
        grid.add(salaryField, 1, 2);
        grid.add(statusLabel, 2, 2);
        grid.add(statusCombo, 3, 2);
        
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
        grid.setVgap(15);
        
        // Emergency Contact Name
        Label contactNameLabel = new Label("Contact Name:");
        contactNameLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        emergencyContactNameField = new TextField();
        emergencyContactNameField.setPromptText("Enter emergency contact name");
        styleTextField(emergencyContactNameField);
        
        // Emergency Contact Phone
        Label contactPhoneLabel = new Label("Contact Phone:");
        contactPhoneLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        
        emergencyContactPhoneField = new TextField();
        emergencyContactPhoneField.setPromptText("0771234567");
        styleTextField(emergencyContactPhoneField);
        
        // Add fields to grid
        grid.add(contactNameLabel, 0, 0);
        grid.add(emergencyContactNameField, 1, 0);
        grid.add(contactPhoneLabel, 2, 0);
        grid.add(emergencyContactPhoneField, 3, 0);
        
        section.getChildren().addAll(sectionTitle, grid);
        
        return section;
    }
    
    /**
     * Create button panel
     */
    private HBox createButtonPanel() {
        HBox buttonPanel = new HBox(15);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setPadding(new Insets(20));
        
        Button saveButton = new Button(currentEmployee == null ? "Add Employee" : "Update Employee");
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
     * Populate form with existing employee data
     */
    private void populateForm(Employee employee) {
        employeeCodeField.setText(employee.getEmployeeCode());
        firstNameField.setText(employee.getFirstName());
        lastNameField.setText(employee.getLastName());
        emailField.setText(employee.getEmail());
        phoneField.setText(employee.getPhone());
        nationalIdField.setText(employee.getNationalId());
        addressArea.setText(employee.getAddress());
        
        if (employee.getDateOfBirth() != null) {
            dateOfBirthPicker.setValue(employee.getDateOfBirth());
        }
        
        genderCombo.setValue(employee.getGender());
        employeeTypeCombo.setValue(employee.getEmployeeType());
        departmentField.setText(employee.getDepartment());
        positionField.setText(employee.getPosition());
        
        if (employee.getHireDate() != null) {
            hireDatePicker.setValue(employee.getHireDate());
        }
        
        salaryField.setText(String.valueOf(employee.getSalary()));
        statusCombo.setValue(employee.getEmploymentStatus());
        emergencyContactNameField.setText(employee.getEmergencyContactName());
        emergencyContactPhoneField.setText(employee.getEmergencyContactPhone());
    }
    
    /**
     * Set default values for new employee
     */
    private void setDefaults() {
        employeeCodeField.setText(employeeService.generateEmployeeCode());
        hireDatePicker.setValue(LocalDate.now());
        statusCombo.setValue("Active");
        genderCombo.setValue("Male");
        employeeTypeCombo.setValue("Driver");
    }
    
    /**
     * Handle save button action
     */
    private void handleSave() {
        if (validateForm()) {
            Employee employee = createEmployeeFromForm();
            resultEmployee = employee;
            close();
        }
    }
    
    /**
     * Validate form inputs
     */
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        
        // Required field validations
        if (employeeCodeField.getText().trim().isEmpty()) {
            errors.append("• Employee Code is required\n");
        }
        
        if (firstNameField.getText().trim().isEmpty()) {
            errors.append("• First Name is required\n");
        }
        
        if (lastNameField.getText().trim().isEmpty()) {
            errors.append("• Last Name is required\n");
        }
        
        if (phoneField.getText().trim().isEmpty()) {
            errors.append("• Phone number is required\n");
        }
        
        if (nationalIdField.getText().trim().isEmpty()) {
            errors.append("• National ID is required\n");
        }
        
        if (employeeTypeCombo.getValue() == null) {
            errors.append("• Employee Type is required\n");
        }
        
        // Email validation
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.append("• Invalid email format\n");
        }
        
        // Phone validation
        String phone = phoneField.getText().trim();
        if (!phone.matches("^[0-9]{10}$")) {
            errors.append("• Phone number must be 10 digits\n");
        }
        
        // National ID validation
        String nationalId = nationalIdField.getText().trim();
        if (!nationalId.matches("^[0-9]{9}[vVxX]?$") && !nationalId.matches("^[0-9]{12}$")) {
            errors.append("• Invalid National ID format\n");
        }
        
        // Salary validation
        try {
            double salary = Double.parseDouble(salaryField.getText().trim());
            if (salary < 0) {
                errors.append("• Salary must be a positive number\n");
            }
        } catch (NumberFormatException e) {
            errors.append("• Invalid salary format\n");
        }
        
        // Check for duplicate employee code (only for new employees)
        if (currentEmployee == null) {
            if (employeeService.employeeCodeExists(employeeCodeField.getText().trim())) {
                errors.append("• Employee Code already exists\n");
            }
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
     * Create employee object from form data
     */
    private Employee createEmployeeFromForm() {
        Employee employee = currentEmployee != null ? currentEmployee : new Employee();
        
        employee.setEmployeeCode(employeeCodeField.getText().trim());
        employee.setFirstName(firstNameField.getText().trim());
        employee.setLastName(lastNameField.getText().trim());
        employee.setEmail(emailField.getText().trim());
        employee.setPhone(phoneField.getText().trim());
        employee.setNationalId(nationalIdField.getText().trim());
        employee.setAddress(addressArea.getText().trim());
        employee.setDateOfBirth(dateOfBirthPicker.getValue());
        employee.setGender(genderCombo.getValue());
        employee.setEmployeeType(employeeTypeCombo.getValue());
        employee.setDepartment(departmentField.getText().trim());
        employee.setPosition(positionField.getText().trim());
        employee.setHireDate(hireDatePicker.getValue());
        employee.setSalary(Double.parseDouble(salaryField.getText().trim()));
        employee.setEmploymentStatus(statusCombo.getValue());
        employee.setEmergencyContactName(emergencyContactNameField.getText().trim());
        employee.setEmergencyContactPhone(emergencyContactPhoneField.getText().trim());
        
        return employee;
    }
    
    /**
     * Show dialog and wait for result
     * @return Optional containing the employee if saved, empty if cancelled
     */
    public Optional<Employee> showDialogAndWait() {
        super.showAndWait();
        return Optional.ofNullable(resultEmployee);
    }
    
    /**
     * Style text field
     */
    private void styleTextField(Control field) {
        field.setStyle("""
            -fx-padding: 8;
            -fx-background-radius: 5;
            -fx-border-color: #bdc3c7;
            -fx-border-radius: 5;
            -fx-font-size: 12;
            """);
        field.setPrefWidth(180);
    }
    
    /**
     * Style text area
     */
    private void styleTextArea(TextArea area) {
        area.setStyle("""
            -fx-padding: 8;
            -fx-background-radius: 5;
            -fx-border-color: #bdc3c7;
            -fx-border-radius: 5;
            -fx-font-size: 12;
            """);
    }
    
    /**
     * Style combo box
     */
    private void styleComboBox(ComboBox<?> combo) {
        combo.setStyle("""
            -fx-padding: 8;
            -fx-background-radius: 5;
            -fx-border-color: #bdc3c7;
            -fx-border-radius: 5;
            -fx-font-size: 12;
            """);
        combo.setPrefWidth(180);
    }
}