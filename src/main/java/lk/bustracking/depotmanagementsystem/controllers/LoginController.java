    // ================================================================
    // SRI LANKA CTB DEPOT MANAGEMENT SYSTEM - ENHANCED LOGIN CONTROLLER
    // File: src/main/java/lk/bustracking/depotmanagementsystem/controllers/LoginController.java
    // Modern Controller with Async Operations and Better Error Handling
    // ================================================================

    package lk.bustracking.depotmanagementsystem.controllers;

    import lk.bustracking.depotmanagementsystem.dao.UserDAO;
    import lk.bustracking.depotmanagementsystem.models.User;
    import lk.bustracking.depotmanagementsystem.views.DashboardView;
    import lk.bustracking.depotmanagementsystem.utils.ValidationUtils;
    import lk.bustracking.depotmanagementsystem.utils.UIConstants;
    import lk.bustracking.depotmanagementsystem.utils.AnimationUtils;

    import javafx.animation.KeyFrame;
    import javafx.animation.Timeline;
    import javafx.animation.FadeTransition;
    import javafx.application.Platform;
    import javafx.concurrent.Task;
    import javafx.stage.Stage;
    import javafx.util.Duration;
    import javafx.scene.control.*;
    import java.util.concurrent.CompletableFuture;
    import java.util.logging.Logger;
    import java.util.logging.Level;

    /**
     * Enhanced LoginController - Handles authentication logic with modern async patterns,
     * comprehensive error handling, and improved user experience.
     * 
     * Features:
     * - Asynchronous authentication to prevent UI freezing
     * - Comprehensive input validation
     * - Enhanced error handling and user feedback
     * - Secure session management
     * - Performance optimized database calls
     */
    public class LoginController {

        // Logger for debugging and monitoring
        private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

        // UI Components
        private final TextField usernameField;
        private final PasswordField passwordField;
        private final Button loginButton;
        private final Label errorLabel;
        private final Label statusLabel;
        private final ProgressIndicator loadingIndicator;
        private final Stage primaryStage;

        // Business Logic Components
        private final UserDAO userDAO;
        private Timeline authenticationTimer;
        private Task<User> currentAuthTask;

        // State Management
        private boolean isAuthenticating = false;
        private int failedAttempts = 0;
        private static final int MAX_FAILED_ATTEMPTS = 8;
        private long lastFailedAttempt = 0;
        private static final long LOCKOUT_DURATION = 300000; // 5 minutes in milliseconds

        /**
         * Constructor - Initialize controller with UI components
         */
        public LoginController(TextField usernameField, PasswordField passwordField,
                              Button loginButton, Label errorLabel, Label statusLabel,
                              ProgressIndicator loadingIndicator, Stage primaryStage) {

            this.usernameField = usernameField;
            this.passwordField = passwordField;
            this.loginButton = loginButton;
            this.errorLabel = errorLabel;
            this.statusLabel = statusLabel;
            this.loadingIndicator = loadingIndicator;
            this.primaryStage = primaryStage;

            // Initialize DAO
            this.userDAO = new UserDAO();

            // Setup initial state
            initialize();

            LOGGER.info("LoginController initialized successfully");
        }

        /**
         * Initialize controller - Setup event handlers and bindings
         */
        private void initialize() {
            try {
                setupEventHandlers();
                setupInitialState();

                LOGGER.info("Controller initialization completed");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to initialize LoginController", e);
                showError("System initialization failed. Please restart the application.");
            }
        }

        /**
         * Setup all event handlers
         */
        private void setupEventHandlers() {
            // Enter key navigation
            usernameField.setOnAction(e -> passwordField.requestFocus());
            passwordField.setOnAction(e -> handleLogin());

            // Login button click
            loginButton.setOnAction(e -> handleLogin());

            // Real-time input validation
            usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.trim().isEmpty() && errorLabel.isVisible()) {
                    hideError();
                }
            });

            passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.isEmpty() && errorLabel.isVisible()) {
                    hideError();
                }
            });
        }

        /**
         * Setup initial UI state
         */
        private void setupInitialState() {
            statusLabel.setText("Ready to sign in");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            loadingIndicator.setVisible(false);
            loginButton.setDisable(false);
        }

        /**
         * Main login handler - Validates input and initiates authentication
         */
        public void handleLogin() {
            // Prevent multiple simultaneous login attempts
            if (isAuthenticating) {
                LOGGER.warning("Login attempt blocked - authentication already in progress");
                return;
            }

            try {
                // Check for account lockout
                if (isAccountLocked()) {
                    long remainingTime = getRemainingLockoutTime();
                    showError(String.format("Account temporarily locked. Try again in %d minutes.", 
                        remainingTime / 60000 + 1));
                    return;
                }

                // Validate input
                String validationError = validateLoginInput();
                if (validationError != null) {
                    showError(validationError);
                    return;
                }

                // Start authentication process
                startAsyncAuthentication();

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error in handleLogin", e);
                showError("An unexpected error occurred. Please try again.");
            }
        }

        /**
         * Validate login input with enhanced validation rules
         */
        private String validateLoginInput() {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            // Check for empty username
            if (username.isEmpty()) {
                usernameField.requestFocus();
                return "Please enter your username";
            }

            // Check for empty password
            if (password.isEmpty()) {
                passwordField.requestFocus();
                return "Please enter your password";
            }

            // Validate username format
            if (!ValidationUtils.isValidUsername(username)) {
                usernameField.requestFocus();
                return "Invalid username format";
            }

            // Validate password strength (basic check)
            if (!ValidationUtils.isValidPassword(password)) {
                passwordField.requestFocus();
                return "Password must be at least 4 characters long";
            }

            return null; // All validations passed
        }

        /**
         * Check if account is currently locked due to failed attempts
         */
        private boolean isAccountLocked() {
            return failedAttempts >= MAX_FAILED_ATTEMPTS && 
                   (System.currentTimeMillis() - lastFailedAttempt) < LOCKOUT_DURATION;
        }

        /**
         * Get remaining lockout time in milliseconds
         */
        private long getRemainingLockoutTime() {
            return LOCKOUT_DURATION - (System.currentTimeMillis() - lastFailedAttempt);
        }

        /**
         * Start asynchronous authentication to prevent UI blocking
         */
        private void startAsyncAuthentication() {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            // Update UI to show authentication in progress
            startLoginAnimation();

            // Create authentication task
            currentAuthTask = new Task<User>() {
                @Override
                protected User call() throws Exception {
                    // Add small delay for better UX (shows loading state)
                    Thread.sleep(800);

                    // Perform authentication
                    LOGGER.info("Attempting authentication for user: " + username);
                    return userDAO.validateLogin(username, password);
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        User user = getValue();
                        handleAuthenticationResult(user, username);
                    });
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        Throwable exception = getException();
                        LOGGER.log(Level.SEVERE, "Authentication task failed", exception);
                        handleAuthenticationFailure("Authentication service unavailable. Please try again.");
                    });
                }

                @Override
                protected void cancelled() {
                    Platform.runLater(() -> {
                        handleAuthenticationFailure("Authentication was cancelled.");
                    });
                }
            };

            // Run authentication task in background thread
            Thread authThread = new Thread(currentAuthTask);
            authThread.setDaemon(true);
            authThread.setName("AuthenticationTask");
            authThread.start();

            // Set timeout for authentication
            authenticationTimer = new Timeline(new KeyFrame(Duration.seconds(10), e -> {
                if (currentAuthTask != null && currentAuthTask.isRunning()) {
                    currentAuthTask.cancel();
                    handleAuthenticationFailure("Authentication timeout. Please try again.");
                }
            }));
            authenticationTimer.play();
        }

        /**
         * Handle successful or failed authentication result
         */
        private void handleAuthenticationResult(User user, String username) {
            stopLoginAnimation();

            if (user != null) {
                // Authentication successful
                handleSuccessfulLogin(user, username);
            } else {
                // Authentication failed
                handleFailedLogin(username);
            }
        }

        /**
         * Handle successful authentication
         */
        private void handleSuccessfulLogin(User user, String username) {
            LOGGER.info("Authentication successful for user: " + username + " with role: " + user.getRole());

            // Reset failed attempts
            failedAttempts = 0;
            lastFailedAttempt = 0;

            // Show success message
            showSuccessMessage("Login successful! Loading dashboard...");

            // Navigate to dashboard after brief delay
            Timeline dashboardDelay = new Timeline(new KeyFrame(Duration.seconds(1.2), e -> {
                navigateToDashboard(user);
            }));
            dashboardDelay.play();
        }

        /**
         * Handle failed authentication
         */
        private void handleFailedLogin(String username) {
            LOGGER.warning("Authentication failed for user: " + username);

            // Increment failed attempts
            failedAttempts++;
            lastFailedAttempt = System.currentTimeMillis();

            // Clear password field
            passwordField.clear();

            // Show appropriate error message
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                showError("Too many failed attempts. Account temporarily locked for 5 minutes.");
            } else {
                int remainingAttempts = MAX_FAILED_ATTEMPTS - failedAttempts;
                showError(String.format("Invalid username or password. %d attempts remaining.", remainingAttempts));
            }

            // Focus back to username field
            usernameField.requestFocus();
        }

        /**
         * Handle authentication failure due to system errors
         */
        private void handleAuthenticationFailure(String errorMessage) {
            stopLoginAnimation();
            showError(errorMessage);
            LOGGER.severe("Authentication system failure: " + errorMessage);
        }

        /**
         * Navigate to dashboard with proper error handling
         */
        private void navigateToDashboard(User user) {
            try {
                LOGGER.info("Navigating to dashboard for user: " + user.getUsername());

                // Create dashboard view
                DashboardView dashboard = new DashboardView(user);
                Stage dashboardStage = new Stage();

                // Start dashboard
                Platform.runLater(() -> {
                    try {
                        dashboard.start(dashboardStage);

                        // Close login window after successful dashboard load
                        primaryStage.close();

                        LOGGER.info("Successfully navigated to dashboard");
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Failed to open dashboard", e);
                        showError("Failed to load dashboard. Please try logging in again.");

                        // Re-enable login on failure
                        resetLoginState();
                    }
                });

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error during dashboard navigation", e);
                showError("System error occurred. Please try again.");
                resetLoginState();
            }
        }

        // ===================== UI Animation Methods =====================

        /**
         * Start login animation with improved visual feedback
         */
        private void startLoginAnimation() {
            isAuthenticating = true;

            loginButton.setDisable(true);
            loginButton.setText("🔐 Authenticating...");
            loadingIndicator.setVisible(true);
            statusLabel.setText("Verifying credentials...");
            statusLabel.setStyle(UIConstants.STATUS_LABEL_AUTHENTICATING);

            // Add subtle animation to login button
            AnimationUtils.addPulseEffect(loginButton);
        }

        /**
         * Stop login animation and reset UI state
         */
        private void stopLoginAnimation() {
            isAuthenticating = false;

            // Stop authentication timer
            if (authenticationTimer != null) {
                authenticationTimer.stop();
            }

            // Reset UI components
            Platform.runLater(() -> {
                loginButton.setDisable(false);
                loginButton.setText("🚀 Sign In to Dashboard");
                loadingIndicator.setVisible(false);
                statusLabel.setText("Ready to sign in");
                statusLabel.setStyle(UIConstants.STATUS_LABEL_STYLE);

                // Remove pulse effect
                AnimationUtils.removePulseEffect(loginButton);
            });
        }

        /**
         * Reset login state after error
         */
        private void resetLoginState() {
            Platform.runLater(() -> {
                stopLoginAnimation();
                passwordField.clear();
                usernameField.requestFocus();
            });
        }

        /**
         * Show success message with animation
         */
        private void showSuccessMessage(String message) {
            statusLabel.setText(message);
            statusLabel.setStyle(UIConstants.STATUS_LABEL_SUCCESS);

            // Add success animation
            AnimationUtils.addSuccessAnimation(statusLabel);
        }

        // ===================== Error Handling Methods =====================

        /**
         * Show error message with enhanced animation
         */
        private void showError(String message) {
            Platform.runLater(() -> {
                errorLabel.setText("⚠️ " + message);
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);

                // Add error animation
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), errorLabel);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();

                // Add shake animation for attention
                AnimationUtils.addShakeAnimation(errorLabel);

                LOGGER.warning("Error displayed to user: " + message);
            });
        }

        /**
         * Hide error message with animation
         */
        private void hideError() {
            if (errorLabel.isVisible()) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(250), errorLabel);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(e -> {
                    errorLabel.setVisible(false);
                    errorLabel.setManaged(false);
                });
                fadeOut.play();
            }
        }

        // ===================== Session Management =====================

        /**
         * Clear sensitive data and reset form
         */
        private void clearSensitiveData() {
            Platform.runLater(() -> {
                passwordField.clear();
                errorLabel.setText("");
                statusLabel.setText("Ready to sign in");
            });
        }

        /**
         * Check if user session is still valid (for future implementation)
         */
        public boolean isSessionValid() {
            // TODO: Implement session validation logic
            return true;
        }

        // ===================== Utility Methods =====================

        /**
         * Get current authentication status
         */
        public boolean isAuthenticating() {
            return isAuthenticating;
        }

        /**
         * Get failed attempts count
         */
        public int getFailedAttempts() {
            return failedAttempts;
        }

        /**
         * Reset failed attempts (for admin override)
         */
        public void resetFailedAttempts() {
            failedAttempts = 0;
            lastFailedAttempt = 0;
            LOGGER.info("Failed attempts counter reset");
        }

        /**
         * Force cancel current authentication
         */
        public void cancelAuthentication() {
            if (currentAuthTask != null && currentAuthTask.isRunning()) {
                currentAuthTask.cancel();
                stopLoginAnimation();
                showError("Authentication cancelled by user");
            }
        }

        /**
         * Cleanup resources when controller is destroyed
         */
        public void cleanup() {
            try {
                // Cancel any running authentication task
                if (currentAuthTask != null && currentAuthTask.isRunning()) {
                    currentAuthTask.cancel();
                }

                // Stop timers
                if (authenticationTimer != null) {
                    authenticationTimer.stop();
                }

                // Clear sensitive data
                clearSensitiveData();

                // Close database connections if any
               // Close database connections if any
                if (userDAO != null) {
                 try {
                userDAO.cleanup();
                 } catch (Exception e) {
                  LOGGER.log(Level.WARNING, "Error during UserDAO cleanup", e);
                }
                }

                LOGGER.info("LoginController cleanup completed");

            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error during controller cleanup", e);
            }
        }

        // ===================== Testing Methods =====================

        /**
         * Set test credentials (for development/testing only)
         */
        public void setTestCredentials(String username, String password) {
            if (!"production".equals(System.getProperty("app.environment"))) {
                Platform.runLater(() -> {
                    usernameField.setText(username);
                    passwordField.setText(password);
                });
                LOGGER.info("Test credentials set for development environment");
            }
        }

        /**
         * Simulate authentication for testing
         */
        public CompletableFuture<Boolean> simulateAuthentication(String username, String password) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    User user = userDAO.validateLogin(username, password);
                    return user != null;
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in simulated authentication", e);
                    return false;
                }
            });
        }
    }