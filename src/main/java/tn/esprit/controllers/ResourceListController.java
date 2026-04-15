package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.entities.categorie;
import tn.esprit.entities.resources;
import tn.esprit.services.CategoryService;
import tn.esprit.services.ResourceService;

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
    private TableColumn<resources, String> dateColumn;

    @FXML
    private TableColumn<resources, Void> actionColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Button createButton;

    private ResourceService resourceService = new ResourceService();
    private CategoryService categoryService = new CategoryService();
    private Map<String, String> categoryNames = new HashMap<>();

    @FXML
    public void initialize() {
        loadCategories();
        setupTable();
        loadResources();
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
            String categorieNom = cellData.getValue().getCategorieNom();
            String name = categoryNames.getOrDefault(categorieNom, "N/A");
            return new javafx.beans.property.SimpleStringProperty(name);
        });

        dateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDisponibleLe()));

        actionColumn.setCellFactory(column -> new TableCell<resources, Void>() {
            private final Button editButton = new Button("Modifier");

            {
                editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4;");

                editButton.setOnAction(event -> {
                    resources resource = getTableView().getItems().get(getIndex());
                    openResourceForm(resource);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8, editButton);
                    setGraphic(box);
                }
            }
        });
    }

    private void loadResources() {
        List<resources> resources = resourceService.getAll();
        resourceTable.setItems(FXCollections.observableArrayList(resources));
    }

    private void loadSearchResults(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadResources();
            return;
        }
        List<resources> resources = resourceService.search(keyword.trim());
        resourceTable.setItems(FXCollections.observableArrayList(resources));
    }

    @FXML
    private void onSearchClick() {
        loadSearchResults(searchField.getText());
    }

    @FXML
    private void onCreateClick() {
        openResourceForm(null);
    }

    private void openResourceForm(resources resource) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterRessource.fxml"));
            Parent root = loader.load();
            ajouterRessource controller = loader.getController();
            if (resource != null) {
                controller.setResource(resource);
            }
            Stage stage = new Stage();
            stage.setTitle(resource == null ? "Créer une ressource" : "Modifier la ressource");
            stage.setScene(new Scene(root));
            stage.setOnHidden(event -> loadResources());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
