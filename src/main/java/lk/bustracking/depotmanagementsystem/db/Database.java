package lk.bustracking.depotmanagementsystem.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Enhanced Database Connection Class with Connection Pooling
 */
public class Database {
    
    private static final Logger LOGGER = Logger.getLogger(Database.class.getName());
    
    // Database Configuration
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=DepotDB;encrypt=false";
    private static final String USER = "sa";
    private static final String PASSWORD = "6238";
    
    // Connection settings
    private static final int CONNECTION_TIMEOUT = 30; // seconds
    private static final int LOGIN_TIMEOUT = 10; // seconds
    
    // Remove the singleton connection - this is causing the issue
    // private static Connection connection;
    
    static {
        try {
            // Register SQL Server JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            LOGGER.info("SQL Server JDBC driver registered successfully");
        } catch (ClassNotFoundException e) {
            LOGGER.severe("Failed to register SQL Server JDBC driver: " + e.getMessage());
        }
    }
    
    /**
     * Get a NEW database connection for each request
     * This prevents "socket closed" issues from shared connections
     */
    public static Connection getConnection() throws SQLException {
        try {
            Properties connectionProps = new Properties();
            connectionProps.put("user", USER);
            connectionProps.put("password", PASSWORD);
            
            // Optimize connection settings
            connectionProps.put("loginTimeout", String.valueOf(LOGIN_TIMEOUT));
            connectionProps.put("socketTimeout", String.valueOf(CONNECTION_TIMEOUT * 1000));
            connectionProps.put("selectMethod", "cursor");
            connectionProps.put("sendStringParametersAsUnicode", "false");
            connectionProps.put("trustServerCertificate", "true");
            connectionProps.put("applicationName", "DepotManagementSystem"); // Identify app
            
            // Set timeouts
            DriverManager.setLoginTimeout(LOGIN_TIMEOUT);
            
            Connection conn = DriverManager.getConnection(URL, connectionProps);
            
            // Configure connection settings
            conn.setAutoCommit(true); // Enable auto-commit for standard operations
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            
            LOGGER.info("New database connection created successfully");
            return conn;
            
        } catch (SQLException e) {
            LOGGER.severe("Failed to create database connection: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get a validated connection with retry logic
     */
    public static Connection getValidatedConnection() throws SQLException {
        Connection conn = getConnection();
        if (conn != null && !conn.isClosed()) {
            // Test the connection with a simple query
            try (var stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT 1").close();
            }
            return conn;
        }
        throw new SQLException("Unable to establish validated connection");
    }
    
    /**
     * Safely close connection with proper error handling
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                    LOGGER.fine("Database connection closed successfully");
                }
            } catch (SQLException e) {
                LOGGER.warning("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Close connection and statement
     */
    public static void closeResources(Connection conn, java.sql.Statement stmt) {
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            LOGGER.warning("Error closing statement: " + e.getMessage());
        }
        closeConnection(conn);
    }
    
    /**
     * Close connection, statement, and result set
     */
    public static void closeResources(Connection conn, java.sql.Statement stmt, java.sql.ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            LOGGER.warning("Error closing result set: " + e.getMessage());
        }
        closeResources(conn, stmt);
    }
    
    /**
     * Test database connection
     */
    public static boolean testConnection() {
        Connection testConn = null;
        try {
            testConn = getConnection();
            if (testConn != null && !testConn.isClosed()) {
                LOGGER.info("Database connection test successful");
                return true;
            } else {
                LOGGER.warning("Database connection test failed - connection is null or closed");
                return false;
            }
        } catch (SQLException e) {
            LOGGER.severe("Database connection test failed: " + e.getMessage());
            return false;
        } finally {
            closeConnection(testConn);
        }
    }
    
    /**
     * Check if connection is valid
     */
    public static boolean isConnectionValid(Connection conn) {
        if (conn == null) return false;
        try {
            return !conn.isClosed() && conn.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Initialize database (for application startup)
     */
    public static void initialize() throws SQLException {
        if (testConnection()) {
            LOGGER.info("Database initialized successfully");
        } else {
            throw new SQLException("Database initialization failed - connection test failed");
        }
    }
    
    
    /**
     * Database Information Class
     */
    public static class DatabaseInfo {
        // ... keep your existing DatabaseInfo class ...
    }
}