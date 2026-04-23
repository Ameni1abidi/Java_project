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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.entities.Evaluation;
import tn.esprit.entities.Examen;
import tn.esprit.entities.User;
import tn.esprit.services.EvaluationService;
import tn.esprit.services.ExamenService;
import tn.esprit.services.UserService;

import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;

public class EvaluationController {

    // ================= TABLE =================
    @FXML private TableView<Evaluation> tableEvaluations;

    @FXML private TableColumn<Evaluation, Integer> colId;
    @FXML private TableColumn<Evaluation, Double> colNote;
    @FXML private TableColumn<Evaluation, String> colAppreciation;
    @FXML private TableColumn<Evaluation, String> colExamen;
    @FXML private TableColumn<Evaluation, String> colEleve;
    @FXML private TableColumn<Evaluation, Void> colActions;

    @FXML private TextField searchField;

    private ObservableList<Evaluation> list;

    // ================= SERVICES =================
    private final EvaluationService service = new EvaluationService();
    private final ExamenService examenService = new ExamenService();
    private final UserService userService = new UserService();

    // ================= MAPS =================
    private Map<Integer, String> examenMap;
    private Map<Integer, String> eleveMap;

    // ================= INIT =================
    @FXML
    public void initialize() {

        // MAP EXAMEN
        examenMap = examenService.getAll()
                .stream()
                .collect(Collectors.toMap(Examen::getId, Examen::getTitre));

        // MAP ELEVE
        try {
            eleveMap = userService.getAllUsers()
                    .stream()
                    .filter(u -> u.getRole() == User.Role.ROLE_ETUDIANT)
                    .collect(Collectors.toMap(User::getId, User::getNom));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // ================= COLUMNS =================


        colNote.setCellValueFactory(d ->
                new javafx.beans.property.SimpleDoubleProperty(d.getValue().getNote()).asObject()
        );

        colAppreciation.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getAppreciation())
        );

        colExamen.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        examenMap.getOrDefault(d.getValue().getExamenId(), "N/A"))
        );

        colEleve.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        eleveMap.getOrDefault(d.getValue().getEleveId(), "N/A"))
        );

        loadData();
        addButtons();

        // SEARCH LIVE
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> rechercherEvaluation());
        }
    }

    // ================= LOAD DATA =================
    private void loadData() {
        list = FXCollections.observableArrayList(service.getAll());
        tableEvaluations.setItems(list);
    }

    // ================= ACTION BUTTONS =================
    private void addButtons() {

        colActions.setCellFactory(param -> new TableCell<>() {

            private final Button btnVoir = new Button("Voir");
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");

            private final HBox pane = new HBox(10, btnVoir, btnEdit, btnDelete);

            {
                pane.setStyle("-fx-alignment: CENTER;");

                btnVoir.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-text-fill: #555;" +
                                "-fx-border-color: #ccc;" +
                                "-fx-border-radius: 6;" +
                                "-fx-background-radius: 6;" +
                                "-fx-padding: 6 16;" +
                                "-fx-min-width: 70;"
                );

                btnEdit.setStyle(
                        "-fx-background-color: #4CAF50;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 6;" +
                                "-fx-padding: 6 16;" +
                                "-fx-min-width: 70;"
                );

                btnDelete.setStyle(
                        "-fx-background-color: #ef130c;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 6;" +
                                "-fx-padding: 6 16;" +
                                "-fx-min-width: 90;"
                );

                // ===== VIEW =====
                btnVoir.setOnAction(event -> {
                    Evaluation e = getTableView().getItems().get(getIndex());
                    ouvrirVoirEvaluation(e, event);
                });

                // ===== EDIT =====
                btnEdit.setOnAction(event -> {
                    Evaluation e = getTableView().getItems().get(getIndex());
                    openEdit(e);
                });

                // ===== DELETE =====
                btnDelete.setOnAction(event -> {
                    Evaluation e = getTableView().getItems().get(getIndex());

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation");
                    alert.setContentText("Supprimer cette évaluation ?");

                    if (alert.showAndWait().get() == ButtonType.OK) {
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

    // ================= VIEW PAGE =================
    private void ouvrirVoirEvaluation(Evaluation e, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/VoirEvaluation.fxml")
            );

            Parent root = loader.load();

            VoirEvaluationController controller = loader.getController();
            controller.setEvaluation(e);

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ================= EDIT =================
    private void openEdit(Evaluation evaluation) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/CreateEvaluation.fxml")
            );

            Parent root = loader.load();

            CreateEvaluationController controller = loader.getController();
            controller.setEvaluation(evaluation);

            Stage stage = (Stage) tableEvaluations.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= SEARCH =================
    public void rechercherEvaluation() {

        String keyword = searchField.getText();

        if (keyword == null || keyword.isEmpty()) {
            loadData();
            return;
        }

        String lower = keyword.toLowerCase();

        list = FXCollections.observableArrayList(
                service.getAll().stream()
                        .filter(e -> {
                            String nom = eleveMap.get(e.getEleveId());
                            return nom != null && nom.toLowerCase().contains(lower);
                        })
                        .collect(Collectors.toList())
        );

        tableEvaluations.setItems(list);
    }

    // ================= NAVIGATION =================
    @FXML private void goDashboard(ActionEvent e) { loadPage(e, "/ProfDashboard.fxml"); }
    @FXML private void goForum(ActionEvent e) { loadPage(e, "/forum.fxml"); }
    @FXML private void goCours(ActionEvent e) { loadPage(e, "/CoursList.fxml"); }
    @FXML private void goRessources(ActionEvent e) { loadPage(e, "/listeRessources.fxml"); }
    @FXML private void goCategories(ActionEvent e) { loadPage(e, "/CategorieList.fxml"); }
    @FXML private void goExamens(ActionEvent e) { loadPage(e, "/ExamenView.fxml"); }

    private void loadPage(ActionEvent e, String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ================= CREATE =================
    @FXML
    private void goToCreateEvaluation() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/CreateEvaluation.fxml")
            );

            Stage stage = (Stage) tableEvaluations.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateEvaluation(String noteStr, String appreciation, Integer examenId, Integer eleveId) {

        StringBuilder errors = new StringBuilder();

        // NOTE
        if (noteStr == null || noteStr.isEmpty()) {
            errors.append("- La note est obligatoire\n");
        } else {
            try {
                double note = Double.parseDouble(noteStr);

                if (note < 0 || note > 20) {
                    errors.append("- La note doit être entre 0 et 20\n");
                }

            } catch (NumberFormatException e) {
                errors.append("- La note doit être un nombre valide\n");
            }
        }

        // APPRECIATION
        if (appreciation == null || appreciation.trim().isEmpty()) {
            errors.append("- L'appréciation est obligatoire\n");
        }

        // EXAMEN
        if (examenId == null) {
            errors.append("- Veuillez sélectionner un examen\n");
        }

        // ELEVE
        if (eleveId == null) {
            errors.append("- Veuillez sélectionner un élève\n");
        }

        // SHOW ERROR
        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de validation");
            alert.setHeaderText("Veuillez corriger les erreurs :");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }
}