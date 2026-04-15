package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.entities.Chapitre;
import tn.esprit.services.ChapitreService;

import java.io.File;

public class ChapitreForm {

    @FXML private TextField titreField;
    @FXML private TextField ordreField;
    @FXML private ChoiceBox<String> typeContenuField;
    @FXML private TextArea contenuTexteField;
    @FXML private TextField contenuFichierField;
    @FXML private TextField dureeField;
    @FXML private TextField coursIdField;

    private final ChapitreService service = new ChapitreService();

    private int coursId;
    private Chapitre chapitreToEdit;

    // =========================
    // INIT DATA
    // =========================
    public void initData(Chapitre ch, int coursId) {

        this.coursId = coursId;
        this.chapitreToEdit = ch;

        coursIdField.setText(String.valueOf(coursId));

        if (ch != null) {
            titreField.setText(ch.getTitre());
            ordreField.setText(String.valueOf(ch.getOrdre()));
            typeContenuField.setValue(ch.getTypeContenu());
            contenuTexteField.setText(ch.getContenuTexte());
            contenuFichierField.setText(ch.getContenuFichier());
            dureeField.setText(String.valueOf(ch.getDureeEstimee()));
        }
    }

    // =========================
    // INIT UI
    // =========================
    @FXML
    public void initialize() {
        typeContenuField.getItems().addAll("PDF", "VIDEO", "IMAGE", "TEXT");
    }

    // =========================
    // SAVE
    // =========================
    @FXML private Label titreError;
    @FXML private Label ordreError;
    @FXML private Label typeError;
    @FXML private Label contenuError;
    @FXML private Label dureeError;
    @FXML
    void saveChapitre() {

        clearErrors();

        boolean valid = true;

        String titre = titreField.getText();
        String ordreText = ordreField.getText();
        String dureeText = dureeField.getText();
        String type = typeContenuField.getValue();

        // TITRE
        if (titre == null || titre.trim().isEmpty()) {
            titreError.setText("Titre obligatoire");
            valid = false;
        } else if (titre.length() > 30) {
            titreError.setText("Max 30 caractères");
            valid = false;
        }

        // ORDRE
        int ordre = 0;
        try {
            ordre = Integer.parseInt(ordreText);
            if (ordre <= 0) {
                ordreError.setText("Doit être positif");
                valid = false;
            }
        } catch (Exception e) {
            ordreError.setText("Nombre invalide");
            valid = false;
        }

        // TYPE
        if (type == null || type.isEmpty()) {
            typeError.setText("Type obligatoire");
            valid = false;
        }

        // CONTENU TEXTE (condition)
        if ("texte".equalsIgnoreCase(type)) {
            if (contenuTexteField.getText() == null || contenuTexteField.getText().isEmpty()) {
                contenuError.setText("Contenu texte obligatoire");
                valid = false;
            }
        }

        // DURÉE
        try {
            int duree = Integer.parseInt(dureeText);
            if (duree <= 0) {
                dureeError.setText("Durée invalide");
                valid = false;
            }
        } catch (Exception e) {
            dureeError.setText("Nombre invalide");
            valid = false;
        }

        if (!valid) return;

        // SAVE
        Chapitre ch = (chapitreToEdit == null) ? new Chapitre() : chapitreToEdit;

        ch.setTitre(titre);
        ch.setOrdre(Integer.parseInt(ordreText));
        ch.setDureeEstimee(Integer.parseInt(dureeText));
        ch.setTypeContenu(type);
        ch.setContenuTexte(contenuTexteField.getText());
        ch.setContenuFichier(contenuFichierField.getText());
        ch.setCoursId(coursId);

        if (chapitreToEdit == null)
            service.ajouter(ch);
        else
            service.modifier(ch);

        goBack();
    }

    // =========================
    // ALERT
    // =========================
    private void clearErrors() {
        titreError.setText("");
        ordreError.setText("");
        typeError.setText("");
        contenuError.setText("");
        dureeError.setText("");
    }

    // =========================
    // BACK
    // =========================
    // 🔙 RETURN TO LIST (FIXED)
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ChapitreList.fxml"));
            Parent root = loader.load();

            ChapitreList controller = loader.getController();
            controller.setCoursId(coursId);

            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void goToList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ChapitreList.fxml"));
            Parent root = loader.load();

            ChapitreList controller = loader.getController();

            // IMPORTANT: نرجع نفس cours
            controller.setCoursId(coursId);

            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void chooseFile() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier");

        String type = typeContenuField.getValue();

        // filters كيما Symfony
        if ("PDF".equalsIgnoreCase(type)) {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
        }
        else if ("IMAGE".equalsIgnoreCase(type)) {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
            );
        }
        else if ("VIDEO".equalsIgnoreCase(type)) {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Videos", "*.mp4", "*.avi")
            );
        }
        else {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );
        }

        Stage stage = (Stage) titreField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            contenuFichierField.setText(file.getAbsolutePath());
        }
    }

}
