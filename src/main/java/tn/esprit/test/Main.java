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

import tn.esprit.entities.categorie;
import tn.esprit.entities.resources;
import tn.esprit.services.CategoryService;
import tn.esprit.services.ResourceService;

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

        // 🔹 AJOUT
        forum f = new forum(0, "Sport", "Real Madrid best club", "football", new Timestamp(System.currentTimeMillis()));
        fs.ajouter(f);

        // 🔹 AFFICHAGE
        fs.afficher().forEach(System.out::println);
    }

}
