package tn.esprit.controllers;

import javafx.event.ActionEvent;
import tn.esprit.entities.User;
import tn.esprit.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Optional;

public class LoginController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;

    private final UserService userService = new UserService();

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String pw    = passwordField.getText().trim();

        if (email.isEmpty() || pw.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        try {
            Optional<User> result = userService.login(email, pw);

            if (result.isEmpty()) {
                showError("Email ou mot de passe incorrect.");
                return;
            }

            redirectByRole(result.get());

        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void redirectByRole(User user) throws Exception {
        String fxml = switch (user.getRole()) {
            case ROLE_ADMIN    -> "/GestionUsers.fxml";
            case ROLE_PROF     -> "/ProfDashboard.fxml";
            case ROLE_ETUDIANT -> "/EtudiantDashboard.fxml";
            case ROLE_PARENT   -> "/ParentDashboard.fxml";
            default            -> "/Login.fxml";
        };
        var resource = getClass().getResource(fxml);
        if (resource == null) {
            // Fallback to Home when specific dashboard is not yet created.
            resource = getClass().getResource("/Home.fxml");
            showError("Dashboard indisponible pour ce role. Redirection vers l'accueil.");
            if (resource == null) {
                throw new IllegalStateException("Home.fxml introuvable");
            }
        }

        Parent root = FXMLLoader.load(resource);
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("EduFlex — " + user.getNom());
        stage.show();
    }

    @FXML
    private void handleGoRegister() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/Register.fxml"));
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(new Scene(root));
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

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}