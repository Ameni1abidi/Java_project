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
import tn.esprit.entities.Examen;
import tn.esprit.entities.Cours;
import tn.esprit.entities.User;
import tn.esprit.services.CalendarService;
import tn.esprit.services.ExamenService;
import tn.esprit.services.CoursService;
import tn.esprit.services.UserService;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

public class ExamenController {

    @FXML
    private TableView<Examen> tableExamens;
    @FXML
    private TableColumn<Examen, Integer> colId;
    @FXML
    private TableColumn<Examen, String> colTitre;
    @FXML
    private TableColumn<Examen, String> colFichier;
    @FXML
    private TableColumn<Examen, String> colType;
    @FXML
    private TableColumn<Examen, LocalDate> colDate;
    @FXML
    private TableColumn<Examen, Integer> colDuree;
    @FXML
    private TableColumn<Examen, Void> colActions;
    @FXML
    private TableColumn<Examen, String> colCours;
    @FXML
    private TableColumn<Examen, String> colEnseignant;
    @FXML
    private TextField searchField;
    private ObservableList<Examen> list;
    private final ExamenService service = new ExamenService();
    private final CoursService coursService = new CoursService();
    private final UserService userService = new UserService();



    private Map<Integer, String> coursMap;
    private Map<Integer, String> enseignantMap;

