package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import tn.esprit.entities.commentaire;
import tn.esprit.services.BadWordsService;
import tn.esprit.services.CommentaireService;
import tn.esprit.services.SentimentService;
import tn.esprit.services.TranslationService;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

public class AjoutCommentaire {

    @FXML private VBox commentContainer;
    @FXML private TextArea contenuField;

    private final CommentaireService cs        = new CommentaireService();
    private final TranslationService ts        = new TranslationService();
    private final SentimentService   sentiment = new SentimentService();
    private final BadWordsService    badWords  = new BadWordsService();

    private int forumId;

    private static final String[][] DEFAULT_REACTIONS = {
            { "👍 J'aime",  "#FFF9C4", "#F57F17" },
            { "❤ Amour",    "#FCE4EC", "#C62828" },
            { "😄 Haha",    "#FFF8E1", "#FF8F00" },
            { "😮 Wow",     "#E3F2FD", "#1565C0" },
            { "😢 Triste",  "#E8EAF6", "#283593" },
            { "🔥 Feu",     "#FBE9E7", "#BF360C" }
    };

    private static final String[][] EXTRA_REACTIONS = {
            { "🎉 Bravo",      "#F3E5F5", "#6A1B9A" },
            { "👏 Applause",   "#E8F5E9", "#2E7D32" },
            { "💪 Force",      "#FFF3E0", "#E65100" },
            { "🏆 Trophee",    "#FFFDE7", "#F9A825" },
            { "⚽ Goal",       "#E0F2F1", "#00695C" },
            { "😍 Super",      "#FCE4EC", "#AD1457" },
            { "🤩 Incroyable", "#EDE7F6", "#4527A0" },
            { "💯 100%",       "#E8F5E9", "#1B5E20" },
            { "🙌 GG",         "#FFF8E1", "#FF6F00" },
            { "😡 Grr",        "#FFEBEE", "#B71C1C" }
    };

    public void setForumId(int id) {
        this.forumId = id;
        loadCommentaires();
    }

