package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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

    private CategoryService service = new CategoryService();
    private categorie currentCategorie;

    @FXML
    public void initialize() {
        titleLabel.setText("Ajouter catégorie");
        saveButton.setText("Créer");
        deleteButton.setVisible(false);
    }

    public void setCategorie(categorie categorie) {
        this.currentCategorie = categorie;
        if (categorie != null) {
            titleLabel.setText("Modifier catégorie");
            saveButton.setText("Enregistrer");
            nomField.setText(categorie.getNom());
            deleteButton.setVisible(true);
        }
    }

    @FXML
    void saveCategorie() {
        String nom = nomField.getText();

        if (nom == null || nom.trim().isEmpty()) {
            showAlert("Erreur", "Le nom de la catégorie est requis.");
            return;
        }

        if (currentCategorie == null) {
            service.add(new categorie(nom));
            showAlert("Succès", "Catégorie ajoutée avec succès !");
        } else {
            currentCategorie.setNom(nom);
            service.update(currentCategorie);
            showAlert("Succès", "Catégorie modifiée avec succès !");
        }

        retourListe();
    }

    @FXML
    void deleteCategorie() {
        if (currentCategorie == null) {
            return;
        }

        service.delete(currentCategorie.getId());
        showAlert("Succès", "Catégorie supprimée avec succès !");
        retourListe();
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