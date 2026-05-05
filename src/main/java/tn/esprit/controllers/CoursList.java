package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.entities.Cours;
import tn.esprit.services.CoursService;

import javafx.event.ActionEvent;
import tn.esprit.utils.FlashSession;
import tn.esprit.utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class CoursList {

    @FXML
    private FlowPane coursContainer;

    @FXML
    private TextField searchField;
    public static String flashMessage = null;
    public static String flashType = null;
    private boolean showOnlyArchived = false;
    @FXML
    private Button btnArchives;
    @FXML private Button btnVoirCours;

    private CoursService service = new CoursService();
    private Connection cnx = MyDatabase.getInstance().getConnection();

    public void initialize() {
        loadData();
        String msg = FlashSession.getMessage();
        String type = FlashSession.getType();

        if (msg != null) {
            showNotification(msg, type);
            FlashSession.clear();
        }
        showOnlyArchived = false;
    }

    public void loadData() {
        coursContainer.getChildren().clear();
        List<Cours> list = service.getAll();

        System.out.println("MODE ARCHIVE = " + showOnlyArchived); // debug

        for (Cours c : list) {

            boolean isArchived = c.getEtat() != null &&
                    c.getEtat().trim().equalsIgnoreCase("ARCHIVE");

            System.out.println(c.getTitre() + " -> " + c.getEtat()); // debug

            if (showOnlyArchived) {
                if (!isArchived) continue;
            } else {
                if (isArchived) continue;
            }
            if (showOnlyArchived) {
                btnArchives.setVisible(false);
                btnArchives.setManaged(false);
            } else {
                btnArchives.setVisible(true);
                btnArchives.setManaged(true);
            }

            coursContainer.getChildren().add(createCard(c));
        }
    }
    private VBox createCard(Cours c) {

        VBox card = new VBox(12);
        card.setPrefWidth(280); // 🔥 تكبير الكارد
        card.setStyle("""
        -fx-background-color:white;
        -fx-padding:18;
        -fx-background-radius:18;
        -fx-border-radius:18;
        -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.15),15,0,0,5);
    """);

        // 🔹 titre
        Label titre = new Label(c.getTitre());
        titre.setStyle("-fx-font-weight:bold; -fx-font-size:18;");

        String badgeValue;

        try {
            badgeValue = c.getBadge();
        } catch (Exception e) {
            badgeValue = "none";
        }

        if (badgeValue == null) badgeValue = "none";


        String badge = c.getBadge(); // ممكن null

        String etat = c.getEtat();

// 🔥 STAT LOGIC (بدل DB badge)
        String badgeText = c.getBadge();
        if (badgeText == null || badgeText.isBlank()) {
            badgeText = "⭐ À la une";
        } String color = switch (badgeText.toLowerCase()) {
            case "populaire" -> "#f39c12";
            case "tendance" -> "#5f27cd";
            default -> "#7c5cbf";
        };
        Label badgeLabel = new Label(badgeText);

        badgeLabel.setStyle(
                "-fx-background-color:" + color + ";" +
                        "-fx-text-fill:white;" +
                        "-fx-padding:4 10;" +
                        "-fx-background-radius:12;" +
                        "-fx-font-size:11;" +
                        "-fx-font-weight:bold;"
        );



        // 🔹 description
        Label desc = new Label(c.getDescription());
        desc.setWrapText(true); // 🔥 مهم
        desc.setStyle("-fx-text-fill:#555; -fx-font-size:13;");

        // 🔹 date
        Label date = new Label("📅 Créé le: " + c.getDateCreation());
        date.setStyle("-fx-text-fill:#888; -fx-font-size:11;");

        // 🔹 buttons
        Button btnChapitre = new Button("Chapitres");
        btnChapitre.setStyle("""
        -fx-border-color:#28a745;
        -fx-text-fill:#28a745;
        -fx-background-color:transparent;
        -fx-border-radius:15;
        -fx-padding:5 12;
    """);
        btnChapitre.setOnAction(e -> goToChapitres(c));

        Button btnModifier = new Button("Modifier");
        btnModifier.setStyle("""
        -fx-border-color:#007bff;
        -fx-text-fill:#007bff;
        -fx-background-color:transparent;
        -fx-border-radius:15;
        -fx-padding:5 12;
    """);
        btnModifier.setOnAction(e -> modifierCours(c));
        // 🔹 archive / restore
        Button btnArchive = new Button();
        boolean isArchived = "ARCHIVE".equalsIgnoreCase(
                c.getEtat() == null ? "" : c.getEtat().trim()
        );

        Button btnDelete = new Button("🗑 Supprimer");

        btnDelete.setStyle("""
    -fx-border-color:#e74c3c;
    -fx-text-fill:#e74c3c;
    -fx-background-color:transparent;
    -fx-border-radius:15;
    -fx-padding:5 12;
"""); btnDelete.setOnAction(e -> deleteCours(c));


        if (isArchived) {
            btnArchive.setText("🔄 Restore");
            btnArchive.setStyle("""
        -fx-border-color:#28a745;
        -fx-text-fill:#28a745;
        -fx-background-color:transparent;
        -fx-border-radius:15;
        -fx-padding:5 12;
    """);
            btnArchive.setOnAction(e -> restoreCours(c));
        } else {
            btnArchive.setText("📦 Archiver");
            btnArchive.setStyle("""
        -fx-border-color:#DAB1DA;
        -fx-text-fill:#DAB1DA;
        -fx-background-color:transparent;
        -fx-border-radius:15;
        -fx-padding:5 12;
    """);
            btnArchive.setOnAction(e -> archiveCours(c));
        }

        HBox actions = new HBox(8, btnChapitre, btnModifier, btnArchive, btnDelete);

// 🔥 style archived
        if ("Archive".equals(c.getEtat())) {
            card.setStyle("""
        -fx-background-color:#f3f3f3;
        -fx-padding:18;
        -fx-background-radius:25;
        -fx-opacity:0.6;
    """);

            Label archivedBadge = new Label("📦 ARCHIVED");
            archivedBadge.setStyle("""
        -fx-background-color:#dc3545;
        -fx-text-fill:white;
        -fx-padding:4 10;
        -fx-background-radius:10;
    """);

            card.getChildren().addAll(titre, badgeLabel, archivedBadge, desc, date, actions);

        } else {
            card.getChildren().addAll(titre, badgeLabel, desc, date, actions);
        }

        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() +
                "-fx-scale-x:1.03; -fx-scale-y:1.03;"));

        card.setOnMouseExited(e -> card.setStyle(card.getStyle()
                .replace("-fx-scale-x:1.03; -fx-scale-y:1.03;", "")));

        // add
       // card.getChildren().addAll(titre, badge, desc, date, actions);

        return card;
    }

    private boolean isRecentCours(java.sql.Date dateCreation) {
        if (dateCreation == null) return false;

        long diff = System.currentTimeMillis() - dateCreation.getTime();
        long days = diff / (1000 * 60 * 60 * 24);

        return days <= 7;
    }
    @FXML
    void showArchived() {
        showOnlyArchived = true;
        loadData();
    }

    @FXML
    void showAll() {
        showOnlyArchived = false;
        loadData();
    }

    void deleteCours(Cours c) {
        try {
            String sql = "DELETE FROM cours WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, c.getId());
            ps.executeUpdate();

            loadData(); // refresh UI

            showNotification("🗑 Cours supprimé avec succès", "success");

        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Erreur suppression", "error");
        }
    }

    @FXML
    void searchCours() {
        String keyword = searchField.getText().toLowerCase().trim();
        coursContainer.getChildren().clear();

        for (Cours c : service.getAll()) {

            String etat = c.getEtat() == null ? "PUBLIE" : c.getEtat().trim().toUpperCase();

            // 🔥 نفس logique متاع archive
            if (showOnlyArchived) {
                if (!etat.equals("ARCHIVE")) continue;
            } else {
                if (etat.equals("ARCHIVE")) continue;
            }

            if (c.getTitre().toLowerCase().contains(keyword)
                    || c.getDescription().toLowerCase().contains(keyword)) {

                coursContainer.getChildren().add(createCard(c));
            }
        }
    }
    @FXML
    private Label notificationLabel;

    private void showNotification(String msg, String type) {

        notificationLabel.setText(msg);
        notificationLabel.setVisible(true);
        notificationLabel.setManaged(true);

        String base = "-fx-padding:10 15; -fx-background-radius:10; -fx-font-weight:bold;";

        switch (type) {
            case "success":
                notificationLabel.setStyle(base + "-fx-background-color:#e8fff0; -fx-text-fill:#1c6b3a;");
                break;

            case "error":
                notificationLabel.setStyle(base + "-fx-background-color:#ffe9e9; -fx-text-fill:#8a1f1f;");
                break;

            case "warning":
                notificationLabel.setStyle(base + "-fx-background-color:#fff7e0; -fx-text-fill:#7a5b00;");
                break;
        }

        // 🔥 auto hide (Symfony-like)
        new Thread(() -> {
            try {
                Thread.sleep(2500);
                javafx.application.Platform.runLater(() -> {
                    notificationLabel.setVisible(false);
                    notificationLabel.setManaged(false);
                });
            } catch (Exception e) {}
        }).start();
    }

    @FXML
    void goToAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CoursForm.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) coursContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void modifierCours(Cours c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CoursForm.fxml"));
            Parent root = loader.load();

            CoursForm controller = loader.getController();
            controller.setCours(c);

            Stage stage = (Stage) coursContainer.getScene().getWindow();
            stage.setScene(new Scene(root));

            showNotification("✏️ Cours modifié avec succès", "success");

        } catch (Exception e) {
            showNotification("Erreur modification", "error");
        }

    }
    void archiveCours(Cours c) {
        try {
            String sql = "UPDATE cours SET etat='ARCHIVE' WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, c.getId());
            ps.executeUpdate();

            loadData(); // يرجع للقا

            showNotification("📦 Cours archivé", "warning");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void restoreCours(Cours c) {
        try {
            String sql = "UPDATE cours SET etat='PUBLIE' WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, c.getId());
            ps.executeUpdate();

            refreshCoursList(); // ← manquait ici !
            showNotification("🔄 Cours restauré", "success");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void goToChapitres(Cours c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ChapitreList.fxml"));
            Parent root = loader.load();

            ChapitreList controller = loader.getController();
            controller.setCoursId(c.getId());

            Stage stage = (Stage) coursContainer.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
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
    void goStats(ActionEvent event) {
        loadPage(event, "/StatsView.fxml");
    }
    public void refreshCoursList() {
        coursContainer.getChildren().clear();
        loadData();
    }

}
