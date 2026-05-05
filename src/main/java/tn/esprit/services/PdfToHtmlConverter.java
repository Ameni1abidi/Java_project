package tn.esprit.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;

public class PdfToHtmlConverter {

    public static String convert(File file) {
        try (PDDocument document = PDDocument.load(file)) {

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            String formatted = text
                    .replaceAll("(?m)^(.+):$", "<h3>$1</h3>") // titres
                    .replace("\n\n", "<br><br>")
                    .replace("\n", "<br>");
            return """
<html>
<head>
<style>
body {
    font-family: 'Times New Roman', serif;
    background: #f5f5f5;
    padding: 30px;
}

.page {
    background: white;
    width: 800px;
    margin: auto;
    padding: 40px;
    box-shadow: 0 0 10px rgba(0,0,0,0.2);
    line-height: 1.6;
}

h1, h2, h3 {
    color: #222;
    margin-top: 20px;
}

p {
    margin: 10px 0;
}

</style>
</head>

<body>
<div class="page">
%s
</div>
</body>
</html>
""".formatted(formatted);

        } catch (Exception e) {
            e.printStackTrace();
            return "<h3>Erreur lecture PDF</h3>";
        }
    }
}
