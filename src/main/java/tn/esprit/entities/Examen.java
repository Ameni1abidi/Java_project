package tn.esprit.entities;

import java.time.LocalDate;

public class Examen {
    private int id;
    private String titre;
    private String contenu;
    private String type;
    private LocalDate dateExamen;
    private int duree;
    private int coursId;
    private int enseignantId;

    public Examen() {}



    public Examen(String titre, String contenu, String type, LocalDate dateExamen, int duree, int coursId, int enseignantId) {
        this.titre = titre;
        this.contenu = contenu;
        this.type = type;
        this.dateExamen = dateExamen;
        this.duree = duree;
        this.coursId = coursId;
        this.enseignantId = enseignantId;
    }
    public int getCoursId() {
        return coursId;
    }

    public void setCoursId(int coursId) {
        this.coursId = coursId;
    }

    public int getEnseignantId() {
        return enseignantId;
    }

    public void setEnseignantId(int enseignantId) {
        this.enseignantId = enseignantId;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDate getDateExamen() { return dateExamen; }
    public void setDateExamen(LocalDate dateExamen) { this.dateExamen = dateExamen; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }

    @Override

    public String toString() {
        return "Examen{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", type='" + type + '\'' +
                ", date=" + dateExamen +
                ", duree=" + duree +
                ", coursId=" + coursId +
                ", enseignantId=" + enseignantId +
                '}';
    }

}
