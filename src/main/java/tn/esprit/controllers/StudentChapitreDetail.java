package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.print.PrinterJob;

import tn.esprit.entities.Chapitre;
import tn.esprit.entities.Cours;
import tn.esprit.entities.StudentChapitreProgress;
import tn.esprit.services.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

public class StudentChapitreDetail {

    @FXML private TextArea resumeText;
    @FXML private WebView pdfViewer;
    @FXML private Button doneBtn;

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

    // ================= USER =================
    private int getCurrentUserId() {
        return 3;
    }
}
