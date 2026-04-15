package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.entities.Cours;
import tn.esprit.entities.Examen;
import tn.esprit.entities.User;
import tn.esprit.services.CoursService;
import tn.esprit.services.ExamenService;
import tn.esprit.services.UserService;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;

public class CreateExamenController {

    @FXML private TextField txtTitre;
    @FXML private ComboBox<String> cbType;
    @FXML private DatePicker dateExamen;
    @FXML private TextField txtDuree;
    @FXML private ComboBox<Cours> cbCours;
    @FXML private ComboBox<User> cbEnseignant;
    private String filePath;

    private ExamenService service = new ExamenService();
    @FXML
    public void initialize() {

        // TYPE
        cbType.getItems().addAll("Examen", "Contrôle");

        // SERVICES
        CoursService coursService = new CoursService();
        UserService userService = new UserService();

        // COURS
        cbCours.getItems().addAll(coursService.getAll());

        // ENSEIGNANTS (filtrer PROF)
        try {
            cbEnseignant.getItems().addAll(
                    userService.getAllUsers().stream()
                            .filter(u -> u.getRole() == User.Role.ROLE_PROF)
                            .toList()
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // AFFICHAGE NOM COURS
        cbCours.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Cours c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getTitre());
            }
        });

        cbCours.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Cours c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getTitre());
            }
        });

        // AFFICHAGE NOM ENSEIGNANT
        cbEnseignant.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : u.getNom());
            }
        });

        cbEnseignant.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : u.getNom());
            }
        });
    }
    @FXML
    void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            filePath = file.getAbsolutePath();
        }
    }

    @FXML

    void handleSave() {
        if (txtTitre.getText().isEmpty() ||
                cbType.getValue() == null ||
                dateExamen.getValue() == null ||
                txtDuree.getText().isEmpty() ||
                cbCours.getValue() == null ||
                cbEnseignant.getValue() == null) {

            new Alert(Alert.AlertType.WARNING, "Remplir tous les champs").show();
            return;
        }
        try {
            Examen e = new Examen();

            e.setTitre(txtTitre.getText());
            e.setContenu(filePath);
            e.setType(cbType.getValue());
            e.setDateExamen(dateExamen.getValue());
            e.setDuree(Integer.parseInt(txtDuree.getText()));

            // 🔥 IMPORTANT
            e.setCoursId(cbCours.getValue().getId());
            e.setEnseignantId(cbEnseignant.getValue().getId());

            service.create(e);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Examen ajouté !");
            alert.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String titre = txtTitre.getText();
        String dureeText = txtDuree.getText();
        String type = cbType.getValue();
        LocalDate date = dateExamen.getValue();
        Cours cours = cbCours.getValue();
        User enseignant = cbEnseignant.getValue();

        // 1️⃣ Vérification champs vides
        if (titre == null || titre.isBlank()) {
            showAlert("Le titre est obligatoire !");
            return;
        }

        if (type == null) {
            showAlert("Veuillez choisir le type d'examen !");
            return;
        }

        if (date == null) {
            showAlert("Veuillez choisir une date !");
            return;
        }

        if (dureeText == null || dureeText.isBlank()) {
            showAlert("La durée est obligatoire !");
            return;
        }

        if (cours == null) {
            showAlert("Veuillez choisir un cours !");
            return;
        }

        if (enseignant == null) {
            showAlert("Veuillez choisir un enseignant !");
            return;
        }

        if (filePath == null || filePath.isBlank()) {
            showAlert("Veuillez choisir un fichier !");
            return;
        }

        // 2️⃣ Vérification durée (doit être un nombre)
        int duree;
        try {
            duree = Integer.parseInt(dureeText);
            if (duree <= 0) {
                showAlert("La durée doit être supérieure à 0 !");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("La durée doit être un nombre !");
            return;
        }

        // 3️⃣ Création objet
        try {
            Examen e = new Examen();
            e.setTitre(titre);
            e.setType(type);
            e.setDateExamen(date);
            e.setDuree(duree);
            e.setContenu(filePath);
            e.setCoursId(cours.getId());
            e.setEnseignantId(enseignant.getId());

            service.create(e);

            new Alert(Alert.AlertType.INFORMATION, "Examen ajouté avec succès !").show();

            clearForm();

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Erreur lors de l'ajout !");
        }
    }

    @FXML
    void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ExamenView.fxml")); // adapte le nom
            Parent root = loader.load();
            txtTitre.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg).show();
    }
    private void clearForm() {
        txtTitre.clear();
        txtDuree.clear();
        cbType.getSelectionModel().clearSelection();
        dateExamen.setValue(null);
        cbCours.getSelectionModel().clearSelection();
        cbEnseignant.getSelectionModel().clearSelection();
        filePath = null;
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