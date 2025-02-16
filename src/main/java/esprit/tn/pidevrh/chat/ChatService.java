package esprit.tn.pidevrh.chat;

import esprit.tn.pidevrh.connection.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatService {

    public void saveMessage(Chat chat) {
        String sql = "INSERT INTO chat (user_id, role, content) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, chat.getUserId());
            stmt.setString(2, chat.getRole());
            stmt.setString(3, chat.getContent());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Chat> getChatHistory(String userId, int limit) {
        List<Chat> messages = new ArrayList<>();
        String sql = "SELECT * FROM chat WHERE user_id = ? ORDER BY timestamp DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Chat chat = new Chat(rs.getString("user_id"),
                        rs.getString("role"),
                        rs.getString("content"));
                chat.setId(rs.getInt("id"));
                chat.setTimestamp(rs.getTimestamp("timestamp"));
                messages.add(chat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
}
