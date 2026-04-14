package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import tn.esprit.entities.Examen;
import tn.esprit.entities.Cours;
import tn.esprit.entities.User;
import tn.esprit.services.ExamenService;
import tn.esprit.services.CoursService;
import tn.esprit.services.UserService;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

public class ExamenController {

    @FXML private TableView<Examen> tableExamens;
    @FXML private TableColumn<Examen, Integer> colId;
    @FXML private TableColumn<Examen, String> colTitre;
    @FXML private TableColumn<Examen, String> colFichier;
    @FXML private TableColumn<Examen, String> colType;
    @FXML private TableColumn<Examen, LocalDate> colDate;
    @FXML private TableColumn<Examen, Integer> colDuree;
    @FXML private TableColumn<Examen, Void> colActions;
    @FXML private TableColumn<Examen, String> colCours;
    @FXML private TableColumn<Examen, String> colEnseignant;

    private ObservableList<Examen> list;
    private final ExamenService service = new ExamenService();
    private final CoursService coursService = new CoursService();
    private final UserService userService = new UserService();

    // Maps pour lookup rapide
    private Map<Integer, String> coursMap;
    private Map<Integer, String> enseignantMap;

    @FXML
    public void initialize() {
        // Charger les maps une seule fois
        coursMap = coursService.getAll().stream()
                .collect(Collectors.toMap(Cours::getId, Cours::getTitre));

        enseignantMap = userService.getAll().stream()
                .filter(u -> u.getRole() == User.Role.ROLE_PROF)
                .collect(Collectors.toMap(User::getId, User::getNom));

        colId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());

        colTitre.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getTitre()));

        colFichier.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty("Télécharger"));

        colType.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getType()));

        colDate.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getDateExamen()));

        colDuree.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getDuree()).asObject());

        // ✅ Afficher le titre du cours au lieu de l'ID
        colCours.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        coursMap.getOrDefault(data.getValue().getCoursId(), "N/A")
                ));

        // ✅ Afficher le nom de l'enseignant au lieu de l'ID
        colEnseignant.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        enseignantMap.getOrDefault(data.getValue().getEnseignantId(), "N/A")
                ));

        loadData();
        addButtons();
    }

    private void loadData() {
        list = FXCollections.observableArrayList(service.getAll());
        tableExamens.setItems(list);
    }

    private void addButtons() {
        colActions.setCellFactory(param -> new TableCell<>() {

            private final Button btnVoir = new Button("Voir");
            private final Button btnEdit = new Button("Editer");
            private final HBox pane = new HBox(10, btnVoir, btnEdit);

            {
                btnVoir.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 8;");
                btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8;");

                btnVoir.setOnAction(event -> {
                    Examen e = getTableView().getItems().get(getIndex());
                    showAlert("Examen", e.toString());
                });

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
            tableExamens.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}