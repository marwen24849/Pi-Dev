package esprit.tn.pidevrh.teams_departements;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Department {

    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty description;
    private final IntegerProperty totalEquipe;

    public Department(int id, String name, String description, int totalEquipe) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
        this.totalEquipe = new SimpleIntegerProperty(totalEquipe);
    }

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public StringProperty descriptionProperty() { return description; }

    public int getTotalEquipe() { return totalEquipe.get(); }
    public void setTotalEquipe(int totalEquipe) { this.totalEquipe.set(totalEquipe); }
    public IntegerProperty totalEquipeProperty() { return totalEquipe; }
}