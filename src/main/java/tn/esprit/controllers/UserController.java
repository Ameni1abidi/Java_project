package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.entities.User;
import tn.esprit.entities.User.Role;
import tn.esprit.services.UserService;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class UserController {

    // ── Formulaire ───────────────────────────────────────────────────────────
    @FXML private TextField     nameField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;

    // ── TableView ────────────────────────────────────────────────────────────
    @FXML private TableView<User>            table;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String>  colName;
    @FXML private TableColumn<User, String>  colEmail;
    @FXML private TableColumn<User, String>  colRole;

    // ── Filtre par rôle ──────────────────────────────────────────────────────
    @FXML private ComboBox<String> filterRoleCombo;

    // ── Service ──────────────────────────────────────────────────────────────
    private final UserService userService = new UserService();

    // Liste complète en mémoire pour le filtrage
    private ObservableList<User> allUsers = FXCollections.observableArrayList();

    // ── Initialisation ───────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Peupler le ComboBox de filtrage
        filterRoleCombo.getItems().addAll(
                "Tous",
                Role.ROLE_ADMIN.name(),
                Role.ROLE_PROF.name(),
                Role.ROLE_ETUDIANT.name(),
                Role.ROLE_PARENT.name()
        );
        filterRoleCombo.setValue("Tous");

        // Écouter les changements du filtre
        filterRoleCombo.setOnAction(e -> applyFilter());

        loadUsers();
        table.setOnMouseClicked(e -> selectUser());
    }

    // ── Charger tous les users ───────────────────────────────────────────────
    public void loadUsers() {
        try {
            allUsers = FXCollections.observableArrayList(userService.getAllUsers());
            table.setItems(allUsers);
            filterRoleCombo.setValue("Tous"); // reset filtre
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les utilisateurs : " + e.getMessage());
        }
    }

    // ── Filtrage par rôle ────────────────────────────────────────────────────
    @FXML
    public void applyFilter() {
        String selected = filterRoleCombo.getValue();

        if (selected == null || selected.equals("Tous")) {
            table.setItems(allUsers);
            return;
        }

        List<User> filtered = allUsers.stream()
                .filter(u -> u.getRole().name().equals(selected))
                .collect(Collectors.toList());

        table.setItems(FXCollections.observableArrayList(filtered));

        if (filtered.isEmpty()) {
            showAlert("Info", "Aucun utilisateur avec le rôle : " + selected);
        }
    }

    // ── Ajouter user ─────────────────────────────────────────────────────────
    @FXML
    public void addUser() {
        if (nameField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showAlert("Erreur", "Champs obligatoires !");
            return;
        }
        if (passwordField.getText().length() < 6) {
            showAlert("Erreur", "Mot de passe trop court (min. 6 caractères).");
            return;
        }

        User user = new User(
                0,
                nameField.getText(),
                passwordField.getText(),
                emailField.getText(),
                Role.ROLE_ETUDIANT
        );

        try {
            boolean ok = userService.register(user);
            if (!ok) {
                showAlert("Erreur", "Cet email est déjà utilisé !");
                return;
            }
            showAlert("Succès", "Utilisateur ajouté !");
            clearFields();
            loadUsers();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Problème lors de l'ajout : " + e.getMessage());
        }
    }

    // ── Supprimer user ───────────────────────────────────────────────────────
    @FXML
    public void deleteUser() {
        User selected = table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Erreur", "Sélectionnez un utilisateur !");
            return;
        }

        // Confirmation avant suppression
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setContentText("Supprimer " + selected.getNom() + " ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userService.deleteUser(selected.getId());
                    showAlert("Succès", "Utilisateur supprimé !");
                    loadUsers();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Problème lors de la suppression : " + e.getMessage());
                }
            }
        });
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

    // ── Alerte ───────────────────────────────────────────────────────────────
    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}