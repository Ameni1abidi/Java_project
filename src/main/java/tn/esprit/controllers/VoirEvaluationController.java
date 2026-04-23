package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.entities.Evaluation;
import tn.esprit.entities.Examen;
import tn.esprit.entities.User;
import tn.esprit.services.ExamenService;
import tn.esprit.services.UserService;

import java.sql.SQLException;
import java.util.Optional;

public class VoirEvaluationController {

    @FXML private Label lblId;
    @FXML private Label lblNote;
    @FXML private Label lblAppreciation;
    @FXML private Label lblExamen;
    @FXML private Label lblEleve;

    private Evaluation evaluation;

    private final ExamenService examenService = new ExamenService();
    private final UserService userService = new UserService();

    // ================= SET DATA =================
    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
        afficherDetails();
    }

    // ================= DISPLAY =================
    private void afficherDetails() {

        if (evaluation == null) return;

        // ID
        lblId.setText(String.valueOf(evaluation.getId()));

        // NOTE
        lblNote.setText(String.valueOf(evaluation.getNote()));

        // APPRECIATION
        lblAppreciation.setText(
                Optional.ofNullable(evaluation.getAppreciation())
                        .filter(s -> !s.isBlank())
                        .orElse("N/A")
        );

        // ================= EXAMEN (nom au lieu ID) =================
        String examenTitre = examenService.getAll()
                .stream()
                .filter(e -> e.getId() == evaluation.getExamenId())
                .map(Examen::getTitre)
                .findFirst()
                .orElse("N/A");

        lblExamen.setText(examenTitre);

        // ================= ELEVE (nom au lieu ID) =================
        try {
            String eleveNom = userService.getAllUsers()
                    .stream()
                    .filter(u -> u.getId() == evaluation.getEleveId())
                    .map(User::getNom)
                    .findFirst()
                    .orElse("N/A");

            lblEleve.setText(eleveNom);

        } catch (SQLException e) {
            lblEleve.setText("N/A");
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/EvaluationView.fxml"));

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goEdit(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/CreateEvaluation.fxml"));

            Parent root = loader.load();

            CreateEvaluationController controller = loader.getController();
            controller.setEvaluation(evaluation); // IMPORTANT

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}