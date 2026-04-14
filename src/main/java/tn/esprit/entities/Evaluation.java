package tn.esprit.entities;

public class Evaluation {

    private int id;
    private double note;
    private String appreciation;
    private int examenId;

    public Evaluation() {}

    public Evaluation(double note, String appreciation, int examenId) {
        this.note = note;
        this.appreciation = appreciation;
        this.examenId = examenId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getNote() { return note; }
    public void setNote(double note) { this.note = note; }

    public String getAppreciation() { return appreciation; }
    public void setAppreciation(String appreciation) { this.appreciation = appreciation; }

    public int getExamenId() { return examenId; }
    public void setExamenId(int examenId) { this.examenId = examenId; }
    @Override
    public String toString() {
        return "Evaluation{" +
                "id=" + id +
                ", note=" + note +
                ", appreciation='" + appreciation + '\'' +
                '}';
    }
}
