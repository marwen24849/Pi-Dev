package esprit.tn.pidevrh.leave;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class PublicHolidayService {
    private static final String API_URL = "https://date.nager.at/Api/v3/PublicHolidays/";

    public static Set<String> getPublicHolidays(int year, String countryCode) {
        Set<String> holidays = new HashSet<>();
        HttpURLConnection connection = null;
        try {
            URL url = new URL(API_URL + year + "/" + countryCode);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("Erreur: API indisponible ou données non trouvées.");
            }

            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();

            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject holiday = jsonArray.get(i).getAsJsonObject();
                String date = holiday.get("date").getAsString();
                String name = holiday.get("localName").getAsString();
                holidays.add(date + " - " + name);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return holidays;
    }
}
