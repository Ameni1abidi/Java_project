package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class EvaluationController {

    @FXML private TableView<Evaluation> tableEvaluations;

    @FXML private TableColumn<Evaluation, Integer> colId;
    @FXML private TableColumn<Evaluation, Double> colNote;
    @FXML private TableColumn<Evaluation, String> colAppreciation;
    @FXML private TableColumn<Evaluation, Integer> colExamen;

    @FXML private TextField txtNote;
    @FXML private TextField txtAppreciation;
    @FXML private TextField txtExamenId;

    private ObservableList<Evaluation> list;
    private EvaluationService service = new EvaluationService();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());

        colNote.setCellValueFactory(data ->
                new javafx.beans.property.SimpleDoubleProperty(data.getValue().getNote()).asObject());

        colAppreciation.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getAppreciation()));

        colExamen.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getExamenId()).asObject());

        loadData();

        // select row
        tableEvaluations.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtNote.setText(String.valueOf(newVal.getNote()));
                txtAppreciation.setText(newVal.getAppreciation());
                txtExamenId.setText(String.valueOf(newVal.getExamenId()));
            }
        });
    }

    private void loadData() {
        list = FXCollections.observableArrayList(service.getAll());
        tableEvaluations.setItems(list);
    }

    // ===== ADD =====
    @FXML
    void handleAdd() {
        try {
            Evaluation e = new Evaluation();
            e.setNote(Double.parseDouble(txtNote.getText()));
            e.setAppreciation(txtAppreciation.getText());
            e.setExamenId(Integer.parseInt(txtExamenId.getText()));

            service.create(e);
            loadData();
            clearFields();

        } catch (Exception ex) {
            showAlert("Erreur", "Vérifie les champs !");
        }
    }

    // ===== UPDATE =====
    @FXML
    void handleUpdate() {
        Evaluation selected = tableEvaluations.getSelectionModel().getSelectedItem();

        if (selected != null) {
            try {
                selected.setNote(Double.parseDouble(txtNote.getText()));
                selected.setAppreciation(txtAppreciation.getText());
                selected.setExamenId(Integer.parseInt(txtExamenId.getText()));

                service.update(selected);
                loadData();

            } catch (Exception e) {
                showAlert("Erreur", "Erreur modification !");
            }
        }
    }

    // ===== DELETE =====
    @FXML
    void handleDelete() {
        Evaluation selected = tableEvaluations.getSelectionModel().getSelectedItem();

        if (selected != null) {
            service.delete(selected.getId());
            loadData();
        }
    }

    private void clearFields() {
        txtNote.clear();
        txtAppreciation.clear();
        txtExamenId.clear();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
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
