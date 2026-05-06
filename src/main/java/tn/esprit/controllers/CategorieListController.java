package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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

    private final CategoryService service = new CategoryService();

    @FXML
    public void initialize() {
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        tableCategorie.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        actionCol.setSortable(false);

        actionCol.setCellFactory(column -> new EditButtonCell());

        try {
            refreshCategories();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de base de donnees");
            alert.setHeaderText("Connexion a la base de donnees echouee");
            alert.setContentText("Verifiez que MySQL est demarre et que la base 'eduflex' existe.\nErreur: " + getRootMessage(e));
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
        private final Button deleteButton = new Button("Supprimer");
        private final HBox container = new HBox(8, editButton, deleteButton);

        public EditButtonCell() {
            editButton.setStyle("-fx-background-color:#ede9fe; -fx-text-fill:#5b21b6; -fx-font-weight:bold; -fx-background-radius:8; -fx-padding:7 12;");
            deleteButton.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#991b1b; -fx-font-weight:bold; -fx-background-radius:8; -fx-padding:7 12;");
            container.setAlignment(Pos.CENTER);
            editButton.setOnAction(event -> {
                categorie item = getTableRow().getItem();
                if (item != null) {
                    showCategorieForm(item);
                }
            });

            deleteButton.setOnAction(event -> {
                categorie item = getTableRow().getItem();
                if (item != null) {
                    deleteCategorie(item);
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


    private void deleteCategorie(categorie item) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer la categorie");
        confirmation.setContentText("Etes-vous sur de vouloir supprimer la categorie \"" + item.getNom() + "\" ?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            int resourceCount = service.countResourcesByCategory(item.getNom());
            if (resourceCount > 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Suppression impossible");
                alert.setHeaderText("Cette categorie est utilisee");
                alert.setContentText(resourceCount + " ressource(s) utilisent encore la categorie \"" + item.getNom() + "\".");
                alert.showAndWait();
                return;
            }

            if (service.delete(item.getNom())) {
                refreshCategories();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible de supprimer la categorie");
            alert.setContentText("Verifiez qu'aucune ressource n'utilise encore cette categorie.\n" + getRootMessage(e));
            alert.showAndWait();
        }
    }

    private void refreshCategories() {
        tableCategorie.setItems(FXCollections.observableArrayList(service.getAll()));
        tableCategorie.refresh();
    }

    private String getRootMessage(Exception e) {
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        return cause.getMessage() == null ? e.getMessage() : cause.getMessage();
    }
}
