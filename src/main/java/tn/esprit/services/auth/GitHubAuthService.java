package tn.esprit.services.auth;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import tn.esprit.config.LocalSecrets;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GitHubAuthService {
    private static final Pattern STRING_FIELD_PATTERN_TEMPLATE = Pattern.compile("\"%s\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
    private static final String GITHUB_AUTH_URL = "https://github.com/login/oauth/authorize";
    private static final String GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String GITHUB_USER_URL = "https://api.github.com/user";
    private static final String GITHUB_EMAILS_URL = "https://api.github.com/user/emails";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    public GitHubProfile authenticate() throws Exception {
        String clientId = requiredConfig("GITHUB_CLIENT_ID");
        String clientSecret = requiredConfig("GITHUB_CLIENT_SECRET");

        String redirectValue = LocalSecrets.get("GITHUB_REDIRECT_URI");
        URI redirectUri = URI.create(
                (redirectValue == null || redirectValue.isBlank())
                        ? "http://localhost:8766/oauth2/github/callback"
                        : redirectValue
        );

        String state = randomUrlSafe(24);
        String codeVerifier = randomUrlSafe(48);
        String codeChallenge = sha256Base64Url(codeVerifier);

        CompletableFuture<Map<String, String>> callbackFuture = new CompletableFuture<>();
        HttpServer server = startCallbackServer(redirectUri, callbackFuture);
        try {
            String authUrl = buildAuthorizationUrl(clientId, redirectUri.toString(), state, codeChallenge);
            openBrowser(authUrl);

            Map<String, String> callback = callbackFuture.get(120, TimeUnit.SECONDS);
            String returnedState = callback.get("state");
            String code = callback.get("code");
            String error = callback.get("error");
            String errorDescription = callback.get("error_description");

            if (error != null && !error.isBlank()) {
                throw new IllegalStateException("GitHub auth annulee: " + error + ((errorDescription == null) ? "" : (" (" + errorDescription + ")")));
            }
            if (!Objects.equals(state, returnedState)) {
                throw new IllegalStateException("State OAuth invalide");
            }
            if (code == null || code.isBlank()) {
                throw new IllegalStateException("Code d'autorisation manquant");
            }

            String tokenResponse = exchangeCodeForToken(code, clientId, clientSecret, redirectUri.toString(), codeVerifier);
            String accessToken = extractStringField(tokenResponse, "access_token");
            if (accessToken == null || accessToken.isBlank()) {
                throw new IllegalStateException("Access token GitHub introuvable");
            }

            String userJson = fetchJson(GITHUB_USER_URL, accessToken);
            String login = extractStringField(userJson, "login");
            String name = extractStringField(userJson, "name");
            if (name == null || name.isBlank()) {
                name = (login == null || login.isBlank()) ? "GitHub User" : login;
            }

            String emailsJson = fetchJson(GITHUB_EMAILS_URL, accessToken);
            String email = pickBestEmailFromEmailsJson(emailsJson);
            if (email == null || email.isBlank()) {
                throw new IllegalStateException("Email GitHub introuvable. Verifie que l'app GitHub a le scope user:email.");
            }

            return new GitHubProfile(email, name, (login == null || login.isBlank()) ? null : login);
        } finally {
            server.stop(0);
        }
    }

    private HttpServer startCallbackServer(URI redirectUri, CompletableFuture<Map<String, String>> future) throws IOException {
        int port = redirectUri.getPort() > 0 ? redirectUri.getPort() : 80;
        String path = (redirectUri.getPath() == null || redirectUri.getPath().isBlank()) ? "/" : redirectUri.getPath();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext(path, exchange -> handleCallback(exchange, future));
        server.start();
        return server;
    }

    private void handleCallback(HttpExchange exchange, CompletableFuture<Map<String, String>> future) throws IOException {
        Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());
        if (!future.isDone()) {
            future.complete(params);
        }
        String body = "<html><body><h3>Connexion GitHub reussie.</h3><p>Vous pouvez fermer cet onglet.</p></body></html>";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String buildAuthorizationUrl(String clientId, String redirectUri, String state, String codeChallenge) {
        return GITHUB_AUTH_URL + "?" +
                "response_type=code" +
                "&client_id=" + encode(clientId) +
                "&redirect_uri=" + encode(redirectUri) +
                "&scope=" + encode("read:user user:email") +
                "&state=" + encode(state) +
                "&code_challenge=" + encode(codeChallenge) +
                "&code_challenge_method=S256";
    }

    private String exchangeCodeForToken(String code, String clientId, String clientSecret, String redirectUri, String codeVerifier)
            throws IOException, InterruptedException {
        String form = "code=" + encode(code)
                + "&client_id=" + encode(clientId)
                + "&client_secret=" + encode(clientSecret)
                + "&redirect_uri=" + encode(redirectUri)
                + "&grant_type=authorization_code"
                + "&code_verifier=" + encode(codeVerifier);

        HttpRequest request = HttpRequest.newBuilder(URI.create(GITHUB_TOKEN_URL))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Token GitHub HTTP " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }

    private String fetchJson(String url, String accessToken) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("GitHub API HTTP " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }

    private static String pickBestEmailFromEmailsJson(String json) {
        if (json == null || json.isBlank()) return null;

        Pattern objectPattern = Pattern.compile("\\{[^\\}]*\\}", Pattern.DOTALL);
        Matcher objMatcher = objectPattern.matcher(json);

        String firstAny = null;
        String firstVerified = null;
        String primaryVerified = null;

        while (objMatcher.find()) {
            String obj = objMatcher.group();
            String email = extractStringField(obj, "email");
            if (email == null || email.isBlank()) continue;

            boolean primary = obj.contains("\"primary\":true") || obj.contains("\"primary\" : true") || obj.contains("\"primary\": true");
            boolean verified = obj.contains("\"verified\":true") || obj.contains("\"verified\" : true") || obj.contains("\"verified\": true");

            if (firstAny == null) firstAny = email;
            if (verified && firstVerified == null) firstVerified = email;
            if (primary && verified) {
                primaryVerified = email;
                break;
            }
        }
        if (primaryVerified != null) return primaryVerified;
        if (firstVerified != null) return firstVerified;
        return firstAny;
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> values = new HashMap<>();
        if (query == null || query.isBlank()) return values;
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            String key = idx >= 0 ? pair.substring(0, idx) : pair;
            String val = idx >= 0 ? pair.substring(idx + 1) : "";
            values.put(urlDecode(key), urlDecode(val));
        }
        return values;
    }

    private static String extractStringField(String json, String fieldName) {
        Pattern p = Pattern.compile(STRING_FIELD_PATTERN_TEMPLATE.pattern().formatted(fieldName), Pattern.DOTALL);
        Matcher m = p.matcher(json);
        if (!m.find()) return null;
        return jsonUnescape(m.group(1));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static String urlDecode(String value) {
        return java.net.URLDecoder.decode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private static String randomUrlSafe(int size) {
        byte[] bytes = new byte[size];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256Base64Url(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

    private static String requiredConfig(String key) {
        String value = LocalSecrets.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Configuration manquante: " + key + " (env, -D, ou local.secrets.properties). " + LocalSecrets.debugSource());
        }
        return value.trim();
    }

    private static String jsonUnescape(String s) {
        return s.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private void openBrowser(String url) throws IOException {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            throw new IllegalStateException("Ouverture navigateur non supportee sur cette machine");
        }
        Desktop.getDesktop().browse(URI.create(url));
    }

    public record GitHubProfile(String email, String name, String login) {}
}

