package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.Chapitre;
import tn.esprit.entities.Cours;
import tn.esprit.services.ChapitreService;
import tn.esprit.utils.ResourceNavigationContext;

import java.io.IOException;
import java.util.List;

public class StudentCourseDetail {

    @FXML
    private VBox chapitreContainer;

    @FXML
    private Label titleLabel;

    private Cours cours;

    public void setCours(Cours c) {
        this.cours = c;
        titleLabel.setText(c.getTitre());
        loadChapitres();
    }

    private void loadChapitres() {
        chapitreContainer.getChildren().clear();

        ChapitreService service = new ChapitreService();
        List<Chapitre> list = service.getByCoursId(cours.getId());

        for (Chapitre ch : list) {
            VBox box = new VBox(5);
            box.setStyle("-fx-background-color:white; -fx-padding:12; -fx-background-radius:12;");

            Label titre = new Label(ch.getTitre());
            Label duree = new Label("Duree: " + ch.getDureeEstimee() + " min");

            Button resourcesButton = new Button("Ressources");
            resourcesButton.setStyle("-fx-background-color:#5b7cfa; -fx-text-fill:white; -fx-background-radius:16; -fx-padding:6 12; -fx-font-weight:bold;");
            resourcesButton.setOnAction(e -> openResourcesForChapter(ch));

            box.getChildren().addAll(titre, duree, resourcesButton);
            chapitreContainer.getChildren().add(box);
        }
    }

    private void openResourcesForChapter(Chapitre chapitre) {
        if (chapitre == null || chapitre.getId() <= 0) {
            showNavigationError("Chapitre invalide. Impossible d'ouvrir les ressources.");
            return;
        }
        try {
            ResourceNavigationContext.openForChapitre(chapitre.getId(), chapitre.getTitre(), true);
            Stage stage = (Stage) chapitreContainer.getScene().getWindow();
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/StudentChapterResources.fxml"));
                stage.setScene(new Scene(root));
                stage.setTitle("Ressources du chapitre");
                stage.show();
            } catch (Exception primaryLoadException) {
                primaryLoadException.printStackTrace();
                Parent fallbackRoot = FXMLLoader.load(getClass().getResource("/listeRessources.fxml"));
                stage.setScene(new Scene(fallbackRoot));
                stage.setTitle("Ressources");
                stage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showNavigationError("Ouverture des ressources echouee: " + e.getMessage());
        }
    }

    private void showNavigationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Navigation");
        alert.setHeaderText("Le bouton Ressources a rencontre une erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
