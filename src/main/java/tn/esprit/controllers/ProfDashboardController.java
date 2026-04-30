package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.AuditLogService;
import tn.esprit.utils.UserSession;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ProfDashboardController {
    private final AuditLogService auditLogService = new AuditLogService();

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
    private void goResources(ActionEvent event) {loadPage(event, "/listeRessources.fxml");
    }

    @FXML
    private void goRessourceDashboard(ActionEvent event) {
        loadPage(event, "/RessourceDashboard.fxml");
    }

    @FXML
    private void goCategories(ActionEvent event) {
        loadPage(event, "/CategorieList.fxml");
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
        User current = UserSession.getCurrentUser();
        if (current != null) {
            auditLogService.log(current.getEmail(), "LOGOUT", "User logged out from Prof dashboard");
        }
        UserSession.clear();
        loadPage(event, "/Login.fxml");
    }

    private void loadPage(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de navigation");
            alert.setHeaderText("Impossible d'ouvrir la page");
            alert.setContentText("Fichier FXML introuvable ou erreur d'ouverture : " + fxmlPath + "\n" + e.getMessage());
            alert.showAndWait();
        }
    }
}
