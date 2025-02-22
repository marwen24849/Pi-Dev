package esprit.tn.pidevrh.Reclamation;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import esprit.tn.pidevrh.email.EmailService;
import esprit.tn.pidevrh.login.SessionManager;
import esprit.tn.pidevrh.login.User;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;

import esprit.tn.pidevrh.connection.EnvLoader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

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

            CompletableFuture.runAsync(() -> {
                String aiResponse = generateAIEmail(text, loggedInUser.getFirstName());

                if (aiResponse != null) {
                    EmailService.sendEmail(loggedInUser.getEmail(), "Confirmation de Réclamation", aiResponse);
                }
            }).exceptionally(ex -> {
                System.out.println("Error occurred: " + ex.getMessage());
                return null;
            });
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

    private String generateAIEmail(String userReclamation , String Firstname) {
        final String FALLBACK = "Nous avons bien reçu votre réclamation et nous la traiterons dans les plus brefs délais.";


        try {
            String apiKey = EnvLoader.get("HUGGINGFACE_API_KEY");
            String model = "mistralai/Mistral-7B-Instruct-v0.3";


            String prompt = "Vous êtes un assistant dans une application de gestion des ressources humaines d'une entreprise. Voici une réclamation d'un employé : \n"
                    + userReclamation + "son nom est "+Firstname+" \n"
                    + "Rédigez un email professionnel en français de confirmation pour l'employé en le remerciant pour sa soumission. "
                    + "Si possible, proposez des solutions temporaires que l'employé peut essayer "
                    + "Mentionnez que l'équipe de support examinera la réclamation et fera de son mieux pour fournir une solution dans les plus brefs délais. "
                    + "L'email doit inclure des étapes concrètes que l'employé peut suivre pour essayer de résoudre le problème en attendant le support si il ya des solutions a proposer. "
                    + "Veillez à ce que l'email soit clair et courtois.\n"
                    + "Ne me donnez pas cette instruction dans la réponse.";


            System.out.println("[DEBUG] SIMPLE PROMPT: " + prompt);

            HttpURLConnection conn = (HttpURLConnection) new URL("https://api-inference.huggingface.co/models/" + model).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject request = new JSONObject()
                    .put("inputs", prompt)
                    .put("parameters", new JSONObject()
                            .put("max_new_tokens", 550)
                            .put("temperature", 0.5)
                            .put("top_p", 0.9)
                            .put("return_full_text", false)
                    );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(request.toString().getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            String rawResponse = new BufferedReader(new InputStreamReader(
                    responseCode == 200 ? conn.getInputStream() : conn.getErrorStream()
            )).lines().collect(Collectors.joining("\n"));


            if (responseCode == 200) {
                JSONArray jsonResponse = new JSONArray(rawResponse);
                String generatedText = jsonResponse.getJSONObject(0).getString("generated_text");

                System.out.println("[DEBUG] GENERATED TEXT:\n" + generatedText);


                return generatedText;
            } else {
                System.out.println("[DEBUG] ERROR: HTTP Response Code " + responseCode);
            }

        } catch (Exception e) {
            System.out.println("[DEBUG] ERROR: " + e.getMessage());
        }

        return FALLBACK;
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
