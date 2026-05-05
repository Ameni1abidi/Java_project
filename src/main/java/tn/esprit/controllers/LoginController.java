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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.services.AuditLogService;
import tn.esprit.services.LoginSecurityService;
import tn.esprit.services.PasswordResetService;
import tn.esprit.services.SessionService;
import tn.esprit.services.auth.GitHubAuthService;
import tn.esprit.services.auth.GoogleAuthService;
import tn.esprit.utils.UserSession;

import java.net.InetAddress;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class LoginController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;
    @FXML private CheckBox      rememberMeCheckBox;

    private final UserService userService = new UserService();
    private final AuditLogService auditLogService = new AuditLogService();
    private final PasswordResetService passwordResetService = new PasswordResetService();
    private final GoogleAuthService googleAuthService = new GoogleAuthService();
    private final GitHubAuthService gitHubAuthService = new GitHubAuthService();
    private final LoginSecurityService loginSecurityService = new LoginSecurityService();
    private final SessionService sessionService = new SessionService();
    private static final DateTimeFormatter LOCK_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String pw    = passwordField.getText().trim();

        if (email.isEmpty() || pw.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        try {
            LoginSecurityService.LoginContext ctx = new LoginSecurityService.LoginContext(bestEffortLocalIp(), deviceFingerprint());
            Optional<User> result = userService.login(email, pw, ctx);

            if (result.isEmpty()) {
                auditLogService.log(email, "LOGIN_FAILED", "Login failed for provided credentials");
                showError("Email ou mot de passe incorrect.");
                return;
            }

            User connectedUser = result.get();
            UserSession.setCurrentUser(connectedUser);
            boolean remember = rememberMeCheckBox != null && rememberMeCheckBox.isSelected();
            String token = sessionService.createSession(connectedUser.getId(), bestEffortLocalIp(), deviceFingerprint(), remember);
            UserSession.setSessionToken(token);
            auditLogService.log(connectedUser.getEmail(), "LOGIN_SUCCESS", "User logged in with role " + connectedUser.getRole());
            redirectByRole(connectedUser);

        } catch (IllegalStateException e) {
            // Example: locked / not verified / blocked / status denied...
            if (isLockedMessage(e.getMessage())) {
                showLockedPopup(email, e.getMessage());
                return;
            }
            showError("Erreur : " + formatError(e));
        } catch (Exception e) {
            showError("Erreur : " + formatError(e));
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
    private void handleForgotPassword() {
        try {
            Optional<String> emailOpt = askEmailForReset();
            if (emailOpt.isEmpty()) return;
            String email = emailOpt.get().trim();
            if (email.isBlank()) {
                showError("Email requis pour la réinitialisation.");
                return;
            }
            if (userService.findByEmail(email).isEmpty()) {
                showError("Aucun compte trouvé avec cet email.");
                return;
            }

            passwordResetService.sendResetCode(email);

            Optional<ResetPayload> payload = askResetPayload(email);
            if (payload.isEmpty()) {
                showError("Réinitialisation annulée.");
                return;
            }
            ResetPayload p = payload.get();
            if (!passwordResetService.verifyCode(email, p.code())) {
                showError("Code invalide ou expiré.");
                return;
            }
            if (!p.newPassword().equals(p.confirmPassword())) {
                showError("Les mots de passe ne correspondent pas.");
                return;
            }
            if (p.newPassword().trim().length() < 6) {
                showError("Mot de passe trop court (min. 6 caractères).");
                return;
            }

            userService.resetPasswordByEmail(email, p.newPassword());
            passwordResetService.consume(email);
            errorLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-size: 12px; -fx-background-color: #E8F5E9; -fx-padding: 8 12; -fx-background-radius: 8;");
            errorLabel.setText("Mot de passe réinitialisé avec succès.");
            errorLabel.setVisible(true);
        } catch (Exception e) {
            showError("Reset mot de passe échoué: " + e.getMessage());
        }
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
                boolean remember = rememberMeCheckBox != null && rememberMeCheckBox.isSelected();
                String token = sessionService.createSession(connectedUser.getId(), bestEffortLocalIp(), deviceFingerprint(), remember);
                UserSession.setSessionToken(token);
                userService.onExternalLoginSuccess(
                        connectedUser,
                        new LoginSecurityService.LoginContext(bestEffortLocalIp(), deviceFingerprint())
                );
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
                boolean remember = rememberMeCheckBox != null && rememberMeCheckBox.isSelected();
                String token = sessionService.createSession(connectedUser.getId(), bestEffortLocalIp(), deviceFingerprint(), remember);
                UserSession.setSessionToken(token);
                userService.onExternalLoginSuccess(
                        connectedUser,
                        new LoginSecurityService.LoginContext(bestEffortLocalIp(), deviceFingerprint())
                );
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

    private Optional<String> askEmailForReset() {
        TextInputDialog d = new TextInputDialog(emailField != null ? emailField.getText() : "");
        d.setTitle("Mot de passe oublié");
        d.setHeaderText("Réinitialiser le mot de passe");
        d.setContentText("Email du compte:");
        return d.showAndWait().map(String::trim).filter(s -> !s.isBlank());
    }

    private Optional<ResetPayload> askResetPayload(String email) {
        Dialog<ResetPayload> dialog = new Dialog<>();
        dialog.setTitle("Confirmation reset");
        dialog.setHeaderText("Code envoyé à " + email);

        ButtonType confirmType = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmType, ButtonType.CANCEL);

        PasswordField codeField = new PasswordField();
        codeField.setPromptText("Code reçu par email");
        PasswordField newPwField = new PasswordField();
        newPwField.setPromptText("Nouveau mot de passe");
        PasswordField confirmPwField = new PasswordField();
        confirmPwField.setPromptText("Confirmer le mot de passe");

        VBox box = new VBox(8, new Label("Code"), codeField, new Label("Nouveau mot de passe"), newPwField, new Label("Confirmation"), confirmPwField);
        dialog.getDialogPane().setContent(box);

        dialog.setResultConverter(btn -> {
            if (btn == confirmType) {
                return new ResetPayload(
                        codeField.getText() == null ? "" : codeField.getText().trim(),
                        newPwField.getText() == null ? "" : newPwField.getText(),
                        confirmPwField.getText() == null ? "" : confirmPwField.getText()
                );
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private record ResetPayload(String code, String newPassword, String confirmPassword) {}

    private static boolean isLockedMessage(String msg) {
        if (msg == null) return false;
        String m = msg.toLowerCase();
        return m.contains("verrouill") || m.contains("locked");
    }

    private void showLockedPopup(String email, String fallbackMessage) {
        String content = fallbackMessage == null ? "Compte temporairement verrouillé." : fallbackMessage;
        try {
            var lock = loginSecurityService.getActiveLock(email);
            if (lock.isPresent() && lock.get().lockedUntil() != null) {
                content = "Compte temporairement verrouillé jusqu'à: " + LOCK_TIME_FMT.format(lock.get().lockedUntil());
            }
        } catch (Exception ignored) {
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Sécurité — Compte verrouillé");
        alert.setHeaderText("Trop de tentatives échouées");
        alert.setContentText(content);
        alert.showAndWait();
    }

    private static String bestEffortLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static String deviceFingerprint() {
        String os = System.getProperty("os.name", "unknown");
        String ver = System.getProperty("os.version", "");
        String user = System.getProperty("user.name", "");
        String host = "";
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (Exception ignored) {
        }
        return ("os=" + os + " " + ver + "; user=" + user + "; host=" + host).trim();
    }
}