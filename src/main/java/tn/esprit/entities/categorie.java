package tn.esprit.entities;

public class categorie {
    private String nom;

    public categorie() {
    }

    public categorie(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @Override
    public String toString() {
        return nom;
    }
}
