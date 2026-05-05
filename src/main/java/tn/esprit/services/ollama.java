package tn.esprit.services;

import tn.esprit.entities.Examen;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

public class ollama {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private static final String MODEL = "llama3:latest";
    private final HttpClient client = HttpClient.newHttpClient();

    public Examen genererExamen(String contenuCours, int coursId, int enseignantId) throws Exception {

        String prompt = buildPrompt(contenuCours);

        String body = "{\"model\":\"" + MODEL + "\","
                + "\"prompt\":" + toJsonString(prompt) + ","
                + "\"stream\":false,"
                + "\"format\":\"json\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OLLAMA_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200)
            throw new RuntimeException("Ollama error: " + response.statusCode());

        String raw = response.body();
        String jsonResponse = extractField(raw, "response");

        return parseExamen(jsonResponse, coursId, enseignantId);
    }

    private Examen parseExamen(String json, int coursId, int enseignantId) {
        String cleaned = json.trim()
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();

        // ✅ Plus de Jackson — extraction manuelle
        Examen examen = new Examen();
        examen.setTitre(extractField(cleaned, "titre"));
        examen.setContenu(extractField(cleaned, "contenu"));
        examen.setType(extractField(cleaned, "type"));

        String dureeStr = extractField(cleaned, "duree");
        examen.setDuree(dureeStr.isEmpty() ? 60 : Integer.parseInt(dureeStr));

        examen.setDateExamen(LocalDate.now().plusDays(7));
        examen.setCoursId(coursId);
        examen.setEnseignantId(enseignantId);

        return examen;
    }

    private String buildPrompt(String contenuCours) {
        return "Tu es un assistant pédagogique. À partir du cours suivant, génère un examen structuré.\n"
                + "Réponds UNIQUEMENT en JSON valide avec ce format exact :\n"
                + "{\n"
                + "  \"titre\": \"string\",\n"
                + "  \"contenu\": \"string (questions numérotées séparées par \\\\n)\",\n"
                + "  \"type\": \"QCM\",\n"
                + "  \"duree\": 60\n"
                + "}\n"
                + "Cours :\n"
                + contenuCours;
    }

    private String extractField(String json, String field) {
        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx == -1) return "";

        int colon = json.indexOf(":", idx);
        int start = colon + 1;

        while (start < json.length() && json.charAt(start) == ' ') start++;

        if (start >= json.length()) return "";

        if (json.charAt(start) == '"') {
            StringBuilder result = new StringBuilder();
            int i = start + 1;
            while (i < json.length()) {
                char c = json.charAt(i);
                if (c == '\\' && i + 1 < json.length()) {
                    char next = json.charAt(i + 1);
                    if (next == 'n') result.append('\n');
                    else if (next == '"') result.append('"');
                    else if (next == '\\') result.append('\\');
                    else result.append(next);
                    i += 2;
                } else if (c == '"') {
                    break;
                } else {
                    result.append(c);
                    i++;
                }
            }
            return result.toString();
        } else {
            int end = start;
            while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.'))
                end++;
            return json.substring(start, end);
        }
    }

    private String toJsonString(String s) {
        return "\"" + s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r") + "\"";
    }
}