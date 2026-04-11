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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import tn.esprit.entities.categorie;
import tn.esprit.services.CategoryService;

public class CategorieListController {

    @FXML
    private TableView<categorie> tableCategorie;

    @FXML
    private TableColumn<categorie, Integer> idCol;

    @FXML
    private TableColumn<categorie, String> nomCol;

    @FXML
    private TableColumn<categorie, Void> actionCol;

    private CategoryService service = new CategoryService();

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        tableCategorie.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        actionCol.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");

            {
                editButton.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
                editButton.setOnAction(event -> {
                    categorie item = getTableRow().getItem();
                    if (item != null) {
                        openEdit(item);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(editButton);
                }
            }
        });

        tableCategorie.setItems(FXCollections.observableArrayList(service.getAll()));
    }

    @FXML
    void goToAdd() {
        loadForm(null);
    }

    private void openEdit(categorie categorie) {
        loadForm(categorie);
    }

    private void loadForm(categorie categorie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/categorieView.fxml"));
            Parent root = loader.load();

            CategorieController controller = loader.getController();
            controller.setCategorie(categorie);

            Stage stage = (Stage) tableCategorie.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(categorie == null ? "Ajouter catégorie" : "Modifier catégorie");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}