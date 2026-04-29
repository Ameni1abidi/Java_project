package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.entities.categorie;
import tn.esprit.services.CategoryService;

public class CategorieController {

    @FXML
    private Label titleLabel;

    @FXML
    private TextField nomField;

    @FXML
    private Button saveButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Label errorLabel;

    private final CategoryService service = new CategoryService();
    private categorie currentCategorie;

    @FXML
    public void initialize() {
        titleLabel.setText("Ajouter categorie");
        saveButton.setText("Creer");
        deleteButton.setVisible(false);
        errorLabel.setText("");
    }

    public void setCategorie(categorie categorie) {
        this.currentCategorie = categorie;
        errorLabel.setText("");
        if (categorie != null) {
            titleLabel.setText("Modifier categorie");
            saveButton.setText("Enregistrer");
            nomField.setText(categorie.getNom());
            deleteButton.setVisible(true);
        }
    }

    @FXML
    void saveCategorie() {
        errorLabel.setText("");
        String nom = nomField.getText().trim();

        if (nom.isEmpty()) {
            errorLabel.setText("Le nom de la categorie est requis.");
            return;
        }

        if (nom.length() > 50) {
            errorLabel.setText("Le nom de la categorie ne peut pas depasser 50 caracteres.");
            return;
        }

        if (service.existsByName(nom) && (currentCategorie == null || !nom.equals(currentCategorie.getNom()))) {
            errorLabel.setText("Une categorie avec ce nom existe deja.");
            return;
        }

        if (currentCategorie == null) {
            service.add(new categorie(nom));
            showAlert("Succes", "Categorie ajoutee avec succes !");
        } else {
            service.update(currentCategorie.getNom(), nom);
            showAlert("Succes", "Categorie modifiee avec succes !");
        }

        retourListe();
    }

    @FXML
    void deleteCategorie() {
        if (currentCategorie == null) {
            return;
        }

        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer la categorie");
        confirmation.setContentText("Etes-vous sur de vouloir supprimer cette categorie ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                service.delete(currentCategorie.getNom());
                showAlert("Succes", "Categorie supprimee avec succes !");
                retourListe();
            }
        });
    }

    @FXML
    void retourListe() {
        navigateTo("/CategorieList.fxml", "Liste des categories");
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

    private void navigateTo(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
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

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.show();
    }
}
