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

public final class GoogleAuthService {
    private static final Pattern STRING_FIELD_PATTERN_TEMPLATE = Pattern.compile("\"%s\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
    private static final Pattern BOOL_FIELD_PATTERN_TEMPLATE = Pattern.compile("\"%s\"\\s*:\\s*(true|false)", Pattern.DOTALL);
    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    public GoogleProfile authenticate() throws Exception {
        String clientId = requiredConfig("GOOGLE_CLIENT_ID");
        String clientSecret = requiredConfig("GOOGLE_CLIENT_SECRET");
        String redirectValue = LocalSecrets.get("GOOGLE_REDIRECT_URI");
        URI redirectUri = URI.create(
                (redirectValue == null || redirectValue.isBlank())
                        ? "http://localhost:8765/oauth2/callback"
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

            if (error != null && !error.isBlank()) {
                throw new IllegalStateException("Google auth annulee: " + error);
            }
            if (!Objects.equals(state, returnedState)) {
                throw new IllegalStateException("State OAuth invalide");
            }
            if (code == null || code.isBlank()) {
                throw new IllegalStateException("Code d'autorisation manquant");
            }

            String tokenResponse = exchangeCodeForTokens(code, clientId, clientSecret, redirectUri.toString(), codeVerifier);
            String accessToken = extractStringField(tokenResponse, "access_token");
            if (accessToken == null || accessToken.isBlank()) {
                throw new IllegalStateException("Access token Google introuvable");
            }

            String userInfoResponse = fetchUserInfo(accessToken);
            String email = extractStringField(userInfoResponse, "email");
            String name = extractStringField(userInfoResponse, "name");
            boolean verified = extractBooleanField(userInfoResponse, "email_verified");

            if (email == null || email.isBlank()) {
                throw new IllegalStateException("Email Google introuvable");
            }
            if (!verified) {
                throw new IllegalStateException("Email Google non verifie");
            }
            return new GoogleProfile(email, (name == null || name.isBlank()) ? email : name);
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
        String body = "<html><body><h3>Connexion Google reussie.</h3><p>Vous pouvez fermer cet onglet.</p></body></html>";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String buildAuthorizationUrl(String clientId, String redirectUri, String state, String codeChallenge) {
        return GOOGLE_AUTH_URL + "?" +
                "response_type=code" +
                "&client_id=" + encode(clientId) +
                "&redirect_uri=" + encode(redirectUri) +
                "&scope=" + encode("openid email profile") +
                "&state=" + encode(state) +
                "&code_challenge=" + encode(codeChallenge) +
                "&code_challenge_method=S256" +
                "&prompt=select_account";
    }

    private String exchangeCodeForTokens(String code, String clientId, String clientSecret, String redirectUri, String codeVerifier)
            throws IOException, InterruptedException {
        String form = "code=" + encode(code)
                + "&client_id=" + encode(clientId)
                + "&client_secret=" + encode(clientSecret)
                + "&redirect_uri=" + encode(redirectUri)
                + "&grant_type=authorization_code"
                + "&code_verifier=" + encode(codeVerifier);

        HttpRequest request = HttpRequest.newBuilder(URI.create(GOOGLE_TOKEN_URL))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Token Google HTTP " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }

    private String fetchUserInfo(String accessToken) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(GOOGLE_USERINFO_URL))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Userinfo Google HTTP " + response.statusCode() + ": " + response.body());
        }
        return response.body();
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

    private static boolean extractBooleanField(String json, String fieldName) {
        Pattern p = Pattern.compile(BOOL_FIELD_PATTERN_TEMPLATE.pattern().formatted(fieldName), Pattern.DOTALL);
        Matcher m = p.matcher(json);
        if (!m.find()) return false;
        return "true".equalsIgnoreCase(m.group(1));
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

    public record GoogleProfile(String email, String name) {}
}
