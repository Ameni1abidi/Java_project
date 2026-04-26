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
    @FXML private Button btnSave;

    @FXML
    public void initialize() {

        UserService userService = new UserService();
        ExamenService examenService = new ExamenService();

        try {
            cbUser.setItems(FXCollections.observableArrayList(
                    userService.getAllUsers().stream()
                            .filter(u -> u.getRole() == User.Role.ROLE_ETUDIANT)
                            .toList()
            ));

            cbExamen.setItems(FXCollections.observableArrayList(
                    examenService.getAll()
            ));

        } catch (Exception e) {
            e.printStackTrace();
        }

        // USER DISPLAY
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

        // EXAMEN DISPLAY
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

        // DISABLE BUTTON (PRO UX)
        btnSave.disableProperty().bind(
                txtNote.textProperty().isEmpty()
                        .or(txtAppreciation.textProperty().isEmpty())
                        .or(cbUser.valueProperty().isNull())
                        .or(cbExamen.valueProperty().isNull())
        );
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

            // 🔴 VALIDATION OBLIGATOIRE
            if (!validateForm()) {
                return;
            }

            Evaluation evaluation = (evaluationToEdit != null)
                    ? evaluationToEdit
                    : new Evaluation();

            double note = Double.parseDouble(txtNote.getText());

            evaluation.setNote(note);
            evaluation.setAppreciation(txtAppreciation.getText().trim());

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
            showAlert("Erreur", "Erreur inattendue !");
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

    private boolean validateForm() {

        StringBuilder errors = new StringBuilder();

        // NOTE
        String noteStr = txtNote.getText();

        if (noteStr == null || noteStr.trim().isEmpty()) {
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
        if (txtAppreciation.getText() == null || txtAppreciation.getText().trim().isEmpty()) {
            errors.append("- L'appréciation est obligatoire\n");
        }

        // USER
        if (cbUser.getValue() == null) {
            errors.append("- Veuillez sélectionner un étudiant\n");
        }

        // EXAMEN
        if (cbExamen.getValue() == null) {
            errors.append("- Veuillez sélectionner un examen\n");
        }

        // SHOW ERROR
        if (errors.length() > 0) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de validation");
            alert.setHeaderText("Veuillez corriger les champs");

            TextArea area = new TextArea(errors.toString());
            area.setEditable(false);
            area.setWrapText(true);

            alert.getDialogPane().setContent(area);

            alert.showAndWait();
            return false;
        }

        return true;
    }
}