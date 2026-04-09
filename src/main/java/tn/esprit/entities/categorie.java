package tn.esprit.entities;

public class categorie {
    private int id;
    private String nom;

    public categorie() {
    }

    public categorie(String nom) {
        this.nom = nom;
    }

    public categorie(int id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @Override
    public String toString() {
        return "categorie{id=" + id + ", nom='" + nom + "'}";
    }
}
