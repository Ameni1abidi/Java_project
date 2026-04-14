package tn.esprit.entities;

import java.sql.Timestamp;

public class forum {

    private int id;
    private String titre;
    private String contenu;
    private String type;
    private Timestamp dateCreation;

    public forum(int id, String titre, String contenu, String type, Timestamp dateCreation) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.type = type;
        this.dateCreation = dateCreation;
    }

    // ================= VALIDATION =================
    public String valider() {

        if (titre == null || titre.trim().isEmpty()) {
            return "Le titre est obligatoire";
        }

        if (titre.length() < 3) {
            return "Le titre doit contenir au moins 3 caractères";
        }

        if (type == null || type.trim().isEmpty()) {
            return "Le type est obligatoire";
        }

        if (contenu == null || contenu.trim().isEmpty()) {
            return "Le contenu est obligatoire";
        }

        if (contenu.length() < 5) {
            return "Le contenu doit contenir au moins 5 caractères";
        }

        if (dateCreation == null) {
            return "La date est obligatoire";
        }

        return null;
    }

    public int getId() { return id; }
    public String getTitre() { return titre; }
    public String getContenu() { return contenu; }
    public String getType() { return type; }
    public Timestamp getDateCreation() { return dateCreation; }

    public void setContenu(String contenu) { this.contenu = contenu; }
}