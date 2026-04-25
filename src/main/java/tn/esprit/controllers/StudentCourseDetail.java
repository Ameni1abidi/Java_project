package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Cours;
import tn.esprit.entities.Chapitre;
import tn.esprit.services.ChapitreService;

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
            Label duree = new Label("Durée: " + ch.getDureeEstimee() + " min");

            box.getChildren().addAll(titre, duree);
            chapitreContainer.getChildren().add(box);
        }
    }
}
