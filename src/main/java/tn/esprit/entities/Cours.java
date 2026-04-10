package tn.esprit.entities;

import java.sql.Date;

public class Cours {
    private int id;
    private String titre;
    private String description;
    private String niveau;
    private Date dateCreation;
    private String titreTraduit;
    private String descriptionTraduit;
    private String badge;

    public Cours() {}

    public Cours(String titre, String description, String niveau, Date dateCreation,
                 String titreTraduit, String descriptionTraduit, String badge) {
        this.titre = titre;
        this.description = description;
        this.niveau = niveau;
        this.dateCreation = dateCreation;
        this.titreTraduit = titreTraduit;
        this.descriptionTraduit = descriptionTraduit;
        this.badge = badge;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getTitreTraduit() {
        return titreTraduit;
    }

    public void setTitreTraduit(String titreTraduit) {
        this.titreTraduit = titreTraduit;
    }

    public String getDescriptionTraduit() {
        return descriptionTraduit;
    }

    public void setDescriptionTraduit(String descriptionTraduit) {
        this.descriptionTraduit = descriptionTraduit;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }
}
