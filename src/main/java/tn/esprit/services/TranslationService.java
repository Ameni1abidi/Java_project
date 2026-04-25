package tn.esprit.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TranslationService {

    private static final String[] SOURCES = {"fr", "en", "ar", "es", "de"};

    public String traduire(String texte, String langCible) {
        if (texte == null || texte.isBlank()) return "";

        for (String src : SOURCES) {
            if (src.equals(langCible)) continue;
            String result = callApi(texte, src, langCible);
            if (result != null) return result;
        }

        return "Traduction indisponible.";
    }

    private String callApi(String texte, String langSource, String langCible) {
        try {
            String encoded = URLEncoder.encode(texte, StandardCharsets.UTF_8);
            String urlStr  = "https://api.mymemory.translated.net/get"
                    + "?q=" + encoded
                    + "&langpair=" + langSource + "|" + langCible;

            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            if (conn.getResponseCode() != 200) return null;

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            String body = sb.toString();
            System.out.println("[Translation] " + langSource + "|" + langCible + " => " + body);

            // Verifier responseStatus == 200
            int statusIdx = body.indexOf("\"responseStatus\":");
            if (statusIdx == -1) return null;
            int statusStart = statusIdx + 17;
            int statusEnd   = body.indexOf(",", statusStart);
            if (statusEnd == -1) statusEnd = body.indexOf("}", statusStart);
            int status = Integer.parseInt(body.substring(statusStart, statusEnd).trim());
            if (status != 200) return null;

            // Extraire translatedText
            int idx = body.indexOf("\"translatedText\":\"");
            if (idx == -1) return null;
            int start = idx + 18;
            int end   = findJsonStringEnd(body, start);
            if (end == -1) return null;

            String raw    = body.substring(start, end);
            String traduit = decodeJsonString(raw);

            if (traduit.isBlank() || traduit.toUpperCase().contains("MYMEMORY WARNING")) {
                return null;
            }

            return traduit;

        } catch (Exception e) {
            System.err.println("[TranslationService] Erreur : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private int findJsonStringEnd(String body, int start) {
        int i = start;
        while (i < body.length()) {
            char ch = body.charAt(i);
            if (ch == '\\') {
                i += 2;
            } else if (ch == '"') {
                return i;
            } else {
                i++;
            }
        }
        return -1;
    }

    private String decodeJsonString(String raw) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < raw.length()) {
            char ch = raw.charAt(i);
            if (ch == '\\' && i + 1 < raw.length()) {
                char next = raw.charAt(i + 1);
                if (next == 'u' && i + 5 < raw.length()) {
                    String hex = raw.substring(i + 2, i + 6);
                    try {
                        sb.append((char) Integer.parseInt(hex, 16));
                        i += 6;
                    } catch (NumberFormatException e) {
                        sb.append(ch);
                        i++;
                    }
                } else if (next == '"') {
                    sb.append('"');
                    i += 2;
                } else if (next == '\\') {
                    sb.append('\\');
                    i += 2;
                } else if (next == '/') {
                    sb.append('/');
                    i += 2;
                } else if (next == 'n') {
                    sb.append('\n');
                    i += 2;
                } else if (next == 'r') {
                    sb.append('\r');
                    i += 2;
                } else if (next == 't') {
                    sb.append('\t');
                    i += 2;
                } else {
                    sb.append(next);
                    i += 2;
                }
            } else {
                sb.append(ch);
                i++;
            }
        }
        return sb.toString();
    }
}