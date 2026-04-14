package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.entities.User.Role;
import tn.esprit.services.UserService;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class UserController {

    // ── Formulaire ───────────────────────────────────────────────────────────
    @FXML private TextField        nameField;
    @FXML private TextField        emailField;
    @FXML private PasswordField    passwordField;
    @FXML private ComboBox<String> roleAddCombo;

    // ── Recherche ────────────────────────────────────────────────────────────
    @FXML private TextField searchField;

    // ── TableView ────────────────────────────────────────────────────────────
    @FXML private TableView<User>            table;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String>  colName;
    @FXML private TableColumn<User, String>  colEmail;
    @FXML private TableColumn<User, String>  colRole;

    // ── Filtre + compteur ────────────────────────────────────────────────────
    @FXML private ComboBox<String> filterRoleCombo;
    @FXML private Label            countLabel;

    // ── Service + données ────────────────────────────────────────────────────
    private final UserService userService = new UserService();
    private ObservableList<User> allUsers = FXCollections.observableArrayList();

    // ── Initialisation ───────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        // ComboBox rôle formulaire
        roleAddCombo.getItems().addAll(
                "ROLE_PROF", "ROLE_ETUDIANT", "ROLE_PARENT"
        );
        roleAddCombo.setValue("ROLE_ETUDIANT");

        // ComboBox filtre
        filterRoleCombo.getItems().addAll(
                "Tous", "ROLE_ADMIN", "ROLE_PROF", "ROLE_ETUDIANT", "ROLE_PARENT"
        );
        filterRoleCombo.setValue("Tous");
        filterRoleCombo.setOnAction(e -> applyFilter());

        loadUsers();
        table.setOnMouseClicked(e -> selectUser());
    }

    // ── Charger tous les users ───────────────────────────────────────────────
    @FXML
    public void loadUsers() {
        try {
            allUsers = FXCollections.observableArrayList(userService.getAllUsers());
            table.setItems(allUsers);
            filterRoleCombo.setValue("Tous");
            if (searchField != null) searchField.clear();
            updateCount(allUsers.size());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les utilisateurs : " + e.getMessage());
        }
    }

    // ── Recherche par nom ────────────────────────────────────────────────────
    @FXML
    public void handleSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
        String roleFilter = filterRoleCombo.getValue();

        List<User> filtered = allUsers.stream()
                .filter(u -> keyword.isEmpty() || u.getNom().toLowerCase().contains(keyword))
                .filter(u -> roleFilter == null || roleFilter.equals("Tous")
                        || u.getRole().name().equals(roleFilter))
                .collect(Collectors.toList());

        table.setItems(FXCollections.observableArrayList(filtered));
        updateCount(filtered.size());
    }

    // ── Filtrage par rôle ────────────────────────────────────────────────────
    @FXML
    public void applyFilter() {
        handleSearch(); // réutilise la logique de recherche combinée
    }

    // ── Ajouter user ─────────────────────────────────────────────────────────
    @FXML
    public void addUser() {
        String nom   = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pw    = passwordField.getText();
        Role   role  = Role.fromString(roleAddCombo.getValue());

        if (nom.isEmpty() || email.isEmpty() || pw.isEmpty()) {
            showAlert("Erreur", "Tous les champs sont obligatoires !");
            return;
        }
        if (!email.matches("^[\\w.+\\-]+@[\\w\\-]+\\.[a-z]{2,}$")) {
            showAlert("Erreur", "Format d'email invalide !");
            return;
        }
        if (pw.length() < 6) {
            showAlert("Erreur", "Mot de passe trop court (min. 6 caractères) !");
            return;
        }

        User user = new User(0, nom, pw, email, role);

        try {
            boolean ok = userService.register(user);
            if (!ok) {
                showAlert("Erreur", "Cet email est déjà utilisé !");
                return;
            }
            showAlert("Succès", "Utilisateur ajouté avec succès !");
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
            showAlert("Erreur", "Sélectionnez un utilisateur dans le tableau !");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer " + selected.getNom() + " ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userService.deleteUser(selected.getId());
                    showAlert("Succès", "Utilisateur supprimé !");
                    loadUsers();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Suppression échouée : " + e.getMessage());
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
            roleAddCombo.setValue(selected.getRole().name());
        }
    }

    // ── Vider les champs ─────────────────────────────────────────────────────
    @FXML
    public void clearFields() {
        nameField.clear();
        emailField.clear();
        passwordField.clear();
        roleAddCombo.setValue("ROLE_ETUDIANT");
    }
    @FXML
    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Utilitaires ──────────────────────────────────────────────────────────
    private void updateCount(int count) {
        if (countLabel != null)
            countLabel.setText(count + " utilisateur(s)");
    }

    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
