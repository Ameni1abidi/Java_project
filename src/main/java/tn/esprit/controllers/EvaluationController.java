package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.entities.Evaluation;
import tn.esprit.services.EvaluationService;

import java.io.IOException;

public class EvaluationController {

    @FXML private TableView<Evaluation> tableEvaluations;

    @FXML private TableColumn<Evaluation, Integer> colId;
    @FXML private TableColumn<Evaluation, Double> colNote;
    @FXML private TableColumn<Evaluation, String> colAppreciation;
    @FXML private TableColumn<Evaluation, Integer> colExamen;
    @FXML private TableColumn<Evaluation, Integer> colEleve;
    @FXML private TableColumn<Evaluation, Void> colActions;

    private ObservableList<Evaluation> list;
    private final EvaluationService service = new EvaluationService();

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

        colEleve.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getEleveId()).asObject());

        loadData();

        colActions.setCellFactory(param -> new TableCell<>() {

            private final Button btnDelete = new Button("Supprimer");
            private final Button btnEdit = new Button("Modifier");

            {
                btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 12;");
                btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 12;");

                btnDelete.setOnAction(event -> {
                    Evaluation e = getTableView().getItems().get(getIndex());
                    if (confirmDelete(e)) {
                        service.delete(e.getId());
                        loadData();
                    }
                });

                btnEdit.setOnAction(event -> {
                    Evaluation e = getTableView().getItems().get(getIndex());
                    openEditEvaluation(e);
                });
            }

            private final HBox pane = new HBox(8, btnEdit, btnDelete);

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                pane.setAlignment(Pos.CENTER);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadData() {
        list = FXCollections.observableArrayList(service.getAll());
        tableEvaluations.setItems(list);
    }

    private boolean confirmDelete(Evaluation evaluation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Supprimer l'évaluation #" + evaluation.getId() + " ?");
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    private void openEditEvaluation(Evaluation evaluation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CreateEvaluation.fxml"));
            Parent root = loader.load();

            CreateEvaluationController controller = loader.getController();
            controller.setEvaluation(evaluation);

            Stage stage = (Stage) tableEvaluations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @FXML
    private void goToCreateEvaluation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CreateEvaluation.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) tableEvaluations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
