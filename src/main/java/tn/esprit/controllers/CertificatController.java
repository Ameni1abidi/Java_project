package tn.esprit.controllers;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.entities.Cours;

import javax.imageio.ImageIO;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;

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

            // cacher le button avant le screenshot
            certifBox.lookupAll(".button").forEach(node -> node.setVisible(false));

            // 📸 screenshot
            WritableImage image = certifBox.snapshot(new SnapshotParameters(), null);


            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer PDF");
            fileChooser.setInitialFileName("certificat.pdf");

            File file = fileChooser.showSaveDialog(certifBox.getScene().getWindow());
            if (file == null) return;


            PdfWriter writer = new PdfWriter(file.getAbsolutePath());
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 🖼 convert image
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", bos);

            ImageData imageData = ImageDataFactory.create(bos.toByteArray());
            Image img = new Image(imageData);

            img.setAutoScale(true);

            document.add(img);
            document.close();


            certifBox.lookupAll(".button").forEach(node -> node.setVisible(true));

            System.out.println("✅ PDF design saved");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleDownloadImage() {
        try {


            certifBox.lookupAll(".button").forEach(n -> n.setVisible(false));

            // 📸 snapshot clean
            WritableImage image = certifBox.snapshot(new SnapshotParameters(), null);


            certifBox.lookupAll(".button").forEach(n -> n.setVisible(true));

            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName("certificat.png");

            File file = fileChooser.showSaveDialog(certifBox.getScene().getWindow());

            if (file != null) {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPage(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void goBack(ActionEvent event) {
        loadPage(event, "/StudentCours.fxml");
    }
}
