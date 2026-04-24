package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.print.PrinterJob;

import tn.esprit.entities.Chapitre;
import tn.esprit.entities.Cours;
import tn.esprit.entities.StudentChapitreProgress;
import tn.esprit.services.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

public class StudentChapitreDetail {

    @FXML private TextArea resumeText;
    @FXML private WebView pdfViewer;
    @FXML private Button doneBtn;
    @FXML private VBox chatBox;
    @FXML private TextField questionField;
    @FXML private Label typingLabel;

    private Chapitre chapitre;

    private final StudentChapitreProgressService progressService =
            new StudentChapitreProgressService();

    // ================= SET CHAPITRE =================
    public void setChapitre(Chapitre chap) {
        this.chapitre = chap;

        loadPDF();
        generateAIResume();

        // 🔥 OPEN action = 20%
        StudentChapitreProgress p =
                progressService.find(getCurrentUserId(), chapitre.getId());

        if (p == null) {
            p = new StudentChapitreProgress();
            p.setUtilisateurId(getCurrentUserId());
            p.setChapitreId(chapitre.getId());
            p.setStartedAt(LocalDateTime.now());
        }

        p.setOpened(true);
        p.setProgress(progressService.calculateProgress(p));

        progressService.saveOrUpdate(p);
    }

    // ================= PDF =================
    private void loadPDF() {
        try {
            File file = getPdfFile();

            if (file == null || !file.exists()) return;

            String html = PdfToHtmlConverter.convert(file);
            pdfViewer.getEngine().loadContent(html);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getPdfFile() {
        if (chapitre == null) return null;

        String path = chapitre.getContenuFichier();
        if (path == null || path.isEmpty()) return null;

        return new File(path);
    }

    // ================= DOWNLOAD =================
    @FXML
    private void handleDownload() {
        try {
            File file = getPdfFile();
            if (file == null || !file.exists()) return;

            FileChooser fc = new FileChooser();
            fc.setInitialFileName(file.getName());

            File dest = fc.showSaveDialog(pdfViewer.getScene().getWindow());

            if (dest != null) {
                Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= PRINT =================
    @FXML
    private void handlePrint() {
        try {
            PrinterJob job = PrinterJob.createPrinterJob();

            if (job != null && job.showPrintDialog(pdfViewer.getScene().getWindow())) {
                job.printPage(pdfViewer);
                job.endJob();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= AI RESUME =================
    private void generateAIResume() {
        try {

            File file = getPdfFile();
            if (file == null || !file.exists()) return;

            String text = PdfExtractor.extractText(file.getAbsolutePath());

            String resume = AIService.generateResume(text);
            resumeText.setText(resume);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= MARK AS DONE =================
    @FXML
    private void handleTerminer() {
        try {
            int userId = getCurrentUserId();

            StudentChapitreProgress p =
                    progressService.find(userId, chapitre.getId());

            if (p == null) {
                p = new StudentChapitreProgress();
                p.setUtilisateurId(userId);
                p.setChapitreId(chapitre.getId());
                p.setStartedAt(LocalDateTime.now());
            }

            p.setCompleted(true); // 🔥 مهم برشة
            p.setCompletedAt(LocalDateTime.now());
            p.setProgress(100);

            progressService.saveOrUpdate(p);

            doneBtn.setText("✔ Terminé");
            doneBtn.setDisable(true);

            showAlert("✔ Chapitre terminé ! Retour aux cours");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // ================= BACK =================
    @FXML
    private void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/studentCours.fxml"));
            Stage stage = (Stage) pdfViewer.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= ALERT =================
    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.show();
    }
    @FXML
    private void handleAsk() {

        String question = questionField.getText();
        if (question == null || question.isBlank()) return;

        // USER MESSAGE
        Label userMsg = new Label("🧑‍🎓 " + question);
        userMsg.setStyle("""
        -fx-background-color:#eae6ff;
        -fx-padding:8;
        -fx-background-radius:12;
        -fx-text-fill:#333;
    """);

        chatBox.getChildren().add(userMsg);
        questionField.clear();

        // TYPING...
        Label botTyping = new Label("🤖 bot is typing...");
        botTyping.setWrapText(true);
        botTyping.setMaxWidth(2000);
        botTyping.setStyle("""
    -fx-background-color:#7c4dff;
    -fx-text-fill:white;
    -fx-padding:12;
    -fx-background-radius:18;
    -fx-font-size:14px;
""");

        chatBox.getChildren().add(botTyping);

        // RUN AI IN BACKGROUND
        new Thread(() -> {

            String answer = generateAnswer(question);

            javafx.application.Platform.runLater(() -> {

                chatBox.getChildren().remove(botTyping);

                Label botMsg = new Label("🤖 " + answer);
                botMsg.setWrapText(true);

                botMsg.setStyle("""
                -fx-background-color:#7c4dff;
                -fx-text-fill:white;
                -fx-padding:10;
                -fx-background-radius:12;
                -fx-max-width:300;
            """);

                chatBox.getChildren().add(botMsg);
            });

        }).start();
    }
    private String generateAnswer(String question) {
        try {
            File file = getPdfFile();
            if (file == null || !file.exists()) {
                return "Chapitre introuvable.";
            }

            String text = PdfExtractor.extractText(file.getAbsolutePath());
            text = text.replaceAll("\\s+", " ");

            String prompt =
                    "Tu es un professeur universitaire expert en génie logiciel.\n" +
                            "Tu réponds UNIQUEMENT en français.\n" +
                            "Tu expliques comme un enseignant simple et clair.\n" +
                            "Réponse max 3 lignes.\n\n" +

                            "CHAMP DU COURS:\n" + text + "\n\n" +

                            "QUESTION:\n" + question + "\n\n" +

                            "RÉPONSE (en français uniquement):";

            return AIService.ask(prompt);

        } catch (Exception e) {
            return "AI error";
        }
    }
    // ================= USER =================
    private int getCurrentUserId() {
        return 3;
    }
}
