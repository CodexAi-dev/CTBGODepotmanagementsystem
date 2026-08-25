package lk.bustracking.depotmanagementsystem.utils;

import javafx.animation.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * Animation utilities for enhanced UI experience
 */
public class AnimationUtils {

    public static Pane createFloatingCircles() {
        Pane floatingPane = new Pane();
        floatingPane.setMouseTransparent(true);
        
        // Create multiple floating circles
        Circle[] circles = {
            createFloatingCircle(100, 150, 40, Color.web(UIConstants.CTB_LIGHT, 0.1)),
            createFloatingCircle(300, 400, 60, Color.web(UIConstants.CTB_ACCENT, 0.08)),
            createFloatingCircle(700, 200, 30, Color.web(UIConstants.CTB_LIGHT, 0.15)),
            createFloatingCircle(900, 500, 50, Color.web(UIConstants.CTB_ACCENT, 0.05)),
            createFloatingCircle(1100, 300, 35, Color.web(UIConstants.CTB_LIGHT, 0.12))
        };
        
        for (Circle circle : circles) {
            floatingPane.getChildren().add(circle);
            startFloatingAnimation(circle);
        }
        
        return floatingPane;
    }
    
    private static Circle createFloatingCircle(double x, double y, double radius, Color color) {
        Circle circle = new Circle(radius, color);
        circle.setCenterX(x);
        circle.setCenterY(y);
        return circle;
    }
    
    private static void startFloatingAnimation(Circle circle) {
        double originalY = circle.getCenterY();
        
        TranslateTransition floatTransition = new TranslateTransition(Duration.seconds(8 + Math.random() * 4), circle);
        floatTransition.setFromY(0);
        floatTransition.setToY(-30 - Math.random() * 40);
        floatTransition.setAutoReverse(true);
        floatTransition.setCycleCount(TranslateTransition.INDEFINITE);
        
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(5 + Math.random() * 3), circle);
        fadeTransition.setFromValue(0.3);
        fadeTransition.setToValue(0.8);
        fadeTransition.setAutoReverse(true);
        fadeTransition.setCycleCount(FadeTransition.INDEFINITE);
        
        ParallelTransition parallelTransition = new ParallelTransition(floatTransition, fadeTransition);
        parallelTransition.play();
    }
    
    public static void setupButtonHoverEffects(Button button) {
        button.setOnMouseEntered(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), button);
            scaleUp.setToX(1.05);
            scaleUp.setToY(1.05);
            scaleUp.play();
            
            button.setStyle(UIConstants.LOGIN_BUTTON_STYLE + 
                "-fx-effect: dropshadow(gaussian, rgba(255, 109, 0, 0.6), 20, 0.4, 0, 5);");
        });
        
        button.setOnMouseExited(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), button);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();
            
            button.setStyle(UIConstants.LOGIN_BUTTON_STYLE);
        });
    }
    
    public static void playEntranceAnimations(VBox brandingSection, VBox loginCard, TextField usernameField) {
        // Branding section animation
        FadeTransition brandFade = new FadeTransition(Duration.millis(800), brandingSection);
        brandFade.setFromValue(0);
        brandFade.setToValue(1);
        
        TranslateTransition brandSlide = new TranslateTransition(Duration.millis(800), brandingSection);
        brandSlide.setFromX(-50);
        brandSlide.setToX(0);
        
        ParallelTransition brandAnimation = new ParallelTransition(brandFade, brandSlide);
        brandAnimation.play();
        
        // Login card animation (delayed)
        PauseTransition delay = new PauseTransition(Duration.millis(300));
        delay.setOnFinished(e -> {
            FadeTransition loginFade = new FadeTransition(Duration.millis(700), loginCard);
            loginFade.setFromValue(0);
            loginFade.setToValue(1);
            
            TranslateTransition loginSlide = new TranslateTransition(Duration.millis(700), loginCard);
            loginSlide.setFromX(50);
            loginSlide.setToX(0);
            
            ParallelTransition loginAnimation = new ParallelTransition(loginFade, loginSlide);
            loginAnimation.setOnFinished(event -> usernameField.requestFocus());
            loginAnimation.play();
        });
        delay.play();
    }
    
    public static void addPulseEffect(Button button) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(1000), button);
        pulse.setFromX(1);
        pulse.setFromY(1);
        pulse.setToX(1.02);
        pulse.setToY(1.02);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(ScaleTransition.INDEFINITE);
        pulse.play();
        
        button.setUserData(pulse); // Store animation for later removal
    }
    
    public static void removePulseEffect(Button button) {
        Animation pulse = (Animation) button.getUserData();
        if (pulse != null) {
            pulse.stop();
        }
        button.setScaleX(1.0);
        button.setScaleY(1.0);
    }
    
    public static void addSuccessAnimation(Label statusLabel) {
        FadeTransition fade = new FadeTransition(Duration.millis(300), statusLabel);
        fade.setFromValue(0.5);
        fade.setToValue(1);
        fade.setAutoReverse(true);
        fade.setCycleCount(4);
        fade.play();
    }
    
    public static void addShakeAnimation(Label errorLabel) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), errorLabel);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }
}