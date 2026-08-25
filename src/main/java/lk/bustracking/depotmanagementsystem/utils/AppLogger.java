package lk.bustracking.depotmanagementsystem.utils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

/**
 * Application Logger Utility Class
 * Provides centralized logging configuration for the entire application
 */
public class AppLogger {
    
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE_PREFIX = "depot_management";
    private static final String LOG_PATTERN = "[%1$tF %1$tT] [%4$s] %3$s: %5$s%n";
    
    private static boolean initialized = false;
    
    /**
     * Initialize the logging system
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            // Create logs directory if it doesn't exist
            File logDir = new File(LOG_DIR);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            // Configure root logger
            Logger rootLogger = Logger.getLogger("");
            rootLogger.setLevel(Level.INFO);
            
            // Remove default console handler
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }
            
            // Add custom console handler
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            consoleHandler.setFormatter(new CustomFormatter());
            rootLogger.addHandler(consoleHandler);
            
            // Add file handler
            String logFileName = String.format("%s/%s_%s.log", 
                LOG_DIR, 
                LOG_FILE_PREFIX, 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            );
            
            FileHandler fileHandler = new FileHandler(logFileName, true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new CustomFormatter());
            rootLogger.addHandler(fileHandler);
            
            initialized = true;
            
            Logger logger = getLogger(AppLogger.class);
            logger.info("Application logging system initialized successfully");
            logger.info("Log file: " + logFileName);
            
        } catch (IOException e) {
            System.err.println("Failed to initialize logging system: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get logger for a specific class
     */
    public static Logger getLogger(Class<?> clazz) {
        if (!initialized) {
            initialize();
        }
        return Logger.getLogger(clazz.getName());
    }
    
    /**
     * Get logger with a specific name
     */
    public static Logger getLogger(String name) {
        if (!initialized) {
            initialize();
        }
        return Logger.getLogger(name);
    }
    
    /**
     * Log application startup
     */
    public static void logAppStart(String appName, String version) {
        Logger logger = getLogger("APPLICATION");
        logger.info("=".repeat(60));
        logger.info(String.format("%s v%s - STARTING", appName, version));
        logger.info("=".repeat(60));
        logger.info("Java Version: " + System.getProperty("java.version"));
        logger.info("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        logger.info("User: " + System.getProperty("user.name"));
        logger.info("Working Directory: " + System.getProperty("user.dir"));
        logger.info("=".repeat(60));
    }
    
    /**
     * Log application shutdown
     */
    public static void logAppShutdown(String appName) {
        Logger logger = getLogger("APPLICATION");
        logger.info("=".repeat(60));
        logger.info(String.format("%s - SHUTTING DOWN", appName));
        logger.info("=".repeat(60));
    }
    
    /**
     * Log database operations
     */
    public static void logDatabaseOperation(String operation, String table, boolean success) {
        Logger logger = getLogger("DATABASE");
        if (success) {
            logger.info(String.format("Database operation successful: %s on %s", operation, table));
        } else {
            logger.warning(String.format("Database operation failed: %s on %s", operation, table));
        }
    }
    
    /**
     * Log user actions
     */
    public static void logUserAction(String username, String action, String details) {
        Logger logger = getLogger("USER_ACTION");
        logger.info(String.format("User: %s | Action: %s | Details: %s", username, action, details));
    }
    
    /**
     * Log security events
     */
    public static void logSecurityEvent(String event, String username, String details) {
        Logger logger = getLogger("SECURITY");
        logger.warning(String.format("Security Event: %s | User: %s | Details: %s", event, username, details));
    }
    
    /**
     * Log GPS tracking events
     */
    public static void logGPSEvent(String deviceId, String event, String details) {
        Logger logger = getLogger("GPS_TRACKING");
        logger.info(String.format("GPS Device: %s | Event: %s | Details: %s", deviceId, event, details));
    }
    
    /**
     * Log system performance metrics
     */
    public static void logPerformanceMetric(String metric, long value, String unit) {
        Logger logger = getLogger("PERFORMANCE");
        logger.fine(String.format("Performance Metric: %s = %d %s", metric, value, unit));
    }
    
    /**
     * Set logging level for the entire application
     */
    public static void setLogLevel(Level level) {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(level);
        
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            handler.setLevel(level);
        }
        
        Logger logger = getLogger(AppLogger.class);
        logger.info("Application log level changed to: " + level.getName());
    }
    
    /**
     * Enable debug logging
     */
    public static void enableDebugLogging() {
        setLogLevel(Level.FINE);
    }
    
    /**
     * Disable debug logging
     */
    public static void disableDebugLogging() {
        setLogLevel(Level.INFO);
    }
    
    /**
     * Custom formatter for consistent log formatting
     */
    private static class CustomFormatter extends Formatter {
        
        private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        
        @Override
        public String format(LogRecord record) {
            return String.format("[%s] [%s] [%s] %s%n",
                LocalDateTime.now().format(timeFormatter),
                record.getLevel().getName(),
                getSimpleClassName(record.getLoggerName()),
                record.getMessage()
            );
        }
        
        private String getSimpleClassName(String fullClassName) {
            if (fullClassName == null) return "Unknown";
            
            String[] parts = fullClassName.split("\\.");
            if (parts.length > 0) {
                return parts[parts.length - 1];
            }
            return fullClassName;
        }
    }
    
    /**
     * Utility method to log exceptions with stack trace
     */
    public static void logException(Class<?> clazz, String message, Throwable exception) {
        Logger logger = getLogger(clazz);
        logger.log(Level.SEVERE, message, exception);
    }
    
    /**
     * Utility method to log method entry (for debugging)
     */
    public static void logMethodEntry(Class<?> clazz, String methodName, Object... parameters) {
        Logger logger = getLogger(clazz);
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder();
            sb.append("ENTER ").append(methodName).append("(");
            if (parameters != null && parameters.length > 0) {
                for (int i = 0; i < parameters.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(parameters[i]);
                }
            }
            sb.append(")");
            logger.fine(sb.toString());
        }
    }
    
    /**
     * Utility method to log method exit (for debugging)
     */
    public static void logMethodExit(Class<?> clazz, String methodName, Object result) {
        Logger logger = getLogger(clazz);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("EXIT " + methodName + " -> " + result);
        }
    }
    
    /**
     * Log configuration changes
     */
    public static void logConfigChange(String component, String setting, String oldValue, String newValue) {
        Logger logger = getLogger("CONFIGURATION");
        logger.info(String.format("Config Change: %s.%s changed from '%s' to '%s'", 
            component, setting, oldValue, newValue));
    }
    
    /**
     * Get current log file path
     */
    public static String getCurrentLogFile() {
        return String.format("%s/%s_%s.log", 
            LOG_DIR, 
            LOG_FILE_PREFIX, 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        );
    }
    
    /**
     * Check if logging is initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }
}