    @FXML
    public void ajouterCommentaire() {
        String texte = contenuField.getText();

        // ── Vérification vide ────────────────────────────────────────────
        if (texte == null || texte.isBlank()) {
            new Alert(Alert.AlertType.ERROR, "Le commentaire ne peut pas etre vide.").showAndWait();
            return;
        }

        // ── Vérification bad words ───────────────────────────────────────
        if (badWords.contientBadWord(texte)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Commentaire inapproprié");
            alert.setHeaderText("⚠️ Langage inapproprié détecté !");
            alert.setContentText("Votre commentaire contient des mots interdits.\nVeuillez modifier votre message.");
            alert.showAndWait();
            return;
        }

        // ── Validation entité ────────────────────────────────────────────
        commentaire c = new commentaire(
                0,
                texte,
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

        // ── Contenu & date ───────────────────────────────────────────────
        Label contenu = new Label(c.getContenu());
        contenu.setWrapText(true);
        contenu.setStyle("-fx-font-size:13px; -fx-text-fill:#222;");

        Label date = new Label(c.getDateEnvoi().toString());
        date.setStyle("-fx-text-fill:#aaa; -fx-font-size:11px;");

        // ── Badge sentiment (analyse automatique) ────────────────────────
        Label sentimentBadge = new Label("⏳ Analyse...");
        sentimentBadge.setStyle(
                "-fx-background-color:#e2e3e5; -fx-text-fill:#383d41;" +
                        "-fx-background-radius:20; -fx-padding:4 14;" +
                        "-fx-font-size:12px; -fx-font-weight:bold;"
        );

        // Lancement automatique de l'analyse en arrière-plan
        new Thread(() -> {
            String result = sentiment.analyserSentiment(c.getContenu());
            Platform.runLater(() -> {
                String bg, fg, emoji;
                switch (result) {
                    case "😊 POSITIF" -> { bg = "#d4edda"; fg = "#155724"; emoji = "😊 POSITIF"; }
                    case "😞 NÉGATIF" -> { bg = "#f8d7da"; fg = "#721c24"; emoji = "😞 NÉGATIF"; }
                    case "😐 NEUTRE"  -> { bg = "#fff3cd"; fg = "#856404"; emoji = "😐 NEUTRE";  }
                    default           -> { bg = "#e2e3e5"; fg = "#383d41"; emoji = "❓ " + result; }
                }
                sentimentBadge.setText(emoji);
                sentimentBadge.setStyle(
                        "-fx-background-color:" + bg + ";" +
                                "-fx-text-fill:" + fg + ";" +
                                "-fx-background-radius:20; -fx-padding:4 14;" +
                                "-fx-font-size:12px; -fx-font-weight:bold;" +
                                "-fx-border-color:" + fg + "; -fx-border-radius:20; -fx-border-width:1;"
                );
            });
        }).start();

        // ── Modifier / Supprimer ─────────────────────────────────────────
        Button edit   = new Button("Modifier");
        Button delete = new Button("Supprimer");
        edit  .setStyle("-fx-background-color:#2ecc71; -fx-text-fill:white; -fx-background-radius:15; -fx-font-size:11px;");
        delete.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white; -fx-background-radius:15; -fx-font-size:11px;");

        delete.setOnAction(e -> { cs.supprimer(c.getId()); loadCommentaires(); });
        edit.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog(c.getContenu());
            dialog.setTitle("Modifier commentaire");
            dialog.showAndWait().ifPresent(newText -> {

                // ── Vérification bad words aussi sur modification ────────
                if (badWords.contientBadWord(newText)) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Modification inappropriée");
                    alert.setHeaderText("⚠️ Langage inapproprié détecté !");
                    alert.setContentText("Votre commentaire contient des mots interdits.");
                    alert.showAndWait();
                    return;
                }

                c.setContenu(newText);
                cs.modifier(c);
                loadCommentaires();
            });
        });

        // ── Traduction ───────────────────────────────────────────────────
        Label translatedLabel = new Label();
        translatedLabel.setWrapText(true);
        translatedLabel.setVisible(false);
        translatedLabel.setManaged(false);
        translatedLabel.setStyle(
                "-fx-font-size:13px; -fx-text-fill:#1a5276;" +
                        "-fx-background-color:#eaf4fb; -fx-padding:8 10;" +
                        "-fx-background-radius:8; -fx-border-color:#aed6f1;" +
                        "-fx-border-radius:8; -fx-border-width:1;"
        );

        Label loadingLabel = new Label("Traduction en cours...");
        loadingLabel.setVisible(false);
        loadingLabel.setManaged(false);
        loadingLabel.setStyle("-fx-font-size:11px; -fx-text-fill:#999; -fx-font-style:italic;");

        ComboBox<String> langueBox = new ComboBox<>();
        langueBox.getItems().add("-- Langue --");
        langueBox.getItems().addAll("fr", "en", "ar", "es", "de", "it", "pt", "zh");
        langueBox.setValue("-- Langue --");
        langueBox.setPrefWidth(120);
        langueBox.setStyle(
                "-fx-background-color:white; -fx-border-color:#ccc;" +
                        "-fx-border-radius:15; -fx-background-radius:15; -fx-font-size:12px;"
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
            new Thread(() -> {
                String result = ts.traduire(c.getContenu(), lang);
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

        // ── Réactions ────────────────────────────────────────────────────
        Map<String, int[]> reactionData = new LinkedHashMap<>();
        for (String[] r : DEFAULT_REACTIONS) reactionData.put(r[0], new int[]{0, 0});

        FlowPane reactionsBar = new FlowPane();
        reactionsBar.setHgap(6);
        reactionsBar.setVgap(6);
        reactionsBar.setPrefWrapLength(400);
        reactionsBar.setStyle("-fx-padding:4 0 2 0;");

        FlowPane pickerPane = new FlowPane();
        pickerPane.setHgap(6);
        pickerPane.setVgap(6);
        pickerPane.setPrefWrapLength(400);
        pickerPane.setVisible(false);
        pickerPane.setManaged(false);
        pickerPane.setStyle(
                "-fx-background-color:#fafafa;" +
                        "-fx-border-color:#e0e0e0; -fx-border-width:1;" +
                        "-fx-border-radius:8; -fx-background-radius:8;" +
                        "-fx-padding:8;"
        );

        Button addBtn = new Button("+");
        addBtn.setStyle(
                "-fx-background-color:transparent;" +
                        "-fx-border-color:#bbb; -fx-border-width:1;" +
                        "-fx-border-radius:20; -fx-background-radius:20;" +
                        "-fx-text-fill:#888; -fx-font-size:14px;" +
                        "-fx-min-width:30; -fx-min-height:28; -fx-padding:2 10;"
        );
        addBtn.setOnAction(ev -> {
            boolean show = !pickerPane.isVisible();
            pickerPane.setVisible(show);
            pickerPane.setManaged(show);
        });

        for (String[] reaction : DEFAULT_REACTIONS) {
            String label    = reaction[0];
            String bgActive = reaction[1];
            String fgActive = reaction[2];
            int[]  data     = reactionData.get(label);
            Button pill     = createPill(label, data, bgActive, fgActive);
            pill.setOnAction(ev -> {
                if (data[1] == 0) { data[0]++; data[1] = 1; }
                else              { data[0] = Math.max(0, data[0] - 1); data[1] = 0; }
                pill.setText(label + "  " + data[0]);
                applyPillStyle(pill, data[1] == 1, bgActive, fgActive);
            });
            reactionsBar.getChildren().add(pill);
        }
        reactionsBar.getChildren().add(addBtn);

        for (String[] reaction : EXTRA_REACTIONS) {
            String em       = reaction[0];
            String bgActive = reaction[1];
            String fgActive = reaction[2];
            Button opt      = new Button(em);
            opt.setStyle(
                    "-fx-background-color:#ececec;" +
                            "-fx-border-color:#ddd; -fx-border-width:1;" +
                            "-fx-border-radius:15; -fx-background-radius:15;" +
                            "-fx-font-size:11px; -fx-padding:4 10; -fx-text-fill:#444;"
            );
            opt.setOnAction(ev -> {
                pickerPane.setVisible(false);
                pickerPane.setManaged(false);
                if (reactionData.containsKey(em)) {
                    int[] data = reactionData.get(em);
                    if (data[1] == 0) { data[0]++; data[1] = 1; }
                    else              { data[0] = Math.max(0, data[0] - 1); data[1] = 0; }
                    for (javafx.scene.Node node : reactionsBar.getChildren()) {
                        if (node instanceof Button btn && btn.getText().startsWith(em)) {
                            btn.setText(em + "  " + data[0]);
                            applyPillStyle(btn, data[1] == 1, bgActive, fgActive);
                            break;
                        }
                    }
                } else {
                    reactionData.put(em, new int[]{1, 1});
                    int[]  data = reactionData.get(em);
                    Button pill = createPill(em, data, bgActive, fgActive);
                    pill.setOnAction(pev -> {
                        if (data[1] == 0) { data[0]++; data[1] = 1; }
                        else              { data[0] = Math.max(0, data[0] - 1); data[1] = 0; }
                        pill.setText(em + "  " + data[0]);
                        applyPillStyle(pill, data[1] == 1, bgActive, fgActive);
                    });
                    reactionsBar.getChildren().add(
                            reactionsBar.getChildren().indexOf(addBtn), pill);
                }
            });
            pickerPane.getChildren().add(opt);
        }

        // ── Assemblage final ─────────────────────────────────────────────
        HBox row1 = new HBox(8, edit, delete);
        HBox row2 = new HBox(8, langueBox);

        VBox card = new VBox(6,
                contenu, date,
                row1,
                sentimentBadge,
                row2,
                reactionsBar, pickerPane,
                loadingLabel, translatedLabel
        );
        card.setStyle(
                "-fx-background-color:white; -fx-padding:14;" +
                        "-fx-background-radius:12; -fx-border-color:#eee; -fx-border-radius:12;"
        );
        return card;
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private Button createPill(String label, int[] data, String bgActive, String fgActive) {
        Button pill = new Button(label + "  " + data[0]);
        applyPillStyle(pill, data[1] == 1, bgActive, fgActive);
        return pill;
    }

    private void applyPillStyle(Button pill, boolean active, String bgActive, String fgActive) {
        if (active) {
            pill.setStyle(
                    "-fx-background-color:" + bgActive + ";" +
                            "-fx-border-color:" + fgActive + "; -fx-border-width:1.5;" +
                            "-fx-border-radius:20; -fx-background-radius:20;" +
                            "-fx-text-fill:" + fgActive + ";" +
                            "-fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 12;"
            );
        } else {
            pill.setStyle(
                    "-fx-background-color:#f5f5f5;" +
                            "-fx-border-color:#ddd; -fx-border-width:1;" +
                            "-fx-border-radius:20; -fx-background-radius:20;" +
                            "-fx-text-fill:#666;" +
                            "-fx-font-size:11px; -fx-padding:4 12;"
            );
        }
    }
}