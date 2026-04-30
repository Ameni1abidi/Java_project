package tn.esprit.services;

import okhttp3.*;
import com.google.gson.*;

public class SentimentService {

    private static final String API_KEY = "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";
    private static final String API_URL =
            "https://router.huggingface.co/hf-inference/models/cardiffnlp/twitter-roberta-base-sentiment-latest";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    public String analyserSentiment(String texte) {
        try {
            String textePropre = texte.replace("\"", "'").replace("\n", " ").trim();
            String bodyJson = "{\"inputs\": \"" + textePropre + "\"}";

            System.out.println("=== SentimentService ===");
            System.out.println("Texte : " + textePropre);

            RequestBody body = RequestBody.create(
                    bodyJson, MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(API_URL)
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build();

            // Premier essai
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            int statusCode = response.code();

            System.out.println("Status : " + statusCode);
            System.out.println("Body   : " + responseBody);

            // Si modèle en chargement → attendre et réessayer
            if (statusCode == 503) {
                System.out.println("Modèle en chargement, attente 8s...");
                Thread.sleep(8000);
                response = client.newCall(request).execute();
                responseBody = response.body().string();
                statusCode = response.code();
                System.out.println("Retry Status : " + statusCode);
                System.out.println("Retry Body   : " + responseBody);
            }

            // Afficher l'erreur exacte
            if (statusCode != 200) {
                System.err.println("ERREUR HTTP " + statusCode + " : " + responseBody);
                return "ERREUR (" + statusCode + ")";
            }

            return parserReponse(responseBody);

        } catch (Exception e) {
            System.err.println("Exception : " + e.getMessage());
            e.printStackTrace();
            return "ERREUR";
        }
    }

    private String parserReponse(String json) {
        try {
            // Format : [[{"label":"positive","score":0.95}, ...]]
            JsonArray outer = JsonParser.parseString(json).getAsJsonArray();
            JsonArray inner = outer.get(0).getAsJsonArray();

            // Trouver le label avec le score max
            String bestLabel = "";
            double bestScore = -1;

            for (JsonElement el : inner) {
                JsonObject obj = el.getAsJsonObject();
                String label = obj.get("label").getAsString().toLowerCase();
                double score = obj.get("score").getAsDouble();
                System.out.println("  " + label + " -> " + score);
                if (score > bestScore) {
                    bestScore = score;
                    bestLabel = label;
                }
            }

            System.out.println("Label final : " + bestLabel);

            return switch (bestLabel) {
                case "positive" -> "😊 POSITIF";
                case "negative" -> "😞 NÉGATIF";
                default         -> "😐 NEUTRE";
            };

        } catch (Exception e) {
            System.err.println("Erreur parsing JSON : " + e.getMessage());
            System.err.println("JSON reçu : " + json);
            return "NEUTRE";
        }
    }
}