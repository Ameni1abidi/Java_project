package tn.esprit.utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class OllamaClient {
    private static final Pattern CONTENT_PATTERN = Pattern.compile("\"content\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
    private static final Pattern TAG_NAME_PATTERN = Pattern.compile("\"name\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);

    private final HttpClient http;
    private final URI baseUri;

    public OllamaClient(URI baseUri) {
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        this.baseUri = Objects.requireNonNull(baseUri, "baseUri");
    }

    public static OllamaClient localDefault() {
        return new OllamaClient(URI.create("http://localhost:11434"));
    }

    public String chatOnce(String model, String system, String userPrompt) throws IOException, InterruptedException {
        return chatOnce(model, system, userPrompt, Duration.ofSeconds(120));
    }

    public String chatOnce(String model, String system, String userPrompt, Duration timeout) throws IOException, InterruptedException {
        String body = """
                {
                  "model": "%s",
                  "stream": false,
                  "messages": [
                    {"role":"system","content":"%s"},
                    {"role":"user","content":"%s"}
                  ]
                }
                """.formatted(jsonEscape(model), jsonEscape(system), jsonEscape(userPrompt));

        HttpRequest req = HttpRequest.newBuilder(baseUri.resolve("/api/chat"))
                .timeout(timeout != null ? timeout : Duration.ofSeconds(120))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new IOException("Ollama HTTP " + resp.statusCode() + ": " + trim(resp.body(), 400));
        }
        String content = extractContent(resp.body());
        if (content == null || content.isBlank()) {
            throw new IOException("Ollama response missing content: " + trim(resp.body(), 400));
        }
        return content;
    }

    public List<String> listModelNames() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(baseUri.resolve("/api/tags"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new IOException("Ollama HTTP " + resp.statusCode() + " (tags): " + trim(resp.body(), 400));
        }

        List<String> names = new ArrayList<>();
        Matcher m = TAG_NAME_PATTERN.matcher(resp.body());
        while (m.find()) {
            String n = jsonUnescape(m.group(1));
            if (n != null && !n.isBlank()) names.add(n);
        }
        return names;
    }

    public String pickAvailableModel(String preferred) throws IOException, InterruptedException {
        List<String> models = listModelNames();
        if (models.isEmpty()) {
            return preferred != null && !preferred.isBlank() ? preferred.trim() : "llama3";
        }
        if (preferred != null && !preferred.isBlank()) {
            String p = preferred.trim();
            for (String m : models) {
                if (p.equalsIgnoreCase(m)) return m;
                // allow "llama3" to match "llama3:latest"
                if (m.toLowerCase().startsWith(p.toLowerCase() + ":")) return m;
            }
        }
        // Heuristic: prefer smaller/faster models when not specified.
        // Order: phi, mistral, llama3.2, llama3.1, llama3, then whatever is first.
        return models.stream()
                .min(Comparator.comparingInt(OllamaClient::modelPreferenceRank))
                .orElse(models.get(0));
    }

    private static int modelPreferenceRank(String name) {
        if (name == null) return 1000;
        String n = name.toLowerCase();
        if (n.contains("phi")) return 0;
        if (n.contains("mistral")) return 1;
        if (n.contains("llama3.2") || n.contains("llama3_2")) return 2;
        if (n.contains("llama3.1") || n.contains("llama3_1")) return 3;
        if (n.contains("llama3")) return 4;
        if (n.contains("gemma")) return 5;
        if (n.contains("qwen")) return 6;
        return 50;
    }

    private static String extractContent(String json) {
        // Prefer the last "content" field (some responses include multiple).
        Matcher m = CONTENT_PATTERN.matcher(json);
        String last = null;
        while (m.find()) {
            last = m.group(1);
        }
        if (last == null) return null;
        return jsonUnescape(last);
    }

    private static String jsonEscape(String s) {
        if (s == null) return "";
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private static String jsonUnescape(String s) {
        // Minimal unescape for common sequences returned by Ollama
        return s
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private static String trim(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max) + "...";
    }
}

