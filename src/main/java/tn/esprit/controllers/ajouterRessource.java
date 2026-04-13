package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.entities.categorie;
import tn.esprit.entities.resources;
import tn.esprit.services.CategoryService;
import tn.esprit.services.ResourceService;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class ajouterRessource {

    @FXML
    private TextField titreField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<categorie> categorieCombo;

    @FXML
    private HBox fileChooserContainer;

    @FXML
    private VBox urlContainer;

    @FXML
    private TextField urlField;

    @FXML
    private Button browseButton;

    @FXML
    private Label selectedFileLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private Button enregistrerButton;

    @FXML
    private Button supprimerButton;

    private ResourceService resourceService = new ResourceService();
    private CategoryService categoryService = new CategoryService();
    private resources currentResource;
    private String selectedFilePath = "";

    @FXML
    public void initialize() {
        loadCategories();
        categorieCombo.valueProperty().addListener((obs, oldCat, newCat) -> handleCategoryChange(newCat));

        fileChooserContainer.setVisible(false);
        urlContainer.setVisible(false);
        supprimerButton.setVisible(false);
    }

    private void handleCategoryChange(categorie categorie) {
        errorLabel.setText("");
        selectedFilePath = "";
        selectedFileLabel.setText("Aucun fichier sélectionné");

        String type = mapCategoryToType(categorie);
        if (type == null) {
            fileChooserContainer.setVisible(false);
            urlContainer.setVisible(false);
            return;
        }

        if (type.equals("lien")) {
            fileChooserContainer.setVisible(false);
            urlContainer.setVisible(true);
            urlField.setPromptText("https://exemple.com");
        } else {
            fileChooserContainer.setVisible(true);
            urlContainer.setVisible(false);
            browseButton.setText("Choisir " + type);
        }
    }

    @FXML
    void browseFile() {
        categorie categorie = categorieCombo.getValue();
        String type = mapCategoryToType(categorie);
        if (type == null) {
            errorLabel.setText("Catégorie invalide : utilisez image, vidéo, audio, pdf ou lien.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une ressource - " + type);

        switch (type) {
            case "image":
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.gif", "*.bmp"));
                break;
            case "vidéo":
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Vidéos", "*.mp4", "*.avi", "*.mkv", "*.mov"));
                break;
            case "audio":
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audios", "*.mp3", "*.wav", "*.flac", "*.m4a"));
                break;
            case "pdf":
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
                break;
        }

        Stage stage = (Stage) browseButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            selectedFilePath = selectedFile.getAbsolutePath();
            selectedFileLabel.setText(selectedFile.getName());
            errorLabel.setText("");
        }
    }

    private void loadCategories() {
        try {
            List<categorie> categories = categoryService.getAll();
            categorieCombo.setItems(javafx.collections.FXCollections.observableArrayList(categories));
        } catch (Exception e) {
            errorLabel.setText("Erreur lors du chargement des catégories.");
            e.printStackTrace();
        }
    }

    public void setResource(resources resource) {
        this.currentResource = resource;
        if (resource != null) {
            titreField.setText(resource.getTitre());
            selectedFilePath = resource.getContenu();
            selectedFileLabel.setText(resource.getContenu());
            urlField.setText("lien".equals(resource.getType()) ? resource.getContenu() : "");

            if (resource.getDisponibleLe() != null && !resource.getDisponibleLe().isEmpty()) {
                try {
                    datePicker.setValue(LocalDate.parse(resource.getDisponibleLe()));
                } catch (Exception ignored) {
                }
            }

            for (categorie cat : categorieCombo.getItems()) {
                if (cat.getId() == resource.getCategorieId()) {
                    categorieCombo.setValue(cat);
                    break;
                }
            }
            handleCategoryChange(categorieCombo.getValue());

            enregistrerButton.setText("Modifier");
            supprimerButton.setVisible(true);
        }
    }

    @FXML
    void enregistrerRessource() {
        errorLabel.setText("");

        String titre = titreField.getText().trim();
        categorie categorie = categorieCombo.getValue();
        LocalDate date = datePicker.getValue();

        if (titre.isEmpty()) {
            errorLabel.setText("Le titre est requis.");
            return;
        }
        if (categorie == null) {
            errorLabel.setText("Veuillez sélectionner une catégorie.");
            return;
        }
        String type = mapCategoryToType(categorie);
        if (type == null) {
            errorLabel.setText("Catégorie non prise en charge : image, vidéo, audio, pdf ou lien seulement.");
            return;
        }
        if (date == null) {
            errorLabel.setText("Veuillez sélectionner une date de disponibilité.");
            return;
        }

        String contenu;
        if (type.equals("lien")) {
            contenu = urlField.getText().trim();
            if (contenu.isEmpty()) {
                errorLabel.setText("Veuillez entrer une URL.");
                return;
            }
            if (!contenu.startsWith("http://") && !contenu.startsWith("https://")) {
                errorLabel.setText("L'URL doit commencer par http:// ou https://");
                return;
            }
        } else {
            if (selectedFilePath.isEmpty()) {
                errorLabel.setText("Veuillez sélectionner un fichier.");
                return;
            }
            contenu = selectedFilePath;
        }

        String disponibleLe = date.toString();

        try {
            if (currentResource == null) {
                resources newResource = new resources(titre, contenu, categorie.getId(), type, disponibleLe);
                resourceService.add(newResource);
                showAlert("Succès", "Ressource créée avec succès !");
            } else {
                currentResource.setTitre(titre);
                currentResource.setContenu(contenu);
                currentResource.setCategorieId(categorie.getId());
                currentResource.setType(type);
                currentResource.setDisponibleLe(disponibleLe);
                resourceService.update(currentResource);
                showAlert("Succès", "Ressource modifiée avec succès !");
            }
            fermerFenetre();
        } catch (Exception e) {
            errorLabel.setText("Erreur lors de l'enregistrement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void supprimerRessource() {
        if (currentResource == null) {
            return;
        }

        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer la ressource");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette ressource ?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                resourceService.delete(currentResource.getId());
                showAlert("Succès", "Ressource supprimée avec succès !");
                fermerFenetre();
            } catch (Exception e) {
                errorLabel.setText("Erreur lors de la suppression: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String mapCategoryToType(categorie categorie) {
        if (categorie == null || categorie.getNom() == null) {
            return null;
        }
        String nom = categorie.getNom().trim().toLowerCase();
        switch (nom) {
            case "image":
                return "image";
            case "vidéo":
            case "video":
                return "vidéo";
            case "audio":
                return "audio";
            case "pdf":
                return "pdf";
            case "lien":
            case "link":
                return "lien";
            default:
                return null;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) enregistrerButton.getScene().getWindow();
        stage.close();
    }
}
