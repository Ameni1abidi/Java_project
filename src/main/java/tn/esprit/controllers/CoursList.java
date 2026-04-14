package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.Cours;
import tn.esprit.services.CoursService;

import java.util.List;

public class CoursList {

    @FXML
    private FlowPane coursContainer;

    private CoursService service = new CoursService();

    @FXML
    public void initialize() {
        loadCours();
    }

    private void loadCours() {
        coursContainer.getChildren().clear();

        List<Cours> list = service.getAll();

        for (Cours c : list) {
            coursContainer.getChildren().add(createCard(c));
        }
    }
    @FXML
    private TextField searchField;
    @FXML
    void searchCours() {

        String keyword = searchField.getText().toLowerCase();

        coursContainer.getChildren().clear();

        List<Cours> list = service.getAll();

        for (Cours c : list) {

            if (c.getTitre().toLowerCase().contains(keyword)) {
                coursContainer.getChildren().add(createCard(c));
            }
        }
    }
    @FXML
    void goToAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CoursForm.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) coursContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createCard(Cours c) {

        VBox card = new VBox(10);
        card.setStyle("""
            -fx-background-color:white;
            -fx-padding:15;
            -fx-background-radius:15;
            -fx-effect:dropshadow(gaussian,rgba(0,0,0,0.1),10,0,0,5);
        """);

        Label titre = new Label(c.getTitre());
        titre.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");

        Label desc = new Label(c.getDescription());
        desc.setWrapText(true);

        Label date = new Label("Créé le: " + c.getDateCreation());

        HBox actions = new HBox(10);

        Button chapitres = new Button("Chapitres");
        chapitres.setStyle("-fx-border-color:#28a745; -fx-text-fill:#28a745;");
        chapitres.setOnAction(e -> openChapitres(c));

        Button delete = new Button("Supprimer");
        delete.setOnAction(e -> {
            service.supprimer(c.getId());
            loadCours();
        });

        actions.getChildren().addAll(chapitres, delete);

        card.getChildren().addAll(titre, desc, date, actions);

        return card;
    }

    private void openChapitres(Cours c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ChapitreList.fxml"));
            Parent root = loader.load();

            ChapitreList controller = loader.getController();
            controller.setCoursId(c.getId());

            Stage stage = (Stage) coursContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
