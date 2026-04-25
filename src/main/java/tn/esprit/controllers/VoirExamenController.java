package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.entities.Examen;
import tn.esprit.entities.Cours;
import tn.esprit.entities.User;
import tn.esprit.services.CoursService;
import tn.esprit.services.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VoirExamenController {

    @FXML private Label lblTitre;
    @FXML private Label lblType;
    @FXML private Label lblDate;
    @FXML private Label lblDuree;
    @FXML private Label lblCours;
    @FXML private Label lblEnseignant;

    private Examen examen;

    private final CoursService coursService = new CoursService();
    private final UserService userService = new UserService();

    public void setExamen(Examen examen) {
        this.examen = examen;

        // format date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        lblTitre.setText(examen.getTitre());
        lblType.setText(examen.getType());
        lblDate.setText(examen.getDateExamen() != null
                ? examen.getDateExamen().format(formatter)
                : "");

        lblDuree.setText(examen.getDuree() + " min");

        // ===== Cours name =====
        String coursName = coursService.getAll()
                .stream()
                .filter(c -> c.getId() == examen.getCoursId())
                .map(Cours::getTitre)
                .findFirst()
                .orElse("N/A");

        lblCours.setText(coursName);

        // ===== Enseignant name =====
        String enseignantName = null;
        try {
            enseignantName = userService.getAllUsers()
                    .stream()
                    .filter(u -> u.getId() == examen.getEnseignantId())
                    .map(User::getNom)
                    .findFirst()
                    .orElse("N/A");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        lblEnseignant.setText(enseignantName);
    }

    // ===== BACK =====
    @FXML
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ExamenView.fxml"));

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===== EDIT =====
    @FXML
    private void goEdit(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/EditExamen.fxml"));

            Parent root = loader.load();

            EditExamenController controller = loader.getController();
            controller.setExamen(examen);

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}