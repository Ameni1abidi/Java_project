package tn.esprit.services;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;

public class PdfExtractor {

    public static String extractText(String filePath) {
        try {
            PDDocument doc = PDDocument.load(new File(filePath));
            PDFTextStripper stripper = new PDFTextStripper();

            String text = stripper.getText(doc);
            doc.close();

            return text;

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
