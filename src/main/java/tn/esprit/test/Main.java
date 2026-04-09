package tn.esprit;

import tn.esprit.entities.forum;
import tn.esprit.entities.commentaire;
import tn.esprit.services.ForumService;
import tn.esprit.services.CommentaireService;

import java.sql.Timestamp;

import tn.esprit.entities.Chapitre;
import tn.esprit.entities.Cours;
import tn.esprit.services.ChapitreService;
import tn.esprit.services.CoursService;
import tn.esprit.utils.MyDatabase;

import java.sql.Date;

import tn.esprit.entities.categorie;
import tn.esprit.entities.resources;
import tn.esprit.services.CategoryService;
import tn.esprit.services.ResourceService;

import tn.esprit.entities.Examen;
import tn.esprit.entities.Evaluation;
import tn.esprit.services.ExamenService;
import tn.esprit.services.EvaluationService;

import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Service Categorie
        CategoryService categoryService = new CategoryService();

        // CREATE Categorie
        categorie c = new categorie("Categorie Test");
        categoryService.add(c);
        System.out.println("categorie added");

        // READ Categorie
        //categoryService.getAll().forEach(System.out::println);

        // UPDATE Categorie
        c.setNom("Categorie Modifiee");
        categoryService.update(c);
        System.out.println("categorie modified");
        // DELETE Categorie
        // categoryService.delete(c.getId());

        // Service Ressource
        ResourceService resourceService = new ResourceService();

        // CREATE Ressource
        resources r = new resources(
                "Ressource Test",
                "Contenu Test",
                c.getId()
        );
        resourceService.add(r);
        System.out.println("ressource added");
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

        // ===== EXAMEN =====
        ExamenService es = new ExamenService();
        //Examen e = new Examen("Examen BD", "SQL", "Final", LocalDate.now(), 90);
        //es.create(e);
        //es.getAll().forEach(System.out::println);
        //e.setTitre("Examen Update");
        //es.update(e);
        //es.delete(e.getId());
        // ===== EVALUATION =====
        EvaluationService evs = new EvaluationService();
        //Evaluation ev = new Evaluation(16, "Bien");
        //evs.create(ev);
        evs.getAll().forEach(System.out::println);
        //ev.setNote(19);
        //ev.setAppreciation("Excellent");
        //evs.update(ev);
        //evs.delete(ev.getId());
    }


}
// DELETE Chapitre
        chapitreService.supprimer(1);


        // READ Ressource
        //resourceService.getAll().forEach(System.out::println);

        // UPDATE Ressource
        r.setTitre("Ressource Modifiee");
        r.setContenu("Contenu Modifie");
        r.setCategorieId(c.getId());
        resourceService.update(r);
        System.out.println("ressource modified");
        // DELETE Ressource
        // resourceService.delete(r.getId());
    }
}

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

        // 🔹 AFFICHAGE FORUMS
        System.out.println("---- Forums ----");
        fs.afficher().forEach(System.out::println);

        // 🔹 AJOUT COMMENTAIRE (⚠️ ID doit exister)
        commentaire c = new commentaire(
                0,
                "Très bon sujet 🔥",
                5,
                new Timestamp(System.currentTimeMillis())
        );
        cs.ajouter(c);

        // 🔹 AFFICHAGE COMMENTAIRES
        System.out.println("---- Commentaires ----");
        cs.afficher().forEach(System.out::println);
    }

}
