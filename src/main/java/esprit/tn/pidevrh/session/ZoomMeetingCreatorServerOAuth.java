package esprit.tn.pidevrh.session;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;


public class ZoomMeetingCreatorServerOAuth {

    private static final Dotenv dotenv = Dotenv.load();
    // Get credentials from .env file
    private static final String CLIENT_ID = dotenv.get("ZOOM_CLIENT_ID");
    private static final String CLIENT_SECRET = dotenv.get("ZOOM_CLIENT_SECRET");
    private static final String ACCOUNT_ID = dotenv.get("ZOOM_ACCOUNT_ID");

    public static String createZoomMeeting() {
        try {
            String accessToken = getAccessToken();
            String meetingResponse = createMeeting(accessToken);
            JSONObject jsonResponse = new JSONObject(meetingResponse);
            return jsonResponse.getString("join_url"); // Return the meeting link
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Get an access token using the account_credentials grant type
    private static String getAccessToken() throws Exception {
        // Encode credentials (client_id:client_secret) in Base64
        String credentials = CLIENT_ID + ":" + CLIENT_SECRET;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        // Token URL for server-to-server OAuth
        String tokenUrl = "https://zoom.us/oauth/token?grant_type=account_credentials&account_id=" + ACCOUNT_ID;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Authorization", "Basic " + encodedCredentials)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject jsonResponse = new JSONObject(response.body());
        return jsonResponse.getString("access_token");
    }

    // Create a meeting using the access token
    private static String createMeeting(String accessToken) throws Exception {
        String url = "https://api.zoom.us/v2/users/me/meetings";

        // Build meeting details payload
        JSONObject payload = new JSONObject();
        payload.put("topic", "Server-to-Server OAuth Meeting");
        payload.put("type", 2); // Scheduled meeting
        payload.put("start_time", "2025-03-02T10:00:00Z");
        payload.put("duration", 60);
        payload.put("timezone", "UTC");
        payload.put("agenda", "Discuss server-to-server OAuth implementation");

        JSONObject settings = new JSONObject();
        settings.put("host_video", true);
        settings.put("participant_video", true);
        settings.put("mute_upon_entry", true);
        settings.put("waiting_room", true);
        payload.put("settings", settings);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}