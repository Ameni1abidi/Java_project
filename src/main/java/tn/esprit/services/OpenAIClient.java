package tn.esprit.services;

public class OpenAIClient {

    public static String ask(String prompt) {

        if (prompt.length() > 500) {
            return "📌 Résumé:\n\n" + prompt.substring(0, 500) + "...";
        }

        return prompt;
    }
}
