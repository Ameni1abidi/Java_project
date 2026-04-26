package tn.esprit.services;

import tn.esprit.entities.Examen;
import java.util.List;

public class DashboardService {

    private final StatistiqueService stat = new StatistiqueService();
    private final ExamenService examenService = new ExamenService();

    // ================= KPI GLOBAL =================

    public double moyenneGlobale() {
        return stat.moyenneGlobale();
    }

    public double tauxReussiteGlobal() {
        return stat.tauxReussiteGlobal();
    }

    public double tauxEchecGlobal() {
        return stat.tauxEchecGlobal();
    }

    public double ecartTypeGlobal() {
        return stat.ecartTypeGlobal();
    }

    public double performanceIndex() {
        return stat.performanceIndex();
    }

    // ================= COMPTEURS =================

    public long totalReussite() {
        return examenService.getAll()
                .stream()
                .mapToLong(e -> stat.nombreReussis(e.getId()))
                .sum();
    }

    public long totalEchecs() {
        return examenService.getAll()
                .stream()
                .mapToLong(e -> stat.nombreEchecs(e.getId()))
                .sum();
    }

    // ================= INSIGHTS =================

    public int examenPlusDifficile() {
        return stat.examenPlusDifficile();
    }

    public int meilleurEleve() {
        return stat.meilleurEleveId();
    }

    public int eleveEnDifficulte() {
        return stat.eleveEnDifficulte();
    }

    public List<Integer> top3Eleves() {
        return stat.top3Eleves();
    }

    // ================= BONUS =================

    public double moyenneParExamenGlobal() {

        List<Examen> examens = examenService.getAll();

        if (examens.isEmpty()) return 0;

        return examens.stream()
                .mapToDouble(e -> stat.moyenneParExamen(e.getId()))
                .average()
                .orElse(0);
    }
}