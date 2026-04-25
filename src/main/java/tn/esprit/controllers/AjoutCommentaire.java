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

        String erreur = c.valider();
        if (erreur != null) {
            new Alert(Alert.AlertType.ERROR, erreur).showAndWait();
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
                .forEach(c -> commentContainer.getChildren().add(buildCommentCard(c)));
    }

    // ================= CARD BUILDER =================
    private VBox buildCommentCard(commentaire c) {

        Label contenu = new Label(c.getContenu());
        contenu.setWrapText(true);
        contenu.setStyle("-fx-font-size:13px;");

        Label date = new Label(c.getDateEnvoi().toString());
        date.setStyle("-fx-text-fill:gray; -fx-font-size:11px;");

        // Boutons Modifier / Supprimer
        String btnGreen = "-fx-background-color:#2ecc71; -fx-text-fill:white; -fx-background-radius:15;";
        String btnRed   = "-fx-background-color:#e74c3c; -fx-text-fill:white; -fx-background-radius:15;";
        String btnGray  = "-fx-background-color:#95a5a6; -fx-text-fill:white; -fx-background-radius:15;";

        Button edit    = new Button("Modifier");
        Button delete  = new Button("Supprimer");
        edit  .setStyle(btnGreen);
        delete.setStyle(btnRed);

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

        // ComboBox langues
        ComboBox<String> langueBox = new ComboBox<>();
        langueBox.getItems().addAll("fr", "en", "ar", "es", "de", "it", "pt", "zh");
        langueBox.setValue("en");
        langueBox.setPrefWidth(80);

        Button translateBtn = new Button("Traduire");
        translateBtn.setStyle(btnGreen);

        Button resetBtn = new Button("Original");
        resetBtn.setStyle(btnGray);
        resetBtn.setVisible(false);

        // Label traduction (separe du texte original)
        Label translatedLabel = new Label();
        translatedLabel.setWrapText(true);
        translatedLabel.setVisible(false);
        translatedLabel.setStyle(
                "-fx-font-size:13px;" +
                        "-fx-text-fill:#1a5276;" +
                        "-fx-background-color:#d6eaf8;" +
                        "-fx-padding:8 10;" +
                        "-fx-background-radius:8;"
        );

        // Traduction dans un thread separe pour ne pas bloquer l'UI
        translateBtn.setOnAction(e -> {
            String lang     = langueBox.getValue();
            String original = c.getContenu();

            translateBtn.setText("...");
            translateBtn.setDisable(true);
            translatedLabel.setVisible(false);
            resetBtn.setVisible(false);

            new Thread(() -> {
                String result = ts.traduire(original, lang);

                Platform.runLater(() -> {
                    translateBtn.setText("Traduire");
                    translateBtn.setDisable(false);

                    if (result != null && !result.isBlank()) {
                        translatedLabel.setText("[" + lang.toUpperCase() + "]  " + result);
                        translatedLabel.setVisible(true);
                        resetBtn.setVisible(true);
                    } else {
                        new Alert(Alert.AlertType.WARNING, "Traduction indisponible.").showAndWait();
                    }
                });
            }).start();
        });

        // Changer de langue masque la traduction precedente
        langueBox.setOnAction(e -> {
            translatedLabel.setVisible(false);
            resetBtn.setVisible(false);
        });

        // Reset : masquer la traduction
        resetBtn.setOnAction(e -> {
            translatedLabel.setVisible(false);
            resetBtn.setVisible(false);
        });

        HBox row1 = new HBox(8, edit, delete);
        HBox row2 = new HBox(8, langueBox, translateBtn, resetBtn);

        VBox card = new VBox(8, contenu, date, row1, row2, translatedLabel);
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