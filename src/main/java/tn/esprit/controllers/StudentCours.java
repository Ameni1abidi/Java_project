package tn.esprit.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.Cours;
import tn.esprit.entities.Chapitre;
import tn.esprit.services.ChapitreService;
import tn.esprit.services.CoursService;
import tn.esprit.services.StudentChapitreProgressService;

import java.awt.*;
import java.io.File;
import java.util.List;

public class StudentCours {

    @FXML
    private FlowPane courseContainer;

    private final CoursService coursService = new CoursService();
    private final ChapitreService chapitreService = new ChapitreService();

    // ================= INIT =================
    @FXML
    public void initialize() {
        loadCourses();
        startAutoRefresh();
        setWeather("Ciel degage", 28);
    }

    // ================= AUTO REFRESH =================
    private void startAutoRefresh() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(5), e -> loadCourses())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    // ================= LOAD COURSES =================
    private void loadCourses() {
        courseContainer.getChildren().clear();

        List<Cours> coursList = coursService.getAll();

        for (Cours c : coursList) {
            courseContainer.getChildren().add(createCourseCard(c));
        }
    }

    // ================= COURSE CARD =================
    private VBox createCourseCard(Cours c) {

        VBox card = new VBox(10);
        card.setStyle("""
            -fx-background-color: white;
            -fx-padding: 18;
            -fx-background-radius: 22;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 18, 0, 0, 6);
            -fx-pref-width: 330;
        """);

        // ===== TITLE =====
        Label title = new Label(c.getTitre());
        title.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#111;");

        // ===== DESCRIPTION =====
        Label desc = new Label(
                c.getDescription() != null
                        ? c.getDescription().substring(0, Math.min(100, c.getDescription().length())) + "..."
                        : "Pas de description"
        );
        desc.setStyle("-fx-text-fill:#666; -fx-font-size:12px;");

        // ===== DATE =====
        Label date = new Label("📅 " + c.getDateCreation());
        date.setStyle("-fx-text-fill:#999; -fx-font-size:11px;");

        // ===== CHAPTER COUNT =====
        int totalChaps = chapitreService.countByCoursId(c.getId());

        Label chapLabel = new Label("📚 Nombre de chapitres: " + totalChaps);
        chapLabel.setStyle("""
            -fx-background-color:#ede9fe;
            -fx-text-fill:#5b21b6;
            -fx-padding:5 10;
            -fx-background-radius:20;
            -fx-font-weight:bold;
        """);



        // ===== PROGRESS =====
        int progress = chapitreService.getCourseProgress(3, c.getId()); // userId = 3

        double percent = progress / 100.0;

        Label progressTitle = new Label("Progression");
        progressTitle.setStyle("-fx-font-size:12px; -fx-text-fill:#444;");

        int done = (int) Math.round((progress / 100.0) * totalChaps);

        Label progressValue = new Label(done + "/" + totalChaps + " (" + progress + "%)");

        ProgressBar progressBar = new ProgressBar(percent);
        progressBar.setPrefWidth(280);
        progressBar.setStyle("-fx-accent:#2d89ef;");

        // ===== CERTIFICATE =====
        Button certBtn = new Button("🏆 Certificat");

        StudentChapitreProgressService progressService = new StudentChapitreProgressService();

        boolean eligible = progressService.getCourseProgress(3, c.getId()) >= 100;

        if (!eligible) {
            certBtn.setDisable(true);
            certBtn.setText("Certificat bloqué");
            certBtn.setStyle("""
        -fx-background-color:#d1d5db;
        -fx-text-fill:#666;
        -fx-background-radius:25;
        -fx-padding:8 14;
    """);
        } else {
            certBtn.setText("Voir Certificat");
            certBtn.setStyle("""
                -fx-background-color:#7c3aed;
                -fx-text-fill:white;
                -fx-background-radius:25;
                -fx-padding:8 14;
                -fx-font-weight:bold;
            """);

            certBtn.setOnAction(e -> {
                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/Certificat.fxml")
                    );

                    Parent root = loader.load();

                    CertificatController controller = loader.getController();

                    Cours cours = coursService.getById(c.getId());

                    controller.setData(
                            "Student",
                            cours,
                            true,
                            0,
                            0
                    );

                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Certificat");
                    stage.show();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }

        // ===== CHAPTERS =====
        VBox chaptersBox = new VBox(8);

        List<Chapitre> chapitres = chapitreService.getByCoursId(c.getId());

        if (chapitres.isEmpty()) {

            Label empty = new Label("Aucun chapitre pour ce cours.");
            empty.setStyle("-fx-text-fill:#aaa;");
            chaptersBox.getChildren().add(empty);

        } else {

            for (Chapitre chap : chapitres) {

                VBox chapCard = new VBox(6);
                chapCard.setStyle("""
    -fx-background-color:#f6f8ff;
    -fx-padding:12;
    -fx-background-radius:12;
""");

// ===== TITLE =====
                Label titre = new Label(chap.getOrdre() + ". " + chap.getTitre());
                title.setStyle("-fx-font-weight:bold; -fx-text-fill:#333;");

// ===== DUREE =====
                Label duree = new Label("📘 Durée : " + chap.getDureeEstimee() + " min");
                duree.setStyle("-fx-text-fill:#555; -fx-font-size:11px;");

// ===== TEMPS PASSE =====
                int tempsPasse = 0;
                Label temps = new Label("⏱ Temps passé : " + tempsPasse + " min");
                temps.setStyle("-fx-text-fill:#555; -fx-font-size:11px;");

// ===== BADGE =====
                Label badge = new Label("🏷 Badge temps : En cours");
                badge.setStyle("""
                    -fx-text-fill:white;
                    -fx-background-color:#8b5cf6;
                    -fx-padding:4 10;
                    -fx-background-radius:12;
                """);


                Label type = new Label("📄 Type : " + chap.getTypeContenu().toUpperCase());
                type.setStyle("-fx-text-fill:#555; -fx-font-size:11px;");

                Button resumeBtn = new Button("👁 Voir Résumé");
                resumeBtn.setStyle("""
    -fx-background-color:#ff416c;
    -fx-text-fill:white;
    -fx-background-radius:20;
    -fx-padding:5 12;
    -fx-font-weight:bold;
""");


                resumeBtn.setOnAction(e -> {

                    try {
                        // ===== 1. فتح صفحة résumé =====
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ChapitreDetail.fxml"));
                        Parent root = loader.load();

                        StudentChapitreDetail controller = loader.getController();
                        controller.setChapitre(chap);

                        Stage stage = (Stage) courseContainer.getScene().getWindow();
                        stage.setScene(new Scene(root));


                        String path = chap.getContenuFichier();

                        System.out.println("PATH = " + path);

                        File file = new File(path);

                        if (file.exists()) {
                            System.out.println("✅ FILE FOUND");
                            Desktop.getDesktop().browse(file.toURI());
                        } else {
                            System.out.println("❌ FILE NOT FOUND");
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

// ===== ADD ALL =====
                chapCard.getChildren().addAll(
                        title,
                        duree,
                        temps,
                        badge,
                        type,
                        resumeBtn
                );

                chaptersBox.getChildren().add(chapCard);
            }
        }

        // ===== BUILD CARD =====
        card.getChildren().addAll(
                title,
                desc,
                date,
                chapLabel,
                progressTitle,
                progressValue,
                progressBar,
                certBtn,
                chaptersBox
        );

        return card;
    }

    // ================= NAVIGATION =================
    public void goDashboard() {}
    public void goForum() {}
    public void goExams() {}
    public void goLogout() {}

    @FXML private Label weatherIcon;
    @FXML private Label weatherTemp;
    @FXML private Label weatherDesc;
    private void setWeather(String desc, double temp) {

        String icon = "☁";

        if (desc.equalsIgnoreCase("Ciel degage")) {
            icon = "☀";
        } else if (desc.equalsIgnoreCase("Pluie") || desc.equalsIgnoreCase("Averses")) {
            icon = "☂";
        } else if (desc.equalsIgnoreCase("Orage")) {
            icon = "⚡";
        } else if (desc.equalsIgnoreCase("Neige")) {
            icon = "❄";
        }

        weatherIcon.setText(icon);
        weatherTemp.setText((int) temp + "°C");
        weatherDesc.setText(desc);
    }


}
