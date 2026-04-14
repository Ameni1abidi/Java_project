package tn.esprit.test;
import tn.esprit.entities.*;

import tn.esprit.entities.forum;
import tn.esprit.entities.commentaire;
import tn.esprit.services.ForumService;
import tn.esprit.services.CommentaireService;

import tn.esprit.services.*;

import java.sql.Date;
import java.sql.Timestamp;

import tn.esprit.entities.Examen;
import tn.esprit.entities.Evaluation;
import tn.esprit.services.ExamenService;
import tn.esprit.services.EvaluationService;

import java.time.LocalDate;
import java.util.List;

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



        // UPDATE Cours
        cours.setId(1);
        cours.setTitre("Java avancé");
        coursService.modifier(cours);

        // DELETE Cours
        coursService.supprimer(1);

        // 🔹 Service Forum
        ForumService fs = new ForumService();

        // CREATE Forum
        CommentaireService cs = new CommentaireService();

        // 🔹 AJOUT FORUM
        forum f = new forum(
                0,
                "Sport",
                "Real Madrid best club",
                "football",
                new Timestamp(System.currentTimeMillis())
        );
        fs.ajouter(f);

        // READ Forum
        // 🔹 AFFICHAGE FORUMS
        System.out.println("---- Forums ----");
        fs.afficher().forEach(System.out::println);

        // 🔹 AJOUT COMMENTAIRE (⚠️ ID doit exister)
        commentaire com = new commentaire(
                0,
                "Très bon sujet 🔥",
                5,
                new Timestamp(System.currentTimeMillis())
        );
        cs.ajouter(com);

        // 🔹 AFFICHAGE COMMENTAIRES
        System.out.println("---- Commentaires ----");
        cs.afficher().forEach(System.out::println);

        // ===== EXAMEN =====
        ExamenService es = new ExamenService();
        Examen e = new Examen("Examen BD", "SQL", "Final", LocalDate.now(), 90);
        es.create(e);
        es.getAll().forEach(System.out::println);
        //e.setTitre("Examen Update");
        //es.update(e);
        //es.delete(e.getId());

        // ===== EVALUATION =====
        EvaluationService evs = new EvaluationService();
        Evaluation ev = new Evaluation(16, "Bien",1);
        evs.create(ev);
        evs.getAll().forEach(System.out::println);
        //ev.setNote(19);
        //ev.setAppreciation("Excellent");
        //evs.update(ev);
        //evs.delete(ev.getId());
    }
}