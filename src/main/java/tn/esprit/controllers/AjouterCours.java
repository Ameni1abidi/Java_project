package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.entities.Cours;
import tn.esprit.services.CoursService;

import java.sql.Date;

public class AjouterCours {

    @FXML
    private TextField titreField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField niveauField;

    @FXML
    private DatePicker dateField;

    @FXML
    private TextField badgeField;

    private CoursService service = new CoursService();

    @FXML
    void ajouterCours() {
        try {
            Cours c = new Cours(
                    titreField.getText(),
                    descriptionField.getText(),
                    niveauField.getText(),
                    Date.valueOf(dateField.getValue()),
                    "", // titre_traduit (optionnel)
                    "", // description_traduit
                    badgeField.getText()
            );

            service.ajouter(c);
            System.out.println("Cours ajouté !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
