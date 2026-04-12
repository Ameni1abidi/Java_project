package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.entities.User;
import tn.esprit.services.UserService;

import java.sql.SQLException;

public class UserController {

    // ── Champs formulaire ────────────────────────────────────────────────────
    @FXML private TextField     nameField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;

    // ── TableView ────────────────────────────────────────────────────────────
    @FXML private TableView<User>           table;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String>  colName;
    @FXML private TableColumn<User, String>  colEmail;

    // ── Service ──────────────────────────────────────────────────────────────
    private final UserService userService = new UserService();

    // ── Initialisation ───────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("nom"));     // ← "nom" pas "name"
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        loadUsers();

        table.setOnMouseClicked(e -> selectUser());
    }

    // ── Charger les users ────────────────────────────────────────────────────
    public void loadUsers() {
        try {
            table.setItems(FXCollections.observableArrayList(userService.getAllUsers()));
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les utilisateurs : " + e.getMessage());
        }
    }

    // ── Ajouter user ─────────────────────────────────────────────────────────
    @FXML
    public void addUser() {
        if (nameField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showAlert("Erreur", "Champs obligatoires !");
            return;
        }

        User user = new User(
                0,
                nameField.getText(),
                passwordField.getText(),
                emailField.getText(),
                User.Role.ETUDIANT
        );

        try {
            userService.register(user);
            showAlert("Succès", "Utilisateur ajouté !");
            clearFields();
            loadUsers();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Problème lors de l'ajout : " + e.getMessage());
        }
    }                                                  // ← accolade manquante ajoutée ici

    // ── Supprimer user ───────────────────────────────────────────────────────
    @FXML
    public void deleteUser() {
        User selected = table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Erreur", "Sélectionnez un utilisateur !");
            return;
        }

        try {
            userService.deleteUser(selected.getId());
            showAlert("Succès", "Utilisateur supprimé !");
            loadUsers();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Problème lors de la suppression : " + e.getMessage());
        }
    }

    // ── Sélection dans la table ──────────────────────────────────────────────
    public void selectUser() {
        User selected = table.getSelectionModel().getSelectedItem();

        if (selected != null) {
            nameField.setText(selected.getNom());
            emailField.setText(selected.getEmail());
            passwordField.setText(selected.getPassword());
        }
    }

    // ── Vider les champs ─────────────────────────────────────────────────────
    public void clearFields() {
        nameField.clear();
        emailField.clear();
        passwordField.clear();
    }

    // ── Afficher une alerte ──────────────────────────────────────────────────
    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
