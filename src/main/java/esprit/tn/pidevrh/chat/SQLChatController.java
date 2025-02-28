package esprit.tn.pidevrh.chat;

import esprit.tn.pidevrh.connection.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.geometry.Pos;
import javafx.application.Platform;

import java.io.IOException;
import java.sql.Connection;
import java.util.*;

public class SQLChatController {
    @FXML
    private VBox chatBox;
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;


    @FXML
    private void initialize() {
        sendButton.setOnAction(event -> processSQLQuery());
        messageField.setOnAction(event -> processSQLQuery());
    }

    private void processSQLQuery() {
        String userQuestion = messageField.getText().trim();
        if (!userQuestion.isEmpty()) {
            displayMessage(userQuestion, "user");
            messageField.clear();
            new Thread(() -> {
                try (Connection connection = DatabaseConnection.getConnection()) {
                    Map<String, List<SQLProcessorAI.ColumnInfo>> schemaInfo = SQLProcessorAI.getDatabaseSchema(connection);
                    String sqlQuery = SQLProcessorAI.generateSQLQuery(userQuestion, schemaInfo);
                    if (sqlQuery.trim().toUpperCase().startsWith("SELECT")) {
                        List<Map<String, Object>> results = SQLProcessorAI.executeSQLWithResults(sqlQuery);
                        Platform.runLater(() -> displayResults(results));
                    } else {
                        int affectedRows = SQLProcessorAI.executeUpdateSQL(sqlQuery);
                        Platform.runLater(() -> displayMessage(affectedRows + " ligne(s) affectée(s).", "assistant"));
                    }

                } catch (IOException e) {
                    Platform.runLater(() -> displayMessage("Erreur API : " + e.getMessage(), "error"));
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> displayMessage("Erreur SQL : " + e.getMessage(), "error"));
                }
            }).start();
        }
    }

    private void displayMessage(String message, String sender) {
        HBox messageContainer = new HBox();
        messageContainer.setPadding(new Insets(5, 10, 5, 10));

        VBox messageContent = new VBox();
        messageContent.setSpacing(5);

        TextFlow formattedText = formatText(message);
        formattedText.getStyleClass().add("message-bubble");

        if (sender.equals("user")) {
            formattedText.getStyleClass().add("user-message");
            messageContainer.setAlignment(Pos.CENTER_RIGHT);
        } else {
            formattedText.getStyleClass().add("assistant-message");
            messageContainer.setAlignment(Pos.CENTER_LEFT);
        }

        messageContent.getChildren().add(formattedText);
        messageContainer.getChildren().add(messageContent);
        chatBox.getChildren().add(messageContainer);
    }

    private void displayResults(List<Map<String, Object>> results) {
        if (results.isEmpty()) {
            displayMessage("Aucun résultat trouvé.", "assistant");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> row : results) {
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            sb.append("\n");
        }

        displayMessage(sb.toString().trim(), "assistant");
    }

    private TextFlow formatText(String text) {
        TextFlow textFlow = new TextFlow();
        textFlow.setMaxWidth(600);

        String[] lines = text.split("\n");
        for (String line : lines) {
            Text textNode = new Text(line + "\n");
            textNode.setStyle("-fx-font-size: 14px; -fx-fill: #333;");
            textFlow.getChildren().add(textNode);
        }

        return textFlow;
    }
}
