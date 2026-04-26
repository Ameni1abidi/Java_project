package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.concurrent.Task;
import tn.esprit.entities.User;
import tn.esprit.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.services.AuditLogService;
import tn.esprit.services.auth.GitHubAuthService;
import tn.esprit.services.auth.GoogleAuthService;
import tn.esprit.utils.UserSession;

import java.util.Optional;

public class LoginController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;

    private final UserService userService = new UserService();
    private final AuditLogService auditLogService = new AuditLogService();
    private final GoogleAuthService googleAuthService = new GoogleAuthService();
    private final GitHubAuthService gitHubAuthService = new GitHubAuthService();

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
                auditLogService.log(email, "LOGIN_FAILED", "Login failed for provided credentials");
                showError("Email ou mot de passe incorrect.");
                return;
            }

            User connectedUser = result.get();
            UserSession.setCurrentUser(connectedUser);
            auditLogService.log(connectedUser.getEmail(), "LOGIN_SUCCESS", "User logged in with role " + connectedUser.getRole());
            redirectByRole(connectedUser);

        } catch (Exception e) {
            showError("Erreur : " + formatError(e));
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
    private void handleGoogleLogin() {
        errorLabel.setVisible(false);
        Task<User> task = new Task<>() {
            @Override
            protected User call() throws Exception {
                GoogleAuthService.GoogleProfile profile = googleAuthService.authenticate();
                return userService.findOrCreateGoogleUser(profile.email(), profile.name());
            }
        };

        task.setOnRunning(event -> {
            errorLabel.setStyle("-fx-text-fill: #7E57C2; -fx-font-size: 12px;");
            errorLabel.setText("Ouverture de Google dans le navigateur...");
            errorLabel.setVisible(true);
        });

        task.setOnSucceeded(event -> {
            try {
                User connectedUser = task.getValue();
                UserSession.setCurrentUser(connectedUser);
                auditLogService.log(connectedUser.getEmail(), "LOGIN_GOOGLE_SUCCESS", "User logged in with Google");
                redirectByRole(connectedUser);
            } catch (Exception e) {
                showError("Erreur Google : " + e.getMessage());
            }
        });

        task.setOnFailed(event -> {
            String err = task.getException() != null ? task.getException().getMessage() : "Erreur inconnue";
            showError("Connexion Google echouee : " + err);
        });

        Thread th = new Thread(task, "google-login");
        th.setDaemon(true);
        th.start();
    }

    @FXML
    private void handleGithubLogin() {
        errorLabel.setVisible(false);
        Task<User> task = new Task<>() {
            @Override
            protected User call() throws Exception {
                GitHubAuthService.GitHubProfile profile = gitHubAuthService.authenticate();
                return userService.findOrCreateGithubUser(profile.email(), profile.name());
            }
        };

        task.setOnRunning(event -> {
            errorLabel.setStyle("-fx-text-fill: #7E57C2; -fx-font-size: 12px;");
            errorLabel.setText("Ouverture de GitHub dans le navigateur...");
            errorLabel.setVisible(true);
        });

        task.setOnSucceeded(event -> {
            try {
                User connectedUser = task.getValue();
                UserSession.setCurrentUser(connectedUser);
                auditLogService.log(connectedUser.getEmail(), "LOGIN_GITHUB_SUCCESS", "User logged in with GitHub");
                redirectByRole(connectedUser);
            } catch (Exception e) {
                showError("Erreur GitHub : " + e.getMessage());
            }
        });

        task.setOnFailed(event -> {
            String err = task.getException() != null ? task.getException().getMessage() : "Erreur inconnue";
            showError("Connexion GitHub echouee : " + err);
        });

        Thread th = new Thread(task, "github-login");
        th.setDaemon(true);
        th.start();
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
        errorLabel.setStyle("-fx-text-fill: #E53935; -fx-font-size: 12px; -fx-background-color: #FFEBEE; -fx-padding: 8 12; -fx-background-radius: 8;");
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    private static String formatError(Throwable t) {
        if (t == null) return "Erreur inconnue";
        Throwable root = t;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String type = root.getClass().getSimpleName();
        String msg = root.getMessage();
        if (msg == null || msg.isBlank()) msg = "message vide";
        StackTraceElement[] st = root.getStackTrace();
        String at = (st != null && st.length > 0)
                ? (st[0].getClassName() + ":" + st[0].getLineNumber())
                : "unknown";
        return type + " — " + msg + " @ " + at;
    }
}