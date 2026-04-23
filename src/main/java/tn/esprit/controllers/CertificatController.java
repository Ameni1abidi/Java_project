package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Cours;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CertificatController {

    @FXML private VBox certifBox;
    @FXML private VBox alertBox;

    @FXML private Label studentName;
    @FXML private Label courseTitle;
    @FXML private Label dateLabel;
    @FXML private Label signatureLabel;
    @FXML private Label conditionText;

    public void setData(String nom, Cours cours, boolean eligible,
                        int spent, int required) {

        if (!eligible) {
            certifBox.setVisible(false);

            conditionText.setText(
                    "Temps passé: " + spent + " min / requis: " + required + " min"
            );
        } else {
            alertBox.setVisible(false);

            studentName.setText(nom);
            courseTitle.setText(cours.getTitre());

            dateLabel.setText(
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            );

            signatureLabel.setText(
                    "EDUFLEX-" + cours.getId() + "-" + nom.hashCode()
            );
        }
    }
    @FXML
    private void exportImage() {
        try {
            javafx.scene.image.WritableImage image = certifBox.snapshot(null, null);

            File file = new File("certificat.png");

            javax.imageio.ImageIO.write(
                    javafx.embed.swing.SwingFXUtils.fromFXImage(image, null),
                    "png",
                    file
            );

            System.out.println("✅ Image saved");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void exportPDF() {
        try {
            String dest = "certificat.pdf";

            com.itextpdf.kernel.pdf.PdfWriter writer =
                    new com.itextpdf.kernel.pdf.PdfWriter(dest);

            com.itextpdf.kernel.pdf.PdfDocument pdf =
                    new com.itextpdf.kernel.pdf.PdfDocument(writer);

            com.itextpdf.layout.Document document =
                    new com.itextpdf.layout.Document(pdf);

            // contenu
            document.add(new com.itextpdf.layout.element.Paragraph("Certificat EduFlex")
                    .setBold().setFontSize(20));

            document.add(new com.itextpdf.layout.element.Paragraph("\n"));

            document.add(new com.itextpdf.layout.element.Paragraph("Etudiant: " + studentName.getText()));
            document.add(new com.itextpdf.layout.element.Paragraph("Cours: " + courseTitle.getText()));
            document.add(new com.itextpdf.layout.element.Paragraph("Date: " + dateLabel.getText()));
            document.add(new com.itextpdf.layout.element.Paragraph("Signature: " + signatureLabel.getText()));

            document.close();

            System.out.println("✅ PDF created");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
