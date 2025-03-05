package esprit.tn.pidevrh.session;

import java.time.LocalDate;

public class Session {
    private Long id;
    private String salle;
    private LocalDate date;
    private boolean isOnline;  // Flag to check if the session is online
    private String roomLink;  // Link for online sessions (if applicable)


    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getRoomLink() {
        return roomLink;
    }

    public void setRoomLink(String roomLink) {
        this.roomLink = roomLink;
    }


    public Session(){}

    public Session(String salle, LocalDate date) {
        this.salle = salle;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSalle() {
        return salle;
    }

    public void setSalle(String salle) {
        this.salle = salle;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
