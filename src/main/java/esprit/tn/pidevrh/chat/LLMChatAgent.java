package esprit.tn.pidevrh.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

import java.io.IOException;
import java.util.List;

public class LLMChatAgent {
    private static final String API_URL = "https://api.together.xyz/v1/chat/completions";
    private static final String API_KEY = "6b4936e05022dd727b3ae86cd8d557326f2e2213c0012b9b0be74f0bff3b1a36";
    private static final int MAX_HISTORY = 1000;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatService chatService = new ChatService();

    public String sendMessage(String userId, String userMessage) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "meta-llama/Meta-Llama-3.1-8B-Instruct-Turbo-128K");
            requestBody.put("max_tokens", 200);

            ArrayNode messagesArray = requestBody.putArray("messages");
            List<Chat> chatHistory = chatService.getChatHistory(userId, MAX_HISTORY);

            for (Chat chat : chatHistory) {
                ObjectNode chatNode = objectMapper.createObjectNode();
                chatNode.put("role", chat.getRole());
                chatNode.put("content", chat.getContent());
                messagesArray.add(chatNode);
            }

            ObjectNode userMessageNode = objectMapper.createObjectNode();
            userMessageNode.put("role", "user");
            userMessageNode.put("content", userMessage);
            messagesArray.add(userMessageNode);

            String jsonRequest = objectMapper.writeValueAsString(requestBody);
            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(jsonRequest, MediaType.parse("application/json")))
                    .build();

            Response response = client.newCall(request).execute();
            if (response.body() != null) {
                String responseBody = response.body().string();
                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                if (jsonResponse.has("choices") && jsonResponse.get("choices").size() > 0) {
                    String assistantResponse = jsonResponse.get("choices").get(0).get("message").get("content").asText();

                    chatService.saveMessage(new Chat(userId, "user", userMessage));
                    chatService.saveMessage(new Chat(userId, "assistant", assistantResponse));

                    return assistantResponse;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Erreur de l'IA";
    }
}
