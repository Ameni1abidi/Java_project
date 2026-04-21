package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.entities.Cours;
import tn.esprit.services.CoursService;

import javafx.event.ActionEvent;
import java.util.List;

public class CoursList {

    @FXML
    private FlowPane coursContainer;

    @FXML
    private TextField searchField;

    private CoursService service = new CoursService();

    public void initialize() {
        loadData();
    }

    public void loadData() {
        coursContainer.getChildren().clear();
        List<Cours> list = service.getAll();

        for (Cours c : list) {
            VBox card = createCard(c);
            coursContainer.getChildren().add(card);
        }
    }
    private VBox createCard(Cours c) {
        VBox card = new VBox(10);
        card.setStyle("""
            -fx-background-color:white;
            -fx-padding:15;
            -fx-background-radius:15;
            -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.1),10,0,0,5);
        """);

        Label titre = new Label(c.getTitre());
        titre.setStyle("-fx-font-weight:bold; -fx-font-size:16;");

        Label desc = new Label(c.getDescription());
        Label date = new Label("Créé le: " + c.getDateCreation());


        Button btnChapitre = new Button("Liste Chapitres");
        btnChapitre.setStyle("-fx-border-color:#007bff; -fx-text-fill:#007bff;");
        btnChapitre.setOnAction(e -> goToChapitres(c));

        Button btnModifier = new Button("Modifier");
        btnModifier.setStyle("-fx-border-color:#28a745; -fx-text-fill:#28a745;");
        btnModifier.setOnAction(e -> modifierCours(c));

        Button btnDelete = new Button("Supprimer");
        btnDelete.setStyle("-fx-border-color:#dc3545; -fx-text-fill:#dc3545;");
        btnDelete.setOnAction(e -> deleteCours(c));
        HBox actions = new HBox(10, btnChapitre, btnModifier, btnDelete);

        card.getChildren().addAll(titre, desc, date, actions);

        return card;
    }
    @FXML
    void searchCours() {
        String keyword = searchField.getText().toLowerCase();
        coursContainer.getChildren().clear();

        for (Cours c : service.getAll()) {
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void modifierCours(Cours c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CoursForm.fxml"));
            Parent root = loader.load();

            CoursForm controller = loader.getController();
            controller.setCours(c);

            Stage stage = (Stage) coursContainer.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void goToChapitres(Cours c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ChapitreList.fxml"));
            Parent root = loader.load();

            ChapitreList controller = loader.getController();
            controller.setCoursId(c.getId());

            Stage stage = (Stage) coursContainer.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void deleteCours(Cours c) {
        service.supprimer(c.getId());
        loadData();
    }
}
