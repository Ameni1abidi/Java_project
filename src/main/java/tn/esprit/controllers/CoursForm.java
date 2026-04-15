package tn.esprit.controllers;

import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.Cours;
import tn.esprit.services.CoursService;

import javafx.event.ActionEvent;
import java.sql.Date;

public class CoursForm {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private TextField niveauField;
    @FXML private DatePicker dateField;
    @FXML private TextField badgeField;

    @FXML private Label titreError;
    @FXML private Label descriptionError;
    @FXML private Label niveauError;
    @FXML private Label dateError;
    @FXML private Label badgeError;

    private CoursService service = new CoursService();
    private Cours currentCours;

    public void setCours(Cours c) {
        this.currentCours = c;

        if (c != null) {
            titreField.setText(c.getTitre());
            descriptionField.setText(c.getDescription());
            niveauField.setText(c.getNiveau());
            badgeField.setText(c.getBadge());
            dateField.setValue(c.getDateCreation().toLocalDate());
        }
    }

    @FXML
    void saveCours() {

        // RESET ERRORS
        titreError.setText("");
        descriptionError.setText("");
        niveauError.setText("");
        dateError.setText("");
        badgeError.setText("");

        boolean valid = true;

        String titre = titreField.getText();
        String description = descriptionField.getText();
        String niveau = niveauField.getText();
        String badge = badgeField.getText();
        var date = dateField.getValue();

        // ======================
        // TITRE
        // ======================
        if (titre == null || titre.trim().isEmpty()) {
            titreError.setText("Titre obligatoire");
            valid = false;
        } else if (titre.length() < 3) {
            titreError.setText("Min 3 caractères");
            valid = false;
        }

        // ======================
        // DESCRIPTION
        // ======================
        if (description == null || description.trim().isEmpty()) {
            descriptionError.setText("Description obligatoire");
            valid = false;
        } else if (description.length() < 10) {
            descriptionError.setText("Min 10 caractères");
            valid = false;
        }

        // ======================
        // NIVEAU
        // ======================
        if (niveau == null || niveau.trim().isEmpty()) {
            niveauError.setText("Niveau obligatoire");
            valid = false;
        }

        // ======================
        // DATE
        // ======================
        if (date == null) {
            dateError.setText("Date obligatoire");
            valid = false;
        }

        // ======================
        // BADGE (optional rule)
        // ======================
        if (badge != null && badge.length() > 20) {
            badgeError.setText("Badge trop long");
            valid = false;
        }

        if (!valid) return;

// ======================
// CREATE / UPDATE
// ======================
        if (currentCours == null) {

            Cours c = new Cours();

            c.setTitre(titre);
            c.setDescription(description);
            c.setNiveau(niveau);
            c.setBadge(badge);
            c.setDateCreation(Date.valueOf(date));

            service.ajouter(c);

        } else {

            currentCours.setTitre(titre);
            currentCours.setDescription(description);
            currentCours.setNiveau(niveau);
            currentCours.setBadge(badge);
            currentCours.setDateCreation(Date.valueOf(date));

            service.modifier(currentCours);
        }

        goToList();
    }

    @FXML
    void goToList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CoursList.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
