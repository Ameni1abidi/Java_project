package tn.esprit.services;

public class AIExamService {

    private final OllamaService ollama = new OllamaService();

    public String ask(String prompt) {
        return ollama.ask(prompt);
    }

    public String generateExam(String course, String level) {

        String prompt =
                "Tu es un professeur expert.\n" +
                        "Génère un examen structuré.\n\n" +
                        "Cours: " + course + "\n" +
                        "Niveau: " + level + "\n\n" +
                        "Format:\n" +
                        "- 10 questions QCM\n" +
                        "- 4 choix par question\n" +
                        "- réponse correcte\n" +
                        "- explication courte";

        return ask(prompt);
    }

    public String correctAnswer(String question, String answer) {

        String prompt =
                "Corrige cette réponse:\n\n" +
                        "Question: " + question + "\n" +
                        "Réponse étudiant: " + answer + "\n\n" +
                        "Donne une note sur 20 + explication.";

        return ask(prompt);
    }
}