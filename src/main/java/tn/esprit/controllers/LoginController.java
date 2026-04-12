package tn.esprit.controllers;
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
        String pw    = passwordField.getText();

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

    /** Charge le bon FXML selon le rôle de l'utilisateur connecté. */
    private void redirectByRole(User user) throws Exception {
        String fxml = switch (user.getRole()) {
            case ADMIN    -> "/AdminDashboard.fxml";
            case PROF     -> "/ProfDashboard.fxml";
            case ETUDIANT -> "/EtudiantDashboard.fxml";
            case PARENT   -> "/ParentDashboard.fxml";
        };
        // ...
    }



    @FXML
    private void handleGoRegister() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/Register.fxml"));
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}