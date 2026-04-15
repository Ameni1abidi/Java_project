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

public class ParentDashboardController {

    @FXML
    private Label dateLabel;
    @FXML
    private ListView<String> planningList;

    @FXML
    public void initialize() {
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy")));
        planningList.setItems(FXCollections.observableArrayList(
                "Lun 09:00 - Mathematiques (enfant)",
                "Mar 14:30 - Java (enfant)",
                "Jeu 11:00 - Evaluation continue",
                "Ven 16:00 - Entretien parent-prof"
        ));
    }

    @FXML
    private void goCoursList(ActionEvent event) {
        loadPage(event, "/CoursList.fxml");
    }

    @FXML
    private void goEvaluations(ActionEvent event) {
        loadPage(event, "/EvaluationView.fxml");
    }

    @FXML
    private void goExams(ActionEvent event) {
        loadPage(event, "/ExamenView.fxml");
    }

    @FXML
    private void goBack(ActionEvent event) {
        loadPage(event, "/Home.fxml");
    }

    @FXML
    private void goHome(ActionEvent event) {
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
