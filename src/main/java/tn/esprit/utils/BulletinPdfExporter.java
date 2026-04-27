package tn.esprit.utils;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import tn.esprit.entities.BulletinRow;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class BulletinPdfExporter {

    public static void exportBulletin(String filePath,
                                      String eleveName,
                                      List<BulletinRow> rows) {


        try {
            PdfWriter writer = new PdfWriter(filePath);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);

            PdfFont bold = PdfFontFactory.createFont();

            // ================= LOGO (AJOUTÉ) =================
            Image logo = new Image(
                    ImageDataFactory.create(
                            BulletinPdfExporter.class.getResource("/logo.png")
                    )
            ).scaleToFit(80, 80);

            // ================= HEADER OFFICIEL =================
            Paragraph ministry = new Paragraph(
                    "RÉPUBLIQUE TUNISIENNE\nMINISTÈRE DE L'ÉDUCATION\nDIRECTION GÉNÉRALE DE L'ENSEIGNEMENT"
            )
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(11)
                    .setBold();

            Paragraph title = new Paragraph("BULLETIN SCOLAIRE OFFICIEL")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(16)
                    .setBold()
                    .setFontColor(ColorConstants.DARK_GRAY);

            // HEADER AVEC LOGO
            Table header = new Table(2);
            header.setWidth(500);

            header.addCell(new Cell()
                    .add(logo)
                    .setBorder(null)
                    .setTextAlignment(TextAlignment.LEFT));

            header.addCell(new Cell()
                    .add(ministry)
                    .add(title)
                    .setBorder(null)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(header);

            doc.add(new Paragraph("\n"));

            // ================= INFO ÉLÈVE =================
            Table infoTable = new Table(2);
            infoTable.setWidth(500);

            infoTable.addCell(infoCell("Nom de l'élève"));
            infoTable.addCell(infoCell(eleveName));

            infoTable.addCell(infoCell("Année scolaire"));
            infoTable.addCell(infoCell("2025 / 2026"));

            infoTable.addCell(infoCell("Date d'émission"));
            infoTable.addCell(infoCell(LocalDate.now().toString()));

            doc.add(infoTable);

            doc.add(new Paragraph("\n"));

            // ================= TABLE NOTES =================
            Table table = new Table(3);
            table.setWidth(500);

            table.addHeaderCell(headerCell("EXAMEN"));
            table.addHeaderCell(headerCell("NOTE /20"));
            table.addHeaderCell(headerCell("APPRÉCIATION"));

            double sum = 0;

            for (BulletinRow r : rows) {
                table.addCell(cell(r.getExamenTitre()));
                table.addCell(cell(String.valueOf(r.getNote())));
                table.addCell(cell(r.getAppreciation()));
                sum += r.getNote();
            }

            doc.add(table);

            // ================= MOYENNE =================
            double avg = rows.isEmpty() ? 0 : sum / rows.size();

            String mention =
                    avg >= 16 ? "TRÈS BIEN" :
                            avg >= 14 ? "BIEN" :
                                    avg >= 12 ? "ASSEZ BIEN" :
                                            avg >= 10 ? "PASSABLE" :
                                                    "INSUFFISANT";

            doc.add(new Paragraph("\n"));

            doc.add(new Paragraph("MOYENNE GÉNÉRALE : " + String.format("%.2f", avg))
                    .setBold());

            doc.add(new Paragraph("MENTION : " + mention)
                    .setBold()
                    .setFontColor(ColorConstants.RED));

            doc.add(new Paragraph("DÉCISION : " +
                    (avg >= 10 ? "ADMIS" : "AJOURNÉ"))
                    .setBold());

            doc.add(new Paragraph("\n-------------------------------------"));

            // ================= SIGNATURE (AJOUTÉE) =================
            Image signature = new Image(
                    ImageDataFactory.create(
                            BulletinPdfExporter.class.getResource("/signature.png")
                    )
            ).scaleToFit(120, 60);

            Table footer = new Table(2);
            footer.setWidth(500);

            footer.addCell(new Cell()
                    .add(new Paragraph("Le Directeur des études"))
                    .add(signature)
                    .setBorder(null)
                    .setTextAlignment(TextAlignment.LEFT));

            footer.addCell(new Cell()
                    .add(new Paragraph("Cachet de l'établissement"))
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 1))
                    .setHeight(60));

            doc.add(footer);

            doc.add(new Paragraph("\n"));
            doc.add(new Paragraph("Document généré automatiquement - EDUFLEX")
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.close();

            System.out.println("✔ Bulletin officiel généré");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= STYLES =================
    private static Cell headerCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private static Cell cell(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setTextAlignment(TextAlignment.CENTER);
    }

    private static Cell infoCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold())
                .setBorder(new SolidBorder(ColorConstants.BLACK, 1));
    }


    private static String generateBulletinNumber(int eleveId) {
        String shortUuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String year = String.valueOf(LocalDate.now().getYear());

        return "EDF-" + year + "-" + eleveId + "-" + shortUuid;
    }
}