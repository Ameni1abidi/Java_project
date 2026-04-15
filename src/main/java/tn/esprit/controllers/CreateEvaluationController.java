package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import tn.esprit.entities.Evaluation;
import tn.esprit.services.EvaluationService;

public class CreateEvaluationController {

    @FXML private TextField txtNote;
    @FXML private TextField txtAppreciation;
    @FXML private ComboBox<Integer> cbEleve;
    @FXML private ComboBox<Integer> cbExamen;

    private final EvaluationService service = new EvaluationService();

    @FXML
    public void initialize() {
        cbEleve.setItems(FXCollections.observableArrayList(1, 2, 3));
        cbExamen.setItems(FXCollections.observableArrayList(1, 2, 3));
    }

    @FXML
    private void handleSave() {
        try {
            Evaluation evaluation = new Evaluation();

            evaluation.setNote(Double.parseDouble(txtNote.getText()));
            evaluation.setAppreciation(txtAppreciation.getText());
            evaluation.setEleveId(cbEleve.getValue());
            evaluation.setExamenId(cbExamen.getValue());

            service.create(evaluation);

            showAlert("Succès", "Évaluation ajoutée !");
            goBack();
        } catch (Exception ex) {
            showAlert("Erreur", "Vérifie les champs !");
        }
    }

    @FXML
    private void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/EvaluationView.fxml"));
            txtNote.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}
