package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import tn.esprit.entities.forum;
import tn.esprit.services.ForumService;

import java.sql.Timestamp;

public class AjoutForum {

    // 🔹 PANES
    @FXML
    private VBox listPane;

    @FXML
    private VBox formPane;

    // 🔹 LIST
    @FXML
    private ListView<String> forumList;

    // 🔹 FORM
    @FXML
    private TextField titreField;

    @FXML
    private TextField typeField;

    @FXML
    private TextArea contenuField;

    private ForumService forumService = new ForumService();

    // ================= INIT =================
    @FXML
    public void initialize() {
        loadForums();
    }

    // ================= SWITCH =================

    @FXML
    public void showCreateForm() {
        listPane.setVisible(false);
        formPane.setVisible(true);
    }

    @FXML
    public void showList() {
        formPane.setVisible(false);
        listPane.setVisible(true);
    }

    // ================= CREATE =================

    @FXML
    public void ajouterForum() {

        if (titreField.getText().isEmpty() || typeField.getText().isEmpty()) {
            System.out.println("Champs vides !");
            return;
        }

        forum f = new forum(
                0,
                titreField.getText(),
                contenuField.getText(),
                typeField.getText(),
                new Timestamp(System.currentTimeMillis())
        );

        forumService.ajouter(f);

        System.out.println("Forum ajouté !");

        clearFields();
        loadForums();
        showList();
    }

    // ================= READ =================

    private void loadForums() {

        forumList.getItems().clear();

        forumService.afficher().forEach(f ->
                forumList.getItems().add(
                        f.getTitre() + " | " + f.getType()
                )
        );
    }

    // ================= UTIL =================

    private void clearFields() {
        titreField.clear();
        contenuField.clear();
        typeField.clear();
    }
}