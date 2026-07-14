package com.dayuda;

import javafx.animation.ScaleTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ModeStandarController {

    // 🌐 KONFIGURASI URL GLOBAL (Tinggal ganti di sini kalau backend Python berubah)
    private static final String BASE_URL = "http://127.0.0.1:5000";
    private static final String ENDPOINT_PROCESS = BASE_URL + "/process-dataset";

    @FXML private Button btnBack;
    @FXML private Button btnImport;
    @FXML private VBox panelUpload;
    @FXML private VBox panelData;
    @FXML private TableView<ObservableList<String>> tableJadwal;

    @FXML private Label lblTitle;
    @FXML private Label lblSubtitle;
    @FXML private Label lblTableStatus;

    @FXML
    private void handleBack() {
        playClickAnimation(btnBack, () -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/PilihMode.fxml"));
                Stage stage = (Stage) btnBack.getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleImport() {
        playClickAnimation(btnImport, () -> {
            Stage stage = (Stage) btnImport.getScene().getWindow();
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Import Dataset (CSV / Excel)");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Dataset Files (*.csv, *.xlsx)", "*.csv", "*.xlsx")
            );
            
            File selectedFile = fileChooser.showOpenDialog(stage);
            
            if (selectedFile != null) {
                String namaFile = selectedFile.getName();
                String namaTanpaEkstensi = namaFile.substring(0, namaFile.lastIndexOf("."));
                
                lblTitle.setText("📊 " + namaTanpaEkstensi.toUpperCase() + " DASHBOARD");
                lblSubtitle.setText("Mengirim data ke server Python untuk dianalisis...");
                
                // Jalankan pengiriman file ke Python
                kirimFileKePython(selectedFile, namaTanpaEkstensi);
            }
        });
    }

    private void kirimFileKePython(File file, String namaTanpaEkstensi) {
        String boundary = "===BoundaryDataJavaFX===";
        String LINE_FEED = "\r\n";

        try {
            // Menggunakan variabel global ENDPOINT_PROCESS yang ada di atas
            URL url = new URL(ENDPOINT_PROCESS);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            OutputStream outputStream = conn.getOutputStream();
            
            // Tulis Form Data Header untuk File
            outputStream.write(("--" + boundary + LINE_FEED).getBytes());
            outputStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"" + LINE_FEED).getBytes());
            outputStream.write(("Content-Type: application/octet-stream" + LINE_FEED + LINE_FEED).getBytes());

            // Kirim isi data byte file
            FileInputStream inputStream = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.write(LINE_FEED.getBytes());
            outputStream.write(("--" + boundary + "--" + LINE_FEED).getBytes());
            
            inputStream.close();
            outputStream.flush();
            outputStream.close();

            // Baca Respon balik dari Python
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder jsonResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonResponse.append(line);
                }
                reader.close();

                // Proses data JSON dan pasang ke tabel secara dinamis
                parseDanTampilkanJson(jsonResponse.toString());

                // Ganti tampilan layar ke tabel data
                lblSubtitle.setText("Analisis data selesai diproses oleh AI.");
                lblTableStatus.setText("📋 Hasil Olah AI: " + namaTanpaEkstensi);
                panelUpload.setVisible(false);
                panelData.setVisible(true);
            } else {
                lblSubtitle.setText("Gagal terhubung ke Python. Pastikan server backend aktif!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            lblSubtitle.setText("Error Koneksi: Server Python luring / offline.");
        }
    }

    private void parseDanTampilkanJson(String jsonStr) {
        tableJadwal.getColumns().clear();
        ObservableList<ObservableList<String>> dataBarisTabel = FXCollections.observableArrayList();

        try {
            String cleanJson = jsonStr.trim();
            
            // Potong & ambil bagian kolom ("columns":[...])
            String colPart = cleanJson.substring(cleanJson.indexOf("[\"") + 1, cleanJson.indexOf("\"]") + 1);
            String[] kolomHeaders = colPart.split("\",\"");

            // 1. Generate Header Kolom Otomatis di TableView
            for (int i = 0; i < kolomHeaders.length; i++) {
                final int indexKolom = i;
                String namaKolom = kolomHeaders[i].replace("\"", "").replace("[", "").replace("]", "");
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(namaKolom);
                col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(indexKolom)));
                tableJadwal.getColumns().add(col);
            }

            // 2. Potong & ambil bagian data ("data":[[...]])
            String dataPart = cleanJson.substring(cleanJson.indexOf("[[") + 2, cleanJson.lastIndexOf("]]"));
            String[] barisMentah = dataPart.split("\\],\\[");

            for (String baris : barisMentah) {
                ObservableList<String> barisData = FXCollections.observableArrayList();
                String[] cellMentah = baris.split("\",\"");
                for (String cell : cellMentah) {
                    barisData.add(cell.replace("\"", "").replace("[", "").replace("]", ""));
                }
                dataBarisTabel.add(barisData);
            }

            tableJadwal.setItems(dataBarisTabel);

        } catch (Exception e) {
            System.out.println("Gagal parsing JSON data.");
            e.printStackTrace();
        }
    }

    private void playClickAnimation(Button button, Runnable onFinishedAction) {
        ScaleTransition shrink = new ScaleTransition(Duration.millis(80), button);
        shrink.setToX(0.95); shrink.setToY(0.95);
        ScaleTransition grow = new ScaleTransition(Duration.millis(80), button);
        grow.setToX(1.0); grow.setToY(1.0);
        shrink.setOnFinished(e -> {
            grow.play();
            grow.setOnFinished(ev -> onFinishedAction.run());
        });
        shrink.play();
    }
}