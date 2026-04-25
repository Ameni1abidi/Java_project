package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import tn.esprit.entities.commentaire;
import tn.esprit.services.CommentaireService;
import tn.esprit.services.TranslationService;

import java.sql.Timestamp;

public class AjoutCommentaire {

    @FXML private VBox commentContainer;
    @FXML private TextArea contenuField;

    private final CommentaireService cs = new CommentaireService();
    private final TranslationService ts = new TranslationService();

    private int forumId;

    public void setForumId(int id) {
        this.forumId = id;
        loadCommentaires();
    }

    @FXML
    public void ajouterCommentaire() {
        commentaire c = new commentaire(
                0,
                contenuField.getText(),
                forumId,
                new Timestamp(System.currentTimeMillis())
        );
        String erreur = c.valider();
        if (erreur != null) {
            new Alert(Alert.AlertType.ERROR, erreur).showAndWait();
            return;
        }
        cs.ajouter(c);
        contenuField.clear();
        loadCommentaires();
    }

    private void loadCommentaires() {
        commentContainer.getChildren().clear();
        cs.afficher().stream()
                .filter(c -> c.getForumId() == forumId)
                .forEach(c -> commentContainer.getChildren().add(buildCommentCard(c)));
    }

    private VBox buildCommentCard(commentaire c) {

        Label contenu = new Label(c.getContenu());
        contenu.setWrapText(true);
        contenu.setStyle("-fx-font-size:13px; -fx-text-fill:#222;");

        Label date = new Label(c.getDateEnvoi().toString());
        date.setStyle("-fx-text-fill:#aaa; -fx-font-size:11px;");

        Button edit   = new Button("Modifier");
        Button delete = new Button("Supprimer");
        edit  .setStyle("-fx-background-color:#2ecc71; -fx-text-fill:white; -fx-background-radius:15; -fx-font-size:11px;");
        delete.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white; -fx-background-radius:15; -fx-font-size:11px;");

        delete.setOnAction(e -> {
            cs.supprimer(c.getId());
            loadCommentaires();
        });
        edit.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog(c.getContenu());
            dialog.setTitle("Modifier commentaire");
            dialog.showAndWait().ifPresent(newText -> {
                c.setContenu(newText);
                cs.modifier(c);
                loadCommentaires();
            });
        });

        Label translatedLabel = new Label();
        translatedLabel.setWrapText(true);
        translatedLabel.setVisible(false);
        translatedLabel.setManaged(false);
        translatedLabel.setStyle(
                "-fx-font-size:13px;" +
                        "-fx-text-fill:#1a5276;" +
                        "-fx-background-color:#eaf4fb;" +
                        "-fx-padding:8 10;" +
                        "-fx-background-radius:8;" +
                        "-fx-border-color:#aed6f1;" +
                        "-fx-border-radius:8;" +
                        "-fx-border-width:1;"
        );

        Label loadingLabel = new Label("Traduction en cours...");
        loadingLabel.setVisible(false);
        loadingLabel.setManaged(false);
        loadingLabel.setStyle("-fx-font-size:11px; -fx-text-fill:#999; -fx-font-style:italic;");

        ComboBox<String> langueBox = new ComboBox<>();
        langueBox.getItems().add("-- Langue --");
        langueBox.getItems().addAll("fr", "en", "ar", "es", "de", "it", "pt", "zh");
        langueBox.setValue("-- Langue --");
        langueBox.setPrefWidth(110);
        langueBox.setStyle(
                "-fx-background-color:white;" +
                        "-fx-border-color:#ccc;" +
                        "-fx-border-radius:15;" +
                        "-fx-background-radius:15;" +
                        "-fx-font-size:12px;"
        );

        langueBox.setOnAction(e -> {
            String lang = langueBox.getValue();
            if (lang == null || lang.equals("-- Langue --")) {
                translatedLabel.setVisible(false);
                translatedLabel.setManaged(false);
                return;
            }
            loadingLabel.setVisible(true);
            loadingLabel.setManaged(true);
            translatedLabel.setVisible(false);
            translatedLabel.setManaged(false);
            langueBox.setDisable(true);

            String original = c.getContenu();
            new Thread(() -> {
                String result = ts.traduire(original, lang);
                Platform.runLater(() -> {
                    langueBox.setDisable(false);
                    loadingLabel.setVisible(false);
                    loadingLabel.setManaged(false);
                    if (result != null && !result.isBlank()) {
                        translatedLabel.setText("[" + lang.toUpperCase() + "]  " + result);
                        translatedLabel.setVisible(true);
                        translatedLabel.setManaged(true);
                    } else {
                        new Alert(Alert.AlertType.WARNING, "Traduction indisponible.").showAndWait();
                    }
                });
            }).start();
        });

        HBox row1 = new HBox(8, edit, delete);
        HBox row2 = new HBox(8, langueBox);

        VBox card = new VBox(6, contenu, date, row1, row2, loadingLabel, translatedLabel);
        card.setStyle(
                "-fx-background-color:white;" +
                        "-fx-padding:12;" +
                        "-fx-background-radius:12;" +
                        "-fx-border-color:#eee;" +
                        "-fx-border-radius:12;"
        );
        return card;
    }
}
/// testestest