package esprit.tn.pidevrh.projet;

import java.time.LocalDateTime;

public class Projet {
    private int id;
    private String nomProjet;
    private String equipe;
    private String responsable;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    public Projet() {}

    public Projet(int id, String nomProjet, String equipe, String responsable, LocalDateTime dateDebut, LocalDateTime dateFin) {
        this.id = id;
        this.nomProjet = nomProjet;
        this.equipe = equipe;
        this.responsable = responsable;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomProjet() { return nomProjet; }
    public void setNomProjet(String nomProjet) { this.nomProjet = nomProjet; }

    public String getEquipe() { return equipe; }
    public void setEquipe(String equipe) { this.equipe = equipe; }

    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    @Override
    public String toString() {
        return "Projet{" +
                "id=" + id +
                ", nomProjet='" + nomProjet + '\'' +
                ", equipe='" + equipe + '\'' +
                ", responsable='" + responsable + '\'' +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                '}';
    }
}