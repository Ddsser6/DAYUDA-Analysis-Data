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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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

public class ModeAdvancedController {

    // 🌐 ENDPOINT PYTHON UNTUK MODE AI (Bisa diganti di sini jika port/URL berbeda)
    private static final String BASE_URL = "http://127.0.0.1:5000";
    private static final String ENDPOINT_PROCESS = BASE_URL + "/process-dataset";
    private static final String ENDPOINT_CHAT = BASE_URL + "/ask-ai";

    @FXML private Button btnBack;
    @FXML private Button btnImport;
    @FXML private Button btnSend;
    @FXML private VBox panelUpload;
    @FXML private VBox panelData;
    @FXML private TableView<ObservableList<String>> tableJadwal;

    @FXML private Label lblTitle;
    @FXML private Label lblSubtitle;
    @FXML private Label lblTableStatus;

    // Komponen Obrolan Chatbot / Prompt AI
    @FXML private TextArea txtChatArea;
    @FXML private TextField txtChatInput;

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
            fileChooser.setTitle("Import AI Dataset (CSV / Excel)");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Dataset Files (*.csv, *.xlsx)", "*.csv", "*.xlsx")
            );
            
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                String namaFile = selectedFile.getName();
                String namaTanpaEkstensi = namaFile.substring(0, namaFile.lastIndexOf("."));
                
                lblTitle.setText("🧠 " + namaTanpaEkstensi.toUpperCase() + " AI MATRIX");
                lblSubtitle.setText("Mengirim data ke otak kecerdasan buatan...");
                
                kirimFileKePython(selectedFile, namaTanpaEkstensi);
            }
        });
    }

    @FXML
    private void handleSendMessage() {
        String userMessage = txtChatInput.getText().trim();
        if (userMessage.isEmpty()) return;

        // 1. Tampilkan chat user ke kotak log chat area
        txtChatArea.appendText("[Kamu]: " + userMessage + "\n");
        txtChatInput.clear(); // Bersihkan kotak input setelah kirim

        // 2. Kirim prompt ke Python lewat Thread terpisah (biar aplikasi gak lag/nge-freeze)
        new Thread(() -> {
            try {
                URL url = new URL(ENDPOINT_CHAT);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                // Format JSON sederhana: {"prompt": "pertanyaan user"}
                String jsonInputString = "{\"prompt\": \"" + userMessage + "\"}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Baca balasan dari Python
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    br.close();

                    // Ambil teks jawaban AI dari JSON Python (Format target: {"response": "jawaban ai"})
                    String jsonResponse = response.toString();
                    String aiAnswer = jsonResponse.substring(jsonResponse.indexOf(":\"") + 2, jsonResponse.lastIndexOf("\""));

                    // Tampilkan jawaban AI ke layar GUI
                    javafx.application.Platform.runLater(() -> {
                        txtChatArea.appendText("[AI]: " + aiAnswer + "\n\n");
                    });
                } else {
                    javafx.application.Platform.runLater(() -> {
                        txtChatArea.appendText("[System]: AI gagal merespon pesan.\n\n");
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    txtChatArea.appendText("[System]: Koneksi ke otak AI terputus. Pastikan Python aktif!\n\n");
                });
            }
        }).start();
    }

    private void kirimFileKePython(File file, String namaTanpaEkstensi) {
        String boundary = "===BoundaryDataJavaFX===";
        String LINE_FEED = "\r\n";

        try {
            URL url = new URL(ENDPOINT_PROCESS);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(("--" + boundary + LINE_FEED).getBytes());
            outputStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"" + LINE_FEED).getBytes());
            outputStream.write(("Content-Type: application/octet-stream" + LINE_FEED + LINE_FEED).getBytes());

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

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder jsonResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonResponse.append(line);
                }
                reader.close();

                parseDanTampilkanJson(jsonResponse.toString());

                lblSubtitle.setText("Deep AI Engine aktif. Tanyakan apa saja mengenai dataset ini.");
                lblTableStatus.setText("📋 Dataset Pratinjau: " + namaTanpaEkstensi);
                
                // Set sapaan awal AI di Chatbox
                txtChatArea.setText("[AI]: Sistem berhasil membaca data " + file.getName() + ".\nSilakan ketik pertanyaan di bawah untuk mulai menganalisis!\n\n");
                
                panelUpload.setVisible(false);
                panelData.setVisible(true);
            } else {
                lblSubtitle.setText("Gagal terhubung ke AI Engine.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblSubtitle.setText("Error Koneksi: Server AI Python luring.");
        }
    }

    private void parseDanTampilkanJson(String jsonStr) {
        tableJadwal.getColumns().clear();
        ObservableList<ObservableList<String>> dataBarisTabel = FXCollections.observableArrayList();

        try {
            String cleanJson = jsonStr.trim();
            String colPart = cleanJson.substring(cleanJson.indexOf("[\"") + 1, cleanJson.indexOf("\"]") + 1);
            String[] kolomHeaders = colPart.split("\",\"");

            for (int i = 0; i < kolomHeaders.length; i++) {
                final int indexKolom = i;
                String namaKolom = kolomHeaders[i].replace("\"", "").replace("[", "").replace("]", "");
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(namaKolom);
                col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(indexKolom)));
                tableJadwal.getColumns().add(col);
            }

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