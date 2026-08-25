package lk.bustracking.depotmanagementsystem.utils;

import java.util.regex.Pattern;

/**
 * Validation utilities for input validation
 */
public class ValidationUtils {
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^.{4,50}$");
    
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }
    
    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    
    // Additional validation methods
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
        return emailPattern.matcher(email.trim()).matches();
    }
    
    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        Pattern phonePattern = Pattern.compile("^[0-9]{10}$");
        return phonePattern.matcher(phone.trim().replaceAll("\\s+", "")).matches();
    }
    
    public static String validateLoginInput(String username, String password) {
        if (!isValidUsername(username)) {
            return "Username must be 3-20 characters (letters, numbers, underscore only)";
        }
        
        if (!isValidPassword(password)) {
            return "Password must be 4-50 characters long";
        }
        
        return null; // No errors
    }
}