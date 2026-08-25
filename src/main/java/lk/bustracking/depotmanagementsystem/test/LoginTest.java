package lk.bustracking.depotmanagementsystem.test;

import lk.bustracking.depotmanagementsystem.dao.UserDAO;
import lk.bustracking.depotmanagementsystem.models.User;
import java.security.MessageDigest;

/**
 * Test login functionality with your existing database
 */
public class LoginTest {
    
    public static void main(String[] args) {
        System.out.println("🧪 Login Test Utility");
        System.out.println("=" + "=".repeat(30));
        
        UserDAO userDAO = new UserDAO();
        
        // Test database connection
        System.out.println("Testing database connection...");
        if (userDAO.testConnection()) {
            System.out.println("✅ Database connection successful");
        } else {
            System.out.println("❌ Database connection failed");
            return;
        }
        
        // Test credentials from your database
        System.out.println("\nTesting login credentials:");
        
        // Test admin user (based on your database data)
        testLogin(userDAO, "admin", "admin");
        testLogin(userDAO, "admin", "admin123");
        testLogin(userDAO, "admin", "password");
        
        // Test staff user
        testLogin(userDAO, "staff1", "staff");
        testLogin(userDAO, "staff1", "staff123");
        testLogin(userDAO, "staff1", "password");
        
        // Show password hashes for debugging
        System.out.println("\n🔍 Password Hash Analysis:");
        showPasswordHash("admin");
        showPasswordHash("admin123");
        showPasswordHash("staff");
        showPasswordHash("password");
        
        System.out.println("\n💡 Your database contains:");
        System.out.println("   User ID 1: admin with hash 0x240BE5...");
        System.out.println("   User ID 2: staff1 with hash 0x10176E...");
    }
    
    private static void testLogin(UserDAO userDAO, String username, String password) {
        System.out.print("Testing " + username + "/" + password + "... ");
        
        try {
            User user = userDAO.validateLogin(username, password);
            if (user != null) {
                System.out.println("✅ SUCCESS (ID: " + user.getUserId() + ", Role: " + user.getRole() + ")");
            } else {
                System.out.println("❌ FAILED");
            }
        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
        }
    }
    
    private static void showPasswordHash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder("0x");
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex.toUpperCase());
            }
            
            System.out.println("'" + password + "' -> " + hexString.toString().substring(0, 10) + "...");
            
        } catch (Exception e) {
            System.out.println("Error hashing " + password + ": " + e.getMessage());
        }
    }
}