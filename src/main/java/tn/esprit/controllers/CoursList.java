package tn.esprit.controllers;

import javafx.fxml.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.control.Label;
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
            -fx-effect:dropshadow(gaussian,rgba(0,0,0,0.1),10,0,0,5);
        """);
        card.setPrefWidth(250);

        Label titre = new Label(c.getTitre());
        titre.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");

        Label desc = new Label(c.getDescription());
        desc.setStyle("-fx-text-fill:#555;");
        desc.setWrapText(true);

        Label date = new Label("Créé le: " + c.getDateCreation());
        date.setStyle("-fx-font-size:11px; -fx-text-fill:#999;");

        // 🔘 Buttons
        HBox actions = new HBox(10);

        Button edit = new Button("Modifier");
        edit.setStyle("-fx-border-color:#007bff; -fx-text-fill:#007bff;");
        edit.setOnAction(e -> openForm(c));

        Button delete = new Button("Supprimer");
        delete.setStyle("-fx-border-color:#dc3545; -fx-text-fill:#dc3545;");
        delete.setOnAction(e -> {
            service.supprimer(c.getId());
            loadCours();
        });

        actions.getChildren().addAll(edit, delete);

        card.getChildren().addAll(titre, desc, date, actions);

        return card;
    }

    @FXML
    void goToAdd() {
        openForm(null);
    }

    private void openForm(Cours c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CoursForm.fxml"));
            Parent root = loader.load();

            CoursForm controller = loader.getController();
            controller.setCours(c);

            Stage stage = (Stage) coursContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
