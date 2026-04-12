package tn.esprit.entities;

import java.sql.Timestamp;

public class commentaire {

    private int id;
    private String contenu;
    private int forumId;
    private Timestamp dateEnvoi;

    public commentaire(int id, String contenu, int forumId, Timestamp dateEnvoi) {
        this.id = id;
        this.contenu = contenu;
        this.forumId = forumId;
        this.dateEnvoi = dateEnvoi;
    }

    public int getId() {
        return id;
    }

    public String getContenu() {
        return contenu;
    }

    public int getForumId() {
        return forumId;
    }

    public Timestamp getDateEnvoi() {
        return dateEnvoi;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }
}