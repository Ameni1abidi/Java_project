package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import tn.esprit.entities.commentaire;
import tn.esprit.services.CommentaireService;

import java.sql.Timestamp;

public class AjoutCommentaire {

    @FXML
    private VBox commentContainer;

    @FXML
    private TextArea contenuField;

    private CommentaireService cs = new CommentaireService();

    private int forumId;

    // ================= SET FORUM =================
    public void setForumId(int id) {
        this.forumId = id;
        loadCommentaires();
    }

    // ================= CREATE =================
    @FXML
    public void ajouterCommentaire() {

        commentaire c = new commentaire(
                0,
                contenuField.getText(),
                forumId,
                new Timestamp(System.currentTimeMillis())
        );

        // 🔥 VALIDATION
        String erreur = c.valider();

        if (erreur != null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText(erreur);
            alert.showAndWait();
            return;
        }

        cs.ajouter(c);

        contenuField.clear();
        loadCommentaires();
    }

    // ================= READ =================
    private void loadCommentaires() {

        commentContainer.getChildren().clear();

        cs.afficher().stream()
                .filter(c -> c.getForumId() == forumId)
                .forEach(c -> {

                    VBox box = new VBox(5);
                    box.setStyle("-fx-background-color:white; -fx-padding:10; -fx-background-radius:10;");

                    Label contenu = new Label(c.getContenu());
                    contenu.setWrapText(true);

                    Label date = new Label(c.getDateEnvoi().toString());
                    date.setStyle("-fx-text-fill:gray; -fx-font-size:11px;");

                    String btnStyle = "-fx-background-color:#2ecc71; -fx-text-fill:white; -fx-background-radius:15;";

                    Button edit = new Button("Modifier");
                    edit.setStyle(btnStyle);

                    Button delete = new Button("Supprimer");
                    delete.setStyle(btnStyle);

                    // 🔹 DELETE
                    delete.setOnAction(e -> {
                        cs.supprimer(c.getId());
                        loadCommentaires();
                    });

                    // 🔹 UPDATE
                    edit.setOnAction(e -> {
                        TextInputDialog dialog = new TextInputDialog(c.getContenu());
                        dialog.setTitle("Modifier commentaire");

                        dialog.showAndWait().ifPresent(newText -> {
                            c.setContenu(newText);
                            cs.modifier(c);
                            loadCommentaires();
                        });
                    });

                    HBox actions = new HBox(10, edit, delete);

                    box.getChildren().addAll(contenu, date, actions);

                    commentContainer.getChildren().add(box);
                });
    }
}