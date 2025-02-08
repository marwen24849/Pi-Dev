package esprit.tn.pidevrh.formation;

public class Formation {
    private Long id;
    private String titre;
    private String description;
    private int duration;

    public Formation(String titre, String description, int duration) {
        this.titre = titre;
        this.description = description;
        this.duration = duration;
    }
    public Formation(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuree() {
        return duration;
    }

    public void setDuree(int duree) {
        this.duration = duree;
    }
}
