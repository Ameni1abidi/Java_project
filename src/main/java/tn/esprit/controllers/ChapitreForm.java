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
import tn.esprit.entities.Chapitre;
import tn.esprit.services.ChapitreService;
import tn.esprit.services.CoursService;
import tn.esprit.entities.Cours;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ChapitreForm {

    @FXML private TextField titreField;
    @FXML private TextField ordreField;
    @FXML private ChoiceBox<String> typeContenuField;
    @FXML private TextArea contenuTexteField;
    @FXML private TextField contenuFichierField;
    @FXML private TextField dureeField;
    @FXML private Label coursTitreLabel;

    private final ChapitreService service = new ChapitreService();
    private CoursService coursService = new CoursService();

    private int coursId;
    private Chapitre chapitreToEdit;

    public void initData(Chapitre ch, int coursId) {

        this.coursId = coursId;
        this.chapitreToEdit = ch;

        Cours cours = coursService.getById(coursId);

        if (cours != null) {
            coursTitreLabel.setText(cours.getTitre());
        }
    }

    @FXML
    public void initialize() {
        typeContenuField.getItems().addAll("PDF", "VIDEO", "IMAGE", "TEXT");
    }

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

        if (titre == null || titre.trim().isEmpty()) {
            titreError.setText("Titre obligatoire");
            valid = false;
        } else if (titre.length() > 30) {
            titreError.setText("Max 30 caractères");
            valid = false;
        }

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

        if (type == null || type.isEmpty()) {
            typeError.setText("Type obligatoire");
            valid = false;
        }
        if ("texte".equalsIgnoreCase(type)) {
            if (contenuTexteField.getText() == null || contenuTexteField.getText().isEmpty()) {
                contenuError.setText("Contenu texte obligatoire");
                valid = false;
            }
        }

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

    private void clearErrors() {
        titreError.setText("");
        ordreError.setText("");
        typeError.setText("");
        contenuError.setText("");
        dureeError.setText("");
    }

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

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        Stage stage = (Stage) titreField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {

            try {
                // 📁 create uploads folder if not exists
                File uploadDir = new File("uploads");
                if (!uploadDir.exists()) uploadDir.mkdir();

                File target = new File(uploadDir, file.getName());

                Files.copy(file.toPath(), target.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);

                // ✅ store ONLY filename
                contenuFichierField.setText(file.getAbsolutePath());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
    private void goEvaluations(ActionEvent event) {
        loadPage(event, "/EvaluationView.fxml");
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