    @FXML
    public void initialize() {

        try {
            // ── Cours ─────────────────────────────
            coursMap = coursService.getAll().stream()
                    .collect(Collectors.toMap(Cours::getId, Cours::getTitre));

            // ── Enseignants ───────────────────────
            enseignantMap = userService.getAllUsers().stream()
                    .filter(u -> u.getRole() == User.Role.ROLE_PROF)
                    .collect(Collectors.toMap(User::getId, User::getNom));

        } catch (Exception e) {
            e.printStackTrace();
        }


        colTitre.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getTitre()));

        colType.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getType()));

        colDate.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        data.getValue().getDateExamen()));

        colDuree.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(
                        data.getValue().getDuree()).asObject());

        // ── Cours ────────────────────────────────────────────────────────────
        colCours.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        coursMap.getOrDefault(data.getValue().getCoursId(), "N/A")));

        // ── Enseignant
        colEnseignant.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        enseignantMap.getOrDefault(data.getValue().getEnseignantId(), "N/A")
                ));

        // ── Fichier cliquable ────────────────────────────────────────────────
        colFichier.setCellFactory(col -> new TableCell<>() {
            private final Hyperlink link = new Hyperlink("Télécharger");

            {
                link.setStyle("-fx-text-fill: #1a73e8;");
                link.setOnAction(e -> {
                    Examen ex = getTableView().getItems().get(getIndex());
                    showAlert("Télécharger", "Fichier de : " + ex.getTitre());
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : link);
            }
        });

        // ── Lignes alternées ─────────────────────────────────────────────────
        tableExamens.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Examen item, boolean empty) {
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

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                tableExamens.setItems(FXCollections.observableArrayList(service.getAll()));
            } else {
                filtrerParTitre(newValue);
            }
        });

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
            private final Button btnDelete = new Button("Supprimer");
            private final HBox pane = new HBox(16, btnVoir, btnEdit, btnDelete);

            {
                pane.setAlignment(javafx.geometry.Pos.CENTER);

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
                        "-fx-background-color: #a54e4e;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 6;" +
                                "-fx-padding: 6 16;" +
                                "-fx-min-width: 90;"
                );

                btnVoir.setOnAction(event -> {
                    try {
                        Examen e = getTableView().getItems().get(getIndex());

                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/VoirExamen.fxml"));

                        Parent root = loader.load();

                        VoirExamenController controller = loader.getController();
                        controller.setExamen(e);

                        Stage stage = (Stage) ((Node) event.getSource())
                                .getScene().getWindow();

                        stage.setScene(new Scene(root));
                        stage.show();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                btnEdit.setOnAction(event -> {
                    try {
                        Examen e = getTableView().getItems().get(getIndex());

                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/EditExamen.fxml"));

                        Parent root = loader.load();

                        // récupérer le controller
                        EditExamenController controller = loader.getController();

                        // envoyer l'objet à modifier
                        controller.setExamen(e);

                        // changer la scène
                        Stage stage = (Stage) ((Node) event.getSource())
                                .getScene().getWindow();

                        stage.setScene(new Scene(root));
                        stage.show();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                btnDelete.setOnAction(event -> {

                    Examen e = getTableView().getItems().get(getIndex());

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmation");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Voulez-vous supprimer cet examen ?");

                    if (confirm.showAndWait().get() == ButtonType.OK) {

                        service.delete(e.getId()); // 🔥 DELETE DB

                        loadData(); // refresh table

                        Alert ok = new Alert(Alert.AlertType.INFORMATION);
                        ok.setTitle("Succès");
                        ok.setContentText("Examen supprimé !");
                        ok.showAndWait();
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
        alert.setContentText(msg);
        alert.show();
    }

    @FXML
    void goToCreateExamen() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/CreateExamen.fxml"));
            Parent root = loader.load();
            tableExamens.getScene().setRoot(root);
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
    private void goEvaluations(ActionEvent event) {
        loadPage(event, "/EvaluationView.fxml");
    }

    @FXML
    private void goResultats(ActionEvent event) {
        showAlert("Resultats", "La page resultats sera bientot disponible.");
    }

    @FXML
    private void goLogout(ActionEvent event) {
        loadPage(event, "/Login.fxml");
    }

    @FXML
    public void goStatistique(ActionEvent event) {
        loadPage(event, "/statistique.fxml");
    }

    @FXML
    public void goIa(ActionEvent event) {
        loadPage(event, "/AIView.fxml");
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

    private void filtrerParTitre(String titre) {
        list = FXCollections.observableArrayList(
                service.rechercherParTitre(titre)
        );
        tableExamens.setItems(list);
    }

    @FXML
    private void rechercherExamen() {
        String titre = searchField.getText();

        if (titre == null || titre.isEmpty()) {
            tableExamens.setItems(FXCollections.observableArrayList(service.getAll()));
        } else {
            filtrerParTitre(titre);
        }
    }

    private boolean validateForm(String titre, String type, String dureeStr, LocalDate date) {

        StringBuilder errors = new StringBuilder();

        // TITRE
        if (titre == null || titre.trim().isEmpty()) {
            errors.append("- Le titre est obligatoire\n");
        } else if (titre.length() < 3) {
            errors.append("- Le titre doit contenir au moins 3 caractères\n");
        }

        // TYPE
        if (type == null || type.trim().isEmpty()) {
            errors.append("- Le type est obligatoire\n");
        }

        // DATE
        if (date == null) {
            errors.append("- La date est obligatoire\n");
        } else if (date.isBefore(LocalDate.now())) {
            errors.append("- La date ne peut pas être dans le passé\n");
        }

        // DUREE
        if (dureeStr == null || dureeStr.trim().isEmpty()) {
            errors.append("- La durée est obligatoire\n");
        } else {
            try {
                int duree = Integer.parseInt(dureeStr);

                if (duree <= 0) {
                    errors.append("- La durée doit être positive\n");
                }

            } catch (NumberFormatException e) {
                errors.append("- La durée doit être un nombre entier\n");
            }
        }

        // AFFICHAGE ERREURS
        if (errors.length() > 0) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de validation");
            alert.setHeaderText("Veuillez corriger les erreurs :");

            TextArea area = new TextArea(errors.toString());
            area.setEditable(false);
            area.setWrapText(true);

            alert.getDialogPane().setContent(area);
            alert.showAndWait();

            return false;
        }

        return true;
    }


    @FXML
    private void handleAddToCalendar() {

        Examen selected = tableExamens.getSelectionModel().getSelectedItem();

        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attention");
            alert.setContentText("Veuillez sélectionner un examen !");
            alert.show();
            return;
        }

        try {
            CalendarService calendarService = new CalendarService();

            String eventId = calendarService.createExamEvent(selected);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Google Calendar");
            alert.setContentText("Examen ajouté au Calendar ! ID: " + eventId);
            alert.show();

        } catch (Exception e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

}
