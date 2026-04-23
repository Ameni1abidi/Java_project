package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.scene.web.WebView;
import tn.esprit.entities.Chapitre;
import tn.esprit.entities.StudentChapitreProgress;
import tn.esprit.services.AIService;
import tn.esprit.services.PdfExtractor;
import tn.esprit.services.PdfToHtmlConverter;
import tn.esprit.services.StudentChapitreProgressService;

import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;

public class StudentChapitreDetail {

    @FXML
    private TextArea resumeText;

    @FXML
    private WebView pdfViewer;

    @FXML
    private Button doneBtn;

    private Chapitre chapitre;

    private final StudentChapitreProgressService progressService =
            new StudentChapitreProgressService();

    // ================= SET CHAPITRE =================
    public void setChapitre(Chapitre chap) {
        this.chapitre = chap;

        loadPDF();
        generateAIResume();
    }

    private File getPdfFile() {

        String path = chapitre.getContenuFichier();

        if (path == null || path.isEmpty()) {
            return null;
        }

        File file = new File(path);

        System.out.println("PATH = " + file.getAbsolutePath());

        return file;
    }
    // ================= PDF =================
    private void loadPDF() {
        try {
            File file = getPdfFile();

            if (file == null || !file.exists()) {
                System.out.println("❌ PDF not found");
                return;
            }

            String html = PdfToHtmlConverter.convert(file);

            pdfViewer.getEngine().loadContent(html);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // ================= AI RESUME =================
    private void generateAIResume() {
        try {

            File file = getPdfFile();

            if (file == null || !file.exists()) {
                resumeText.setText("❌ Aucun fichier PDF trouvé");
                return;
            }

            String pdfText = PdfExtractor.extractText(file.getAbsolutePath());

            if (pdfText == null || pdfText.trim().isEmpty()) {
                resumeText.setText("❌ PDF vide");
                return;
            }

            String resume = AIService.generateResume(pdfText);
            resumeText.setText(resume);

        } catch (Exception e) {
            e.printStackTrace();
            resumeText.setText("❌ Erreur génération résumé");
        }
    }

    // ================= MARK AS DONE =================
    @FXML
    private void handleTerminer() {

        try {
            int userId = getCurrentUserId(); // ⚠️ عدّلها حسب login متاعك

            StudentChapitreProgress p = progressService.find(userId, chapitre.getId());

            if (p == null) {
                p = new StudentChapitreProgress();
                p.setUtilisateurId(userId);
                p.setChapitreId(chapitre.getId());
                p.setStartedAt(LocalDateTime.now());
            }

            p.setCompletedAt(LocalDateTime.now());

            progressService.saveOrUpdate(p);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("🎉 Chapitre terminé !");
            alert.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= MOCK USER =================
    private int getCurrentUserId() {
        // replace with session / auth system
        return 3;
    }
}
