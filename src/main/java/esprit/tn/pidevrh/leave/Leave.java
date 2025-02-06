package esprit.tn.pidevrh.leave;

import java.time.LocalDate;

public class Leave {
    private int id;
    private String typeConge;
    private String autre;
    private String justification;
    private String status;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    // Constructor
    public Leave(int id,  String typeConge, String autre, String justification, String status, LocalDate dateDebut, LocalDate dateFin) {
        this.id = id;
        this.typeConge = typeConge;
        this.autre = autre;
        this.justification = justification;
        this.status = status;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    // Getters and Setters
    public int getId() { return id; }

    public String getTypeConge() { return typeConge; }
    public String getAutre() { return autre; }
    public String getJustification() { return justification; }
    public String getStatus() { return status; }
    public LocalDate getDateDebut() { return dateDebut; }
    public LocalDate getDateFin() { return dateFin; }

    public void setTypeConge(String typeConge) { this.typeConge = typeConge; }
    public void setAutre(String autre) { this.autre = autre; }
    public void setJustification(String justification) { this.justification = justification; }
    public void setStatus(String status) { this.status = status; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
}
