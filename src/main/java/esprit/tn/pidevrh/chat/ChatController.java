package esprit.tn.pidevrh.chat;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

public class ChatController {
    @FXML
    private ListView<String> chatListView;
    @FXML
    private TextField userInputField;
    @FXML
    private Button sendButton;

    private final LLMChatAgent chatAgent = new LLMChatAgent();
    private final String userId = "aziz";

    @FXML
    public void initialize() {
        chatListView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item);
                            if (item.startsWith("Vous: ")) {
                                setStyle("-fx-alignment: CENTER-RIGHT; -fx-background-color: #DCF8C6; -fx-padding: 5px; -fx-background-radius: 10px;");
                            } else {
                                setStyle("-fx-alignment: CENTER-LEFT; -fx-background-color: #ECECEC; -fx-padding: 5px; -fx-background-radius: 10px;");
                            }
                        }
                    }
                };
            }
        });
    }

    @FXML
    public void handleSendMessage() {
        String userMessage = userInputField.getText();
        if (!userMessage.trim().isEmpty()) {
            chatListView.getItems().add("Vous: " + userMessage);
            String response = chatAgent.sendMessage(userId, userMessage);
            chatListView.getItems().add("IA: " + response);
            userInputField.clear();
        }
    }
}