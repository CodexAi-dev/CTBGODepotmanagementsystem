// ================================================================
// SRI LANKA CTB DEPOT MANAGEMENT SYSTEM - ENHANCED LOGIN VIEW
// File: src/main/java/lk/bustracking/depotmanagementsystem/views/LoginView.java
// Modern Professional Login Interface with Clean MVC Architecture
// ================================================================

package lk.bustracking.depotmanagementsystem.views;

import lk.bustracking.depotmanagementsystem.controllers.LoginController;
import lk.bustracking.depotmanagementsystem.utils.UIConstants;
import lk.bustracking.depotmanagementsystem.utils.AnimationUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

/**
 * LoginView - Modern, responsive login interface with clean separation of concerns
 * Follows MVC pattern with proper UI/Controller separation
 */
public class LoginView extends Application {
    
    // UI Components
    private Stage primaryStage;
    private ScrollPane mainScrollPane;
    private VBox loginCard;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Label errorLabel;
    private Label statusLabel;
    private Label timeLabel;
    private ProgressIndicator loadingIndicator;
    private Timeline clockTimer;
    
    // Controller
    private LoginController loginController;
    
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        
        initializeStage();
        buildModernUI();
        setupController();
        
        Scene scene = new Scene(mainScrollPane, 1200, 800);
        scene.setFill(Color.TRANSPARENT);
        
        primaryStage.setTitle("Sri Lanka CTB - Depot Management System");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
        
        // Start animations and clock
        startEntranceAnimations();
        initializeClock();
        
