package lk.bustracking.depotmanagementsystem;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lk.bustracking.depotmanagementsystem.views.LoginView;
import lk.bustracking.depotmanagementsystem.utils.AppLogger;
import lk.bustracking.depotmanagementsystem.db.Database;

import java.util.logging.Logger;

/**
 * Main Application Class for CTB Depot Management System
 * Enhanced with proper logging, error handling, and system initialization
 */
public class DepotManagementSystem extends Application {
    
    private static final Logger LOGGER = AppLogger.getLogger(DepotManagementSystem.class);
    
    private static final String APP_NAME = "CTB Depot Management System";
    private static final String APP_VERSION = "3.0.0";
    
    @Override
    public void init() throws Exception {
        super.init();
        
        try {
            // Initialize logging system
            AppLogger.initialize();
            AppLogger.logAppStart(APP_NAME, APP_VERSION);
            
            // Initialize application components
            initializeApplication();
            
            LOGGER.info("Application initialization completed successfully");
            
        } catch (Exception e) {
            LOGGER.severe("CRITICAL: Application initialization failed: " + e.getMessage());
            AppLogger.logException(DepotManagementSystem.class, "Application initialization failed", e);
            throw e; // Re-throw to prevent startup
        }
    }
    
    @Override
    public void start(Stage primaryStage) {
        try {
            LOGGER.info("Starting JavaFX application...");
            
            // Set global exception handler
            setupGlobalExceptionHandler();
            
            // Configure primary stage
            configurePrimaryStage(primaryStage);
            
            // Test database connection before proceeding
            if (!testDatabaseConnection()) {
                showStartupErrorAndExit(new Exception("Cannot connect to database. Please check database server and try again."));
                return;
            }
            
            // Create and show login view
            LoginView loginView = new LoginView();
            loginView.start(primaryStage);
            
            LOGGER.info("Login view displayed successfully");
            
        } catch (Exception e) {
            LOGGER.severe("Failed to start application: " + e.getMessage());
            AppLogger.logException(DepotManagementSystem.class, "Application startup failed", e);
            
            // Show error dialog and exit
            showStartupErrorAndExit(e);
        }
    }
    
    @Override
    public void stop() throws Exception {
        try {
            LOGGER.info("Application shutdown initiated...");
            
            // Perform cleanup operations here
            performCleanup();
            
            AppLogger.logAppShutdown(APP_NAME);
            
        } catch (Exception e) {
            LOGGER.severe("Error during application shutdown: " + e.getMessage());
            AppLogger.logException(DepotManagementSystem.class, "Shutdown error", e);
        } finally {
            super.stop();
        }
    }
    
    /**
     * Initialize application-wide components
     */
    private void initializeApplication() {
        try {
            // Initialize system properties for better performance
            System.setProperty("javafx.animation.pulse", "60");
            System.setProperty("prism.lcdtext", "false");
            System.setProperty("prism.subpixeltext", "true");
            System.setProperty("prism.vsync", "true"); // Enable VSync for smoother rendering
            
            // Set application-wide properties
            System.setProperty("app.name", APP_NAME);
            System.setProperty("app.version", APP_VERSION);
            System.setProperty("app.environment", "development"); // Change to "production" for release
            
            LOGGER.info("System properties configured");
            
            // Database initialization will be done in start() method
            LOGGER.info("Application components initialized");
            
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize application components: " + e.getMessage());
            AppLogger.logException(DepotManagementSystem.class, "Application initialization failed", e);
            throw new RuntimeException("Application initialization failed", e);
        }
    }
    
    /**
     * Test database connection before showing UI
     */
    private boolean testDatabaseConnection() {
        try {
            LOGGER.info("Testing database connection...");
            boolean connected = Database.testConnection();
            if (connected) {
                LOGGER.info("Database connection test successful");
                return true;
            } else {
                LOGGER.severe("Database connection test failed");
                return false;
            }
        } catch (Exception e) {
            LOGGER.severe("Database connection test error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Configure the primary stage
     */
    private void configurePrimaryStage(Stage primaryStage) {
        primaryStage.setTitle(APP_NAME + " v" + APP_VERSION);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        primaryStage.setMaximized(false);
        
        // Set application icon (when available)
        try {
            // primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/app-icon.png")));
        } catch (Exception e) {
            LOGGER.warning("Could not load application icon: " + e.getMessage());
        }
        
        // Handle close request
        primaryStage.setOnCloseRequest(event -> {
            LOGGER.info("Application close requested");
            AppLogger.logUserAction("SYSTEM", "APPLICATION_EXIT_REQUESTED", "User clicked close button");
            event.consume(); // Prevent immediate close
            handleApplicationExit();
        });
        
        LOGGER.info("Primary stage configured");
    }
    
    /**
     * Setup global exception handler for JavaFX
     */
    private void setupGlobalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            LOGGER.severe("Uncaught exception in thread " + thread.getName() + ": " + exception.getMessage());
            AppLogger.logException(DepotManagementSystem.class, 
                "Uncaught exception in thread " + thread.getName(), exception);
            
            Platform.runLater(() -> {
                showErrorDialog("Unexpected Error", 
                    "An unexpected error occurred: " + exception.getMessage());
            });
        });
        
        LOGGER.info("Global exception handler configured");
    }
    
