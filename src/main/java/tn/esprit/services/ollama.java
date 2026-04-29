package tn.esprit.services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ollama {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private static final String MODEL = "llama3:latest";

    // ===================== BASIC CHAT =====================
    public String poserQuestion(String question) {

        String prompt =
                "Tu es un assistant EduFlex.\n" +
                        "Réponds uniquement aux questions liées à l'éducation.\n\n" +
                        "Question: " + question;

        return callOllama(prompt);
    }

    // ===================== GENERATE EXAM =====================
    public String generateExam(String course, String level) {

        String prompt =
                "Tu es un professeur expert.\n" +
                        "Génère un examen complet basé UNIQUEMENT sur le cours.\n\n" +
                        "Cours: " + course + "\n" +
                        "Niveau: " + level + "\n\n" +
                        "FORMAT OBLIGATOIRE:\n" +
                        "- 10 QCM\n" +
                        "- 4 choix (A, B, C, D)\n" +
                        "- réponse correcte\n\n" +
                        "Réponds uniquement avec l'examen.";

        return callOllama(prompt);
    }

    // ===================== CORRECTION =====================
    public String correctAnswer(String question, String answer) {

        String prompt =
                "Corrige cet exercice.\n\n" +
                        "Question: " + question + "\n" +
                        "Réponse étudiant: " + answer + "\n\n" +
                        "Donne: note /20 + explication simple.";

        return callOllama(prompt);
    }

    // ===================== CORE REQUEST =====================
    private String callOllama(String prompt) {

        try {
            String jsonBody = """
            {
              "model": "%s",
              "prompt": "%s",
              "stream": false
            }
            """.formatted(MODEL, escapeJson(prompt));

            HttpURLConnection conn = (HttpURLConnection)
                    new URL(OLLAMA_URL).openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(60000);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();

            // ❌ ERROR DEBUG
            if (code != 200) {

                BufferedReader err = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8)
                );

                StringBuilder error = new StringBuilder();
                String line;

                while ((line = err.readLine()) != null) {
                    error.append(line);
                }

                return "OLLAMA ERROR " + code + " : " + error;
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            return extractResponse(response.toString());

        } catch (Exception e) {
            return "Erreur Ollama: " + e.getMessage();
        }
    }

    // ===================== JSON PARSER =====================
    private String extractResponse(String json) {

        String key = "\"response\":\"";
        int start = json.indexOf(key);

        if (start == -1) return "Réponse vide";

        start += key.length();

        StringBuilder result = new StringBuilder();

        for (int i = start; i < json.length(); i++) {

            char c = json.charAt(i);

            if (c == '"') break;

            if (c == '\\' && i + 1 < json.length()) {

                char next = json.charAt(i + 1);

                switch (next) {
                    case 'n' -> {
                        result.append("\n");
                        i++;
                    }
                    case 't' -> {
                        result.append("\t");
                        i++;
                    }
                    case '"' -> {
                        result.append('"');
                        i++;
                    }
                    case '\\' -> {
                        result.append('\\');
                        i++;
                    }
                    default -> result.append(next);
                }

            } else {
                result.append(c);
            }
        }

        return result.toString().trim();
    }

    // ===================== JSON ESCAPE =====================
    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}