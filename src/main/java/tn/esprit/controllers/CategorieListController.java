package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    private CategoryService service = new CategoryService();

    @FXML
    public void initialize() {

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));

        tableCategorie.setItems(
                FXCollections.observableArrayList(service.getAll())
        );
    }
    @FXML
    void goToAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/categorieView.fxml") // page ajout
            );

            Parent root = loader.load();

            Stage stage = (Stage) tableCategorie.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter catégorie");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}