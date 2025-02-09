package esprit.tn.pidevrh.session;

import javafx.scene.control.DatePicker;

import java.time.LocalDate;

public class Session {
    private Long id;
    private int salle;
    private LocalDate date;

    public Session(){}

    public Session(int salle, LocalDate date) {
        this.salle = salle;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSalle() {
        return salle;
    }

    public void setSalle(int salle) {
        this.salle = salle;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
