package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.AuditLogService;
import tn.esprit.services.SessionService;
import tn.esprit.utils.UserSession;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ParentDashboardController {
    private final AuditLogService auditLogService = new AuditLogService();
    private final SessionService sessionService = new SessionService();
    private static final DateTimeFormatter SESSION_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private Label dateLabel;
    @FXML
    private ListView<String> planningList;

    @FXML
    public void initialize() {
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy")));
        planningList.setItems(FXCollections.observableArrayList(
                "Lun 09:00 - Mathematiques (enfant)",
                "Mar 14:30 - Java (enfant)",
                "Jeu 11:00 - Evaluation continue",
                "Ven 16:00 - Entretien parent-prof"
        ));
    }

    @FXML
    private void goCoursList(ActionEvent event) {
        loadPage(event, "/CoursList.fxml");
    }

    @FXML
    private void goEvaluations(ActionEvent event) {
        loadPage(event, "/EvaluationView.fxml");
    }

    @FXML
    private void goExams(ActionEvent event) {
        loadPage(event, "/ExamenView.fxml");
    }

    @FXML
    private void goBack(ActionEvent event) {
        loadPage(event, "/Home.fxml");
    }

    @FXML
    private void goHome(ActionEvent event) {
        loadPage(event, "/Home.fxml");
    }

    @FXML
    private void logout(ActionEvent event) {
        User current = UserSession.getCurrentUser();
        if (current != null) {
            auditLogService.log(current.getEmail(), "LOGOUT", "User logged out from Parent dashboard");
        }
        try {
            sessionService.revoke(UserSession.getSessionToken());
        } catch (Exception ignored) {}
        UserSession.clear();
        loadPage(event, "/Login.fxml");
    }

    @FXML
    private void showSessions(ActionEvent event) {
        User current = UserSession.getCurrentUser();
        if (current == null) {
            loadPage(event, "/Login.fxml");
            return;
        }
        try {
            String token = UserSession.getSessionToken();
            if (token == null || !sessionService.isValid(token)) {
                auditLogService.log(current.getEmail(), "SESSION_EXPIRED", "Session expired (Parent dashboard)");
                UserSession.clear();
                loadPage(event, "/Login.fxml");
                return;
            }
            sessionService.touch(token);

            List<SessionService.SessionInfo> sessions = sessionService.listActiveSessions(current.getId());
            StringBuilder sb = new StringBuilder();
            sb.append("Sessions actives: ").append(sessions.size()).append("\n\n");
            for (SessionService.SessionInfo s : sessions) {
                sb.append("- lastSeen=")
                        .append(s.lastSeenAt() == null ? "N/A" : SESSION_FMT.format(s.lastSeenAt()))
                        .append(", expires=")
                        .append(s.expiresAt() == null ? "N/A" : SESSION_FMT.format(s.expiresAt()))
                        .append(", ip=").append(s.ip() == null ? "N/A" : s.ip())
                        .append("\n  device=").append(s.fingerprint() == null ? "N/A" : s.fingerprint())
                        .append(s.token() != null && s.token().equals(token) ? "  (cette session)" : "")
                        .append("\n");
            }

            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Sessions");
            a.setHeaderText("Gestion de sessions");
            a.setContentText(sb.toString());

            ButtonType disconnectAll = new ButtonType("Déconnecter tous les appareils", ButtonBar.ButtonData.OK_DONE);
            a.getButtonTypes().setAll(disconnectAll, ButtonType.CLOSE);
            a.showAndWait().ifPresent(btn -> {
                if (btn == disconnectAll) {
                    try {
                        sessionService.revokeAllForUser(current.getId());
                        auditLogService.log(current.getEmail(), "SESSION_REVOKE_ALL", "User revoked all sessions (Parent dashboard)");
                    } catch (Exception ignored) {
                    }
                    UserSession.clear();
                    loadPage(event, "/Login.fxml");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Sessions");
            alert.setHeaderText("Impossible de charger les sessions");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void loadPage(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
