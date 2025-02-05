package esprit.tn.pidevrh.response;

public class Resultat {

    private int score;
    private double percentage;
    private boolean resultat;

    public Resultat(int score, double percentage, boolean resultat) {
        this.score = score;
        this.percentage = percentage;
        this.resultat = resultat;
    }

    public Resultat() {
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public boolean isResultat() {
        return resultat;
    }

    public void setResultat(boolean resultat) {
        this.resultat = resultat;
    }
}
