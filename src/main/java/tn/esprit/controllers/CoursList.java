package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.entities.Cours;
import tn.esprit.services.CoursService;

import java.util.List;

public class CoursList {

    @FXML
    private FlowPane coursContainer;

    @FXML
    private TextField searchField;

    private CoursService service = new CoursService();

    // LOAD DATA
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

    // CREATE CARD
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

        // BUTTONS
        Button btnChapitre = new Button("liste Chapitres");
        btnChapitre.setStyle("-fx-border-color:#28a745; -fx-text-fill:#28a745;");
        btnChapitre.setOnAction(e -> goToChapitres(c));

        Button btnModifier = new Button("Modifier");
        btnModifier.setStyle("-fx-border-color:#007bff; -fx-text-fill:#007bff;");
        btnModifier.setOnAction(e -> modifierCours(c));

        Button btnDelete = new Button("Supprimer");
        btnDelete.setStyle("-fx-border-color:#dc3545; -fx-text-fill:#dc3545;");
        btnDelete.setOnAction(e -> deleteCours(c));

        HBox actions = new HBox(10, btnModifier, btnChapitre, btnDelete);

        card.getChildren().addAll(titre, desc, date, actions);

        return card;
    }

    // 🔍 SEARCH
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

    // ➕ ADD
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

    // ✏️ MODIFY
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

    // 📚 CHAPITRES
    void goToChapitres(Cours c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ChapitreList.fxml"));
            Parent root = loader.load();

            ChapitreList controller = loader.getController();

            // ✅ CORRECT METHOD
            controller.setCoursId(c.getId());

            Stage stage = (Stage) coursContainer.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ❌ DELETE
    void deleteCours(Cours c) {
        service.supprimer(c.getId());
        loadData();
    }
}
