package lk.bustracking.depotmanagementsystem.services;

import lk.bustracking.depotmanagementsystem.models.User;
import lk.bustracking.depotmanagementsystem.models.ActivityLog;
import lk.bustracking.depotmanagementsystem.models.ActivityType;
import lk.bustracking.depotmanagementsystem.dao.UserDAO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * User Service - Fixed to work with your database schema
 */
public class UserService {
    
    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());
    
    private final UserDAO userDAO;
    
    public UserService() {
        this.userDAO = new UserDAO();
    }
    
    /**
     * Authenticate user with username and password
     */
    public User authenticateUser(String username, String password) {
        try {
            LOGGER.info("Authentication attempt for username: " + username);
            
            // Validate input
            if (username == null || username.trim().isEmpty()) {
                LOGGER.warning("Authentication failed: Empty username");
                return null;
            }
            
            if (password == null || password.isEmpty()) {
                LOGGER.warning("Authentication failed: Empty password");
                return null;
            }
            
            // Use DAO to validate credentials
            User user = userDAO.validateLogin(username.trim(), password);
            
            if (user != null) {
                // Log successful authentication
                logUserActivity(user.getId(), "User logged in successfully", LocalDateTime.now());
                LOGGER.info("User authenticated successfully: " + username);
                return user;
            } else {
                LOGGER.warning("Authentication failed for user: " + username);
                return null;
            }
            
        } catch (Exception e) {
            LOGGER.severe("Authentication error for user " + username + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(String userId) {
        try {
            int id = Integer.parseInt(userId);
            return userDAO.getUserById(id);
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid user ID format: " + userId);
            return null;
        } catch (Exception e) {
            LOGGER.severe("Error retrieving user by ID " + userId + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        try {
            return userDAO.getUserByUsername(username);
        } catch (Exception e) {
            LOGGER.severe("Error retrieving user by username " + username + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Validate user session
     */
    public boolean validateUserSession(User user) {
        try {
            if (user == null) return false;
            
            User currentUser = getUserById(user.getId());
            if (currentUser == null || !currentUser.isActive()) {
                return false;
            }
            
            // For now, assume session is valid if user exists
            // In production, you'd check session expiry
            return true;
            
        } catch (Exception e) {
            LOGGER.severe("Error validating user session: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update user's last activity
     */
    public void updateUserActivity(String userId) {
        try {
            User user = getUserById(userId);
            if (user != null) {
                user.setLastLogin(LocalDateTime.now());
                LOGGER.info("Updated activity for user: " + user.getUsername());
            }
        } catch (Exception e) {
            LOGGER.severe("Error updating user activity for ID " + userId + ": " + e.getMessage());
        }
    }
    
    /**
     * Log user activity
     */
    public void logUserActivity(String userId, String activity, LocalDateTime timestamp) {
        try {
            User user = getUserById(userId);
            if (user != null) {
                LOGGER.info("Activity logged for user " + user.getUsername() + ": " + activity);
                
                // Create activity log entry
                ActivityLog log = new ActivityLog(
                    activity + " - " + user.getDisplayName(),
                    timestamp,
                    ActivityType.USER_LOGIN,
                    "info"
                );
                log.setUserId(userId);
                
                // In production, save to database activity log table
            }
        } catch (Exception e) {
            LOGGER.severe("Error logging user activity: " + e.getMessage());
        }
    }
    
    /**
     * Change user password
     */
    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        try {
            int id = Integer.parseInt(userId);
            
            // Validate new password
            if (!isValidPassword(newPassword)) {
                LOGGER.warning("Password change failed: New password doesn't meet requirements");
                return false;
            }
            
            // Update password using DAO
            boolean updated = userDAO.updatePassword(id, newPassword);
            
            if (updated) {
                logUserActivity(userId, "Password changed", LocalDateTime.now());
                LOGGER.info("Password changed successfully for user ID: " + userId);
            }
            
            return updated;
            
        } catch (Exception e) {
            LOGGER.severe("Error changing password for user ID " + userId + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate password strength
     */
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 4) {
            return false;
        }
        return true;
    }
    
    /**
     * Get all active users
     */
    public List<User> getActiveUsers() {
        try {
            return userDAO.getAllActiveUsers();
        } catch (Exception e) {
            LOGGER.severe("Error retrieving active users: " + e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Check if username is available
     */
    public boolean isUsernameAvailable(String username) {
        try {
            return userDAO.isUsernameAvailable(username);
        } catch (Exception e) {
            LOGGER.severe("Error checking username availability: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Create new user
     */
    public boolean createUser(String username, String password, String email, String fullName, String role) {
        try {
            if (!isUsernameAvailable(username)) {
                LOGGER.warning("Cannot create user: Username already exists - " + username);
                return false;
            }
            
            if (!isValidPassword(password)) {
                LOGGER.warning("Cannot create user: Invalid password for username - " + username);
                return false;
            }
            
            boolean created = userDAO.createUser(username, password, role, email, fullName);
            
            if (created) {
                LOGGER.info("New user created successfully: " + username);
            }
            
            return created;
            
        } catch (Exception e) {
            LOGGER.severe("Error creating user " + username + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Deactivate user
     */
    public boolean deactivateUser(String userId) {
        try {
            int id = Integer.parseInt(userId);
            
            boolean deactivated = userDAO.deactivateUser(id);
            
            if (deactivated) {
                logUserActivity(userId, "User account deactivated", LocalDateTime.now());
                LOGGER.info("User deactivated: " + userId);
            }
            
            return deactivated;
            
        } catch (Exception e) {
            LOGGER.severe("Error deactivating user ID " + userId + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get user's role permissions
     */
    public List<String> getUserPermissions(User user) {
        List<String> permissions = List.of();
        
        if (user == null) return permissions;
        
        String role = user.getRole();
        
        switch (role.toUpperCase()) {
            case "ADMIN":
                permissions = List.of(
                    "VIEW_DASHBOARD", "MANAGE_BUSES", "MANAGE_EMPLOYEES", 
                    "VIEW_REPORTS", "SYSTEM_SETTINGS", "USER_MANAGEMENT",
                    "GPS_TRACKING", "MAINTENANCE_MANAGEMENT"
                );
                break;
                
            case "STAFF":
                permissions = List.of(
                    "VIEW_DASHBOARD", "VIEW_BUSES", "GPS_TRACKING", "VIEW_REPORTS"
                );
                break;
                
            default:
                permissions = List.of("VIEW_DASHBOARD");
                break;
        }
        
        return permissions;
    }
    
    /**
     * Check if user has specific permission
     */
    public boolean hasPermission(User user, String permission) {
        List<String> permissions = getUserPermissions(user);
        return permissions.contains(permission);
    }
    
    /**
     * Get user statistics from database
     */
    public UserDAO.UserStats getUserStatistics() {
        try {
            return userDAO.getUserStatistics();
        } catch (Exception e) {
            LOGGER.severe("Error getting user statistics: " + e.getMessage());
            return new UserDAO.UserStats(0, 0, 0, 0);
        }
    }
    
    /**
     * Test database connectivity
     */
    public boolean testDatabaseConnection() {
        try {
            return userDAO.testConnection();
        } catch (Exception e) {
            LOGGER.severe("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Initialize system with default users if needed
     */
    public void initializeSystemUsers() {
        try {
            userDAO.initializeDefaultUser();
            LOGGER.info("System user initialization completed");
        } catch (Exception e) {
            LOGGER.severe("Error during system user initialization: " + e.getMessage());
        }
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        try {
            if (userDAO != null) {
                userDAO.cleanup();
            }
            LOGGER.info("UserService cleanup completed");
        } catch (Exception e) {
            LOGGER.warning("Error during UserService cleanup: " + e.getMessage());
        }
    }
}