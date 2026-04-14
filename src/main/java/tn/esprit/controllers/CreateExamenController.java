package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import tn.esprit.entities.Examen;
import tn.esprit.services.ExamenService;

import java.io.File;
import java.time.LocalDate;

public class CreateExamenController {

    @FXML private TextField txtTitre;
    @FXML private ComboBox<String> cbType;
    @FXML private DatePicker dateExamen;
    @FXML private TextField txtDuree;

    private String filePath;

    private ExamenService service = new ExamenService();

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
        try {
            Examen e = new Examen();
            e.setTitre(txtTitre.getText());
            e.setContenu(filePath);
            e.setType(cbType.getValue());
            e.setDateExamen(dateExamen.getValue());
            e.setDuree(Integer.parseInt(txtDuree.getText()));

            service.create(e);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Examen ajouté !");
            alert.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}