package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.Evaluation;
import tn.esprit.services.EvaluationService;

public class CreateEvaluationController {

    @FXML private TextField txtNote;
    @FXML private TextField txtAppreciation;
    @FXML private ComboBox<Integer> cbEleve;
    @FXML private ComboBox<Integer> cbExamen;

    private final EvaluationService service = new EvaluationService();
    private Evaluation evaluationToEdit;

    @FXML
    public void initialize() {
        cbEleve.setItems(FXCollections.observableArrayList(1, 2, 3));
        cbExamen.setItems(FXCollections.observableArrayList(1, 2, 3));
    }

    public void setEvaluation(Evaluation evaluation) {
        if (evaluation == null) {
            return;
        }

        this.evaluationToEdit = evaluation;

        txtNote.setText(String.valueOf(evaluation.getNote()));
        txtAppreciation.setText(evaluation.getAppreciation());

        if (!cbEleve.getItems().contains(evaluation.getEleveId())) {
            cbEleve.getItems().add(evaluation.getEleveId());
        }
        cbEleve.setValue(evaluation.getEleveId());

        if (!cbExamen.getItems().contains(evaluation.getExamenId())) {
            cbExamen.getItems().add(evaluation.getExamenId());
        }
        cbExamen.setValue(evaluation.getExamenId());
    }

    @FXML
    private void handleSave() {
        try {
            Evaluation evaluation = (evaluationToEdit != null) ? evaluationToEdit : new Evaluation();

            evaluation.setNote(Double.parseDouble(txtNote.getText()));
            evaluation.setAppreciation(txtAppreciation.getText());
            evaluation.setEleveId(cbEleve.getValue());
            evaluation.setExamenId(cbExamen.getValue());

            if (evaluation.getId() == 0) {
                service.create(evaluation);
                showAlert("Succès", "Évaluation ajoutée !");
            } else {
                service.update(evaluation);
                showAlert("Succès", "Évaluation modifiée !");
            }
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


    @FXML
    private void goDashboard(ActionEvent event) {
        loadPage(event, "/ProfDashboard.fxml");
    }

    @FXML
    private void goForum(ActionEvent event) {
        loadPage(event, "/forum.fxml");
    }

    @FXML
    private void goCours(ActionEvent event) {
        loadPage(event, "/CoursList.fxml");
    }

    @FXML
    private void goRessources(ActionEvent event) {
        loadPage(event, "/listeRessources.fxml");
    }

    @FXML
    private void goCategories(ActionEvent event) {
        loadPage(event, "/CategorieList.fxml");
    }

    @FXML
    private void goExamens(ActionEvent event) {
        loadPage(event, "/ExamenView.fxml");
    }

    @FXML
    private void goResultats(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Resultats");
        alert.setHeaderText(null);
        alert.setContentText("La page resultats sera bientot disponible.");
        alert.showAndWait();
    }

    @FXML
    private void goLogout(ActionEvent event) {
        loadPage(event, "/Login.fxml");
    }

    private void loadPage(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
