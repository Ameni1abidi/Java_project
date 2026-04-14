package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ProfDashboardController {

    @FXML
    private Label dateLabel;
    @FXML
    private ListView<String> scheduleList;

    @FXML
    public void initialize() {
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy")));
        scheduleList.setItems(FXCollections.observableArrayList(
                "Lun 09:00 - Java avance (Salle B12)",
                "Mar 14:00 - Correction TP (Salle A03)",
                "Mer 10:00 - Reunion pedagogique",
                "Jeu 08:30 - Examen blanc POO",
                "Ven 11:00 - Suivi etudiants"
        ));
    }

    @FXML
    private void goCoursList(ActionEvent event) {
        loadPage(event, "/CoursList.fxml");
    }

    @FXML
    private void goExams(ActionEvent event) {
        loadPage(event, "/ExamenView.fxml");
    }

    @FXML
    private void goEvaluations(ActionEvent event) {
        loadPage(event, "/EvaluationView.fxml");
    }

    @FXML
    private void goForum(ActionEvent event) {
        loadPage(event, "/forum.fxml");
    }

    @FXML
    private void goBack(ActionEvent event) {
        loadPage(event, "/Home.fxml");
    }

    @FXML
    private void logout(ActionEvent event) {
        loadPage(event, "/Login.fxml");
    }

    private void loadPage(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
