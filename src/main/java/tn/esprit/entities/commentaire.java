package tn.esprit.entities;

import java.sql.Timestamp;

public class commentaire {

    private int id;
    private String contenu;
    private int forumId;
    private Timestamp dateEnvoi;

    public commentaire() {}

    public commentaire(int id, String contenu, int forumId, Timestamp dateEnvoi) {
        this.id = id;
        this.contenu = contenu;
        this.forumId = forumId;
        this.dateEnvoi = dateEnvoi;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public int getForumId() { return forumId; }
    public void setForumId(int forumId) { this.forumId = forumId; }

    public Timestamp getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(Timestamp dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    @Override
    public String toString() {
        return "commentaire{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", forumId=" + forumId +
                ", dateEnvoi=" + dateEnvoi +
                '}';
    }
}