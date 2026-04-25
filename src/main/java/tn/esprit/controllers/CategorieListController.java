package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import tn.esprit.entities.categorie;
import tn.esprit.services.CategoryService;

public class CategorieListController {

    @FXML
    private TableView<categorie> tableCategorie;

    @FXML
    private TableColumn<categorie, String> nomCol;

    @FXML
    private TableColumn<categorie, Void> actionCol;

    private CategoryService service = new CategoryService();

    @FXML
    public void initialize() {
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        tableCategorie.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        actionCol.setSortable(false);

        actionCol.setCellFactory(column -> new EditButtonCell());

        try {
            tableCategorie.setItems(FXCollections.observableArrayList(service.getAll()));
            tableCategorie.refresh();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de base de données");
            alert.setHeaderText("Connexion à la base de données échouée");
            alert.setContentText("Vérifiez que MySQL est démarré et que la base 'eduflex' existe.\nErreur: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    void goToAdd() {
        showCategorieForm(null);
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

    private void showCategorieForm(categorie categorie) {
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

    private class EditButtonCell extends TableCell<categorie, Void> {
        private final Button editButton = new Button("Modifier");
        private final HBox container = new HBox(editButton);

        public EditButtonCell() {
            editButton.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
            container.setAlignment(Pos.CENTER);
            editButton.setOnAction(event -> {
                categorie item = getTableRow().getItem();
                if (item != null) {
                    showCategorieForm(item);
                }
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
            } else {
                setGraphic(container);
            }
        }
    }
}
