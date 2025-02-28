package esprit.tn.pidevrh.chat;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import okhttp3.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class SQLProcessorAI {
    private static final String TOGETHER_API_URL = "https://api.together.xyz/v1/chat/completions";
    private static final String TOGETHER_API_KEY = "01301ddd79db215d207cd1e4f3a6c11ef9847f64f10dba8fb5e7aeae94106a38";

    private static final String LOG_FILE_PATH = "src/main/resources/query_log.sql";

    public static String generateSQLQuery(String question, Map<String, List<ColumnInfo>> schemaInfo) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode prompt = mapper.createObjectNode();
        ArrayNode messages = mapper.createArrayNode();

        StringBuilder schemaText = new StringBuilder();
        for (Map.Entry<String, List<ColumnInfo>> entry : schemaInfo.entrySet()) {
            schemaText.append("- Table `").append(entry.getKey()).append("`: ");
            for (ColumnInfo column : entry.getValue()) {
                schemaText.append(column.getName()).append(" (").append(column.getType()).append("), ");
            }
            schemaText.append("\n");
        }

        String promptText = String.format("""
                Base de données : SGRH
                
                Voici la structure des tables :
                %s
                
                Convertis cette question en requête SQL valide pour MySQL.
                Réponds uniquement avec la requête SQL, formatée entre ```sql et ```.
                
                Question : "%s"
                """, schemaText.toString(), question);

        messages.add(mapper.createObjectNode()
                .put("role", "system")
                .put("content", "Tu es un assistant expert en SQL MySQL 8.0.27 ."));
        messages.add(mapper.createObjectNode()
                .put("role", "user")
                .put("content", promptText));

        prompt.put("model", "meta-llama/Meta-Llama-3.1-8B-Instruct-Turbo-128K")
                .set("messages", messages);

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(mapper.writeValueAsString(prompt), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(TOGETHER_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + TOGETHER_API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JsonNode responseJson = mapper.readTree(response.body().string());
                String rawSqlResponse = responseJson.get("choices").get(0).get("message").get("content").asText();
                return extractSQLQuery(rawSqlResponse);
            } else {
                throw new IOException("Erreur API : " + response.body().string());
            }
        }
    }


    private static String extractSQLQuery(String response) {
        int start = response.indexOf("```sql") + 6;
        int end = response.indexOf("```", start);
        return (start >= 0 && end >= 0) ? response.substring(start, end).trim() : "Erreur : Impossible d'extraire la requête SQL.";
    }

    public static void executeSQL(String query) {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement stmt = connection.createStatement()) {

            if (query.trim().toUpperCase().startsWith("SELECT")) {
                ResultSet rs = stmt.executeQuery(query);
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                List<Map<String, Object>> results = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metaData.getColumnName(i), rs.getObject(i));
                    }
                    results.add(row);
                }

                if (results.isEmpty()) {
                    System.out.println("Aucun résultat trouvé.");
                } else {
                    System.out.println("Résultats :");
                    results.forEach(System.out::println);
                }
            } else {
                int affectedRows = stmt.executeUpdate(query);
                System.out.println("Opération réussie ! Lignes affectées : " + affectedRows);
            }

            logQuery(query);
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'exécution de la requête : " + e.getMessage());
        }
    }

    static Map<String, List<ColumnInfo>> getDatabaseSchema(Connection connection) throws SQLException {
        Map<String, List<ColumnInfo>> schemaInfo = new HashMap<>();
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            ResultSet columns = metaData.getColumns(null, null, tableName, null);

            List<ColumnInfo> columnList = new ArrayList<>();
            while (columns.next()) {
                columnList.add(new ColumnInfo(columns.getString("COLUMN_NAME"), columns.getString("TYPE_NAME")));
            }
            schemaInfo.put(tableName, columnList);
        }
        return schemaInfo;
    }

    private static void logQuery(String query) {
        try {
            File logFile = new File(LOG_FILE_PATH);
            if (!logFile.exists()) {
                Files.createDirectories(Paths.get("src/main/resources"));
                logFile.createNewFile();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                writer.write(timestamp + " \n " + query + "\n");
                writer.write(  "******************************************** " +
                        "********************************************\n");
            }
        } catch (IOException e) {
            System.out.println("Erreur lors de la sauvegarde de la requête : " + e.getMessage());
        }
    }

    public static List<Map<String, Object>> executeSQLWithResults(String query) {
        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             Statement stmt = connection.createStatement()) {

            if (query.trim().toUpperCase().startsWith("SELECT")) {
                ResultSet rs = stmt.executeQuery(query);
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metaData.getColumnName(i), rs.getObject(i));
                    }
                    results.add(row);
                }
            }
            logQuery(query);

        } catch (SQLException e) {
            System.out.println("Erreur SQL : " + e.getMessage());
        }

        return results;
    }
    public static int executeUpdateSQL(String query) {
        int affectedRows = 0;
        try (Connection connection = DatabaseConnection.getConnection()) {
            Statement stmt = connection.createStatement();
            affectedRows = stmt.executeUpdate(query);
            logQuery(query);
        } catch (SQLException e) {
            System.out.println("Erreur SQL : " + e.getMessage());
        }
        return affectedRows;
    }


    static class ColumnInfo {
        private final String name;
        private final String type;

        public ColumnInfo(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }

    public static void main(String[] args) {
        try (Connection connection = DatabaseConnection.getConnection();
             Scanner scanner = new Scanner(System.in)) {


            Map<String, List<ColumnInfo>> schemaInfo = getDatabaseSchema(connection);

            while (true) {
                System.out.print("Entrez votre question en langage naturel : ");
                String question = scanner.nextLine();

                String sqlQuery = SQLProcessorAI.generateSQLQuery(question, schemaInfo);
                System.out.println("Requête SQL générée : " + sqlQuery);

                SQLProcessorAI.executeSQL(sqlQuery);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

}
