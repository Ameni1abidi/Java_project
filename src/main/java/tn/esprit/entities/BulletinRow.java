package tn.esprit.entities;

public class BulletinRow {

    private String examenTitre;
    private double note;
    private String appreciation;

    public BulletinRow() {}

    public BulletinRow(String examenTitre, double note, String appreciation) {
        this.examenTitre = examenTitre;
        this.note = note;
        this.appreciation = appreciation;
    }

    public String getExamenTitre() {
        return examenTitre;
    }

    public void setExamenTitre(String examenTitre) {
        this.examenTitre = examenTitre;
    }

    public double getNote() {
        return note;
    }

    public void setNote(double note) {
        this.note = note;
    }

    public String getAppreciation() {
        return appreciation;
    }

    public void setAppreciation(String appreciation) {
        this.appreciation = appreciation;
    }

    @Override
    public String toString() {
        return "BulletinRow{" +
                "examenTitre='" + examenTitre + '\'' +
                ", note=" + note +
                ", appreciation='" + appreciation + '\'' +
                '}';
    }
}