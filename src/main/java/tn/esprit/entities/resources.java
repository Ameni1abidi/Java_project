package tn.esprit.entities;

public class resources {
    private int id;
    private String titre;
    private String contenu;
    private String categorieNom;
    private String type;
    private String disponibleLe;
    private int chapitreId;
    private boolean favori;

    public resources() {
    }

    public resources(String titre, String contenu, String categorieNom, String type, String disponibleLe) {
        this.titre = titre;
        this.contenu = contenu;
        this.categorieNom = categorieNom;
        this.type = type;
        this.disponibleLe = disponibleLe;
        this.chapitreId = 0;
    }

    public resources(int id, String titre, String contenu, String categorieNom, String type, String disponibleLe) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.categorieNom = categorieNom;
        this.type = type;
        this.disponibleLe = disponibleLe;
        this.chapitreId = 0;
    }

    public resources(String titre, String contenu, String categorieNom, String type, String disponibleLe, int chapitreId) {
        this.titre = titre;
        this.contenu = contenu;
        this.categorieNom = categorieNom;
        this.type = type;
        this.disponibleLe = disponibleLe;
        this.chapitreId = chapitreId;
    }

    public resources(int id, String titre, String contenu, String categorieNom, String type, String disponibleLe, int chapitreId) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.categorieNom = categorieNom;
        this.type = type;
        this.disponibleLe = disponibleLe;
        this.chapitreId = chapitreId;
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

    public String getCategorieNom() {
        return categorieNom;
    }

    public void setCategorieNom(String categorieNom) {
        this.categorieNom = categorieNom;
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

    public int getChapitreId() {
        return chapitreId;
    }

    public void setChapitreId(int chapitreId) {
        this.chapitreId = chapitreId;
    }

    public boolean isFavori() {
        return favori;
    }

    public void setFavori(boolean favori) {
        this.favori = favori;
    }

    @Override
    public String toString() {
        return "resources{id=" + id + ", titre='" + titre + "', contenu='" + contenu + "', type='" + type + "', categorieNom='" + categorieNom + "', disponibleLe='" + disponibleLe + "', chapitreId=" + chapitreId + ", favori=" + favori + "}";
    }
}

