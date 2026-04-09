package tn.esprit.test;
import tn.esprit.entities.*;


import tn.esprit.services.*;

import java.sql.Date;
import java.sql.Timestamp;

public class Main {

    public static void main(String[] args) {

        // 🔹 Service Categorie
        CategoryService categoryService = new CategoryService();

        // CREATE Categorie
        categorie c = new categorie("Categorie Test");
        categoryService.add(c);
        System.out.println("Categorie ajoutée");

        // UPDATE Categorie
        c.setNom("Categorie Modifiée");
        categoryService.update(c);
        System.out.println("Categorie modifiée");

        // 🔹 Service Ressource
        ResourceService resourceService = new ResourceService();

        // CREATE Ressource
        resources r = new resources(
                "Ressource Test",
                "Contenu Test",
                c.getId()
        );
        resourceService.add(r);
        System.out.println("Ressource ajoutée");

        // UPDATE Ressource
        r.setTitre("Ressource Modifiée");
        r.setContenu("Contenu Modifié");
        r.setCategorieId(c.getId());
        resourceService.update(r);
        System.out.println("Ressource modifiée");

        // 🔹 Service Chapitre
        ChapitreService chapitreService = new ChapitreService();

        // CREATE Chapitre
        Chapitre ch = new Chapitre(
                "Chapitre 1",
                1,
                "texte",
                "Contenu texte",
                "fichier.pdf",
                60,
                "Résumé chapitre",
                2
        );
        chapitreService.ajouter(ch);

        // READ Chapitre
        chapitreService.afficher().forEach(System.out::println);

        // UPDATE Chapitre
        ch.setId(1);
        ch.setTitre("Chapitre modifié");
        chapitreService.modifier(ch);

        // DELETE Chapitre
        chapitreService.supprimer(1);

        // 🔹 Service Cours
        CoursService coursService = new CoursService();

        // CREATE Cours
        Cours cours = new Cours(
                "Java",
                "Cours Java",
                "Débutant",
                Date.valueOf("2026-04-08"),
                "Java EN",
                "Java Course",
                "gold"
        );
        coursService.ajouter(cours);

        // READ Cours
        coursService.afficher().forEach(System.out::println);

        // UPDATE Cours
        cours.setId(1);
        cours.setTitre("Java avancé");
        coursService.modifier(cours);

        // DELETE Cours
        coursService.supprimer(1);

        // 🔹 Service Forum
        ForumService fs = new ForumService();

        // CREATE Forum
        forum f = new forum(
                0,
                "Sport",
                "Real Madrid best club",
                "football",
                new Timestamp(System.currentTimeMillis())
        );
        fs.ajouter(f);

        // READ Forum
        fs.afficher().forEach(System.out::println);
    }
}
