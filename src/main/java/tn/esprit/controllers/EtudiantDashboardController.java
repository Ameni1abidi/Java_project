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
import tn.esprit.entities.User;
import tn.esprit.services.AuditLogService;
import tn.esprit.utils.UserSession;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EtudiantDashboardController {
    private final AuditLogService auditLogService = new AuditLogService();

    @FXML
    private Label dateLabel;
    @FXML
    private ListView<String> planningList;

    @FXML
    public void initialize() {
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy")));
        planningList.setItems(FXCollections.observableArrayList(
                "Lun 08:30 - Cours Java",
                "Mar 13:00 - TD Base de donnees",
                "Mer 10:30 - Mini projet",
                "Jeu 09:00 - Revision examens",
                "Ven 15:00 - Atelier Forum"
        ));
    }

    @FXML
    public void goCoursList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StudentCours.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) planningList.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
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
    private void goFavorites(ActionEvent event) {
        loadPage(event, "/StudentFavorites.fxml");
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
            auditLogService.log(current.getEmail(), "LOGOUT", "User logged out from Etudiant dashboard");
        }
        UserSession.clear();
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
