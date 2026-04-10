package tn.esprit.entities;

public class resources {
    private int id;
    private String titre;
    private String contenu;
    private int categorieId;

    public resources() {
    }

    public resources(String titre, String contenu, int categorieId) {
        this.titre = titre;
        this.contenu = contenu;
        this.categorieId = categorieId;
    }

    public resources(int id, String titre, String contenu, int categorieId) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.categorieId = categorieId;
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

    public int getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(int categorieId) {
        this.categorieId = categorieId;
    }

    @Override
    public String toString() {
        return "resources{id=" + id + ", titre='" + titre + "', contenu='" + contenu + "', categorieId=" + categorieId + "}";
    }
}

