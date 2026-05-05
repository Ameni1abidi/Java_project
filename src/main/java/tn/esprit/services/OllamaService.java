package tn.esprit.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class OllamaService {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private static final String MODEL = "llama3";

    public String poserQuestion(String question) {
        try {
            String prompt = "RÈGLE IMPORTANTE :\n"
                    + "Tu es un assistant EduFlex.\n"
                    + "Tu réponds uniquement aux questions sur : éducation, cours, plateforme EduFlex, bugs.\n"
                    + "Si la question est hors sujet, tu réponds EXACTEMENT :\n"
                    + "\"Veuillez poser une question liée à EduFlex.\"\n"
                    + "Ne donne aucune autre information.\n\n"
                    + "Question : " + question;

            String jsonBody = "{"
                    + "\"model\": \"" + MODEL + "\","
                    + "\"prompt\": \"" + escapeJson(prompt) + "\","
                    + "\"stream\": false"
                    + "}";

            HttpURLConnection conn = (HttpURLConnection) new URL(OLLAMA_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(60000);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            if (code != 200) {
                return "Erreur Ollama (code " + code + "). Verifiez qu Ollama est lance.";
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            return extractJsonField(sb.toString(), "response");

        } catch (java.net.ConnectException e) {
            return "Impossible de contacter Ollama. Verifiez qu il est lance.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur : " + e.getMessage();
        }
    }

    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String extractJsonField(String json, String field) {
        String key = "\"" + field + "\":\"";
        int idx = json.indexOf(key);
        if (idx == -1) return "Reponse vide.";

        StringBuilder sb = new StringBuilder();
        int i = idx + key.length();
        while (i < json.length()) {
            char ch = json.charAt(i);
            if (ch == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                if (next == 'u' && i + 5 < json.length()) {
                    try {
                        sb.append((char) Integer.parseInt(json.substring(i + 2, i + 6), 16));
                        i += 6;
                    } catch (NumberFormatException e) { sb.append(ch); i++; }
                } else if (next == '"')  { sb.append('"');  i += 2; }
                else if (next == '\\') { sb.append('\\'); i += 2; }
                else if (next == 'n')  { sb.append('\n'); i += 2; }
                else if (next == 't')  { sb.append('\t'); i += 2; }
                else                   { sb.append(next); i += 2; }
            } else if (ch == '"') {
                break;
            } else {
                sb.append(ch);
                i++;
            }
        }
        return sb.toString().trim();
    }


}