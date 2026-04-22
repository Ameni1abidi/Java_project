package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import tn.esprit.entities.Examen;
import tn.esprit.entities.Cours;
import tn.esprit.entities.User;
import tn.esprit.services.ExamenService;
import tn.esprit.services.CoursService;
import tn.esprit.services.UserService;

import java.io.IOException;
import java.sql.SQLException;

public class EditExamenController {

    @FXML private TextField titreField;
    @FXML private TextField typeField;
    @FXML private DatePicker dateField;
    @FXML private TextField dureeField;

    @FXML private ComboBox<Cours> coursCombo;
    @FXML private ComboBox<User> enseignantCombo;

    private final CoursService coursService = new CoursService();
    private final UserService userService = new UserService();
    private final ExamenService service = new ExamenService();

    private Examen examen;

    @FXML
    public void initialize() {

        // Cours
        coursCombo.setItems(FXCollections.observableArrayList(coursService.getAll()));

        // Enseignants
        try {
            enseignantCombo.setItems(FXCollections.observableArrayList(
                    userService.getAllUsers()
                            .stream()
                            .filter(u -> u.getRole() == User.Role.ROLE_PROF)
                            .toList()
            ));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // ✅ AFFICHAGE PROPRE COURS
        coursCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Cours item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitre());
            }
        });

        coursCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Cours item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitre());
            }
        });

        // ✅ AFFICHAGE PROPRE USER
        enseignantCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });

        enseignantCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });
    }

    public void setExamen(Examen examen) {
        this.examen = examen;

        titreField.setText(examen.getTitre());
        typeField.setText(examen.getType());
        dateField.setValue(examen.getDateExamen());
        dureeField.setText(String.valueOf(examen.getDuree()));

        // sélectionner cours
        coursCombo.getSelectionModel().select(
                coursCombo.getItems().stream()
                        .filter(c -> c.getId() == examen.getCoursId())
                        .findFirst().orElse(null)
        );

        // sélectionner enseignant
        enseignantCombo.getSelectionModel().select(
                enseignantCombo.getItems().stream()
                        .filter(u -> u.getId() == examen.getEnseignantId())
                        .findFirst().orElse(null)
        );
    }

    @FXML
    private void modifierExamen(ActionEvent event) {

        try {
            if (titreField.getText().isEmpty()
                    || typeField.getText().isEmpty()
                    || dateField.getValue() == null
                    || dureeField.getText().isEmpty()
                    || coursCombo.getValue() == null
                    || enseignantCombo.getValue() == null) {

                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setContentText("Remplir tous les champs !");
                a.showAndWait();
                return;
            }

            // UPDATE
            examen.setTitre(titreField.getText());
            examen.setType(typeField.getText());
            examen.setDateExamen(dateField.getValue());
            examen.setDuree(Integer.parseInt(dureeField.getText()));

            // ✅ IMPORTANT (corrigé ici)
            examen.setCoursId(coursCombo.getValue().getId());
            examen.setEnseignantId(enseignantCombo.getValue().getId());

            service.update(examen);

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setContentText("Examen modifié !");
            ok.showAndWait();

            // retour
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ExamenView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Durée invalide").show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ExamenView.fxml"));

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}