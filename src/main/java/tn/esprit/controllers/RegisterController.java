package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import com.sun.net.httpserver.HttpServer;
import tn.esprit.entities.User;
import tn.esprit.entities.User.Role;
import tn.esprit.config.LocalSecrets;
import tn.esprit.services.security.RecaptchaService;
import tn.esprit.services.UserService;
import tn.esprit.services.EmailVerificationService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class RegisterController {
    @FXML private TextField      nomField;
    @FXML private TextField      emailField;
    @FXML private PasswordField  passwordField;
    @FXML private PasswordField  confirmField;
    @FXML private TextField      passwordVisibleField;
    @FXML private TextField      confirmVisibleField;
    @FXML private WebView        recaptchaWebView;
    @FXML private Button         registerButton;
    @FXML private CheckBox       togglePasswordsCheckBox;
    @FXML private CheckBox       agreeTermsCheckBox;
    @FXML private ComboBox<Role> roleCombo;
    @FXML private Label          messageLabel;

    private final UserService userService = new UserService();
    private final RecaptchaService recaptchaService = new RecaptchaService();
    private final EmailVerificationService emailVerificationService = new EmailVerificationService();
    private volatile String recaptchaToken = "";
    private HttpServer recaptchaLocalServer;

    @FXML
    public void initialize() {
        roleCombo.getItems().addAll(
                User.Role.ROLE_PROF,
                User.Role.ROLE_ETUDIANT,
                User.Role.ROLE_PARENT
        );
        roleCombo.setValue(User.Role.ROLE_ETUDIANT);
        if (passwordVisibleField != null) {
            passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
        }
        if (confirmVisibleField != null) {
            confirmVisibleField.textProperty().bindBidirectional(confirmField.textProperty());
        }
        if (togglePasswordsCheckBox != null) {
            togglePasswordsCheckBox.selectedProperty().addListener((obs, oldVal, show) -> togglePasswordVisibility(show));
        }
        initRecaptchaWidget();
    }

    @FXML
    private void handleRegister() {
        String nom     = nomField.getText().trim();
        String email   = emailField.getText().trim();
        String pw      = passwordField.getText();
        String confirm = confirmField.getText();
        Role   role    = roleCombo.getValue();

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
        if (agreeTermsCheckBox != null && !agreeTermsCheckBox.isSelected()) {
            showError("Veuillez accepter les conditions d'utilisation.");
            return;
        }
        // Fallback: some JavaFX WebView runs do not always trigger JS->Java callback reliably.
        String liveToken = readRecaptchaTokenFromWebView();
        if (liveToken != null && !liveToken.isBlank()) {
            recaptchaToken = liveToken.trim();
        }
        if (recaptchaToken == null || recaptchaToken.isBlank()) {
            showError("Veuillez valider le reCAPTCHA.");
            return;
        }

        try {
            RecaptchaService.RecaptchaResult captcha = recaptchaService.verify(recaptchaToken);
            if (!captcha.success()) {
                showError("Verification reCAPTCHA echouee: " + (captcha.errors().isBlank() ? "token invalide" : captcha.errors()));
                resetRecaptchaWidget();
                return;
            }
            boolean ok = userService.register(new User(nom, pw, email, role));

            if (!ok) {
                showError("Cet email est déjà utilisé.");
                resetRecaptchaWidget();
                return;
            }

            showSuccess("Compte créé. Envoi du code de confirmation...");
            emailVerificationService.sendVerificationCode(email);

            Optional<String> code = askForVerificationCode(email);
            if (code.isEmpty()) {
                showError("Confirmation annulée. Votre compte reste en attente.");
                resetRecaptchaWidget();
                return;
            }
            if (!emailVerificationService.verifyCode(email, code.get())) {
                showError("Code invalide ou expiré. Veuillez réessayer.");
                resetRecaptchaWidget();
                return;
            }
            emailVerificationService.markConsumed(email);
            userService.markUserVerified(email);

            showSuccess("Email confirmé. Vous pouvez vous connecter.");
            new Thread(() -> {
                try { Thread.sleep(900); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> {
                    try { handleGoLogin(); } catch (Exception ex) { ex.printStackTrace(); }
                });
            }).start();

        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
            resetRecaptchaWidget();
            e.printStackTrace();
        }
    }

    private Optional<String> askForVerificationCode(String email) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Confirmation email");
        dialog.setHeaderText("Un code a été envoyé à " + email);
        dialog.setContentText("Code (6 chiffres):");
        dialog.getEditor().setPromptText("123456");
        return dialog.showAndWait().map(String::trim).filter(s -> !s.isBlank());
    }

    private void initRecaptchaWidget() {
        if (recaptchaWebView == null) return;

        String siteKey = LocalSecrets.get("RECAPTCHA_SITE_KEY");
        if (siteKey == null || siteKey.isBlank()) {
            if (registerButton != null) registerButton.setDisable(true);
            showError("Configuration manquante: RECAPTCHA_SITE_KEY");
            return;
        }

        WebEngine engine = recaptchaWebView.getEngine();
        engine.setJavaScriptEnabled(true);
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaRecaptcha", new RecaptchaBridge());
            }
        });
        try {
            ensureRecaptchaLocalServer(siteKey.trim());
            int port = recaptchaLocalServer.getAddress().getPort();
            engine.load("http://localhost:" + port + "/recaptcha");
        } catch (IOException e) {
            showError("Impossible de demarrer le serveur local reCAPTCHA: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            if (registerButton != null) registerButton.setDisable(true);
        }
    }

    private String buildRecaptchaHtml(String siteKey) {
        String escapedKey = siteKey.replace("\\", "\\\\").replace("'", "\\'");
        return """
                <!doctype html>
                <html>
                <head>
                  <meta charset='utf-8'>
                  <script src='https://www.google.com/recaptcha/api.js' async defer></script>
                  <style>
                    body { margin: 0; font-family: Arial, sans-serif; background: #FAF7FF; }
                    .wrap { padding: 8px; display: flex; justify-content: center; }
                  </style>
                </head>
                <body>
                  <div class='wrap'>
                    <div class='g-recaptcha'
                         data-sitekey='%s'
                         data-callback='onCaptchaSuccess'
                         data-expired-callback='onCaptchaExpired'
                         data-error-callback='onCaptchaError'></div>
                  </div>
                  <script>
                    function onCaptchaSuccess(token) { window.javaRecaptcha.onToken(token); }
                    function onCaptchaExpired() { window.javaRecaptcha.onToken(''); }
                    function onCaptchaError() { window.javaRecaptcha.onToken(''); }
                  </script>
                </body>
                </html>
                """.formatted(escapedKey);
    }

    private void ensureRecaptchaLocalServer(String siteKey) throws IOException {
        if (recaptchaLocalServer != null) return;
        // Bind on an ephemeral port to avoid collisions with already used ports.
        recaptchaLocalServer = HttpServer.create(new InetSocketAddress(0), 0);
        recaptchaLocalServer.createContext("/recaptcha", exchange -> {
            String html = buildRecaptchaHtml(siteKey);
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        recaptchaLocalServer.start();
    }

    public class RecaptchaBridge {
        public void onToken(String token) {
            Platform.runLater(() -> recaptchaToken = token == null ? "" : token.trim());
        }
    }

    private String readRecaptchaTokenFromWebView() {
        try {
            if (recaptchaWebView == null) return "";
            Object value = recaptchaWebView.getEngine().executeScript(
                    "window.grecaptcha && grecaptcha.getResponse ? grecaptcha.getResponse() : ''"
            );
            return value == null ? "" : String.valueOf(value).trim();
        } catch (Exception ignored) {
            return "";
        }
    }

    private void resetRecaptchaWidget() {
        recaptchaToken = "";
        try {
            if (recaptchaWebView != null) {
                recaptchaWebView.getEngine().executeScript(
                        "if (window.grecaptcha && grecaptcha.reset) { grecaptcha.reset(); }"
                );
            }
        } catch (Exception ignored) {
        }
    }

    private void togglePasswordVisibility(boolean showPlainText) {
        if (passwordVisibleField != null) {
            passwordVisibleField.setVisible(showPlainText);
            passwordVisibleField.setManaged(showPlainText);
        }
        if (confirmVisibleField != null) {
            confirmVisibleField.setVisible(showPlainText);
            confirmVisibleField.setManaged(showPlainText);
        }
        passwordField.setVisible(!showPlainText);
        passwordField.setManaged(!showPlainText);
        confirmField.setVisible(!showPlainText);
        confirmField.setManaged(!showPlainText);
    }

    @FXML
    private void handleGoLogin() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
        Stage stage = (Stage) nomField.getScene().getWindow();
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