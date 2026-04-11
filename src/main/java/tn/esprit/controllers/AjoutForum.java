package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import tn.esprit.entities.forum;
import tn.esprit.services.ForumService;

import java.sql.Timestamp;

public class AjoutForum {

    @FXML
    private VBox forumContainer;

    @FXML
    private VBox formPane;

    @FXML
    private TextField titreField;

    @FXML
    private TextField typeField;

    @FXML
    private TextArea contenuField;

    private ForumService fs = new ForumService();

    // ================= INIT =================
    @FXML
    public void initialize() {
        loadForums();
    }

    // ================= SWITCH =================
    @FXML
    public void showCreateForm() {
        formPane.setVisible(true);
    }

    @FXML
    public void showList() {
        formPane.setVisible(false);
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

        fs.ajouter(f);

        // reset form
        titreField.clear();
        contenuField.clear();
        typeField.clear();

        showList();
        loadForums();
    }

    // ================= READ =================
    private void loadForums() {

        forumContainer.getChildren().clear();

        fs.afficher().forEach(f -> {

            try {
                VBox card = new VBox(10);
                card.setStyle("-fx-background-color:white; -fx-padding:15; -fx-background-radius:10;");

                Label titre = new Label(f.getTitre());
                titre.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");

                Label info = new Label("Type: " + f.getType() + " | " + f.getDateCreation());
                info.setStyle("-fx-text-fill:gray;");

                Label contenu = new Label(f.getContenu());

                // 🔹 boutons CRUD forum
                Button delete = new Button("Supprimer");
                Button edit = new Button("Modifier");

                // DELETE
                delete.setOnAction(e -> {
                    fs.supprimer(f.getId());
                    loadForums();
                });

                // UPDATE
                edit.setOnAction(e -> {
                    TextInputDialog dialog = new TextInputDialog(f.getContenu());
                    dialog.setTitle("Modifier forum");

                    dialog.showAndWait().ifPresent(newText -> {
                        f.setContenu(newText);
                        fs.modifier(f);
                        loadForums();
                    });
                });

                HBox actions = new HBox(10, edit, delete);

                // 🔥 charger les commentaires
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/commentaire.fxml"));
                Parent commentUI = loader.load();

                AjoutCommentaire cc = loader.getController();
                cc.setForumId(f.getId());

                // ajouter tout
                card.getChildren().addAll(titre, info, contenu, actions, commentUI);

                forumContainer.getChildren().add(card);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}