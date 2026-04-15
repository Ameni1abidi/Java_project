package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent; // ✅ الصحيح

import tn.esprit.entities.Cours;
import tn.esprit.services.CoursService;

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

    // =====================
    // SET DATA (EDIT MODE)
    // =====================
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

    // =====================
    // SAVE
    // =====================
    @FXML
    void saveCours(ActionEvent event) {  // ✅ لازم event

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

        // VALIDATION
        if (titre == null || titre.trim().isEmpty()) {
            titreError.setText("Titre obligatoire");
            valid = false;
        } else if (titre.length() < 3) {
            titreError.setText("Min 3 caractères");
            valid = false;
        }

        if (description == null || description.trim().isEmpty()) {
            descriptionError.setText("Description obligatoire");
            valid = false;
        } else if (description.length() < 10) {
            descriptionError.setText("Min 10 caractères");
            valid = false;
        }

        if (niveau == null || niveau.trim().isEmpty()) {
            niveauError.setText("Niveau obligatoire");
            valid = false;
        }

        if (date == null) {
            dateError.setText("Date obligatoire");
            valid = false;
        }

        if (badge != null && badge.length() > 20) {
            badgeError.setText("Badge trop long");
            valid = false;
        }

        if (!valid) return;

        // SAVE / UPDATE
        if (currentCours == null) {

            Cours c = new Cours(
                    titre,
                    description,
                    niveau,
                    Date.valueOf(date),
                    "",
                    "",
                    badge
            );

            service.ajouter(c);

        } else {

            currentCours.setTitre(titre);
            currentCours.setDescription(description);
            currentCours.setNiveau(niveau);
            currentCours.setBadge(badge);
            currentCours.setDateCreation(Date.valueOf(date));

            service.modifier(currentCours);
        }

        // 🔁 الرجوع للـ list
        goToList(event);  // ✅ مهم برشا
    }

    // =====================
    // NAVIGATION
    // =====================
    @FXML
    void goToList(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CoursList.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
