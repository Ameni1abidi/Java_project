package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import tn.esprit.entities.forum;
import tn.esprit.services.ForumService;

import java.sql.Timestamp;

public class AjoutForum {

    @FXML
    private TextField titreField;

    @FXML
    private TextArea contenuField;

    @FXML
    private TextField typeField;

    @FXML
    private TextField idField;

    private ForumService forumService = new ForumService();

    // ✅ CREATE → appel service
    @FXML
    public void ajouterForum() {

        String titre = titreField.getText();
        String contenu = contenuField.getText();
        String type = typeField.getText();

        forum f = new forum(
                0,
                titre,
                contenu,
                type,
                new Timestamp(System.currentTimeMillis())
        );

        forumService.ajouter(f);

        System.out.println("Ajout OK (via service)");
        clearFields();
    }

    // ✅ UPDATE → appel service
    @FXML
    public void modifierForum() {

        int id = Integer.parseInt(idField.getText());

        forum f = new forum(
                id,
                titreField.getText(),
                contenuField.getText(),
                typeField.getText(),
                null
        );

        forumService.modifier(f);

        System.out.println("Modification OK (via service)");
        clearFields();
    }

    // ✅ DELETE → appel service
    @FXML
    public void supprimerForum() {

        int id = Integer.parseInt(idField.getText());

        forumService.supprimer(id);

        System.out.println("Suppression OK (via service)");
        clearFields();
    }

    // ✅ READ → appel service
    @FXML
    public void afficherForums() {

        forumService.afficher().forEach(System.out::println);
    }

    // 🔹 utilitaire
    private void clearFields() {
        titreField.clear();
        contenuField.clear();
        typeField.clear();
        idField.clear();
    }
}