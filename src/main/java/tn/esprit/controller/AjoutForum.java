package tn.esprit.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import tn.esprit.entities.forum;
import tn.esprit.services.ForumService;

import java.sql.Timestamp;

public class AjoutForum {

    // 🔹 PANES (switch view)
    @FXML
    private VBox listPane;

    @FXML
    private VBox formPane;

    // 🔹 LIST VIEW
    @FXML
    private ListView<String> forumList;

    // 🔹 FORM FIELDS
    @FXML
    private TextField titreField;

    @FXML
    private TextField typeField;

    @FXML
    private TextArea contenuField;

    // 🔹 SERVICE
    private ForumService forumService = new ForumService();

    // ================= INIT =================
    @FXML
    public void initialize() {
        loadForums();
    }

    // ================= SWITCH VIEWS =================

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

    // ================= CRUD =================

    // 🔹 CREATE
    @FXML
    public void ajouterForum() {

        if (titreField.getText().isEmpty() || typeField.getText().isEmpty()) {
            System.out.println("Veuillez remplir les champs !");
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

    // 🔹 READ (chargement liste)
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