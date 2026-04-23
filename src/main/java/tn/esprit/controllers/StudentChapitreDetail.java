package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import tn.esprit.entities.Chapitre;
import tn.esprit.services.AIService;
import tn.esprit.services.PdfExtractor;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;

public class StudentChapitreDetail {
    @FXML
    private TextArea resumeText;
    @FXML private WebView pdfViewer;

    private Chapitre chapitre;

    public void setChapitre(Chapitre chap) {
        this.chapitre = chap;

        loadPDF();
        generateAIResume();
    }

    // ================= PDF =================
    private void loadPDF() {
        try {
            String path = "uploads/" + chapitre.getContenuFichier();
            pdfViewer.getEngine().load(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= AI RESUME =================
    private void generateAIResume() {

        try {
            String pdfText = PdfExtractor.extractText("uploads/" + chapitre.getContenuFichier());

            String resume = AIService.generateResume(pdfText);

            resumeText.setText(resume);

        } catch (Exception e) {
            resumeText.setText("Erreur génération résumé");
            e.printStackTrace();
        }
    }



}
