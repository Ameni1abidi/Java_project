package tn.esprit.controllers;
import tn.esprit.entities.User;
import tn.esprit.entities.User.Role;
import tn.esprit.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField      nomField;
    @FXML private TextField      emailField;
    @FXML private PasswordField  passwordField;
    @FXML private PasswordField  confirmField;
    @FXML private ComboBox<Role> roleCombo;
    @FXML private Label          messageLabel;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        // ADMIN ne s'inscrit pas via ce formulaire
        roleCombo.getItems().addAll(Role.PROF, Role.ETUDIANT, Role.PARENT);
        roleCombo.setValue(Role.ETUDIANT);
    }

    @FXML
    private void handleRegister() {
        String nom     = nomField.getText().trim();
        String email   = emailField.getText().trim();
        String pw      = passwordField.getText();
        String confirm = confirmField.getText();
        Role   role    = roleCombo.getValue();

        // ── Validations ──────────────────────────────────────────────────────
        if (nom.isEmpty() || email.isEmpty() || pw.isEmpty()) {
            showError("Tous les champs sont obligatoires.");
            return;
        }
        if (!email.matches("^[\\w.+\\-]+@[\\w\\-]+\\.[a-z]{2,}$")) {
            showError("Format d'email invalide.");
            return;
        }
        if (!pw.equals(confirm)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }
        if (pw.length() < 6) {
            showError("Mot de passe trop court (min. 6 caractères).");
            return;
        }

        try {
            boolean ok = userService.register(new User(nom, pw, email, role));

            if (!ok) {
                showError("Cet email est déjà utilisé.");
                return;
            }

            showSuccess("Compte créé avec succès ! Redirection...");

            // Petite pause pour laisser lire le message, puis retour au login
            new Thread(() -> {
                try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() -> {
                    try { handleGoLogin(); } catch (Exception ex) { ex.printStackTrace(); }
                });
            }).start();

        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoLogin() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
        messageLabel.setText(msg);
        messageLabel.setVisible(true);
    }

    private void showSuccess(String msg) {
        messageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px;");
        messageLabel.setText(msg);
        messageLabel.setVisible(true);
    }
}