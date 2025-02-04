package esprit.tn.pidevrh.leave;

import java.time.LocalDate;

public class Leave {
    private int id;
    private int userId;
    private int congeId;
    private String justification;
    private String status;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    // Constructor
    public Leave(int id, int userId, int congeId, String justification, String status, LocalDate dateDebut, LocalDate dateFin) {
        this.id = id;
        this.userId = userId;
        this.congeId = congeId;
        this.justification = justification;
        this.status = status;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    // Getters and Setters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getCongeId() { return congeId; }
    public String getJustification() { return justification; }
    public String getStatus() { return status; }
    public LocalDate getDateDebut() { return dateDebut; }
    public LocalDate getDateFin() { return dateFin; }

    public void setStatus(String status) { this.status = status; }


}
