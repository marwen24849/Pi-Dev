package esprit.tn.pidevrh.chat;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatController {
    @FXML
    private VBox chatBox;
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;

    private final LLMChatAgent chatAgent = new LLMChatAgent();
    private final ChatService chatService = new ChatService();
    private final String userId = "1";

    @FXML
    private void initialize() {
        sendButton.setOnAction(event -> sendMessage());
        messageField.setOnAction(event -> sendMessage());
        List<Chat> chatHistory = chatService.getChatHistory(userId, 50);
        Collections.reverse(chatHistory);
        chatHistory.forEach(chat -> displayMessage(chat.getContent(), chat.getRole()));    }

    private void sendMessage() {
        String userMessage = messageField.getText().trim();
        if (!userMessage.isEmpty()) {
            displayMessage(userMessage, "user");
            messageField.clear();

            new Thread(() -> {
                String response = chatAgent.sendMessage(userId, userMessage);
                javafx.application.Platform.runLater(() -> displayMessage(response, "assistant"));
            }).start();
        }
    }

    private void displayMessage(String message, String sender) {
        HBox messageContainer = new HBox();
        messageContainer.setPadding(new Insets(5, 10, 5, 10));

        VBox messageContent = new VBox();
        messageContent.setSpacing(5);

        if (sender.equals("user")) {
            TextFlow formattedText = formatText(message);
            formattedText.getStyleClass().add("message-bubble");
            formattedText.getStyleClass().add("user-message");
            messageContainer.setAlignment(Pos.CENTER_RIGHT);
            messageContent.getChildren().add(formattedText);
        } else {
            List<Node> parsedContent = parseMessageContent(message);
            for (Node node : parsedContent) {
                messageContent.getChildren().add(node);
            }
            messageContainer.setAlignment(Pos.CENTER_LEFT);
        }

        messageContainer.getChildren().add(messageContent);
        chatBox.getChildren().add(messageContainer);
    }


    private List<Node> parseMessageContent(String message) {
        List<Node> nodes = new ArrayList<>();
        Pattern pattern = Pattern.compile("```(.*?)```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(message);

        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String textPart = message.substring(lastEnd, matcher.start());
                nodes.add(formatText(textPart));
            }
            String code = matcher.group(1).trim();
            TextArea codeArea = new TextArea(code);
            codeArea.setEditable(false);
            codeArea.setWrapText(true);
            codeArea.getStyleClass().add("code-block");
            codeArea.setMinHeight(100);
            nodes.add(codeArea);

            lastEnd = matcher.end();
        }

        if (lastEnd < message.length()) {
            nodes.add(formatText(message.substring(lastEnd)));
        }

        return nodes;
    }

    private TextFlow formatText(String text) {
        TextFlow textFlow = new TextFlow();
        Pattern pattern = Pattern.compile("(\\*\\*(.*?)\\*\\*|_(.*?)_)");
        Matcher matcher = pattern.matcher(text);

        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                textFlow.getChildren().add(new Text(text.substring(lastEnd, matcher.start())));
            }

            Text styledText;
            if (matcher.group(2) != null) {
                styledText = new Text(matcher.group(2));
                styledText.setStyle("-fx-font-weight: bold;");
            } else {
                styledText = new Text(matcher.group(3));
                styledText.setStyle("-fx-font-style: italic;");
            }

            textFlow.getChildren().add(styledText);
            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) {
            textFlow.getChildren().add(new Text(text.substring(lastEnd)));
        }

        return textFlow;
    }


}