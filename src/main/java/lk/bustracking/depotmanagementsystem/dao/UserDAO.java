package lk.bustracking.depotmanagementsystem.dao;

import lk.bustracking.depotmanagementsystem.db.Database;
import lk.bustracking.depotmanagementsystem.models.User;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Fixed UserDAO compatible with your existing database schema
 */
public class UserDAO {
    
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());
    
    /**
     * Hash password using SHA-256 (same as your existing data)
     */
    private byte[] hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(password.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, "Error hashing password", e);
            throw new RuntimeException("Password hashing failed", e);
        }
    }
    
    /**
     * Validate user login credentials - FIXED for your database schema
     */
    public User validateLogin(String username, String password) {
        // Your current table structure appears to be: user_id, username, password, role
        String sql = "SELECT user_id, username, role FROM Users WHERE username = ? AND password = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setBytes(2, hashPassword(password));
            
            LOGGER.info("Executing login query for user: " + username);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Create user with the constructor that matches your original UserDAO
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"), 
                    rs.getString("role")
                );
                
                // Set additional properties
                user.setActive(true); // Assume active if in database
                user.setLastLogin(LocalDateTime.now());
                
                // Update last login in database
                updateLastLogin(user.getUserId());
                
                LOGGER.info("User authentication successful: " + username + " (ID: " + user.getUserId() + ")");
                return user;
            } else {
                LOGGER.warning("Authentication failed for username: " + username);
                
                // Debug: Check if user exists at all
                debugUserLogin(username, password);
                
                return null;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during login validation for user: " + username, e);
            throw new RuntimeException("Database connection failed", e);
        }
    }
    
    /**
     * Debug method to help troubleshoot login issues
     */
    private void debugUserLogin(String username, String password) {
        try (Connection conn = Database.getConnection()) {
            
            // Check if user exists
            String checkUserSql = "SELECT user_id, username, role FROM Users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkUserSql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    LOGGER.info("User exists in database: " + username + " (ID: " + rs.getInt("user_id") + ")");
                    
                    // Check password hash
                    String hashSql = "SELECT password FROM Users WHERE username = ?";
                    try (PreparedStatement hashStmt = conn.prepareStatement(hashSql)) {
                        hashStmt.setString(1, username);
                        ResultSet hashRs = hashStmt.executeQuery();
                        
                        if (hashRs.next()) {
                            byte[] storedHash = hashRs.getBytes("password");
                            byte[] providedHash = hashPassword(password);
                            
                            LOGGER.info("Stored hash length: " + (storedHash != null ? storedHash.length : 0));
                            LOGGER.info("Provided hash length: " + providedHash.length);
                            
                            // Compare first few bytes for debugging
                            if (storedHash != null && storedHash.length > 0 && providedHash.length > 0) {
                                LOGGER.info("First byte of stored hash: " + String.format("0x%02X", storedHash[0]));
                                LOGGER.info("First byte of provided hash: " + String.format("0x%02X", providedHash[0]));
                            }
                        }
                    }
                } else {
                    LOGGER.warning("User does not exist in database: " + username);
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error during debug login check", e);
        }
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        String sql = "SELECT user_id, username, role FROM Users WHERE user_id = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("role")
                );
                user.setActive(true);
                return user;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting user by ID: " + userId, e);
        }
        
        return null;
    }
    
    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT user_id, username, role FROM Users WHERE username = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("role")
                );
                user.setActive(true);
                return user;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting user by username: " + username, e);
        }
        
        return null;
    }
    
    /**
     * Update user's last login time (if you have this column)
     */
    private void updateLastLogin(int userId) {
        // Since your current table might not have last_login column, make this optional
        String sql = "UPDATE Users SET last_login = GETDATE() WHERE user_id = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            
            LOGGER.info("Last login updated for user ID: " + userId);
            
        } catch (SQLException e) {
            // Don't fail login if last_login column doesn't exist
            LOGGER.log(Level.INFO, "Could not update last login (column might not exist): " + e.getMessage());
        }
    }
    
    /**
     * Create new user
     */
    public boolean createUser(String username, String password, String role, String email, String fullName) {
        // Check if username already exists
        if (getUserByUsername(username) != null) {
            LOGGER.warning("Attempted to create user with existing username: " + username);
            return false;
        }
        
        String sql = "INSERT INTO Users (username, password, role) VALUES (?, ?, ?)";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setBytes(2, hashPassword(password));
            stmt.setString(3, role);
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                LOGGER.info("User created successfully: " + username);
                return true;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating user: " + username, e);
        }
        
        return false;
    }
    
    /**
     * Get all active users
     */
    public List<User> getAllActiveUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, role FROM Users ORDER BY username";
        
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("role")
                );
                user.setActive(true);
                users.add(user);
            }
            
            LOGGER.info("Retrieved " + users.size() + " users");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all active users", e);
        }
        
        return users;
    }
    
    /**
     * Check if username is available
     */
    public boolean isUsernameAvailable(String username) {
        return getUserByUsername(username) == null;
    }
    
    /**
     * Test database connection
     */
    public boolean testConnection() {
        try (Connection conn = Database.getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection test failed", e);
            return false;
        }
    }
    
    /**
     * Get user statistics
     */
    public UserStats getUserStatistics() {
        String sql = "SELECT COUNT(*) as total_users, " +
                    "SUM(CASE WHEN role = 'admin' THEN 1 ELSE 0 END) as admin_users, " +
                    "SUM(CASE WHEN role = 'staff' THEN 1 ELSE 0 END) as staff_users " +
                    "FROM Users";
        
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return new UserStats(
                    rs.getInt("total_users"),
                    rs.getInt("total_users"), // Assume all are active
                    rs.getInt("admin_users"),
                    rs.getInt("staff_users")
                );
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting user statistics", e);
        }
        
        return new UserStats(0, 0, 0, 0);
    }
    
    /**
     * Update user password
     */
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE Users SET password = ? WHERE user_id = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBytes(1, hashPassword(newPassword));
            stmt.setInt(2, userId);
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                LOGGER.info("Password updated for user ID: " + userId);
                return true;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating password for user ID: " + userId, e);
        }
        
        return false;
    }
    
    /**
     * Deactivate user (add this method to fix compilation error)
     */
    public boolean deactivateUser(int userId) {
        // Since your table might not have an 'active' column, we'll simulate deactivation
        // by setting role to 'INACTIVE' or similar approach
        String sql = "UPDATE Users SET role = 'INACTIVE' WHERE user_id = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                LOGGER.info("User deactivated: " + userId);
                return true;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deactivating user ID: " + userId, e);
        }
        
        return false;
    }
    
    /**
     * Initialize default user (add this method to fix compilation error)
     */
    public void initializeDefaultUser() {
        try {
            // Check if any admin users exist
            String checkSql = "SELECT COUNT(*) FROM Users WHERE role = 'admin'";
            try (Connection conn = Database.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(checkSql)) {
                
                if (rs.next() && rs.getInt(1) == 0) {
                    // No admin users exist, create one
                    boolean created = createUser("admin", "admin123", "admin", "admin@ctb.lk", "System Administrator");
                    
                    if (created) {
                        LOGGER.info("Default admin user created successfully");
                    } else {
                        LOGGER.warning("Failed to create default admin user");
                    }
                } else {
                    LOGGER.info("Admin user already exists, skipping initialization");
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking/creating default user", e);
        }
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        LOGGER.info("UserDAO cleanup completed");
    }
    
    /**
     * Inner class for user statistics
     */
    public static class UserStats {
        public final int totalUsers;
        public final int activeUsers;
        public final int adminUsers;
        public final int managerUsers;
        
        public UserStats(int totalUsers, int activeUsers, int adminUsers, int managerUsers) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.adminUsers = adminUsers;
            this.managerUsers = managerUsers;
        }
    }
}