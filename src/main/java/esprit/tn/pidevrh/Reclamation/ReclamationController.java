package esprit.tn.pidevrh.Reclamation;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.login.SessionManager;
import esprit.tn.pidevrh.login.User;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReclamationController {

    @FXML
    private TextArea reclamationTextArea;

    private final User loggedInUser = SessionManager.getInstance().getUser();

    @FXML
    private void handleCorrection() {
        String text = reclamationTextArea.getText();
        if (text.isEmpty()) {
            showAlert("Erreur", "Veuillez écrire une réclamation avant de corriger.");
            return;
        }

        String correctedText = correctGrammar(text);
        if (correctedText != null) {
            reclamationTextArea.setText(correctedText);
            showAlert("Correction", "Texte corrigé avec succès !");
        } else {
            showAlert("Erreur", "Impossible de corriger la grammaire. Vérifiez votre connexion internet.");
        }
    }

    @FXML
    private void handleSubmit() {
        if (loggedInUser == null) {
            showAlert("Erreur", "Utilisateur non connecté.");
            return;
        }

        String text = reclamationTextArea.getText();
        if (text.isEmpty()) {
            showAlert("Erreur", "Veuillez écrire une réclamation avant d'envoyer.");
            return;
        }

        if (saveReclamation(loggedInUser.getId(), text)) {
            showAlert("Succès", "Réclamation envoyée avec succès !");
            reclamationTextArea.clear();
        } else {
            showAlert("Erreur", "Échec de l'envoi de la réclamation.");
        }
    }

    private String correctGrammar(String text) {
        try {
            URL url = new URL("https://api.languagetool.org/v2/check");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String data = "text=" + encodedText + "&language=fr";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(data.getBytes());
                os.flush();
            }


            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("API request failed. Response Code: " + responseCode);
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray matches = jsonResponse.getJSONArray("matches");

            return applyCorrections(text, matches);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String applyCorrections(String text, JSONArray matches) {
        StringBuilder correctedText = new StringBuilder(text);
        int offsetCorrection = 0;

        for (int i = 0; i < matches.length(); i++) {
            JSONObject match = matches.getJSONObject(i);
            JSONArray replacements = match.getJSONArray("replacements");

            if (replacements.length() > 0) {
                String replacement = replacements.getJSONObject(0).getString("value");
                int offset = match.getInt("offset") + offsetCorrection;
                int length = match.getInt("length");


                correctedText.replace(offset, offset + length, replacement);


                offsetCorrection += replacement.length() - length;
            }
        }

        return correctedText.toString();
    }

    private boolean saveReclamation(long userId, String text) {
        String query = "INSERT INTO reclamation (user_id, sujet, description, statut, date_creation) VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setLong(1, userId);
            pstmt.setString(2, "Réclamation");
            pstmt.setString(3, text);
            pstmt.setString(4, "En attente");
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
