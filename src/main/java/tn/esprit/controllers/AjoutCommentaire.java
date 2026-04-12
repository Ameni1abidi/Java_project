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

    public void setForumId(int id) {
        this.forumId = id;
        loadCommentaires();
    }

    @FXML
    public void ajouterCommentaire() {

        if (contenuField.getText().isEmpty()) return;

        commentaire c = new commentaire(
                0,
                contenuField.getText(),
                forumId,
                new Timestamp(System.currentTimeMillis())
        );

        cs.ajouter(c);

        contenuField.clear();
        loadCommentaires();
    }

    private void loadCommentaires() {

        commentContainer.getChildren().clear();

        cs.afficher().stream()
                .filter(c -> c.getForumId() == forumId)
                .forEach(c -> {

                    VBox box = new VBox(5);
                    box.setStyle("-fx-background-color:white; -fx-padding:8; -fx-background-radius:8;");

                    Label contenu = new Label(c.getContenu());

                    Label date = new Label(c.getDateEnvoi().toString());
                    date.setStyle("-fx-text-fill:gray; -fx-font-size:11px;");

                    Button edit = new Button("Modifier");
                    Button delete = new Button("Supprimer");

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