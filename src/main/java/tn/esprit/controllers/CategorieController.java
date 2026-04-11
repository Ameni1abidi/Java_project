package tn.esprit.controllers;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import tn.esprit.entities.categorie;
import tn.esprit.services.CategoryService;
import tn.esprit.entities.categorie;

public class CategorieController {
    @FXML
    private TextField nomField;

    private CategoryService categoryService = new CategoryService();

    @FXML
    void ajouterCategorie() {
        String nom = nomField.getText();

        if (nom.isEmpty()) {
            showAlert("Erreur", "Le champ nom est vide !");
            return;
        }

        categorie c = new categorie(nom);
        categoryService.add(c);

        showAlert("Succès", "Catégorie ajoutée !");
        nomField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
