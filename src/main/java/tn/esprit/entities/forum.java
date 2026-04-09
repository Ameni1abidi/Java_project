package tn.esprit.entities;

import java.sql.Timestamp;

public class forum {

    private int id;
    private String titre;
    private String contenu;
    private String type;
    private Timestamp dateCreation;

    public forum() {}

    public forum(int id, String titre, String contenu, String type, Timestamp dateCreation) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.type = type;
        this.dateCreation = dateCreation;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }

    @Override
    public String toString() {
        return "forum{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", contenu='" + contenu + '\'' +
                ", type='" + type + '\'' +
                ", dateCreation=" + dateCreation +
                '}';
    }
}