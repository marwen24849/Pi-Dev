package esprit.tn.pidevrh.quiz;

public class Quiz {

    private int time;

    private Long Id;

    private String title;
    private String category;
    private double minimumSuccessPercentage;
    private String difficultylevel;
    private boolean passer= false;

    public Quiz(String title, String category, double minimumSuccessPercentage, String difficultylevel, boolean passer) {
        this.title = title;
        this.category = category;
        this.minimumSuccessPercentage = minimumSuccessPercentage;
        this.difficultylevel = difficultylevel;
        this.passer = passer;
    }

    public Quiz() {
    }


    public Quiz(int time, String title, String category, double minimumSuccessPercentage, String difficultylevel, boolean passer) {
        this.time = time;
        this.title = title;
        this.category = category;
        this.minimumSuccessPercentage = minimumSuccessPercentage;
        this.difficultylevel = difficultylevel;
        this.passer = passer;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getMinimumSuccessPercentage() {
        return minimumSuccessPercentage;
    }

    public void setMinimumSuccessPercentage(double minimumSuccessPercentage) {
        this.minimumSuccessPercentage = minimumSuccessPercentage;
    }

    public String getDifficultylevel() {
        return difficultylevel;
    }

    public void setDifficultylevel(String difficultylevel) {
        this.difficultylevel = difficultylevel;
    }

    public boolean isPasser() {
        return passer;
    }

    public void setPasser(boolean passer) {
        this.passer = passer;
    }
}
