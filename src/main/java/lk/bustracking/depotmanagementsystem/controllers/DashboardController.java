package lk.bustracking.depotmanagementsystem.controllers;

import lk.bustracking.depotmanagementsystem.models.User;
import lk.bustracking.depotmanagementsystem.models.DashboardData;
import lk.bustracking.depotmanagementsystem.services.DashboardService;
import lk.bustracking.depotmanagementsystem.services.UserService;
import lk.bustracking.depotmanagementsystem.views.DashboardView;
import lk.bustracking.depotmanagementsystem.views.LoginView;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Dashboard Controller - Handles business logic for dashboard operations
 * Follows MVC architecture pattern
 */
public class DashboardController {
    
    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());
    
    private final DashboardView view;
    private final DashboardService dashboardService;
    private final UserService userService;
    private User currentUser;
    private DashboardData currentDashboardData;
    
    // Dashboard update interval (in seconds)
    private static final int UPDATE_INTERVAL = 30;
    
    public DashboardController(DashboardView view) {
        this.view = view;
        this.dashboardService = new DashboardService();
        this.userService = new UserService();
        this.currentDashboardData = new DashboardData();
    }
    
    /**
     * Initialize dashboard with user data
     */
    public void initializeDashboard(User user) {
        this.currentUser = user;
        
        // Load initial dashboard data
        loadDashboardDataAsync();
        
        // Start real-time updates
        startRealTimeUpdates();
        
        LOGGER.info("Dashboard initialized for user: " + user.getUsername());
    }
    
    /**
     * Load dashboard data asynchronously
     */
    private void loadDashboardDataAsync() {
        Task<DashboardData> loadDataTask = new Task<DashboardData>() {
            @Override
            protected DashboardData call() throws Exception {
                return dashboardService.getDashboardData();
            }
            
            @Override
            protected void succeeded() {
                currentDashboardData = getValue();
                updateDashboardUI();
                LOGGER.info("Dashboard data loaded successfully");
            }
            
            @Override
            protected void failed() {
                LOGGER.severe("Failed to load dashboard data: " + getException().getMessage());
                view.showErrorMessage("Failed to load dashboard data. Please refresh.");
            }
        };
        
        Thread loadThread = new Thread(loadDataTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    /**
     * Update dashboard UI with current data
     */
    private void updateDashboardUI() {
        Platform.runLater(() -> {
            view.updateStatistics(currentDashboardData);
            view.updateRecentActivity(currentDashboardData.getRecentActivities());
            view.updateSystemStatus(currentDashboardData.getSystemStatus());
        });
    }
    
    /**
     * Start real-time updates for dashboard
     */
    private void startRealTimeUpdates() {
        Task<Void> updateTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while (!isCancelled()) {
                    Thread.sleep(UPDATE_INTERVAL * 1000);
                    
                    if (!isCancelled()) {
                        DashboardData newData = dashboardService.getDashboardData();
                        Platform.runLater(() -> {
                            currentDashboardData = newData;
                            updateDashboardUI();
                        });
                    }
                }
                return null;
            }
        };
        
        Thread updateThread = new Thread(updateTask);
        updateThread.setDaemon(true);
        updateThread.start();
        
        // Store the task for later cancellation
        view.setUpdateTask(updateTask);
    }
    
    /**
     * Handle user logout
     */
    public void handleLogout(Stage currentStage) {
        try {
            // Log the logout activity
            userService.logUserActivity(currentUser.getId(), "User logged out", LocalDateTime.now());
            
            // Close current stage
            currentStage.close();
            
            // Open login view
            LoginView loginView = new LoginView();
            Stage loginStage = new Stage();
            loginView.start(loginStage);
            
            LOGGER.info("User logged out successfully: " + currentUser.getUsername());
            
        } catch (Exception e) {
            LOGGER.severe("Error during logout: " + e.getMessage());
            view.showErrorMessage("Error occurred during logout.");
        }
    }
    
    /**
     * Refresh dashboard data manually
     */
    public void refreshDashboard() {
        view.showLoadingIndicator(true);
        loadDashboardDataAsync();
    }
    
    /**
     * Get current time formatted
     */
    public String getCurrentTimeFormatted() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, MMM dd yyyy | HH:mm:ss");
        return now.format(formatter);
    }
    
    // Getters for accessing current state
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public DashboardData getCurrentDashboardData() {
        return currentDashboardData;
    }
    
    /**
     * Cleanup resources when dashboard is closed
     */
    public void cleanup() {
        // Stop any background tasks
        if (view.getUpdateTask() != null) {
            view.getUpdateTask().cancel();
        }
        
        LOGGER.info("Dashboard controller cleanup completed");
    }
}