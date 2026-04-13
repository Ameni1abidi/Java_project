package tn.esprit.entities;

public class resources {
    private int id;
    private String titre;
    private String contenu;
    private int categorieId;
    private String type;
    private String disponibleLe;

    public resources() {
    }

    public resources(String titre, String contenu, int categorieId, String type, String disponibleLe) {
        this.titre = titre;
        this.contenu = contenu;
        this.categorieId = categorieId;
        this.type = type;
        this.disponibleLe = disponibleLe;
    }

    public resources(int id, String titre, String contenu, int categorieId, String type, String disponibleLe) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.categorieId = categorieId;
        this.type = type;
        this.disponibleLe = disponibleLe;
    }

    public resources(String ressourceTest, String contenuTest, int id) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDisponibleLe() {
        return disponibleLe;
    }

    public void setDisponibleLe(String disponibleLe) {
        this.disponibleLe = disponibleLe;
    }

    @Override
    public String toString() {
        return "resources{id=" + id + ", titre='" + titre + "', contenu='" + contenu + "', type='" + type + "', categorieId=" + categorieId + ", disponibleLe='" + disponibleLe + "'}";
    }
}

