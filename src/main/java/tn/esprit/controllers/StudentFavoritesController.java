package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.entities.resources;
import tn.esprit.services.ResourceService;
import tn.esprit.utils.UserSession;

import java.io.IOException;
import java.util.List;

public class StudentFavoritesController {

    @FXML
    private FlowPane cardsContainer;
    @FXML
    private Label infoLabel;

    private final ResourceService resourceService = new ResourceService();
    private int currentUserId = -1;

    @FXML
    public void initialize() {
        User user = UserSession.getCurrentUser();
        if (user != null && user.getId() > 0) {
            currentUserId = user.getId();
        }
        loadFavorites();
    }

    private void loadFavorites() {
        cardsContainer.getChildren().clear();

        if (currentUserId <= 0) {
            infoLabel.setText("Session invalide. Veuillez vous reconnecter.");
            return;
        }

        List<resources> favorites = resourceService.getFavoritesByUserId(currentUserId);
        infoLabel.setText(favorites.size() + " ressource(s) favorite(s).");

        if (favorites.isEmpty()) {
            Label empty = new Label("Vous n'avez aucune ressource favorite.");
            empty.setStyle("-fx-text-fill:#667085; -fx-font-size:14;");
            cardsContainer.getChildren().add(empty);
            return;
        }

        for (resources r : favorites) {
            cardsContainer.getChildren().add(buildCard(r));
        }
    }

    private VBox buildCard(resources resource) {
        VBox card = new VBox(10);
        card.setPrefWidth(360);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color:white; -fx-background-radius:14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08),8,0,0,3);");

        HBox topRow = new HBox(8);
        Label titre = new Label(safe(resource.getTitre()));
        titre.setStyle("-fx-font-size:18; -fx-font-weight:bold; -fx-text-fill:#1d2939;");
        Label typeBadge = new Label(safeType(resource.getType()));
        typeBadge.setStyle("-fx-background-color:#eef2ff; -fx-text-fill:#3f3f46; -fx-padding:3 8; -fx-background-radius:8; -fx-font-weight:bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        topRow.getChildren().addAll(titre, spacer, typeBadge);

        Label contenu = new Label(safe(resource.getContenu()));
        contenu.setWrapText(true);
        contenu.setStyle("-fx-text-fill:#667085;");

        HBox actions = new HBox(10);
        Button openBtn = new Button("Ouvrir");
        openBtn.setStyle("-fx-background-color:#e0f2fe; -fx-text-fill:#075985; -fx-font-weight:bold; -fx-background-radius:10;");
        openBtn.setOnAction(e -> openResource(resource));

        Button removeBtn = new Button("Retirer des favoris");
        removeBtn.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#991b1b; -fx-font-weight:bold; -fx-background-radius:10;");
        removeBtn.setOnAction(e -> {
            removeFavorite(resource);
            loadFavorites();
        });

        actions.getChildren().addAll(openBtn, removeBtn);
        card.getChildren().addAll(topRow, contenu, actions);
        return card;
    }

    private void openResource(resources resource) {
        try {
            String contenu = resource.getContenu();
            if (contenu != null && (contenu.startsWith("http://") || contenu.startsWith("https://"))) {
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(contenu));
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Ressource");
                alert.setHeaderText(resource.getTitre());
                alert.setContentText("Contenu : " + contenu);
                alert.showAndWait();
            }
        } catch (Exception ex) {
            showError("Impossible d'ouvrir la ressource : " + ex.getMessage());
        }
    }

    private void removeFavorite(resources resource) {
        if (currentUserId <= 0) {
            return;
        }
        resourceService.setFavorite(currentUserId, resource.getId(), false);
    }

    @FXML
    private void goDashboard() {
        loadPage("/EtudiantDashboard.fxml");
    }

    private void loadPage(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }

    private String safeType(String type) {
        if (type == null || type.isBlank()) {
            return "TYPE";
        }
        return type.toUpperCase();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
