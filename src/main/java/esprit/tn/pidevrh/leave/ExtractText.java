package esprit.tn.pidevrh.leave;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import org.json.JSONObject;

public class ExtractText {
    private static final String API_URL = "https://image-to-text30.p.rapidapi.com/api/rapidapi/image-to-text";
    private static final String API_KEY = "7a912502cfmsh56f1c1d478c69bep169184jsn3b1cbcf569b1";

    public static CompletableFuture<String> extractTextFromImage(File imageFile) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Open a connection
                HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("X-RapidAPI-Key", API_KEY);
                connection.setRequestProperty("X-RapidAPI-Host", "image-to-text30.p.rapidapi.com");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=---Boundary123");
                connection.setDoOutput(true);

                // Prepare the request body
                try (OutputStream outputStream = connection.getOutputStream();
                     PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true)) {

                    writer.append("-----Boundary123\r\n");
                    writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"" + imageFile.getName() + "\"\r\n");
                    writer.append("Content-Type: " + Files.probeContentType(imageFile.toPath()) + "\r\n\r\n");
                    writer.flush();

                    // Write image data
                    Files.copy(imageFile.toPath(), outputStream);
                    outputStream.flush();

                    writer.append("\r\n-----Boundary123--\r\n");
                    writer.flush();
                }

                // Get response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse.optString("text", "Aucun texte détecté !");
            } catch (Exception e) {
                return "Erreur API: " + e.getMessage();
            }
        });
    }
}
