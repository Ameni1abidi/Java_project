package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import tn.esprit.entities.Examen;
import tn.esprit.services.DashboardService;
import tn.esprit.services.ExamenService;
import tn.esprit.services.StatistiqueService;

import java.util.List;

public class StatistiqueController {

    @FXML private Label lblMoyenneGlobale;
    @FXML private Label lblReussite;
    @FXML private Label lblEchec;
    @FXML private Label lblTauxReussite;
    @FXML private Label lblTauxEchec;
    @FXML private Label lblEcartType;
    @FXML private Label lblPerformance;
    @FXML private Label lblTop3;
    @FXML private Label lblExamenDifficile;
    @FXML private Label lblEleveDifficulte;
    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;

    private final DashboardService dashboard = new DashboardService();
    private final StatistiqueService stat = new StatistiqueService();
    private final ExamenService examenService = new ExamenService();

    @FXML
    public void initialize() {

        loadKPIs();
        loadPieChart();
        loadBarChart();
        loadInsights();
    }

    private void loadKPIs() {

        lblMoyenneGlobale.setText(String.format("%.2f", dashboard.moyenneGlobale()));

        lblTauxReussite.setText(String.format("%.1f%%", stat.tauxReussiteGlobal()));

        lblTauxEchec.setText(String.format("%.1f%%", stat.tauxEchecGlobal()));

        lblEcartType.setText(String.format("%.2f", stat.ecartTypeGlobal()));

        lblPerformance.setText(String.format("%.2f", stat.performanceIndex()));
    }

    // ================= PIE CHART (REALISTIC VIEW) =================
    private void loadPieChart() {

        pieChart.getData().clear();

        double tauxReussite = stat.tauxReussiteGlobal();
        double tauxEchec = stat.tauxEchecGlobal();

        pieChart.getData().addAll(
                new PieChart.Data("Réussite %", tauxReussite),
                new PieChart.Data("Échec %", tauxEchec)
        );
    }

    // ================= BAR CHART =================
    private void loadBarChart() {

        barChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Moyenne par examen");

        List<Examen> examens = examenService.getAll();

        for (Examen e : examens) {

            double moyenne = stat.moyenneParExamen(e.getId());

            series.getData().add(
                    new XYChart.Data<>(e.getTitre(), moyenne)
            );
        }

        barChart.getData().add(series);
    }

    private void loadInsights() {

        lblTop3.setText(stat.top3Eleves().toString());

        lblExamenDifficile.setText(
                String.valueOf(stat.examenPlusDifficile())
        );

        lblEleveDifficulte.setText(
                String.valueOf(stat.eleveEnDifficulte())
        );
    }

}