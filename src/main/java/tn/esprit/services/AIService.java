package tn.esprit.services;

public class AIService {

    public static String generateResume(String text) {

        try {
            // pseudo API call (OpenAI)
            String prompt = "Résume ce cours simplement:\n" + text;

            // هنا تربط OpenAI API
            return OpenAIClient.ask(prompt);

        } catch (Exception e) {
            return "Résumé non disponible";
        }
    }
}
