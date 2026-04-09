package tn.esprit;

import tn.esprit.entities.forum;
import tn.esprit.services.ForumService;

import java.sql.Timestamp;

import tn.esprit.entities.Chapitre;
import tn.esprit.entities.Cours;
import tn.esprit.services.ChapitreService;
import tn.esprit.services.CoursService;
import tn.esprit.utils.MyDatabase;

import java.sql.Date;

public class Main {
    public static void main(String[] args) {

        // Service Chapitre
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




        // Service Cours
        CoursService coursService = new CoursService();

        // CREATE Cours
        Cours c = new Cours(
                "Java",
                "Cours Java",
                "Débutant",
                Date.valueOf("2026-04-08"),
                "Java EN",
                "Java Course",
                "gold"
        );
        coursService.ajouter(c);

        // READ Cours
        coursService.afficher().forEach(System.out::println);

        // UPDATE Cours
        c.setId(1); // Assure-toi que cet ID existe
        c.setTitre("Java avancé");
        coursService.modifier(c);

        // DELETE Cours
        coursService.supprimer(1);
        ForumService fs = new ForumService();

        // 🔹 AJOUT
        forum f = new forum(0, "Sport", "Real Madrid best club", "football", new Timestamp(System.currentTimeMillis()));
        fs.ajouter(f);

        // 🔹 AFFICHAGE
        fs.afficher().forEach(System.out::println);
    }

}