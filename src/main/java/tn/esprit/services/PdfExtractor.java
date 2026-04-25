package tn.esprit.services;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;

public class PdfExtractor {

    public static String extractText(String filePath) {
        try {
            File file = new File(filePath);

            if (!file.exists()) {
                System.out.println("❌ File not found: " + filePath);
                return "";
            }

            PDDocument doc = PDDocument.load(file);

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);

            doc.close();

            System.out.println("TEXT LENGTH = " + text.length());

            return text;

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