    /**
     * Handle application exit with confirmation
     */
    private void handleApplicationExit() {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exit Application");
            alert.setHeaderText("Are you sure you want to exit?");
            alert.setContentText("Any unsaved changes will be lost.");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.OK) {
                    LOGGER.info("User confirmed application exit");
                    AppLogger.logUserAction("SYSTEM", "APPLICATION_EXIT_CONFIRMED", "User confirmed exit");
                    
                    // Perform cleanup before exit
                    try {
                        performCleanup();
                    } catch (Exception e) {
                        LOGGER.warning("Error during cleanup: " + e.getMessage());
                    }
                    
                    Platform.exit();
                    System.exit(0);
                } else {
                    AppLogger.logUserAction("SYSTEM", "APPLICATION_EXIT_CANCELLED", "User cancelled exit");
                }
            });
        });
    }
    
    /**
     * Perform cleanup operations before shutdown
     */
private void performCleanup() {
    try {
        LOGGER.info("Starting cleanup operations...");
        LOGGER.info("Database connections managed automatically (connection-per-request)");
        LOGGER.info("Cleanup operations completed");
        
    } catch (Exception e) {
        LOGGER.warning("Error during cleanup: " + e.getMessage());
        AppLogger.logException(DepotManagementSystem.class, "Cleanup error", e);
    }
}
    
    /**
     * Show startup error dialog and exit
     */
    private void showStartupErrorAndExit(Exception e) {
        Platform.runLater(() -> {
            showErrorDialog("Startup Error", 
                "Failed to start the application: " + e.getMessage() + 
                "\n\nPlease check:\n" +
                "1. Database server is running\n" +
                "2. Database connection settings are correct\n" +
                "3. Network connectivity");
            
            // Perform cleanup before exit
            try {
                performCleanup();
            } catch (Exception ex) {
                LOGGER.warning("Error during emergency cleanup: " + ex.getMessage());
            }
            
            Platform.exit();
            System.exit(1);
        });
    }
    
    /**
     * Show error dialog
     */
    private void showErrorDialog(String title, String message) {
        try {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText("Error");
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            // Fallback to console if JavaFX fails
            System.err.println(title + ": " + message);
        }
    }
    
    /**
     * Get application information
     */
    public static String getApplicationInfo() {
        return APP_NAME + " v" + APP_VERSION;
    }
    
    /**
     * Check if running in development mode
     */
    public static boolean isDevelopmentMode() {
        return "development".equals(System.getProperty("app.environment", "production"));
    }
    
    /**
     * Main method - Entry point of the application
     */
    public static void main(String[] args) {
        try {
            // Print startup banner
            printStartupBanner();
            
            // Set system properties for better rendering
            System.setProperty("javafx.preloader", "lk.bustracking.depotmanagementsystem.utils.ApplicationPreloader");
            
            // Launch JavaFX application
            launch(args);
            
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to launch application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Print startup banner to console
     */
    private static void printStartupBanner() {
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                CTB DEPOT MANAGEMENT SYSTEM                        ║");
        System.out.println("║                      Version 3.0.0                               ║");
        System.out.println("║                                                                    ║");
        System.out.println("║            Real-time Bus Fleet Management                         ║");
        System.out.println("║              with GPS Tracking System                             ║");
        System.out.println("║                                                                    ║");
        System.out.println("║              Built with JavaFX & MVC                              ║");
        System.out.println("║                                                                    ║");
        System.out.println("║           © 2024 Sri Lanka CTB - All Rights Reserved              ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Starting application...");
        System.out.println();
    }
}