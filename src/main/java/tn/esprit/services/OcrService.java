package tn.esprit.services;

import org.json.JSONArray;
import org.json.JSONObject;
import tn.esprit.config.LocalSecrets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class OcrService {

    private static final String OCR_API_URL = "https://api.ocr.space/parse/image";
    private static final String DEFAULT_API_KEY = "helloworld";
    private static final String[] OCR_LANGUAGES = {"fre", "eng", "ara"};

    public String extractTextFromUrl(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IOException("URL image invalide.");
        }

        IOException lastError = null;
        for (String language : OCR_LANGUAGES) {
            try {
                String body = "apikey=" + encode(apiKey())
                        + "&url=" + encode(imageUrl)
                        + "&language=" + language
                        + "&isOverlayRequired=false"
                        + "&OCREngine=2"
                        + "&scale=true"
                        + "&detectOrientation=true";

                HttpURLConnection conn = openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }
                String text = parseResponse(readResponse(conn));
                if (!text.isBlank() || "ara".equals(language)) {
                    return text;
                }
            } catch (IOException e) {
                lastError = e;
            }
        }
        throw lastError == null ? new IOException("OCR indisponible.") : lastError;
    }

    public String extractTextFromFile(Path imagePath) throws IOException {
        if (imagePath == null || !Files.exists(imagePath)) {
            throw new IOException("Fichier image introuvable.");
        }

        IOException lastError = null;
        for (String language : OCR_LANGUAGES) {
            try {
                String boundary = "EduFlexOcrBoundary" + System.currentTimeMillis();
                HttpURLConnection conn = openConnection();
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                try (OutputStream os = conn.getOutputStream()) {
                    writeFormField(os, boundary, "apikey", apiKey());
                    writeFormField(os, boundary, "language", language);
                    writeFormField(os, boundary, "isOverlayRequired", "false");
                    writeFormField(os, boundary, "OCREngine", "2");
                    writeFormField(os, boundary, "scale", "true");
                    writeFormField(os, boundary, "detectOrientation", "true");
                    writeFileField(os, boundary, "file", imagePath);
                    os.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
                }

                String text = parseResponse(readResponse(conn));
                if (!text.isBlank() || "ara".equals(language)) {
                    return text;
                }
            } catch (IOException e) {
                lastError = e;
            }
        }
        throw lastError == null ? new IOException("OCR indisponible.") : lastError;
    }

    private HttpURLConnection openConnection() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(OCR_API_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(45000);
        conn.setDoOutput(true);
        return conn;
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        int code = conn.getResponseCode();
        InputStream stream = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
        String body = readAll(stream);
        if (code < 200 || code >= 300) {
            throw new IOException("API OCR HTTP " + code + " : " + body);
        }
        return body;
    }

    private String parseResponse(String body) throws IOException {
        JSONObject json = new JSONObject(body);
        if (json.optBoolean("IsErroredOnProcessing", false)) {
            throw new IOException(readOcrError(json));
        }

        JSONArray results = json.optJSONArray("ParsedResults");
        if (results == null || results.length() == 0) {
            return "";
        }

        StringBuilder extracted = new StringBuilder();
        for (int i = 0; i < results.length(); i++) {
            String text = results.getJSONObject(i).optString("ParsedText", "").trim();
            if (!text.isBlank()) {
                if (extracted.length() > 0) {
                    extracted.append("\n\n");
                }
                extracted.append(text);
            }
        }
        return extracted.toString().trim();
    }

    private String readOcrError(JSONObject json) {
        Object message = json.opt("ErrorMessage");
        if (message instanceof JSONArray array && array.length() > 0) {
            return array.optString(0, "OCR indisponible.");
        }
        if (message != null) {
            return message.toString();
        }
        return json.optString("ErrorDetails", "OCR indisponible.");
    }

    private void writeFormField(OutputStream os, String boundary, String name, String value) throws IOException {
        os.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        os.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        os.write((value + "\r\n").getBytes(StandardCharsets.UTF_8));
    }

    private void writeFileField(OutputStream os, String boundary, String name, Path file) throws IOException {
        String fileName = file.getFileName().toString();
        os.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        os.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        os.write(("Content-Type: " + detectContentType(file) + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        Files.copy(file, os);
        os.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private String detectContentType(Path file) throws IOException {
        String type = Files.probeContentType(file);
        return type == null || type.isBlank() ? "application/octet-stream" : type;
    }

    private String apiKey() {
        String configured = LocalSecrets.get("OCR_SPACE_API_KEY");
        if (configured == null || configured.isBlank() || configured.contains("your-")) {
            return DEFAULT_API_KEY;
        }
        return configured.trim();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String readAll(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        try (InputStream in = stream; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            in.transferTo(out);
            return out.toString(StandardCharsets.UTF_8);
        }
    }
}
