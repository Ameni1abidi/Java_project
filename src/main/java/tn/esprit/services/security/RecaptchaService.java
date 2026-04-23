package tn.esprit.services.security;

import tn.esprit.config.LocalSecrets;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RecaptchaService {
    private static final URI VERIFY_URI = URI.create("https://www.google.com/recaptcha/api/siteverify");
    private static final Pattern SUCCESS_PATTERN = Pattern.compile("\"success\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SCORE_PATTERN = Pattern.compile("\"score\"\\s*:\\s*([0-9]*\\.?[0-9]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ACTION_PATTERN = Pattern.compile("\"action\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
    private static final Pattern ERROR_CODES_PATTERN = Pattern.compile("\"error-codes\"\\s*:\\s*\\[(.*?)]", Pattern.DOTALL);

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    public RecaptchaResult verify(String token) throws IOException, InterruptedException {
        String secret = LocalSecrets.get("RECAPTCHA_SECRET_KEY");
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Configuration manquante: RECAPTCHA_SECRET_KEY");
        }
        if (token == null || token.isBlank()) {
            return new RecaptchaResult(false, 0.0, null, "token-vide");
        }

        String form = "secret=" + encode(secret.trim()) + "&response=" + encode(token.trim());
        HttpRequest request = HttpRequest.newBuilder(VERIFY_URI)
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("HTTP " + response.statusCode() + " reCAPTCHA");
        }
        return parseResponse(response.body());
    }

    private RecaptchaResult parseResponse(String json) {
        boolean success = parseSuccess(json);
        double score = parseScore(json);
        String action = parseAction(json);
        String errors = parseErrors(json);
        return new RecaptchaResult(success, score, action, errors);
    }

    private boolean parseSuccess(String json) {
        Matcher m = SUCCESS_PATTERN.matcher(json);
        return m.find() && "true".equalsIgnoreCase(m.group(1));
    }

    private double parseScore(String json) {
        Matcher m = SCORE_PATTERN.matcher(json);
        if (!m.find()) return 0.0;
        try {
            return Double.parseDouble(m.group(1));
        } catch (NumberFormatException ignored) {
            return 0.0;
        }
    }

    private String parseAction(String json) {
        Matcher m = ACTION_PATTERN.matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private String parseErrors(String json) {
        Matcher m = ERROR_CODES_PATTERN.matcher(json);
        return m.find() ? m.group(1).replace("\"", "").trim() : "";
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public record RecaptchaResult(boolean success, double score, String action, String errors) {}
}
