package com.dayuda;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PilihModeController {

    @FXML private VBox cardLightMode;
    @FXML private VBox cardDeepMode;
    @FXML private Circle aiStatusDot;
    @FXML private Label lblAiStatus;

    @FXML
    public void initialize() {
        setAiStatus(true); // Default aktif
    }

    public void setAiStatus(boolean isActive) {
        if (isActive) {
            aiStatusDot.setFill(Color.web("#10B981"));
            lblAiStatus.setText("AI ACTIVE");
            lblAiStatus.setStyle("-fx-text-fill: #10B981; -fx-font-size: 11px; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-letter-spacing: 1.5px;");
            aiStatusDot.setStyle("-fx-effect: dropshadow(three-pass-box, #10B981, 8, 0, 0, 0);");
        } else {
            aiStatusDot.setFill(Color.web("#EF4444"));
            lblAiStatus.setText("AI OFFLINE");
            lblAiStatus.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 11px; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-letter-spacing: 1.5px;");
            aiStatusDot.setStyle("-fx-effect: dropshadow(three-pass-box, #EF4444, 8, 0, 0, 0);");
        }
    }

    @FXML
    private void handleLightMode() {
        playClickAnimation(cardLightMode);
        
        ScaleTransition delay = new ScaleTransition(Duration.millis(200), cardLightMode);
        delay.setOnFinished(event -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/ModeStandar.fxml"));
                Stage stage = (Stage) cardLightMode.getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        delay.play();
    }

    @FXML
    private void handleDeepMode() {
        playClickAnimation(cardDeepMode);
    
        // Tambahkan efek delay visual agar animasi klik selesai dulu
        ScaleTransition delay = new ScaleTransition(Duration.millis(200), cardDeepMode);
        delay.setOnFinished(event -> {
            try {
                // 🚀 Arahkan ke file FXML baru kita ModeAdvanced.fxml
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/ModeAdvanced.fxml"));
                Stage stage = (Stage) cardDeepMode.getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        delay.play();
    }

    // Fungsi universal menggunakan Node
    private void playClickAnimation(javafx.scene.Node component) {
        ScaleTransition shrink = new ScaleTransition(Duration.millis(100), component);
        shrink.setToX(0.95);
        shrink.setToY(0.95);

        ScaleTransition grow = new ScaleTransition(Duration.millis(100), component);
        grow.setToX(1.0);
        grow.setToY(1.0);

        shrink.setOnFinished(event -> grow.play());
        shrink.play();
    }
}