package tn.esprit.entities;

public class Evaluation {

    private int id;
    private double note;
    private String appreciation;
    private int examenId;
    private int eleveId;

    public Evaluation() {}

    public Evaluation(double note, String appreciation, int examenId, int eleveId) {
        this.note = note;
        this.appreciation = appreciation;
        this.examenId = examenId;
        this.eleveId = eleveId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getNote() { return note; }
    public void setNote(double note) { this.note = note; }

    public String getAppreciation() { return appreciation; }
    public void setAppreciation(String appreciation) { this.appreciation = appreciation; }

    public int getExamenId() { return examenId; }
    public void setExamenId(int examenId) { this.examenId = examenId; }
    public int getEleveId() {return eleveId;}
    public void setEleveId(int eleveId) {this.eleveId = eleveId;}
    @Override

    public String toString() {
        return "Evaluation{" +
                ", note=" + note +
                ", appreciation='" + appreciation + '\'' +
                ", examenId=" + examenId +
                ", eleveId=" + eleveId +
                '}';
    }
}
