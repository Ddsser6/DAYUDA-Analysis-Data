package com.dayuda;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MenuUtamaController {

    @FXML
    private Button btnGetStarted;

    @FXML
    private void handleGetStarted() {
        ScaleTransition shrink = new ScaleTransition(Duration.millis(100), btnGetStarted);
        shrink.setToX(0.9);
        shrink.setToY(0.9);

        ScaleTransition grow = new ScaleTransition(Duration.millis(100), btnGetStarted);
        grow.setToX(1.0);
        grow.setToY(1.0);

        shrink.setOnFinished(event -> {
            grow.play();
            // Jalankan perpindahan halaman setelah animasi tombol selesai
            switchPageToPilihMode();
        });
        
        shrink.play();
    }

    private void switchPageToPilihMode() {
        try {
            // Memuat halaman PilihMode.fxml
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/PilihMode.fxml"));
            Stage stage = (Stage) btnGetStarted.getScene().getWindow();
            
            // Set halaman baru ke jendela yang sama tanpa ganti ukuran window
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}