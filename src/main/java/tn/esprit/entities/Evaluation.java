package tn.esprit.entities;

public class Evaluation {

    private int id;
    private double note;
    private String appreciation;

    public Evaluation() {}

    public Evaluation(double note, String appreciation) {
        this.note = note;
        this.appreciation = appreciation;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getNote() { return note; }
    public void setNote(double note) { this.note = note; }

    public String getAppreciation() { return appreciation; }
    public void setAppreciation(String appreciation) { this.appreciation = appreciation; }

    @Override
    public String toString() {
        return "Evaluation{" +
                "id=" + id +
                ", note=" + note +
                ", appreciation='" + appreciation + '\'' +
                '}';
    }
}
