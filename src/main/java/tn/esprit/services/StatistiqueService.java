package tn.esprit.services;

import tn.esprit.entities.Evaluation;

import java.util.*;
import java.util.stream.Collectors;

public class StatistiqueService {

    private final EvaluationService evaluationService = new EvaluationService();

    private List<Evaluation> cache;

    // ================= CACHE =================
    private List<Evaluation> getAll() {
        if (cache == null) {
            cache = evaluationService.getAll();
        }
        return cache;
    }

    public void refresh() {
        cache = evaluationService.getAll();
    }

    // ================= EXAMEN =================

    public double moyenneParExamen(int examenId) {
        return getAll().stream()
                .filter(e -> e.getExamenId() == examenId)
                .mapToDouble(Evaluation::getNote)
                .average()
                .orElse(0);
    }

    public long nombreReussis(int examenId) {
        return getAll().stream()
                .filter(e -> e.getExamenId() == examenId)
                .filter(e -> e.getNote() >= 10)
                .count();
    }

    public long nombreEchecs(int examenId) {
        return getAll().stream()
                .filter(e -> e.getExamenId() == examenId)
                .filter(e -> e.getNote() < 10)
                .count();
    }

    public double tauxReussite(int examenId) {

        List<Evaluation> list = getAll().stream()
                .filter(e -> e.getExamenId() == examenId)
                .toList();

        if (list.isEmpty()) return 0;

        long ok = list.stream()
                .filter(e -> e.getNote() >= 10)
                .count();

        return (ok * 100.0) / list.size();
    }

    public double noteMax(int examenId) {
        return getAll().stream()
                .filter(e -> e.getExamenId() == examenId)
                .mapToDouble(Evaluation::getNote)
                .max()
                .orElse(0);
    }

    public double noteMin(int examenId) {
        return getAll().stream()
                .filter(e -> e.getExamenId() == examenId)
                .mapToDouble(Evaluation::getNote)
                .min()
                .orElse(0);
    }

    // ================= ELEVE =================

    public double moyenneEleve(int eleveId) {
        return getAll().stream()
                .filter(e -> e.getEleveId() == eleveId)
                .mapToDouble(Evaluation::getNote)
                .average()
                .orElse(0);
    }

    public String statutEleve(int eleveId) {

        double m = moyenneEleve(eleveId);

        if (m >= 10) return "ADMIS";
        if (m >= 8) return "EN RISQUE";
        return "AJOURNÉ";
    }

    // ================= GLOBAL =================

    public double moyenneGlobale() {
        return getAll().stream()
                .mapToDouble(Evaluation::getNote)
                .average()
                .orElse(0);
    }

    public double tauxReussiteGlobal() {

        long total = getAll().size();
        if (total == 0) return 0;

        long ok = getAll().stream()
                .filter(e -> e.getNote() >= 10)
                .count();

        return (ok * 100.0) / total;
    }

    public double tauxEchecGlobal() {

        long total = getAll().size();
        if (total == 0) return 0;

        long fail = getAll().stream()
                .filter(e -> e.getNote() < 10)
                .count();

        return (fail * 100.0) / total;
    }

    // ================= EXAMEN ANALYTICS =================

    public int examenPlusDifficile() {

        return getAll().stream()
                .collect(Collectors.groupingBy(
                        Evaluation::getExamenId,
                        Collectors.averagingDouble(Evaluation::getNote)
                ))
                .entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(-1);
    }

    public double ecartTypeGlobal() {

        List<Double> notes = getAll().stream()
                .map(Evaluation::getNote)
                .toList();

        double mean = notes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        return Math.sqrt(
                notes.stream()
                        .mapToDouble(n -> Math.pow(n - mean, 2))
                        .average()
                        .orElse(0)
        );
    }

    // ================= DISTRIBUTIONS =================

    public Map<String, Long> distributionNiveaux() {

        return getAll().stream()
                .collect(Collectors.groupingBy(e -> {

                    if (e.getNote() < 10) return "Échec";
                    if (e.getNote() < 14) return "Moyen";
                    if (e.getNote() < 16) return "Bon";
                    return "Excellent";

                }, Collectors.counting()));
    }

    public Map<String, Long> distributionNotes() {

        return getAll().stream()
                .collect(Collectors.groupingBy(e -> {

                    double n = e.getNote();

                    if (n < 5) return "0-5";
                    if (n < 10) return "5-10";
                    if (n < 12) return "10-12";
                    if (n < 14) return "12-14";
                    if (n < 16) return "14-16";
                    return "16-20";

                }, Collectors.counting()));
    }

    // ================= TOP / INSIGHTS =================

    public List<Integer> top3Eleves() {

        return getAll().stream()
                .collect(Collectors.groupingBy(
                        Evaluation::getEleveId,
                        Collectors.averagingDouble(Evaluation::getNote)
                ))
                .entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
    }

    public int meilleurEleveId() {

        return getAll().stream()
                .collect(Collectors.groupingBy(
                        Evaluation::getEleveId,
                        Collectors.averagingDouble(Evaluation::getNote)
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(-1);
    }

    public int eleveEnDifficulte() {

        return getAll().stream()
                .collect(Collectors.groupingBy(
                        Evaluation::getEleveId,
                        Collectors.averagingDouble(Evaluation::getNote)
                ))
                .entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(-1);
    }

    // ================= INDICATEUR AVANCÉ =================

    public double performanceIndex() {

        double moyenne = moyenneGlobale();
        double ecart = ecartTypeGlobal();

        return moyenne - (ecart * 0.5);
    }

    public String examenLePlusReussi() {
        return getAll().stream()
                .collect(Collectors.groupingBy(
                        Evaluation::getExamenId,
                        Collectors.averagingDouble(Evaluation::getNote)
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> "Examen ID " + e.getKey())
                .orElse("N/A");
    }

    public String examenLePlusDifficileLabel() {
        return getAll().stream()
                .collect(Collectors.groupingBy(
                        Evaluation::getExamenId,
                        Collectors.averagingDouble(Evaluation::getNote)
                ))
                .entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(e -> "Examen ID " + e.getKey())
                .orElse("N/A");
    }

    
}