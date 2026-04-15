package tn.esprit.controllers;

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

import java.sql.Timestamp;

public class AjoutForum {

    @FXML
    private FlowPane forumContainer;

    @FXML
    private VBox formPane;

    @FXML
    private TextField titreField;

    @FXML
    private TextField typeField;

    @FXML
    private TextArea contenuField;

    @FXML
    private Label pageLabel;

    private ForumService fs = new ForumService();

    private int currentPage = 1;
    private final int pageSize = 2;
    private int totalPages;

    @FXML
    public void initialize() {
        loadForums();
    }

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

        forum f = new forum(
                0,
                titreField.getText(),
                contenuField.getText(),
                typeField.getText(),
                new Timestamp(System.currentTimeMillis())
        );

        String erreur = f.valider();

        if (erreur != null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(erreur);
            alert.showAndWait();
            return;
        }

        fs.ajouter(f);

        titreField.clear();
        contenuField.clear();
        typeField.clear();

        showList();
        loadForums();
    }

    // ================= READ + PAGINATION =================
    private void loadForums() {

        forumContainer.getChildren().clear();

        int totalItems = fs.countForums();
        totalPages = (int) Math.ceil((double) totalItems / pageSize);

        fs.getPaginated(currentPage, pageSize).forEach(f -> {

            try {
                VBox card = new VBox(10);
                card.setPrefWidth(300);
                card.setStyle("-fx-background-color:white; -fx-padding:15; -fx-background-radius:10;");

                Label titre = new Label(f.getTitre());
                titre.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");

                Label info = new Label("Type: " + f.getType() + " | " + f.getDateCreation());
                info.setStyle("-fx-text-fill:gray;");

                Label contenu = new Label(f.getContenu());

                String btnStyle = "-fx-background-color:#2ecc71; -fx-text-fill:white; -fx-background-radius:15;";

                Button edit = new Button("Modifier");
                edit.setStyle(btnStyle);

                Button delete = new Button("Supprimer");
                delete.setStyle(btnStyle);

                delete.setOnAction(e -> {
                    fs.supprimer(f.getId());
                    loadForums();
                });

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

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/commentaire.fxml"));
                Parent commentUI = loader.load();

                AjoutCommentaire cc = loader.getController();
                cc.setForumId(f.getId());

                card.getChildren().addAll(titre, info, contenu, actions, commentUI);

                forumContainer.getChildren().add(card);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        pageLabel.setText("Page " + currentPage + " / " + totalPages);
    }

    // ================= PAGINATION =================
    @FXML
    void nextPage(ActionEvent event) {
        if (currentPage < totalPages) {
            currentPage++;
            loadForums();
        }
    }

    @FXML
    void previousPage(ActionEvent event) {
        if (currentPage > 1) {
            currentPage--;
            loadForums();
        }
    }

    // ================= NAVIGATION =================
    @FXML
    private void goDashboard(ActionEvent event) {
        loadPage(event, "/ProfDashboard.fxml");
    }

    @FXML
    private void goCours(ActionEvent event) {
        loadPage(event, "/CoursList.fxml");
    }

    @FXML
    private void goRessources(ActionEvent event) {
        loadPage(event, "/listeRessources.fxml");
    }

    @FXML
    private void goCategories(ActionEvent event) {
        loadPage(event, "/CategorieList.fxml");
    }

    @FXML
    private void goExamens(ActionEvent event) {
        loadPage(event, "/ExamenView.fxml");
    }

    @FXML
    private void goEvaluations(ActionEvent event) {
        loadPage(event, "/EvaluationView.fxml");
    }

    @FXML
    private void goResultats(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("La page resultats sera bientot disponible.");
        alert.showAndWait();
    }

    @FXML
    private void goLogout(ActionEvent event) {
        loadPage(event, "/Login.fxml");
    }

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