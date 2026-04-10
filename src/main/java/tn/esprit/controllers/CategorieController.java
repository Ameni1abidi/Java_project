package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.entities.categorie;
import tn.esprit.services.CategoryService;

public class CategorieController {

    @FXML
    private TextField nomField;

    private CategoryService service = new CategoryService();

    @FXML
    void ajouterCategorie() {

        String nom = nomField.getText();

        if (nom == null || nom.trim().isEmpty()) {
            showAlert("Erreur", "Nom vide !");
            return;
        }

        // 1️⃣ INSERT DB
        service.add(new categorie(nom));

        // 2️⃣ SUCCESS MESSAGE
        showAlert("Succès", "Catégorie ajoutée avec succès !");

        nomField.clear();

        // 3️⃣ OPEN TABLE VIEW
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/CategorieList.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage) nomField.getScene().getWindow(); // 👈 même fenêtre
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