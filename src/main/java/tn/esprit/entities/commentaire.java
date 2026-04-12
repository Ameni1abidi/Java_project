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

    // 🔥 VALIDATION
    public String valider() {

        if (contenu == null || contenu.trim().isEmpty()) {
            return "Le commentaire est obligatoire";
        }

        if (contenu.length() < 3) {
            return "Le commentaire doit contenir au moins 3 caractères";
        }

        if (forumId <= 0) {
            return "Forum invalide";
        }

        if (dateEnvoi == null) {
            return "Date invalide";
        }

        return null;
    }

    public int getId() { return id; }
    public String getContenu() { return contenu; }
    public int getForumId() { return forumId; }
    public Timestamp getDateEnvoi() { return dateEnvoi; }

    public void setContenu(String contenu) { this.contenu = contenu; }
}