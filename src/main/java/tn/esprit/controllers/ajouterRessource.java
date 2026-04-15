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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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

    private final ResourceService resourceService = new ResourceService();
    private final CategoryService categoryService = new CategoryService();
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
        selectedFileLabel.setText("Aucun fichier selectionne");

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
            errorLabel.setText("Categorie invalide : utilisez image, video, audio, pdf ou lien.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une ressource - " + type);

        switch (type) {
            case "image":
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.gif", "*.bmp"));
                break;
            case "video":
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Videos", "*.mp4", "*.avi", "*.mkv", "*.mov"));
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
            errorLabel.setText("Erreur lors du chargement des categories.");
            e.printStackTrace();
        }
    }

    public void setResource(resources resource) {
        this.currentResource = resource;
        if (resource != null) {
            titreField.setText(resource.getTitre());
            selectedFilePath = resource.getContenu();
            selectedFileLabel.setText(extractFileName(resource.getContenu()));
            urlField.setText("lien".equals(resource.getType()) ? resource.getContenu() : "");

            if (resource.getDisponibleLe() != null && !resource.getDisponibleLe().isEmpty()) {
                try {
                    datePicker.setValue(LocalDate.parse(resource.getDisponibleLe()));
                } catch (Exception ignored) {
                }
            }

            for (categorie cat : categorieCombo.getItems()) {
                if (cat.getNom().equals(resource.getCategorieNom())) {
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
            errorLabel.setText("Veuillez selectionner une categorie.");
            return;
        }

        String type = mapCategoryToType(categorie);
        if (type == null) {
            errorLabel.setText("Categorie non prise en charge : image, video, audio, pdf ou lien seulement.");
            return;
        }
        if (date == null) {
            errorLabel.setText("Veuillez selectionner une date de disponibilite.");
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
                errorLabel.setText("Veuillez selectionner un fichier.");
                return;
            }
            try {
                contenu = prepareStoredFilePath(selectedFilePath);
            } catch (IOException e) {
                errorLabel.setText("Impossible de sauvegarder le fichier: " + e.getMessage());
                return;
            }
        }

        String disponibleLe = date.toString();

        try {
            if (currentResource == null) {
                resources newResource = new resources(titre, contenu, categorie.getNom(), type, disponibleLe);
                resourceService.add(newResource);
                showAlert("Succes", "Ressource creee avec succes !");
            } else {
                currentResource.setTitre(titre);
                currentResource.setContenu(contenu);
                currentResource.setCategorieNom(categorie.getNom());
                currentResource.setType(type);
                currentResource.setDisponibleLe(disponibleLe);
                resourceService.update(currentResource);
                showAlert("Succes", "Ressource modifiee avec succes !");
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
        confirmation.setContentText("Etes-vous sur de vouloir supprimer cette ressource ?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                resourceService.delete(currentResource.getId());
                showAlert("Succes", "Ressource supprimee avec succes !");
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
            case "video":
            //case "vid�o":
            case "vidéo":
                return "video";
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

    private String prepareStoredFilePath(String sourcePath) throws IOException {
        Path source = Path.of(sourcePath);
        if (!Files.exists(source)) {
            throw new IOException("fichier introuvable");
        }

        Path storageDir = Path.of("storage", "resources").toAbsolutePath().normalize();
        Files.createDirectories(storageDir);

        Path absoluteSource = source.toAbsolutePath().normalize();
        if (absoluteSource.startsWith(storageDir)) {
            return absoluteSource.toString();
        }

        String originalName = source.getFileName().toString();
        String extension = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) {
            extension = originalName.substring(dot);
        }

        String targetName = UUID.randomUUID() + extension;
        Path target = storageDir.resolve(targetName);
        Files.copy(absoluteSource, target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString();
    }

    private String extractFileName(String content) {
        if (content == null || content.isBlank()) {
            return "Aucun fichier selectionne";
        }
        if (content.startsWith("http://") || content.startsWith("https://")) {
            return content;
        }
        try {
            return Path.of(content).getFileName().toString();
        } catch (Exception ignored) {
            return content;
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
