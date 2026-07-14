package com.dayuda;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MenuUtama.fxml"));
        primaryStage.setTitle("DAYUDA - Data Analytics App");
        primaryStage.setScene(new Scene(root, 800, 600));
        
        // ✨ PERBAIKAN: Ditambahkan tanda / di depan nama file
        primaryStage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/IconDYD.jpg")));
        
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}