        System.out.println("🚌 Sri Lanka CTB Login System - Enhanced Version Loaded");
    }
    
    /**
     * Initialize stage properties
     */
    private void initializeStage() {
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        
        // Cleanup on close
        primaryStage.setOnCloseRequest(e -> cleanup());
    }
    
    /**
     * Build the complete modern UI structure
     */
    private void buildModernUI() {
        createMainContainer();
        addBackgroundElements();
        
        HBox centerContent = createCenterLayout();
        VBox brandingSection = buildBrandingSection();
        VBox loginSection = buildLoginSection();
        
        centerContent.getChildren().addAll(brandingSection, loginSection);
        ((BorderPane) mainScrollPane.getContent()).setCenter(centerContent);
        
        // Set initial animation states
        setInitialAnimationStates(brandingSection, loginSection);
    }
    
    /**
     * Create main container with gradient background
     */
    private void createMainContainer() {
        mainScrollPane = new ScrollPane();
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setFitToHeight(true);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        BorderPane mainContainer = new BorderPane();
        mainContainer.setMinHeight(800);
        mainContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);
        mainContainer.setStyle(UIConstants.GRADIENT_BACKGROUND);
        
        mainScrollPane.setContent(mainContainer);
    }
    
    /**
     * Add floating background elements for visual appeal
     */
    private void addBackgroundElements() {
        BorderPane mainContainer = (BorderPane) mainScrollPane.getContent();
        Pane backgroundLayer = AnimationUtils.createFloatingCircles();
        mainContainer.getChildren().add(0, backgroundLayer);
    }
    
    /**
     * Create center layout container
     */
    private HBox createCenterLayout() {
        HBox centerContent = new HBox(60);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new javafx.geometry.Insets(40));
        centerContent.setMinHeight(720);
        return centerContent;
    }
    
    /**
     * Build the branding/information section
     */
    private VBox buildBrandingSection() {
        VBox branding = new VBox(25);
        branding.setAlignment(Pos.TOP_CENTER);
        branding.setPadding(new javafx.geometry.Insets(40, 30, 40, 30));
        branding.setPrefWidth(480);
        branding.setMaxWidth(480);
        branding.setMinWidth(480);
        
        // Logo section
        StackPane logoSection = createLogoSection();
        
        // Headings
        VBox headings = createHeadingSection();
        
        // Features list
        VBox features = createFeaturesList();
        
        // Time display
        timeLabel = createTimeLabel();
        
        branding.getChildren().addAll(logoSection, headings, features, timeLabel);
        return branding;
    }
    
    /**
     * Create CTB logo section
     */
    private StackPane createLogoSection() {
        Circle logoBackground = new Circle(65);
        logoBackground.setFill(Color.web(UIConstants.CTB_LIGHT, 0.95));
        logoBackground.setStroke(Color.web(UIConstants.CTB_ACCENT, 0.8));
        logoBackground.setStrokeWidth(3);
        
        Label logoText = new Label("🚌");
        logoText.setStyle("-fx-font-size: 48px;");
        
        StackPane logoStack = new StackPane();
        logoStack.getChildren().addAll(logoBackground, logoText);
        
        return logoStack;
    }
    
    /**
     * Create heading section
     */
    private VBox createHeadingSection() {
        VBox headings = new VBox(8);
        headings.setAlignment(Pos.CENTER);
        
        Label mainHeading = new Label("Sri Lanka CTB");
        mainHeading.setStyle(UIConstants.MAIN_HEADING_STYLE);
        
        Label subHeading = new Label("Depot Management System");
        subHeading.setStyle(UIConstants.SUB_HEADING_STYLE);
        
        headings.getChildren().addAll(mainHeading, subHeading);
        return headings;
    }
    
    /**
     * Create features list
     */
    private VBox createFeaturesList() {
        VBox features = new VBox(8);
        features.setAlignment(Pos.CENTER_LEFT);
        features.setMaxWidth(380);
        
        String[] featureList = {
            "✓ Real-time GPS Bus Tracking",
            "✓ Advanced Route Management", 
            "✓ Driver Performance Analytics",
            "✓ Smart Fleet Monitoring",
            "✓ Revenue Management Tools",
            "✓ Maintenance Scheduling"
        };
        
        for (String feature : featureList) {
            Label featureLabel = new Label(feature);
            featureLabel.setStyle(UIConstants.FEATURE_LABEL_STYLE);
            features.getChildren().add(featureLabel);
        }
        
        return features;
    }
    
    /**
     * Create time label
     */
    private Label createTimeLabel() {
        Label timeLabel = new Label();
        timeLabel.setStyle(UIConstants.TIME_LABEL_STYLE);
        return timeLabel;
    }
    
    /**
     * Build the login form section
     */
    private VBox buildLoginSection() {
        VBox loginSection = new VBox();
        loginSection.setAlignment(Pos.CENTER);
        loginSection.setPrefWidth(420);
        loginSection.setMaxWidth(420);
        loginSection.setMinWidth(420);
        
        loginCard = new VBox(25);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setMaxWidth(400);
        loginCard.setStyle(UIConstants.LOGIN_CARD_STYLE);
        
        // Build login components
        VBox header = createLoginHeader();
        VBox form = createLoginForm();
        VBox footer = createLoginFooter();
        
        loginCard.getChildren().addAll(header, form, footer);
        loginSection.getChildren().add(loginCard);
        
        return loginSection;
    }
    
    /**
     * Create login header
     */
    private VBox createLoginHeader() {
        VBox header = new VBox(12);
        header.setAlignment(Pos.CENTER);
        
        Label welcomeHeading = new Label("Welcome Back");
        welcomeHeading.setStyle(UIConstants.WELCOME_HEADING_STYLE);
        
        Label subtitle = new Label("Sign in to access your depot dashboard");
        subtitle.setStyle(UIConstants.SUBTITLE_STYLE);
        
        header.getChildren().addAll(welcomeHeading, subtitle);
        return header;
    }
    
    /**
     * Create main login form
     */
    private VBox createLoginForm() {
        VBox form = new VBox(20);
        form.setAlignment(Pos.CENTER);
        
        // Input fields
        VBox usernameContainer = createStyledInputField("👤", "Username", "Enter your username", false);
        usernameField = extractTextField(usernameContainer);
        usernameField.setText("admin"); // Default for testing
        
        VBox passwordContainer = createStyledInputField("🔒", "Password", "Enter your password", true);
        passwordField = (PasswordField) extractTextField(passwordContainer);
        passwordField.setText("admin123"); // Default for testing
        
        // Error display
        errorLabel = createErrorLabel();
        
        // Status container
        HBox statusContainer = createStatusContainer();
        
        // Login button
        loginButton = createLoginButton();
        
        // Quick access buttons
        VBox quickAccess = createQuickAccessSection();
        
        form.getChildren().addAll(
            usernameContainer, passwordContainer, errorLabel,
            statusContainer, loginButton, quickAccess
        );
        
        return form;
    }
    
    /**
     * Create styled input field with enhanced UX
     */
    private VBox createStyledInputField(String icon, String labelText, String promptText, boolean isPassword) {
        VBox container = new VBox(6);
        
        Label label = new Label(labelText);
        label.setStyle(UIConstants.INPUT_LABEL_STYLE);
        
        HBox fieldContainer = new HBox(10);
        fieldContainer.setAlignment(Pos.CENTER_LEFT);
        fieldContainer.setStyle(UIConstants.INPUT_FIELD_CONTAINER_NORMAL);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle(UIConstants.INPUT_ICON_STYLE);
        
        TextField field = isPassword ? new PasswordField() : new TextField();
        field.setPromptText(promptText);
        field.setStyle(UIConstants.INPUT_FIELD_STYLE);
        field.setPrefWidth(250);
        
        // Enhanced focus effects
        setupInputFieldFocusEffects(field, fieldContainer);
        
        fieldContainer.getChildren().addAll(iconLabel, field);
        container.getChildren().addAll(label, fieldContainer);
        
        return container;
    }
    
    /**
     * Setup focus effects for input fields
     */
    private void setupInputFieldFocusEffects(TextField field, HBox container) {
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                container.setStyle(UIConstants.INPUT_FIELD_CONTAINER_FOCUSED);
            } else {
                container.setStyle(UIConstants.INPUT_FIELD_CONTAINER_NORMAL);
            }
        });
    }
    
    /**
     * Extract TextField from container for controller binding
     */
    private TextField extractTextField(VBox container) {
        HBox fieldContainer = (HBox) container.getChildren().get(1);
        return (TextField) fieldContainer.getChildren().get(1);
    }
    
    /**
     * Create error label
     */
    private Label createErrorLabel() {
        Label errorLabel = new Label();
        errorLabel.setStyle(UIConstants.ERROR_LABEL_STYLE);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        return errorLabel;
    }
    
    /**
     * Create status container with loading indicator
     */
    private HBox createStatusContainer() {
        HBox statusContainer = new HBox(8);
        statusContainer.setAlignment(Pos.CENTER);
        
        statusLabel = new Label("Ready to sign in");
        statusLabel.setStyle(UIConstants.STATUS_LABEL_STYLE);
        
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(20, 20);
        loadingIndicator.setVisible(false);
        loadingIndicator.setStyle(UIConstants.LOADING_INDICATOR_STYLE);
        
        statusContainer.getChildren().addAll(statusLabel, loadingIndicator);
        return statusContainer;
    }
    
    /**
     * Create main login button
     */
    private Button createLoginButton() {
        Button button = new Button("🚀 Sign In to Dashboard");
        button.setPrefWidth(330);
        button.setPrefHeight(48);
        button.setStyle(UIConstants.LOGIN_BUTTON_STYLE);
        
        // Add hover effects
        AnimationUtils.setupButtonHoverEffects(button);
        
        return button;
    }
    
    /**
     * Create quick access section
     */
    private VBox createQuickAccessSection() {
        VBox container = new VBox(12);
        container.setAlignment(Pos.CENTER);
        
        // Separator line
        HBox separator = createSeparatorLine();
        
        // Quick login buttons
        HBox quickButtons = new HBox(8);
        quickButtons.setAlignment(Pos.CENTER);
        
        Button adminBtn = createQuickLoginButton("👨‍💼 Admin", "admin", "admin123");
        Button managerBtn = createQuickLoginButton("👨‍💻 Manager", "manager", "manager123");
        Button driverBtn = createQuickLoginButton("🚗 Driver", "driver", "driver123");
        
        quickButtons.getChildren().addAll(adminBtn, managerBtn, driverBtn);
        container.getChildren().addAll(separator, quickButtons);
        
        return container;
    }
    
    /**
     * Create separator line for quick access
     */
    private HBox createSeparatorLine() {
        HBox separator = new HBox();
        separator.setAlignment(Pos.CENTER);
        separator.setSpacing(12);
        
        Region line1 = new Region();
        line1.setPrefSize(60, 1);
        line1.setStyle("-fx-background-color: #ddd;");
        
        Label orLabel = new Label("Quick Access");
        orLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px; -fx-font-weight: 600;");
        
        Region line2 = new Region();
        line2.setPrefSize(60, 1);
        line2.setStyle("-fx-background-color: #ddd;");
        
        separator.getChildren().addAll(line1, orLabel, line2);
        return separator;
    }
    
    /**
     * Create quick login button
     */
    private Button createQuickLoginButton(String text, String username, String password) {
        Button btn = new Button(text);
        btn.setStyle(UIConstants.QUICK_LOGIN_BUTTON_STYLE);
        
        btn.setOnAction(e -> {
            usernameField.setText(username);
            passwordField.setText(password);
            Timeline quickLogin = new Timeline(new KeyFrame(Duration.millis(300), ev -> {
                if (loginController != null) {
                    loginController.handleLogin();
                }
            }));
            quickLogin.play();
        });
        
        return btn;
    }
    
    /**
     * Create footer section
     */
    private VBox createLoginFooter() {
        VBox footer = new VBox(6);
        footer.setAlignment(Pos.CENTER);
        
        Label copyright = new Label("© 2024 Sri Lanka Central Transport Board");
        copyright.setStyle(UIConstants.COPYRIGHT_STYLE);
        
        Label version = new Label("System Version 3.0.0 - Enhanced Edition");
        version.setStyle(UIConstants.VERSION_STYLE);
        
        footer.getChildren().addAll(copyright, version);
        return footer;
    }
    
    /**
     * Setup controller after UI creation
     */
    private void setupController() {
        loginController = new LoginController(
            usernameField, passwordField, loginButton, 
            errorLabel, statusLabel, loadingIndicator, primaryStage
        );
        
        // Setup keyboard navigation
        setupKeyboardNavigation();
    }
    
    /**
     * Setup keyboard navigation
     */
    private void setupKeyboardNavigation() {
        usernameField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> loginController.handleLogin());
    }
    
    /**
     * Set initial states for entrance animations
     */
    private void setInitialAnimationStates(VBox branding, VBox loginSection) {
        loginCard.setOpacity(0);
        loginCard.setTranslateX(50);
        branding.setOpacity(0);
        branding.setTranslateX(-50);
    }
    
    /**
     * Start entrance animations
     */
    private void startEntranceAnimations() {
        HBox centerContent = (HBox) ((BorderPane) mainScrollPane.getContent()).getCenter();
        VBox brandingSection = (VBox) centerContent.getChildren().get(0);
        
        AnimationUtils.playEntranceAnimations(brandingSection, loginCard, usernameField);
    }
    
    /**
     * Initialize and start the clock
     */
    private void initializeClock() {
        clockTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateClock()));
        clockTimer.setCycleCount(Timeline.INDEFINITE);
        clockTimer.play();
        updateClock();
    }
    
    /**
     * Update clock display
     */
    private void updateClock() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy\nHH:mm:ss");
        timeLabel.setText(now.format(formatter));
    }
    
    /**
     * Get the primary stage (for controller access)
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Cleanup resources on application close
     */
    private void cleanup() {
        if (clockTimer != null) {
            clockTimer.stop();
        }
        if (loginController != null) {
            loginController.cleanup();
        }
        Platform.exit();
    }
    
    /**
     * Main method for standalone testing
     */
    public static void main(String[] args) {
        launch(args);
    }
}