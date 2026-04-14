package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import tn.esprit.entities.Examen;
import tn.esprit.services.ExamenService;

import java.time.LocalDate;

public class ExamenController {

    @FXML private TableView<Examen> tableExamens;

    @FXML private TableColumn<Examen, Integer> colId;
    @FXML private TableColumn<Examen, String> colTitre;
    @FXML private TableColumn<Examen, String> colFichier;
    @FXML private TableColumn<Examen, String> colType;
    @FXML private TableColumn<Examen, LocalDate> colDate;
    @FXML private TableColumn<Examen, Integer> colDuree;
    @FXML private TableColumn<Examen, Void> colActions;

    private ObservableList<Examen> list;
    private ExamenService service = new ExamenService();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());

        colTitre.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getTitre()));

        // bouton "Télécharger"
        colFichier.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty("Télécharger"));

        colType.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getType()));

        colDate.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getDateExamen()));

        colDuree.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getDuree()).asObject());

        loadData();
        addButtons();
    }

    private void loadData() {
        list = FXCollections.observableArrayList(service.getAll());
        tableExamens.setItems(list);
    }

    // ===== ACTION BUTTONS =====
    private void addButtons() {
        colActions.setCellFactory(param -> new TableCell<>() {

            private final Button btnVoir = new Button("Voir");
            private final Button btnEdit = new Button("Editer");
            private final HBox pane = new HBox(10, btnVoir, btnEdit);

            {
                btnVoir.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 8;");
                btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8;");

                // ===== VOIR =====
                btnVoir.setOnAction(event -> {
                    Examen e = getTableView().getItems().get(getIndex());
                    showAlert("Examen", e.toString());
                });

                // ===== EDIT =====
                btnEdit.setOnAction(event -> {
                    Examen e = getTableView().getItems().get(getIndex());
                    showAlert("Edit", "Modifier : " + e.getTitre());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    // ===== ALERT =====
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.show();
    }
    @FXML
    void goToCreateExamen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CreateExamen.fxml"));
            Parent root = loader.load();

            // récupérer la scène actuelle
            tableExamens.getScene().setRoot(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}