package tn.esprit.services;

import tn.esprit.entities.Examen;

public class AIExamService {

    private final ollama ollamaService = new ollama();
    private final ExamenService examenService = new ExamenService();

    /**
     * Génère un examen via Ollama et le sauvegarde en BD.
     */
    public Examen generateExamEntity(String cours, String niveau) {
        try {
            String contenuCours = "Cours: " + cours + "\nNiveau: " + niveau;

            // coursId=0, enseignantId=0 par défaut (à adapter selon ta session)
            Examen examen = ollamaService.genererExamen(contenuCours, 0, 0);

            examenService.create(examen); // ← utilise ton ExamenService existant

            System.out.println("✔ Examen sauvegardé : " + examen);
            return examen;

        } catch (Exception e) {
            System.err.println("Erreur AIExamService : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}