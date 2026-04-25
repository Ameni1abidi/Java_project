package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import tn.esprit.entities.forum;
import tn.esprit.services.ForumService;
import tn.esprit.services.OllamaService;

import java.sql.Timestamp;

public class AjoutForum {

    @FXML private FlowPane forumContainer;
    @FXML private VBox     formPane;
    @FXML private TextField titreField;
    @FXML private TextField typeField;
    @FXML private TextArea  contenuField;
    @FXML private Label     pageLabel;

    private final ForumService  fs     = new ForumService();
    private final OllamaService ollama = new OllamaService();

    private int currentPage = 1;
    private final int pageSize = 3;   // ← 3 forums par page
    private int totalPages;

    @FXML
    public void initialize() { loadForums(); }

    @FXML public void showCreateForm() { formPane.setVisible(true);  }
    @FXML public void showList()       { formPane.setVisible(false); }

    // ── CREATE ────────────────────────────────────────────────────────────
    @FXML
    public void ajouterForum() {
        forum f = new forum(
                0,
                titreField.getText(),
                contenuField.getText(),
                typeField.getText(),
                new Timestamp(System.currentTimeMillis())
        );
        String erreur = f.valider();
        if (erreur != null) {
            new Alert(Alert.AlertType.ERROR, erreur).showAndWait();
            return;
        }
        fs.ajouter(f);
        titreField.clear();
        contenuField.clear();
        typeField.clear();
        showList();
        loadForums();
    }

    // ── READ + PAGINATION ─────────────────────────────────────────────────
    private void loadForums() {
        forumContainer.getChildren().clear();

        int totalItems = fs.countForums();
        totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages == 0) totalPages = 1;

        fs.getPaginated(currentPage, pageSize).forEach(f -> {
            try {
                // ── Card ─────────────────────────────────────────────────
                VBox card = new VBox(10);
                card.setPrefWidth(260);
                card.setMaxWidth(260);
                card.setStyle(
                        "-fx-background-color:white;" +
                                "-fx-padding:15;" +
                                "-fx-background-radius:12;" +
                                "-fx-border-color:#ebebeb;" +
                                "-fx-border-radius:12;" +
                                "-fx-border-width:1;" +
                                "-fx-effect: dropshadow(gaussian,#e0e0e0,6,0,0,2);"
                );

                Label titre = new Label(f.getTitre());
                titre.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");

                Label info = new Label("Type: " + f.getType() + " | " + f.getDateCreation());
                info.setStyle("-fx-text-fill:gray; -fx-font-size:11px;");

                Label contenu = new Label(f.getContenu());
                contenu.setWrapText(true);
                contenu.setStyle("-fx-font-size:12px;");

                String btnGreen  = "-fx-background-color:#2ecc71; -fx-text-fill:white; -fx-background-radius:15; -fx-font-size:11px;";
                String btnViolet = "-fx-background-color:#7c3aed; -fx-text-fill:white; -fx-background-radius:15; -fx-font-size:11px;";
                String btnRed    = "-fx-background-color:#e74c3c; -fx-text-fill:white; -fx-background-radius:15; -fx-font-size:11px;";

                Button edit   = new Button("Modifier");
                Button delete = new Button("Supprimer");
                edit  .setStyle(btnGreen);
                delete.setStyle(btnRed);

                delete.setOnAction(e -> { fs.supprimer(f.getId()); loadForums(); });
                edit.setOnAction(e -> {
                    TextInputDialog dialog = new TextInputDialog(f.getContenu());
                    dialog.setTitle("Modifier forum");
                    dialog.showAndWait().ifPresent(newText -> {
                        f.setContenu(newText);
                        fs.modifier(f);
                        loadForums();
                    });
                });

                // ── Bouton IA ─────────────────────────────────────────────
                Button iaBtn = new Button("Demander a l'IA");
                iaBtn.setStyle(btnViolet);

                Label iaLoading = new Label("L'IA reflechit...");
                iaLoading.setVisible(false);
                iaLoading.setManaged(false);
                iaLoading.setStyle("-fx-font-size:11px; -fx-text-fill:#7c3aed; -fx-font-style:italic;");

                Label iaResponse = new Label();
                iaResponse.setWrapText(true);
                iaResponse.setVisible(false);
                iaResponse.setManaged(false);
                iaResponse.setStyle(
                        "-fx-background-color:#f5f3ff;" +
                                "-fx-border-color:#c4b5fd; -fx-border-width:1;" +
                                "-fx-border-radius:8; -fx-background-radius:8;" +
                                "-fx-padding:10; -fx-font-size:12px; -fx-text-fill:#3b0764;"
                );

                iaBtn.setOnAction(e -> {
                    if (iaResponse.isVisible()) {
                        iaResponse.setVisible(false);
                        iaResponse.setManaged(false);
                        iaBtn.setText("Demander a l'IA");
                        iaBtn.setStyle(btnViolet);
                        return;
                    }
                    iaBtn.setDisable(true);
                    iaBtn.setText("Reflexion...");
                    iaLoading.setVisible(true);
                    iaLoading.setManaged(true);

                    String question = f.getTitre() + " : " + f.getContenu();
                    new Thread(() -> {
                        String reponse = ollama.poserQuestion(question);
                        Platform.runLater(() -> {
                            iaLoading.setVisible(false);
                            iaLoading.setManaged(false);
                            iaBtn.setDisable(false);
                            iaBtn.setText("Masquer la reponse IA");
                            iaResponse.setText("IA : " + reponse);
                            iaResponse.setVisible(true);
                            iaResponse.setManaged(true);
                        });
                    }).start();
                });

                HBox actions = new HBox(10, edit, delete);

                // ── Commentaires ──────────────────────────────────────────
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/commentaire.fxml"));
                Parent commentUI = loader.load();
                AjoutCommentaire cc = loader.getController();
                cc.setForumId(f.getId());

                card.getChildren().addAll(
                        titre, info, contenu, actions,
                        iaBtn, iaLoading, iaResponse,
                        commentUI
                );

                forumContainer.getChildren().add(card);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        pageLabel.setText("Page " + currentPage + " / " + totalPages);
    }

    // ── PAGINATION ────────────────────────────────────────────────────────
    @FXML void nextPage(ActionEvent event) {
        if (currentPage < totalPages) { currentPage++; loadForums(); }
    }

    @FXML void previousPage(ActionEvent event) {
        if (currentPage > 1) { currentPage--; loadForums(); }
    }

    // ── NAVIGATION ────────────────────────────────────────────────────────
    @FXML private void goDashboard(ActionEvent event)  { loadPage(event, "/ProfDashboard.fxml"); }
    @FXML private void goCours(ActionEvent event)      { loadPage(event, "/CoursList.fxml"); }
    @FXML private void goRessources(ActionEvent event) { loadPage(event, "/listeRessources.fxml"); }
    @FXML private void goCategories(ActionEvent event) { loadPage(event, "/CategorieList.fxml"); }
    @FXML private void goExamens(ActionEvent event)    { loadPage(event, "/ExamenView.fxml"); }
    @FXML private void goEvaluations(ActionEvent event){ loadPage(event, "/EvaluationView.fxml"); }

    @FXML private void goResultats(ActionEvent event) {
        new Alert(Alert.AlertType.INFORMATION, "La page resultats sera bientot disponible.").showAndWait();
    }

    @FXML private void goLogout(ActionEvent event) { loadPage(event, "/Login.fxml"); }

    private void loadPage(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
