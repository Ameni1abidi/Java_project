package tn.esprit.entities;

public class categorie {
    private Integer id;
    private String nom;

    public categorie() {
    }

    public categorie(String nom) {
        this.nom = nom;
    }

    public categorie(Integer id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
        return nom;
    }
}
