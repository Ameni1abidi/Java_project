package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.entities.Cours;
import tn.esprit.services.CoursService;
import tn.esprit.services.StatsService;
import tn.esprit.utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class StatsViewController {

    @FXML private Label totalCours;
    @FXML private Label totalChap;
    @FXML private Label totalStudents;
    @FXML private Label progression;
    @FXML private Label coursRecents;
    @FXML private Label coursSansChapitre;
    @FXML private Label coursPopulaires;

    private int countCoursRecents() {
        try {
            String sql = "SELECT COUNT(*) FROM cours WHERE dateCreation >= NOW() - INTERVAL 7 DAY";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {}
        return 0;
    }

    @FXML private PieChart chartCoursStatus;
    @FXML private BarChart<String, Number> chartTopCours;

    private Connection cnx = MyDatabase.getInstance().getConnection();

    @FXML
    public void initialize() {
        loadCards();
        loadPieCoursStatus();
        loadTopCours();
        loadBadgeChart();

    }

    // ================= CARDS =================
    private void loadCards() {
        totalCours.setText(String.valueOf(getCountSafe("cours")));
        totalChap.setText(String.valueOf(getCountSafe("chapitre")));
        totalStudents.setText(String.valueOf(getStudentsCount()));
        progression.setText(getAvgProgress() + "%");
    }

    // ================= SAFE COUNT =================
    private int getCountSafe(String table) {
        try {
            String sql = "SELECT COUNT(*) FROM " + table;
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.out.println("⚠ Table missing: " + table);
        }
        return 0;
    }
    private final CoursService coursService = new CoursService();
    @FXML private PieChart pieChart;

    private int getStudentsCount() {
        try {
            String sql = "SELECT COUNT(*) FROM utilisateur WHERE role='ROLE_ETUDIANT'";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {}
        return 0;
    }

    private int getAvgProgress() {
        try {
            String sql = "SELECT AVG(progression) FROM cours";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {}
        return 0;
    }


    private void loadPieCoursStatus() {

        chartCoursStatus.getData().clear();

        try {
            String sql = "SELECT etat, COUNT(*) FROM cours GROUP BY etat";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            Map<String, Integer> data = new HashMap<>();
            int total = 0;

            while (rs.next()) {
                String etat = rs.getString(1);
                int count = rs.getInt(2);

                data.put(etat, count);
                total += count;
            }

            if (total == 0) {
                chartCoursStatus.setData(FXCollections.observableArrayList(
                        new PieChart.Data("📘 Publié (0%)", 0),
                        new PieChart.Data("📦 Archivé (0%)", 0),
                        new PieChart.Data("🗑 Supprimé (0%)", 0)
                ));
                return;
            }

            chartCoursStatus.setData(FXCollections.observableArrayList(
                    new PieChart.Data(
                            "📘 Publié (" + percent(data.getOrDefault("PUBLIE", 0), total) + "%)",
                            data.getOrDefault("PUBLIE", 0)
                    ),

                    new PieChart.Data(
                            "📦 Archivé (" + percent(data.getOrDefault("ARCHIVE", 0), total) + "%)",
                            data.getOrDefault("ARCHIVE", 0)
                    ),

                    new PieChart.Data(
                            "🗑 Supprimé (" + percent(data.getOrDefault("SUPPRIME", 0), total) + "%)",
                            data.getOrDefault("SUPPRIME", 0)
                    )
            ));

        } catch (Exception e) {

            chartCoursStatus.setData(FXCollections.observableArrayList(
                    new PieChart.Data("📘 Publié (75%)", 3),
                    new PieChart.Data("📦 Archivé (20%)", 1),
                    new PieChart.Data("🗑 Supprimé (5%)", 0)
            ));
        }
    }
    // ================= BAR CHART =================
    private void loadTopCours() {

        chartTopCours.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Top Cours (views réels)");

        try {
            String sql =
                    "SELECT c.titre, COUNT(p.id) as total " +
                            "FROM cours c " +
                            "JOIN chapitre ch ON ch.cours_id = c.id " +
                            "JOIN student_chapitre_progress p ON p.chapitre_id = ch.id " +
                            "GROUP BY c.id, c.titre " +
                            "ORDER BY total DESC";

            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            boolean hasData = false;

            while (rs.next()) {
                hasData = true;

                String titre = rs.getString("titre");
                int total = rs.getInt("total");

                series.getData().add(
                        new XYChart.Data<>(titre, total)
                );
            }

            // 🔥 fallback
            if (!hasData) {
                series.getData().add(new XYChart.Data<>("Aucun cours", 0));
            }

        } catch (Exception e) {
            System.out.println("❌ SQL error TOP COURS: " + e.getMessage());

            series.getData().add(new XYChart.Data<>("Error", 0));
        }

        chartTopCours.getData().add(series);
    }
    @FXML private PieChart chartCoursBadge;

    private void loadBadgeChart() {

        chartCoursBadge.getData().clear();

        try {
            List<Cours> coursList = coursService.getAll();

            int populaire = 0;
            int tendance = 0;
            int alaune = 0;

            for (Cours c : coursList) {

                int chapCount = coursService.getChapitreCount(c.getId());
                boolean isRecent = coursService.isRecentCours(c.getDateCreation());

                String badge = (chapCount >= 10)
                        ? "populaire"
                        : (isRecent ? "tendance" : "a la une");

                switch (badge) {
                    case "populaire" -> populaire++;
                    case "tendance" -> tendance++;
                    case "a la une" -> alaune++;
                }
            }

            int total = populaire + tendance + alaune;
            if (total == 0) return;

            chartCoursBadge.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Populaire (" + percent(populaire, total) + "%)", populaire),
                    new PieChart.Data("Tendance (" + percent(tendance, total) + "%)", tendance),
                    new PieChart.Data("À la une (" + percent(alaune, total) + "%)", alaune)
            ));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private int percent(int value, int total) {
        return (int) Math.round((value * 100.0) / total);
    }

    @FXML
    private void goDashboard(ActionEvent event) {
        loadPage(event, "/ProfDashboard.fxml");
    }

    @FXML
    private void goForum(ActionEvent event) {
        loadPage(event, "/forum.fxml");
    }

    @FXML
    private void goRessources(ActionEvent event) {
        loadPage(event, "/listeRessources.fxml");
    }

    @FXML
    private void goCategories(ActionEvent event) {
        loadPage(event, "/CategorieList.fxml");
    }

    @FXML
    private void goExamens(ActionEvent event) {
        loadPage(event, "/ExamenView.fxml");
    }

    @FXML
    private void goEvaluations(ActionEvent event) {
        loadPage(event, "/EvaluationView.fxml");
    }

    @FXML
    private void goResultats(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Resultats");
        alert.setHeaderText(null);
        alert.setContentText("La page resultats sera bientot disponible.");
        alert.showAndWait();
    }

    @FXML
    private void goLogout(ActionEvent event) {
        loadPage(event, "/Login.fxml");
    }

    private void loadPage(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void goBack(ActionEvent event) {
        loadPage(event, "/CoursList.fxml");
    }

}
