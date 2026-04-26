package tn.esprit.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.Chapitre;
import tn.esprit.entities.Cours;
import tn.esprit.services.ChapitreService;
import tn.esprit.services.CoursService;
import tn.esprit.utils.ResourceNavigationContext;

import java.util.List;

public class StudentCours {

    @FXML
    private FlowPane courseContainer;
    @FXML
    private Label weatherIcon;
    @FXML
    private Label weatherTemp;
    @FXML
    private Label weatherDesc;

    private final CoursService coursService = new CoursService();
    private final ChapitreService chapitreService = new ChapitreService();
    private Timeline refreshTimeline;

    @FXML
    public void initialize() {
        loadCourses();
        startAutoRefresh();
        setWeather("Ciel degage", 28);
    }

    private void startAutoRefresh() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> loadCourses()));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    private void loadCourses() {
        courseContainer.getChildren().clear();
        List<Cours> coursList = coursService.getAll();
        for (Cours c : coursList) {
            courseContainer.getChildren().add(createCourseCard(c));
        }
    }

    private VBox createCourseCard(Cours c) {
        VBox card = new VBox(10);
        card.setStyle("""
            -fx-background-color: white;
            -fx-padding: 18;
            -fx-background-radius: 22;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 18, 0, 0, 6);
            -fx-pref-width: 330;
        """);

        Label title = new Label(c.getTitre());
        title.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#111;");

        Label desc = new Label(
                c.getDescription() != null
                        ? c.getDescription().substring(0, Math.min(100, c.getDescription().length())) + "..."
                        : "Pas de description"
        );
        desc.setStyle("-fx-text-fill:#666; -fx-font-size:12px;");

        Label date = new Label("Date: " + c.getDateCreation());
        date.setStyle("-fx-text-fill:#999; -fx-font-size:11px;");

        int totalChaps = chapitreService.countByCoursId(c.getId());
        Label chapLabel = new Label("Nombre de chapitres: " + totalChaps);
        chapLabel.setStyle("""
            -fx-background-color:#ede9fe;
            -fx-text-fill:#5b21b6;
            -fx-padding:5 10;
            -fx-background-radius:20;
            -fx-font-weight:bold;
        """);

        int done = (int) (totalChaps * 0.6);
        double percent = totalChaps == 0 ? 0 : (double) done / totalChaps;
        Label progressTitle = new Label("Progression");
        progressTitle.setStyle("-fx-font-size:12px; -fx-text-fill:#444;");
        Label progressValue = new Label(done + "/" + totalChaps + " (" + (int) (percent * 100) + "%)");
        progressValue.setStyle("-fx-font-size:11px; -fx-text-fill:#555;");
        ProgressBar progressBar = new ProgressBar(percent);
        progressBar.setPrefWidth(280);
        progressBar.setStyle("-fx-accent:#2d89ef;");

        Button certBtn = new Button(percent >= 0.8 ? "Voir Certificat" : "Certificat bloque");
        certBtn.setDisable(percent < 0.8);
        certBtn.setStyle(percent >= 0.8
                ? "-fx-background-color:#7c3aed; -fx-text-fill:white; -fx-background-radius:25; -fx-padding:8 14; -fx-font-weight:bold;"
                : "-fx-background-color:#d1d5db; -fx-text-fill:#666; -fx-background-radius:25; -fx-padding:8 14;");

        VBox chaptersBox = new VBox(8);
        List<Chapitre> chapitres = chapitreService.getByCoursId(c.getId());
        if (chapitres.isEmpty()) {
            Label empty = new Label("Aucun chapitre pour ce cours.");
            empty.setStyle("-fx-text-fill:#aaa;");
            chaptersBox.getChildren().add(empty);
        } else {
            for (Chapitre chap : chapitres) {
                VBox chapCard = new VBox(4);
                chapCard.setStyle("-fx-background-color:#f6f8ff; -fx-padding:10; -fx-background-radius:12;");

                Label t = new Label(chap.getOrdre() + ". " + chap.getTitre());
                t.setStyle("-fx-font-weight:bold; -fx-text-fill:#333;");

                Label info = new Label("Duree: " + chap.getDureeEstimee() + " min | Type: " + chap.getTypeContenu());
                info.setStyle("-fx-text-fill:#666; -fx-font-size:11px;");

                Label status = new Label("Temps: 0 min | En cours");
                status.setStyle("-fx-text-fill:#f39c12; -fx-font-size:11px;");

                Button openChap = new Button("Voir Resume");
                openChap.setStyle("-fx-background-color:#ff416c; -fx-text-fill:white; -fx-background-radius:20; -fx-padding:5 10;");

                Button ressourcesBtn = new Button("Ressources");
                ressourcesBtn.setStyle("-fx-background-color:#5b7cfa; -fx-text-fill:white; -fx-background-radius:20; -fx-padding:5 12; -fx-font-weight:bold;");
                ressourcesBtn.setOnAction(e -> openChapterResources(e, chap));

                chapCard.getChildren().addAll(t, info, status, openChap, ressourcesBtn);
                chaptersBox.getChildren().add(chapCard);
            }
        }

        card.getChildren().addAll(title, desc, date, chapLabel, progressTitle, progressValue, progressBar, certBtn, chaptersBox);
        return card;
    }

    private void openChapterResources(ActionEvent event, Chapitre chapitre) {
        if (chapitre == null || chapitre.getId() <= 0) {
            showNavigationError("Chapitre invalide. Impossible d'ouvrir les ressources.");
            return;
        }

        try {
            if (refreshTimeline != null) {
                refreshTimeline.stop();
            }
            ResourceNavigationContext.openForChapitre(chapitre.getId(), chapitre.getTitre(), true);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/StudentChapterResources.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("Ressources du chapitre");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showNavigationError("Ouverture des ressources echouee: " + e.getMessage());
        }
    }

    private void showNavigationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Navigation");
        alert.setHeaderText("Le bouton Ressources a rencontre une erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void goDashboard() {}
    public void goForum() {}
    public void goExams() {}
    public void goLogout() {}

    private void setWeather(String desc, double temp) {
        String icon = "?";
        if (desc.equalsIgnoreCase("Ciel degage")) {
            icon = "Sun";
        } else if (desc.equalsIgnoreCase("Pluie") || desc.equalsIgnoreCase("Averses")) {
            icon = "Rain";
        } else if (desc.equalsIgnoreCase("Orage")) {
            icon = "Storm";
        } else if (desc.equalsIgnoreCase("Neige")) {
            icon = "Snow";
        }

        weatherIcon.setText(icon);
        weatherTemp.setText((int) temp + "C");
        weatherDesc.setText(desc);
    }
}
