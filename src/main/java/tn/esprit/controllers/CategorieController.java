package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

    private CategoryService service = new CategoryService();
    private categorie currentCategorie;

    @FXML
    public void initialize() {
        titleLabel.setText("Ajouter catégorie");
        saveButton.setText("Créer");
        deleteButton.setVisible(false);
        errorLabel.setText("");
    }

    public void setCategorie(categorie categorie) {
        this.currentCategorie = categorie;
        errorLabel.setText("");
        if (categorie != null) {
            titleLabel.setText("Modifier catégorie");
            saveButton.setText("Enregistrer");
            nomField.setText(categorie.getNom());
            deleteButton.setVisible(true);
        }
    }

    @FXML
    void saveCategorie() {
        errorLabel.setText(""); // Clear previous errors
        String nom = nomField.getText().trim();

        // Validation: not empty
        if (nom.isEmpty()) {
            errorLabel.setText("Le nom de la catégorie est requis.");
            return;
        }

        // Validation: length 1-50
        if (nom.length() > 50) {
            errorLabel.setText("Le nom de la catégorie ne peut pas dépasser 50 caractères.");
            return;
        }

        // Validation: no duplicate names
        // Check if the new name already exists (excluding the current name if updating)
        if (service.existsByName(nom) && (currentCategorie == null || !nom.equals(currentCategorie.getNom()))) {
            errorLabel.setText("Une catégorie avec ce nom existe déjà.");
            return;
        }

        if (currentCategorie == null) {
            service.add(new categorie(nom));
            showAlert("Succès", "Catégorie ajoutée avec succès !");
        } else {
            service.update(currentCategorie.getNom(), nom);
            showAlert("Succès", "Catégorie modifiée avec succès !");
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
        confirmation.setHeaderText("Supprimer la catégorie");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette catégorie ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                service.delete(currentCategorie.getNom());
                showAlert("Succès", "Catégorie supprimée avec succès !");
                retourListe();
            }
        });
    }

    @FXML
    void retourListe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CategorieList.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des catégories");
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