package tn.esprit.test;

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
