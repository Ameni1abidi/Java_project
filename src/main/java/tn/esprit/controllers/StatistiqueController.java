package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import tn.esprit.entities.Examen;
import tn.esprit.services.ExamenService;
import tn.esprit.services.StatistiqueService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatistiqueController {


    // ================= KPI =================
    @FXML private Label lblMoyenneGlobale;
    @FXML private Label lblTauxReussite;
    @FXML private Label lblTauxEchec;
    @FXML private Label lblEcartType;
    @FXML private Label lblPerformance;

    // NEW KPI
    @FXML private Label lblNoteMax;
    @FXML private Label lblNoteMin;

    // ================= INSIGHTS =================
    @FXML private Label lblTop3;
    @FXML private Label lblExamenDifficile;
    @FXML private Label lblEleveDifficulte;
    @FXML private Label lblAnalyse;

    // ================= CHARTS =================
    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;
    private final StatistiqueService stat = new StatistiqueService();
    private final ExamenService examenService = new ExamenService();

    @FXML
    public void initialize() {

        loadKPIs();
        loadPieChart();
        loadBarChart();
        loadInsights();
    }
    // ================= KPI =================
    private void loadKPIs() {

        lblMoyenneGlobale.setText(String.format("%.2f", stat.moyenneGlobale()));
        lblTauxReussite.setText(String.format("%.1f%%", stat.tauxReussiteGlobal()));
        lblTauxEchec.setText(String.format("%.1f%%", stat.tauxEchecGlobal()));
        lblEcartType.setText(String.format("%.2f", stat.ecartTypeGlobal()));
        lblPerformance.setText(String.format("%.2f", stat.performanceIndex()));

        // NEW KPI
        lblNoteMax.setText(String.format("%.2f", stat.noteMaxGlobale()));
        lblNoteMin.setText(String.format("%.2f", stat.noteMinGlobale()));

        // Couleur dynamique (UX pro)
        if (stat.tauxReussiteGlobal() > 70) {
            lblTauxReussite.setStyle("-fx-text-fill: green;");
        } else {
            lblTauxReussite.setStyle("-fx-text-fill: red;");
        }
    }

    // ================= PIE CHART (FIXED) =================
    private void loadPieChart() {

        pieChart.getData().clear();
        Map<String, Long> distribution = stat.distributionNiveaux();

        distribution.forEach((niveau, count) -> {
            pieChart.getData().add(
                    new PieChart.Data(niveau, count)
            );
        });
    }

    // ================= BAR CHART (SORTED) =================
    private void loadBarChart() {

        barChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Moyenne par examen");

        List<Examen> examens = examenService.getAll();

        examens.stream()
                .sorted((e1, e2) -> Double.compare(
                        stat.moyenneParExamen(e2.getId()),
                        stat.moyenneParExamen(e1.getId())
                ))
                .forEach(e -> {

                    double moyenne = stat.moyenneParExamen(e.getId());

                    series.getData().add(
                            new XYChart.Data<>(e.getTitre(), moyenne)
                    );
                });

        barChart.getData().add(series);
    }

    // ================= INSIGHTS =================
    private void loadInsights() {

        // TOP 3 (format lisible)
        lblTop3.setText(
                stat.top3Eleves().stream()
                        .map(id -> "Élève " + id)
                        .collect(Collectors.joining(", "))
        );

        // Examen difficile (titre au lieu ID)
        int idDiff = stat.examenPlusDifficile();

        String titre = examenService.getAll().stream()
                .filter(e -> e.getId() == idDiff)
                .map(Examen::getTitre)
                .findFirst()
                .orElse("N/A");

        lblExamenDifficile.setText(titre);

        // Élève en difficulté
        lblEleveDifficulte.setText("Élève " + stat.eleveEnDifficulte());

        // Analyse intelligente
        lblAnalyse.setText(stat.analyseAutomatique());
    }
}