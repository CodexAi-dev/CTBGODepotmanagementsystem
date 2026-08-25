package lk.bustracking.depotmanagementsystem.utils;

/**
 * UI Constants - Centralized styling and configuration
 */
public class UIConstants {
    
    // Color Scheme
    public static final String CTB_PRIMARY = "#1a237e";
    public static final String CTB_LIGHT = "#534bae";
    public static final String CTB_ACCENT = "#ff6d00";
    public static final String CTB_DARK = "#000051";
    public static final String CTB_SUCCESS = "#2e7d32";
    public static final String CTB_ERROR = "#c62828";
    
    // Backgrounds
    public static final String GRADIENT_BACKGROUND = 
        "-fx-background-color: linear-gradient(to bottom right, #1a237e, #534bae, #3949ab);";
    
    public static final String LOGIN_CARD_STYLE = 
        "-fx-background-color: rgba(255, 255, 255, 0.95);" +
        "-fx-background-radius: 20;" +
        "-fx-border-radius: 20;" +
        "-fx-border-color: rgba(255, 255, 255, 0.3);" +
        "-fx-border-width: 1;" +
        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 25, 0.3, 0, 5);" +
        "-fx-padding: 40 35 35 35;";
    
    // Text Styles
    public static final String MAIN_HEADING_STYLE = 
        "-fx-text-fill: white;" +
        "-fx-font-size: 42px;" +
        "-fx-font-weight: bold;" +
        "-fx-font-family: 'Segoe UI', Arial, sans-serif;";
    
    public static final String SUB_HEADING_STYLE = 
        "-fx-text-fill: rgba(255, 255, 255, 0.9);" +
        "-fx-font-size: 18px;" +
        "-fx-font-weight: 600;" +
        "-fx-font-family: 'Segoe UI', Arial, sans-serif;";
    
    public static final String FEATURE_LABEL_STYLE = 
        "-fx-text-fill: rgba(255, 255, 255, 0.85);" +
        "-fx-font-size: 14px;" +
        "-fx-font-weight: 500;" +
        "-fx-font-family: 'Segoe UI', Arial, sans-serif;";
    
    public static final String TIME_LABEL_STYLE = 
        "-fx-text-fill: rgba(255, 255, 255, 0.8);" +
        "-fx-font-size: 13px;" +
        "-fx-font-weight: 500;" +
        "-fx-font-family: 'Segoe UI', Arial, sans-serif;" +
        "-fx-alignment: center;" +
        "-fx-text-alignment: center;";
    
    public static final String WELCOME_HEADING_STYLE = 
        "-fx-text-fill: #1a237e;" +
        "-fx-font-size: 28px;" +
        "-fx-font-weight: bold;" +
        "-fx-font-family: 'Segoe UI', Arial, sans-serif;";
    
    public static final String SUBTITLE_STYLE = 
        "-fx-text-fill: #666;" +
        "-fx-font-size: 14px;" +
        "-fx-font-weight: 500;" +
        "-fx-font-family: 'Segoe UI', Arial, sans-serif;";
    
    // Input Field Styles
    public static final String INPUT_LABEL_STYLE = 
        "-fx-text-fill: #444;" +
        "-fx-font-size: 13px;" +
        "-fx-font-weight: 600;" +
        "-fx-font-family: 'Segoe UI', Arial, sans-serif;";
    
    public static final String INPUT_FIELD_CONTAINER_NORMAL = 
        "-fx-background-color: #f8f9fa;" +
        "-fx-background-radius: 10;" +
        "-fx-border-radius: 10;" +
        "-fx-border-color: #e0e0e0;" +
        "-fx-border-width: 2;" +
        "-fx-padding: 12 15;";
    
    public static final String INPUT_FIELD_CONTAINER_FOCUSED = 
        "-fx-background-color: #ffffff;" +
        "-fx-background-radius: 10;" +
        "-fx-border-radius: 10;" +
        "-fx-border-color: #534bae;" +
        "-fx-border-width: 2;" +
        "-fx-padding: 12 15;";
    
    public static final String INPUT_ICON_STYLE = 
        "-fx-font-size: 16px;" +
        "-fx-text-fill: #666;" +
        "-fx-padding: 0 5 0 0;";
    
    public static final String INPUT_FIELD_STYLE = 
        "-fx-background-color: transparent;" +
        "-fx-border-width: 0;" +
        "-fx-text-fill: #333;" +
        "-fx-font-size: 14px;" +
        "-fx-font-weight: 500;" +
        "-fx-font-family: 'Segoe UI', Arial, sans-serif;" +
        "-fx-prompt-text-fill: #999;";
    
    // Button Styles
    public static final String LOGIN_BUTTON_STYLE = 
        "-fx-background-color: linear-gradient(to right, #ff6d00, #ff9100);" +
        "-fx-background-radius: 12;" +
        "-fx-text-fill: white;" +
        "-fx-font-size: 15px;" +
        "-fx-font-weight: bold;" +
        "-fx-font-family: 'Segoe UI', Arial, sans-serif;" +
        "-fx-cursor: hand;" +
        "-fx-effect: dropshadow(gaussian, rgba(255, 109, 0, 0.4), 15, 0.3, 0, 3);";
    
    public static final String QUICK_LOGIN_BUTTON_STYLE = 
        "-fx-background-color: transparent;" +
        "-fx-border-color: #ddd;" +
        "-fx-border-radius: 8;" +
        "-fx-border-width: 1.5;" +
        "-fx-text-fill: #666;" +
        "-fx-font-size: 11px;" +
        "-fx-font-weight: 600;" +
        "-fx-font-family: 'Segoe UI', Arial, sans-serif;" +
        "-fx-cursor: hand;" +
        "-fx-padding: 6 10;";
    
    // Status Styles
    public static final String ERROR_LABEL_STYLE = 
        "-fx-text-fill: #c62828;" +
        "-fx-font-size: 12px;" +
        "-fx-font-weight: 600;" +
        "-fx-font-family: 'Segoe UI', Arial, sans-serif;" +
        "-fx-background-color: #ffebee;" +
        "-fx-background-radius: 6;" +
        "-fx-padding: 8 12;" +
        "-fx-alignment: center;" +
        "-fx-max-width: 300;";
    
    public static final String STATUS_LABEL_STYLE = 
        "-fx-text-fill: #666;" +
        "-fx-font-size: 12px;" +
        "-fx-font-weight: 500;" +
        "-fx-font-family: 'Segoe UI', Arial, sans-serif;";
    
    public static final String STATUS_LABEL_AUTHENTICATING = 
        "-fx-text-fill: #ff6d00;" +
        "-fx-font-size: 12px;" +
        "-fx-font-weight: 600;" +
        "-fx-font-family: 'Segoe UI', Arial, sans-serif;";
    
    public static final String STATUS_LABEL_SUCCESS = 
        "-fx-text-fill: #2e7d32;" +
        "-fx-font-size: 12px;" +
        "-fx-font-weight: 600;" +
        "-fx-font-family: 'Segoe UI', Arial, sans-serif;";
    
    public static final String LOADING_INDICATOR_STYLE = 
        "-fx-progress-color: #534bae;";
    
    // Footer Styles
    public static final String COPYRIGHT_STYLE = 
        "-fx-text-fill: #999;" +
        "-fx-font-size: 11px;" +
        "-fx-font-weight: 500;" +
        "-fx-font-family: 'Segoe UI', Arial, sans-serif;";
    
    public static final String VERSION_STYLE = 
        "-fx-text-fill: #bbb;" +
        "-fx-font-size: 10px;" +
        "-fx-font-weight: 400;" +
        "-fx-font-family: 'Segoe UI', Arial, sans-serif;";
}