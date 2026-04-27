package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import tn.esprit.services.RessourceDashboardService;
import tn.esprit.services.RessourceDashboardService.DailyInteraction;
import tn.esprit.services.RessourceDashboardService.ResourceEngagement;
import tn.esprit.utils.ResourceNavigationContext;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class RessourceDashboardController {
    @FXML private BarChart<String, Number> engagementChart;
    @FXML private BarChart<String, Number> topScoreChart;
    @FXML private PieChart categoryPieChart;
    @FXML private LineChart<String, Number> evolutionChart;
    @FXML private CategoryAxis engagementXAxis;
    @FXML private NumberAxis engagementYAxis;
    @FXML private CategoryAxis topScoreXAxis;
    @FXML private NumberAxis topScoreYAxis;

    private RessourceDashboardService dashboardService;
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM");

    @FXML
    public void initialize() {
        setupAxes();
        try {
            dashboardService = new RessourceDashboardService();
            loadEngagementChart();
            loadTopScoreChart();
            loadCategoryPieChart();
            loadEvolutionChart();
        } catch (Exception e) {
            e.printStackTrace();
            clearCharts();
        }
    }

    private void setupAxes() {
        engagementXAxis.setLabel("");
        engagementYAxis.setLabel("");
        topScoreXAxis.setLabel("");
        topScoreYAxis.setLabel("");
        engagementChart.setAnimated(false);
        topScoreChart.setAnimated(false);
        evolutionChart.setAnimated(false);
    }

    private void loadEngagementChart() {
        List<ResourceEngagement> stats = dashboardService.getEngagementByResource();

        XYChart.Series<String, Number> views = new XYChart.Series<>();
        views.setName("Vues");
        XYChart.Series<String, Number> likes = new XYChart.Series<>();
        likes.setName("Likes");
        XYChart.Series<String, Number> favorites = new XYChart.Series<>();
        favorites.setName("Favoris");

        for (ResourceEngagement stat : stats) {
            String title = shortTitle(stat.title());
            views.getData().add(new XYChart.Data<>(title, stat.views()));
            likes.getData().add(new XYChart.Data<>(title, stat.likes()));
            favorites.getData().add(new XYChart.Data<>(title, stat.favorites()));
        }

        engagementChart.getData().setAll(views, likes, favorites);
    }

    private void clearCharts() {
        engagementChart.getData().clear();
        topScoreChart.getData().clear();
        categoryPieChart.getData().clear();
        evolutionChart.getData().clear();
    }

    private void loadTopScoreChart() {
        XYChart.Series<String, Number> score = new XYChart.Series<>();
        score.setName("Score");
        for (ResourceEngagement stat : dashboardService.getTopResourcesByScore(5)) {
            score.getData().add(new XYChart.Data<>(shortTitle(stat.title()), stat.score()));
        }
        topScoreChart.getData().setAll(score);
    }

    private void loadCategoryPieChart() {
        Map<String, Integer> data = dashboardService.getResourcesByCategory();
        categoryPieChart.setData(FXCollections.observableArrayList(
                data.entrySet().stream()
                        .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                        .toList()
        ));
    }

    private void loadEvolutionChart() {
        List<DailyInteraction> evolution = dashboardService.getEvolutionLast30Days();

        XYChart.Series<String, Number> views = new XYChart.Series<>();
        views.setName("Vues");
        XYChart.Series<String, Number> likes = new XYChart.Series<>();
        likes.setName("Likes");
        XYChart.Series<String, Number> favorites = new XYChart.Series<>();
        favorites.setName("Favoris");

        for (DailyInteraction day : evolution) {
            String label = day.day().format(DAY_FORMAT);
            views.getData().add(new XYChart.Data<>(label, day.views()));
            likes.getData().add(new XYChart.Data<>(label, day.likes()));
            favorites.getData().add(new XYChart.Data<>(label, day.favorites()));
        }

        evolutionChart.getData().setAll(views, likes, favorites);
    }

    private String shortTitle(String value) {
        if (value == null || value.isBlank()) {
            return "N/A";
        }
        return value.length() <= 14 ? value : value.substring(0, 11) + "...";
    }

    @FXML private void goDashboard(ActionEvent event) { loadPage(event, "/ProfDashboard.fxml"); }
    @FXML private void goForum(ActionEvent event) { loadPage(event, "/forum.fxml"); }
    @FXML private void goCours(ActionEvent event) { loadPage(event, "/CoursList.fxml"); }
    @FXML private void goRessources(ActionEvent event) { loadPage(event, "/listeRessources.fxml"); }
    @FXML private void goCategories(ActionEvent event) { loadPage(event, "/CategorieList.fxml"); }
    @FXML private void goExamens(ActionEvent event) { loadPage(event, "/ExamenView.fxml"); }
    @FXML private void goEvaluations(ActionEvent event) { loadPage(event, "/EvaluationView.fxml"); }
    @FXML private void goLogout(ActionEvent event) { loadPage(event, "/Login.fxml"); }

    private void loadPage(ActionEvent event, String fxmlPath) {
        try {
            ResourceNavigationContext.clear();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de navigation");
            alert.setHeaderText("Impossible d'ouvrir la page");
            alert.setContentText(fxmlPath + "\n" + e.getMessage());
            alert.showAndWait();
        }
    }
}
