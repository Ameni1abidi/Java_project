package tn.esprit.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.entities.categorie;
import tn.esprit.entities.resources;
import tn.esprit.services.CategoryService;
import tn.esprit.services.ResourceService;
import tn.esprit.utils.ResourceNavigationContext;
import tn.esprit.utils.UserSession;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceListController {

    @FXML
    private TableView<resources> resourceTable;

    @FXML
    private TableColumn<resources, String> titreColumn;

    @FXML
    private TableColumn<resources, String> categorieColumn;

    @FXML
    private TableColumn<resources, String> dateColumn;

    @FXML
    private TableColumn<resources, Void> fileColumn;

    @FXML
    private TableColumn<resources, Void> actionColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Button createButton;

    @FXML
    private Label pageTitleLabel;

    private final ResourceService resourceService = new ResourceService();
    private final CategoryService categoryService = new CategoryService();
    private final Map<String, String> categoryNames = new HashMap<>();

    private Integer selectedChapitreId;
    private boolean studentMode;
    private int currentUserId;

    @FXML
    public void initialize() {
        currentUserId = resolveCurrentUserId();
        selectedChapitreId = ResourceNavigationContext.getChapitreId();
        studentMode = ResourceNavigationContext.isStudentMode() || selectedChapitreId != null;
        User currentUser = UserSession.getCurrentUser();
        if (currentUser != null && currentUser.getRole() != User.Role.ROLE_ETUDIANT) {
            studentMode = false;
            selectedChapitreId = null;
            ResourceNavigationContext.clear();
        }

        loadCategories();
        setupTable();
        configureModeUi();
        loadResources();
    }

    private int resolveCurrentUserId() {
        User user = UserSession.getCurrentUser();
        return user != null ? user.getId() : -1;
    }

    private void configureModeUi() {
        if (!studentMode) {
            return;
        }

        if (createButton != null) {
            createButton.setVisible(false);
            createButton.setManaged(false);
        }

        String titreChapitre = ResourceNavigationContext.getChapitreTitre();
        if (pageTitleLabel != null && titreChapitre != null && !titreChapitre.isBlank()) {
            pageTitleLabel.setText("Ressources du chapitre - " + titreChapitre);
        }
    }

    private void loadCategories() {
        List<categorie> categories = categoryService.getAll();
        for (categorie cat : categories) {
            categoryNames.put(cat.getNom(), cat.getNom());
        }
    }

    private void setupTable() {
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));

        if (studentMode) {
            categorieColumn.setText("Type");
            categorieColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(toDisplayType(cellData.getValue().getType())));

            dateColumn.setText("Disponibilite");
            dateColumn.setCellValueFactory(cellData -> {
                resources res = cellData.getValue();
                String status = resourceService.isDisponible(res)
                        ? "Disponible"
                        : "Disponible le " + valueOrNA(res.getDisponibleLe());
                return new SimpleStringProperty(status);
            });
        } else {
            categorieColumn.setCellValueFactory(cellData -> {
                String categorieNom = cellData.getValue().getCategorieNom();
                String name = categoryNames.getOrDefault(categorieNom, "N/A");
                return new SimpleStringProperty(name);
            });

            dateColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getDisponibleLe()));
        }

        fileColumn.setCellFactory(column -> new TableCell<>() {
            private final Hyperlink downloadLink = new Hyperlink(studentMode ? "Voir / Telecharger" : "Telecharger");

            {
                downloadLink.setOnAction(event -> {
                    resources resource = getTableView().getItems().get(getIndex());
                    if (studentMode && !resourceService.isDisponible(resource)) {
                        showError("Cette ressource n'est pas encore disponible.");
                        return;
                    }
                    downloadResource(resource);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                resources resource = getTableView().getItems().get(getIndex());
                boolean hasContent = resource != null
                        && resource.getContenu() != null
                        && !resource.getContenu().trim().isEmpty();

                downloadLink.setDisable(!hasContent);
                setGraphic(downloadLink);
            }
        });

        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button favoriButton = new Button();

            {
                editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4;");
                editButton.setOnAction(event -> {
                    resources resource = getTableView().getItems().get(getIndex());
                    openResourceForm(resource);
                });

                favoriButton.setOnAction(event -> {
                    resources resource = getTableView().getItems().get(getIndex());
                    toggleFavorite(resource);
                    getTableView().refresh();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                if (studentMode) {
                    resources resource = getTableView().getItems().get(getIndex());
                    boolean isFav = resource != null && resource.isFavori();
                    favoriButton.setText(isFav ? "Retirer Favori" : "Favori");
                    favoriButton.setStyle(isFav
                            ? "-fx-background-color: #f8d7da; -fx-text-fill: #842029; -fx-font-weight: bold; -fx-background-radius: 8;"
                            : "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-font-weight: bold; -fx-background-radius: 8;");
                    setGraphic(new HBox(8, favoriButton));
                } else {
                    setGraphic(new HBox(8, editButton));
                }
            }
        });
    }

    private void toggleFavorite(resources resource) {
        if (resource == null) {
            return;
        }
        if (currentUserId <= 0) {
            showError("Session utilisateur invalide pour gerer les favoris.");
            return;
        }
        boolean next = !resource.isFavori();
        resourceService.setFavorite(currentUserId, resource.getId(), next);
        resource.setFavori(next);
    }

    private String toDisplayType(String type) {
        if (type == null || type.isBlank()) {
            return "N/A";
        }
        return type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();
    }

    private String valueOrNA(String value) {
        return (value == null || value.isBlank()) ? "N/A" : value;
    }

    private void downloadResource(resources resource) {
        if (resource == null || resource.getContenu() == null || resource.getContenu().trim().isEmpty()) {
            showError("Aucun fichier a telecharger pour cette ressource.");
            return;
        }

        String contenu = resource.getContenu().trim();
        if (contenu.startsWith("http://") || contenu.startsWith("https://")) {
            downloadFromUrl(contenu, resource.getTitre());
            return;
        }

        Path source = resolveLocalResourcePath(contenu);
        if (source == null) {
            showError("Fichier introuvable pour cette ressource:\n" + contenu);
            return;
        }

        downloadFromLocalPath(source);
    }

    private Path resolveLocalResourcePath(String rawContent) {
        try {
            Path direct = Paths.get(rawContent).toAbsolutePath().normalize();
            if (Files.exists(direct)) {
                return direct;
            }
        } catch (Exception ignored) {
        }

        try {
            Path fromProject = Paths.get("").toAbsolutePath().resolve(rawContent).normalize();
            if (Files.exists(fromProject)) {
                return fromProject;
            }
        } catch (Exception ignored) {
        }

        try {
            Path fileName = Paths.get(rawContent).getFileName();
            if (fileName != null) {
                Path fromStorage = Paths.get("storage", "resources")
                        .toAbsolutePath()
                        .resolve(fileName.toString())
                        .normalize();
                if (Files.exists(fromStorage)) {
                    return fromStorage;
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private void downloadFromLocalPath(Path source) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Telecharger le fichier");
        chooser.setInitialFileName(source.getFileName().toString());
        File targetFile = chooser.showSaveDialog(resourceTable.getScene().getWindow());
        if (targetFile == null) {
            return;
        }

        try {
            Files.copy(source, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            showInfo("Telechargement termine", "Fichier enregistre:\n" + targetFile.getAbsolutePath());
        } catch (IOException e) {
            showError("Erreur de telechargement: " + e.getMessage());
        }
    }

    private void downloadFromUrl(String url, String titre) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Telecharger le fichier");
        chooser.setInitialFileName(buildDefaultFileName(url, titre));
        File targetFile = chooser.showSaveDialog(resourceTable.getScene().getWindow());
        if (targetFile == null) {
            return;
        }

        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            showInfo("Telechargement termine", "Fichier enregistre:\n" + targetFile.getAbsolutePath());
        } catch (IOException e) {
            showError("Impossible de telecharger ce lien: " + e.getMessage());
        }
    }

    private String buildDefaultFileName(String url, String titre) {
        try {
            String path = new URL(url).getPath();
            if (path != null && !path.isBlank()) {
                int idx = path.lastIndexOf('/');
                String name = idx >= 0 ? path.substring(idx + 1) : path;
                if (!name.isBlank()) {
                    return name;
                }
            }
        } catch (Exception ignored) {
        }

        if (titre != null && !titre.isBlank()) {
            return titre;
        }

        return "fichier";
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadResources() {
        List<resources> resourcesList;
        if (selectedChapitreId != null) {
            resourcesList = resourceService.getByChapitreId(selectedChapitreId);
        } else {
            resourcesList = resourceService.getAll();
        }

        if (studentMode && currentUserId > 0) {
            for (resources resource : resourcesList) {
                resource.setFavori(resourceService.isFavorite(currentUserId, resource.getId()));
            }
        }

        resourceTable.setItems(FXCollections.observableArrayList(resourcesList));
    }

    private void loadSearchResults(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadResources();
            return;
        }

        String k = keyword.trim().toLowerCase();
        List<resources> base;
        if (selectedChapitreId != null) {
            base = resourceService.getByChapitreId(selectedChapitreId);
        } else {
            base = resourceService.search(keyword.trim());
        }

        List<resources> filtered = base.stream()
                .filter(r -> r.getTitre() != null && r.getTitre().toLowerCase().contains(k))
                .toList();

        if (studentMode && currentUserId > 0) {
            for (resources resource : filtered) {
                resource.setFavori(resourceService.isFavorite(currentUserId, resource.getId()));
            }
        }

        resourceTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void onSearchClick() {
        loadSearchResults(searchField.getText());
    }

    @FXML
    private void onCreateClick() {
        openResourceForm(null);
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

    private void openResourceForm(resources resource) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterRessource.fxml"));
            Parent root = loader.load();
            ajouterRessource controller = loader.getController();
            if (resource != null) {
                controller.setResource(resource);
            }
            Stage stage = (Stage) resourceTable.getScene().getWindow();
            stage.setTitle(resource == null ? "Creer une ressource" : "Modifier la ressource");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPage(ActionEvent event, String fxmlPath) {
        try {
            if (!"/listeRessources.fxml".equals(fxmlPath)) {
                ResourceNavigationContext.clear();
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de navigation");
            alert.setHeaderText("Impossible d'ouvrir la page");
            alert.setContentText("Fichier FXML introuvable ou erreur d'ouverture : " + fxmlPath + "\n" + e.getMessage());
            alert.showAndWait();
        }
    }
}
