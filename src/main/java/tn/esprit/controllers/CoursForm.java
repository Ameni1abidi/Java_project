package tn.esprit.controllers;

import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.Cours;
import tn.esprit.services.CoursService;

import java.sql.Date;

public class CoursForm {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private TextField niveauField;
    @FXML private DatePicker dateField;
    @FXML private TextField badgeField;

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

        if (currentCours == null) {

            Cours c = new Cours(
                    titreField.getText(),
                    descriptionField.getText(),
                    niveauField.getText(),
                    Date.valueOf(dateField.getValue()),
                    "",
                    "",
                    badgeField.getText()
            );

            service.ajouter(c);

        } else {

            currentCours.setTitre(titreField.getText());
            currentCours.setDescription(descriptionField.getText());
            currentCours.setNiveau(niveauField.getText());
            currentCours.setBadge(badgeField.getText());
            currentCours.setDateCreation(Date.valueOf(dateField.getValue()));

            service.modifier(currentCours);
        }

        goToList();
    }

    @FXML
    void goToList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cours_list.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
