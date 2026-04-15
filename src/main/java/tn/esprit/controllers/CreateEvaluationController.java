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
import tn.esprit.entities.Examen;
import tn.esprit.entities.User;
import tn.esprit.services.EvaluationService;
import tn.esprit.services.ExamenService;
import tn.esprit.services.UserService;

public class CreateEvaluationController {

    @FXML private TextField txtNote;
    @FXML private TextField txtAppreciation;

    @FXML private ComboBox<User> cbUser;     // 🔥 user au lieu eleve
    @FXML private ComboBox<Examen> cbExamen; // 🔥 objet examen

    private final EvaluationService service = new EvaluationService();
    private Evaluation evaluationToEdit;

    @FXML
    public void initialize() {

        UserService userService = new UserService();
        ExamenService examenService = new ExamenService();

        try {
            // 🔥 USERS (tu peux filtrer si besoin)
            UserService UserService = new UserService();

            try {
                cbUser.setItems(FXCollections.observableArrayList(
                        userService.getAllUsers().stream()
                                .filter(u -> u.getRole() == User.Role.ROLE_ETUDIANT)
                                .toList()
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 🔥 EXAMENS
        cbExamen.setItems(FXCollections.observableArrayList(
                examenService.getAll()
        ));

        // ================= AFFICHAGE USER =================
        cbUser.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : u.getNom());
            }
        });

        cbUser.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : u.getNom());
            }
        });

        // ================= AFFICHAGE EXAMEN =================
        cbExamen.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Examen ex, boolean empty) {
                super.updateItem(ex, empty);
                setText(empty || ex == null ? null : ex.getTitre());
            }
        });

        cbExamen.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Examen ex, boolean empty) {
                super.updateItem(ex, empty);
                setText(empty || ex == null ? null : ex.getTitre());
            }
        });
    }

    // ================= EDIT =================
    public void setEvaluation(Evaluation evaluation) {
        if (evaluation == null) return;

        this.evaluationToEdit = evaluation;

        txtNote.setText(String.valueOf(evaluation.getNote()));
        txtAppreciation.setText(evaluation.getAppreciation());

        // 🔥 sélectionner user
        cbUser.getItems().stream()
                .filter(u -> u.getId() == evaluation.getEleveId())
                .findFirst()
                .ifPresent(cbUser::setValue);

        // 🔥 sélectionner examen
        cbExamen.getItems().stream()
                .filter(ex -> ex.getId() == evaluation.getExamenId())
                .findFirst()
                .ifPresent(cbExamen::setValue);
    }

    // ================= SAVE =================
    @FXML
    private void handleSave() {
        try {
            if (cbUser.getValue() == null || cbExamen.getValue() == null) {
                showAlert("Erreur", "Choisir utilisateur et examen !");
                return;
            }

            Evaluation evaluation = (evaluationToEdit != null)
                    ? evaluationToEdit
                    : new Evaluation();

            evaluation.setNote(Double.parseDouble(txtNote.getText()));
            evaluation.setAppreciation(txtAppreciation.getText());

            // 🔥 IMPORTANT
            evaluation.setEleveId(cbUser.getValue().getId());
            evaluation.setExamenId(cbExamen.getValue().getId());

            if (evaluation.getId() == 0) {
                service.create(evaluation);
                showAlert("Succès", "Évaluation ajoutée !");
            } else {
                service.update(evaluation);
                showAlert("Succès", "Évaluation modifiée !");
            }

            goBack();

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Vérifie les champs !");
        }
    }

    // ================= NAV =================
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

    // ================= MENU =================
    @FXML
    private void goDashboard(ActionEvent event) { loadPage(event, "/ProfDashboard.fxml"); }

    @FXML
    private void goForum(ActionEvent event) { loadPage(event, "/forum.fxml"); }

    @FXML
    private void goCours(ActionEvent event) { loadPage(event, "/CoursList.fxml"); }

    @FXML
    private void goRessources(ActionEvent event) { loadPage(event, "/listeRessources.fxml"); }

    @FXML
    private void goCategories(ActionEvent event) { loadPage(event, "/CategorieList.fxml"); }

    @FXML
    private void goExamens(ActionEvent event) { loadPage(event, "/ExamenView.fxml"); }

    @FXML
    private void goLogout(ActionEvent event) { loadPage(event, "/Login.fxml"); }

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
    private void goResultats(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Resultats");
        alert.setHeaderText(null);
        alert.setContentText("La page resultats sera bientot disponible.");
        alert.showAndWait();
    }
}