package esprit.tn.pidevrh.chat;

import java.sql.Timestamp;

public class Chat {
    private int id;
    private String userId;
    private String role;
    private String content;
    private Timestamp timestamp;

    public Chat(String userId, String role, String content) {
        this.userId = userId;
        this.role = role;
        this.content = content;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
