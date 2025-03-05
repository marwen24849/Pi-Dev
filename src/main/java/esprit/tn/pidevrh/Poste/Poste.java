package esprit.tn.pidevrh.Poste;

import java.util.Date;

public class Poste {

    private long id;
    private long userId;
    private String content;
    private double salaire;
    private String description;
    private Date datePoste;
    private String state;


    public Poste() {
    }

    public Poste(String content, double salaire, String description, Date datePoste , String state) {
        this.content = content;
        this.salaire = salaire;
        this.description = description;
        this.datePoste = datePoste;
        this.state = state;
    }
    
    

    public Poste(long id, long userId, String content, double salaire, String description, java.sql.Date  datePoste , String state) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.salaire = salaire;
        this.description = description;
        this.datePoste = datePoste;
        this.state = state;

    }

    public Poste(Long userId, String content, double salaire, String description, java.sql.Date datePoste, String state) {
        this.userId = userId;
        this.content = content;
        this.salaire = salaire;
        this.description = description;
        this.datePoste = datePoste;
        this.state = state;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public double getSalaire() {
        return salaire;
    }

    public void setSalaire(double salaire) {
        this.salaire = salaire;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date  getDatePoste() {
        return (java.sql.Date) datePoste;
    }

    public void setDatePoste(Date datePoste) {
        this.datePoste = datePoste;
    }


    @Override
    public String toString() {
        return "Poste{" +
                "id=" + id +
                ", userId=" + userId +
                ", content='" + content + '\'' +
                ", salaire=" + salaire +
                ", description='" + description + '\'' +
                ", datePoste=" + datePoste +
                '}';
    }





}
