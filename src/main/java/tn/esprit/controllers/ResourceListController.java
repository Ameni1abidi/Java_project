package tn.esprit.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.entities.categorie;
import tn.esprit.entities.resources;
import tn.esprit.services.CategoryService;
import tn.esprit.services.ResourceService;
import tn.esprit.utils.ResourceNavigationContext;
import tn.esprit.utils.UserSession;

import java.io.IOException;
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
    private TableColumn<resources, String> chapitreColumn;
    @FXML
    private TableColumn<resources, String> dateColumn;
    @FXML
    private TableColumn<resources, String> statusColumn;
    @FXML
    private TableColumn<resources, String> contenuColumn;
    @FXML
    private TableColumn<resources, Void> actionsColumn;
    @FXML
    private TextField searchField;
    @FXML
    private Button createButton;
    @FXML
    private Label pageTitleLabel;

    private final ResourceService resourceService = new ResourceService();
    private final CategoryService categoryService = new CategoryService();
    private final Map<String, String> categoryNames = new HashMap<>();
    private Map<Integer, String> chapitreTitles = new HashMap<>();

    private Integer selectedChapitreId;
    private boolean studentMode;

    @FXML
    public void initialize() {
        selectedChapitreId = ResourceNavigationContext.getChapitreId();
        studentMode = ResourceNavigationContext.isStudentMode() || selectedChapitreId != null;
        User currentUser = UserSession.getCurrentUser();
        if (currentUser != null && currentUser.getRole() != User.Role.ROLE_ETUDIANT) {
            studentMode = false;
            selectedChapitreId = null;
            ResourceNavigationContext.clear();
        }

        loadCategories();
        chapitreTitles = resourceService.getChapitreTitles();
        setupTable();
        configureModeUi();
        loadResources();
    }

    private void configureModeUi() {
        if (!studentMode) {
            return;
        }

        if (createButton != null) {
            createButton.setVisible(false);
            createButton.setManaged(false);
        }
        if (actionsColumn != null) {
            actionsColumn.setVisible(false);
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

        categorieColumn.setCellValueFactory(cellData -> {
            resources r = cellData.getValue();
            if (studentMode) {
                return new SimpleStringProperty(toDisplayType(r.getType()));
            }
            return new SimpleStringProperty(categoryNames.getOrDefault(r.getCategorieNom(), "N/A"));
        });

        chapitreColumn.setCellValueFactory(cellData -> {
            resources r = cellData.getValue();
            String titreChapitre = chapitreTitles.get(r.getChapitreId());
            return new SimpleStringProperty(titreChapitre == null || titreChapitre.isBlank() ? "N/A" : titreChapitre);
        });

        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(valueOrNA(cellData.getValue().getDisponibleLe())));

        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(resourceService.isDisponible(cellData.getValue()) ? "Disponible" : "Non disponible"));
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                if ("Disponible".equals(item)) {
                    setStyle("-fx-text-fill: #198754; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                }

            }
        });

        contenuColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatContenu(cellData.getValue().getContenu())));

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox pane = new HBox(8, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color:#ede9fe; -fx-text-fill:#5b21b6; -fx-font-weight:bold; -fx-background-radius:8; -fx-padding:7 12;");
                deleteBtn.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#991b1b; -fx-font-weight:bold; -fx-background-radius:8; -fx-padding:7 12;");
                pane.setAlignment(Pos.CENTER);
                editBtn.setOnAction(event -> {
                    resources res = getTableView().getItems().get(getIndex());
                    if (res != null) {
                        openResourceForm(res);
                    }
                });
                deleteBtn.setOnAction(event -> {
                    resources res = getTableView().getItems().get(getIndex());
                    if (res != null) {
                        handleDelete(res);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        contenuColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatContenu(cellData.getValue().getContenu())));
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

    private String formatContenu(String contenu) {
        if (contenu == null || contenu.isBlank()) {
            return "N/A";
        }
        String normalized = contenu.replace("\n", " ").trim();
        if (normalized.length() <= 60) {
            return normalized;
        }
        return normalized.substring(0, 57) + "...";
    }

    private void loadResources() {
        List<resources> resourcesList = (selectedChapitreId != null)
                ? resourceService.getByChapitreId(selectedChapitreId)
                : resourceService.getAll();
        resourceTable.setItems(FXCollections.observableArrayList(resourcesList));
    }

    private void loadSearchResults(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadResources();
            return;
        }
        String k = keyword.trim().toLowerCase();
        List<resources> base = (selectedChapitreId != null)
                ? resourceService.getByChapitreId(selectedChapitreId)
                : resourceService.search(keyword.trim());

        List<resources> filtered = base.stream()
                .filter(r -> containsIgnoreCase(r.getTitre(), k) || containsIgnoreCase(r.getContenu(), k))
                .toList();
        resourceTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
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
    private void goRessourceDashboard(ActionEvent event) {
        loadPage(event, "/RessourceDashboard.fxml");
    }

    @FXML
    private void goRessourceCalendar(ActionEvent event) {
        loadPage(event, "/RessourceCalendar.fxml");
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

    private void handleDelete(resources resource) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer la ressource");
        confirmation.setContentText("Etes-vous sur de vouloir supprimer la ressource \"" + resource.getTitre() + "\" ?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                resourceService.delete(resource.getId());
                loadResources();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succes");
                alert.setHeaderText(null);
                alert.setContentText("Ressource supprimee avec succes !");
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Impossible de supprimer la ressource");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
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
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
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
