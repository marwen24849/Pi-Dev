package esprit.tn.pidevrh.congeApprove;

import java.time.LocalDate;

public class Conge {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private int userId;

    public Conge(Long id, LocalDate startDate, LocalDate endDate, int userId) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    @Override
    public String toString() {
        return "Conge{" +
                "id=" + id +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", userId=" + userId +
                '}';
    }
}
