package esprit.tn.pidevrh.leave;

import java.time.LocalDate;

public class Leave {
    private int id;
    private int userId; // Ajout de l'ID utilisateur
    private String typeConge;
    private String autre;
    private String justification;
    private String status;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private byte[] certificate; // Ajout du certificat (si congé maladie)

    // Constructeur mis à jour
    public Leave(int id, int userId, String typeConge, String autre, String justification, String status, LocalDate dateDebut, LocalDate dateFin, byte[] certificate) {
        this.id = id;
        this.userId = userId;
        this.typeConge = typeConge;
        this.autre = autre;
        this.justification = justification;
        this.status = status;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.certificate = certificate;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; } // Getter pour userId
    public String getTypeConge() { return typeConge; }
    public String getAutre() { return autre; }
    public String getJustification() { return justification; }
    public String getStatus() { return status; }
    public LocalDate getDateDebut() { return dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public byte[] getCertificate() { return certificate; } // Getter pour certificat

    // Setters
    public void setTypeConge(String typeConge) { this.typeConge = typeConge; }
    public void setAutre(String autre) { this.autre = autre; }
    public void setJustification(String justification) { this.justification = justification; }
    public void setStatus(String status) { this.status = status; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public void setCertificate(byte[] certificate) { this.certificate = certificate; } // Setter pour certificat
}
