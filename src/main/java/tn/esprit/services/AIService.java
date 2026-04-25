package tn.esprit.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;

public class AIService {

    public static String generateResume(String text) {
        try {

            HttpClient client = HttpClient.newHttpClient();

            String cleanText = text
                    .replace("\"", "'")
                    .replace("\n", " ")
                    .replace("\r", " ")
                    .trim();

            if (cleanText.length() > 1000) {
                cleanText = cleanText.substring(0, 1000);
            }

            String prompt = "Summarize in 5 points: " + cleanText;

            String json = """
        {
          "model": "phi",
          "prompt": "%s",
          "stream": false
        }
        """.formatted(prompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:11434/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("RAW RESPONSE: " + response.body());

            JSONObject obj = new JSONObject(response.body());

            return obj.optString("response", "❌ no response field");

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ JSON error / AI failed";
        }
    }
    public static String ask(String prompt) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            JsonObject json = new JsonObject();
            json.addProperty("model", "phi3"); // أو mistral / llama3
            json.addProperty("prompt",
                    prompt +
                            "\n\nIMPORTANT: answer in max 3 lines only, clear and simple."
            );
            json.addProperty("stream", false);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:11434/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonObject res = JsonParser.parseString(response.body()).getAsJsonObject();

            return res.has("response") ? res.get("response").getAsString() : "No response";

        } catch (Exception e) {
            e.printStackTrace();
            return "AI error";
        }
    }
}