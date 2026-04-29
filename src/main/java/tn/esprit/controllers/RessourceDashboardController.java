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
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.services.RessourceDashboardService;
import tn.esprit.services.RessourceDashboardService.ResourceEngagement;
import tn.esprit.utils.ResourceNavigationContext;

import java.util.List;
import java.util.Map;

public class RessourceDashboardController {
    @FXML private BarChart<String, Number> topFavoritesChart;
    @FXML private PieChart categoryPieChart;
    @FXML private CategoryAxis topFavoritesXAxis;
    @FXML private NumberAxis topFavoritesYAxis;
    @FXML private Label favoriteTotalLabel;
    @FXML private Label favoriteLeaderLabel;

    private RessourceDashboardService dashboardService;

    @FXML
    public void initialize() {
        setupAxes();
        try {
            dashboardService = new RessourceDashboardService();
            loadTopFavoritesChart();
            loadFavoriteAnalysis();
            loadCategoryPieChart();
        } catch (Exception e) {
            e.printStackTrace();
            clearCharts();
        }
    }

    private void setupAxes() {
        topFavoritesXAxis.setLabel("");
        topFavoritesYAxis.setLabel("Favoris");
        topFavoritesChart.setAnimated(false);
    }

    private void clearCharts() {
        topFavoritesChart.getData().clear();
        categoryPieChart.getData().clear();
        favoriteTotalLabel.setText("0 favori eleve enregistre");
        favoriteLeaderLabel.setText("Aucune ressource favorite pour le moment");
    }

    private void loadTopFavoritesChart() {
        topFavoritesChart.setTitle("");
        XYChart.Series<String, Number> favorites = new XYChart.Series<>();
        favorites.setName("Favoris");
        List<ResourceEngagement> topFavorites = dashboardService.getTopResourcesByFavorites(5);
        if (topFavorites.isEmpty()) {
            topFavoritesChart.setTitle("Aucun favori eleve pour le moment");
            topFavoritesChart.getData().clear();
            return;
        }

        for (ResourceEngagement stat : topFavorites) {
            favorites.getData().add(new XYChart.Data<>(shortTitle(stat.title()), stat.favorites()));
        }
        topFavoritesChart.getData().setAll(favorites);
    }

    private void loadFavoriteAnalysis() {
        int totalFavorites = dashboardService.getTotalFavorites();
        favoriteTotalLabel.setText(totalFavorites + " favori(s) eleve(s) enregistre(s)");

        List<ResourceEngagement> topFavorites = dashboardService.getTopResourcesByFavorites(1);
        if (topFavorites.isEmpty()) {
            favoriteLeaderLabel.setText("Aucune ressource favorite pour le moment");
            return;
        }

        ResourceEngagement leader = topFavorites.get(0);
        favoriteLeaderLabel.setText("Ressource la plus favorite: " + leader.title()
                + " (" + leader.favorites() + " favori(s))");
    }

    private void loadCategoryPieChart() {
        Map<String, Integer> data = dashboardService.getResourcesByCategory();
        categoryPieChart.setData(FXCollections.observableArrayList(
                data.entrySet().stream()
                        .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                        .toList()
        ));
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
