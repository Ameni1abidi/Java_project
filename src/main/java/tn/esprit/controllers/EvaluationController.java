package tn.esprit.controllers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import tn.esprit.entities.Evaluation;
import tn.esprit.services.EvaluationService;

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
                new SimpleIntegerProperty(data.getValue().getId()).asObject());

        colNote.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().getNote()).asObject());

        colAppreciation.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getAppreciation()));

        colExamen.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getExamenId()).asObject());

        colEleve.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getEleveId()).asObject());

        tableEvaluations.setRowFactory(tv -> new TableRow<Evaluation>() {
            @Override
            protected void updateItem(Evaluation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    setStyle(getIndex() % 2 == 0
                            ? "-fx-background-color: white;"
                            : "-fx-background-color: #fafafa;");
                }
            }
        });

        loadData();
        addButtons();
    }

    private void loadData() {
        list = FXCollections.observableArrayList(service.getAll());
        tableEvaluations.setItems(list);
    }

    @FXML
    void goToCreateEvaluation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CreateEvaluation.fxml"));
            Parent root = loader.load();
            tableEvaluations.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addButtons() {
        if (colActions == null) {
            return;
        }

        colActions.setCellFactory(param -> new TableCell<Evaluation, Void>() {

            private final Button btnVoir = new Button("Voir");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox pane = new HBox(8, btnVoir, btnSupprimer);

            {
                pane.setAlignment(javafx.geometry.Pos.CENTER);

                btnVoir.setStyle(
                        "-fx-background-color: white; -fx-text-fill: #555;" +
                                "-fx-border-color: #ccc; -fx-border-radius: 6;" +
                                "-fx-background-radius: 6; -fx-padding: 4 12;");

                btnSupprimer.setStyle(
                        "-fx-background-color: #e53935; -fx-text-fill: white;" +
                                "-fx-background-radius: 6; -fx-padding: 4 12;");

                btnVoir.setOnAction(event -> {
                    Evaluation e = getTableView().getItems().get(getIndex());
                    showAlert("Évaluation", e.toString());
                });

                btnSupprimer.setOnAction(event -> {
                    Evaluation e = getTableView().getItems().get(getIndex());

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmation");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Supprimer l'évaluation #" + e.getId() + " ?");

                    if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                        service.delete(e.getId());
                        loadData();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}
