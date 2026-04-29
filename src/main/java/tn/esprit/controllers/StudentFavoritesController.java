package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.entities.resources;
import tn.esprit.services.CloudinaryStorageService;
import tn.esprit.services.OcrViewerPageService;
import tn.esprit.services.QrCodeService;
import tn.esprit.services.ResourceService;
import tn.esprit.services.SensitiveResourceAccessService;
import tn.esprit.utils.UserSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class StudentFavoritesController {

    @FXML
    private FlowPane cardsContainer;
    @FXML
    private Label infoLabel;

    private final ResourceService resourceService = new ResourceService();
    private final QrCodeService qrCodeService = new QrCodeService();
    private final SensitiveResourceAccessService sensitiveAccessService = new SensitiveResourceAccessService();
    private final CloudinaryStorageService cloudinaryStorageService = new CloudinaryStorageService();
    private final OcrViewerPageService ocrViewerPageService = new OcrViewerPageService(cloudinaryStorageService);
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
        if (isMultimedia(resource)) {
            card.getChildren().addAll(topRow, buildProtectedMediaPreview(resource));
        } else {
            card.getChildren().addAll(topRow, contenu);
        }
        card.getChildren().add(actions);
        return card;
    }

    private StackPane buildProtectedMediaPreview(resources resource) {
        StackPane preview = new StackPane();
        preview.setPrefSize(334, 210);
        preview.setMinSize(334, 210);
        preview.setMaxSize(334, 210);
        preview.setAlignment(Pos.CENTER);
        preview.setStyle("-fx-background-color:#e5e7eb; -fx-background-radius:12;");

        if ("image".equalsIgnoreCase(resource.getType())) {
            Image image = buildImage(resource.getContenu());
            if (image != null) {
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(334);
                imageView.setFitHeight(210);
                imageView.setPreserveRatio(false);

                ColorAdjust dim = new ColorAdjust();
                dim.setBrightness(-0.28);
                dim.setSaturation(-0.35);
                BoxBlur blur = new BoxBlur(8, 8, 3);
                dim.setInput(blur);
                imageView.setEffect(dim);
                preview.getChildren().add(imageView);
            } else {
                Label fallback = new Label("Image protegee");
                fallback.setStyle("-fx-text-fill:#475467; -fx-font-size:18; -fx-font-weight:bold;");
                preview.getChildren().add(fallback);
            }
        } else {
            Label videoPlaceholder = new Label("VIDEO");
            videoPlaceholder.setStyle("-fx-text-fill:#475467; -fx-font-size:34; -fx-font-weight:bold;");
            preview.getChildren().add(videoPlaceholder);
        }

        VBox overlay = buildCenteredQrOverlay(resource);
        preview.getChildren().add(overlay);
        StackPane.setAlignment(overlay, Pos.CENTER);
        return preview;
    }

    private VBox buildCenteredQrOverlay(resources resource) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));
        box.setMaxWidth(190);
        box.setStyle("-fx-background-color:rgba(255,255,255,0.92); -fx-background-radius:14;");

        String accessUrl = resolveAccessUrl(resource);
        if (accessUrl == null || accessUrl.isBlank()) {
            Label unavailable = new Label("QR indisponible");
            unavailable.setStyle("-fx-text-fill:#dc2626; -fx-font-weight:bold;");
            box.getChildren().add(unavailable);
            return box;
        }

        Image qrImage = qrCodeService.generateImage(accessUrl, 138);
        if (qrImage == null) {
            Label unavailable = new Label("QR indisponible");
            unavailable.setStyle("-fx-text-fill:#dc2626; -fx-font-weight:bold;");
            box.getChildren().add(unavailable);
            return box;
        }

        ImageView qrView = new ImageView(qrImage);
        qrView.setFitWidth(138);
        qrView.setFitHeight(138);
        qrView.setPreserveRatio(true);

        Label hint = new Label("Scanner OCR");
        hint.setStyle("-fx-text-fill:#334155; -fx-font-size:12; -fx-font-weight:bold;");
        box.getChildren().addAll(qrView, hint);
        return box;
    }

    private VBox buildQrBox(resources resource) {
        VBox box = new VBox(6);
        box.setStyle("-fx-background-color:#f8fafc; -fx-background-radius:12; -fx-padding:10;");

        Label label = new Label("QR code d'acces securise");
        label.setStyle("-fx-text-fill:#334155; -fx-font-weight:bold;");

        User currentUser = UserSession.getCurrentUser();
        if (!sensitiveAccessService.canAccess(currentUser, resource)) {
            Label denied = new Label("QR code protege.");
            denied.setStyle("-fx-text-fill:#dc2626; -fx-font-weight:bold;");
            box.getChildren().addAll(label, denied);
            return box;
        }
        String accessUrl = resolveAccessUrl(resource);
        if (accessUrl == null || accessUrl.isBlank()) {
            Label unavailable = new Label("QR code indisponible pour cette ressource.");
            unavailable.setStyle("-fx-text-fill:#dc2626;");
            box.getChildren().addAll(label, unavailable);
            return box;
        }

        Image qrImage = qrCodeService.generateImage(accessUrl, 150);
        if (qrImage == null) {
            Label unavailable = new Label("QR code indisponible.");
            unavailable.setStyle("-fx-text-fill:#dc2626;");
            box.getChildren().addAll(label, unavailable);
            return box;
        }

        ImageView qrView = new ImageView(qrImage);
        qrView.setFitWidth(150);
        qrView.setFitHeight(150);
        qrView.setPreserveRatio(true);

        Label hint = new Label("Scan -> Cloudinary");
        hint.setStyle("-fx-text-fill:#64748b;");
        box.getChildren().addAll(label, qrView, hint);
        return box;
    }

    private boolean isMultimedia(resources resource) {
        return resource != null && ("image".equalsIgnoreCase(resource.getType()) || "video".equalsIgnoreCase(resource.getType()));
    }

    private boolean isRemoteUrl(String value) {
        return value != null && (value.startsWith("http://") || value.startsWith("https://"));
    }

    private Image buildImage(String contenu) {
        if (contenu == null || contenu.isBlank()) {
            return null;
        }
        try {
            if (isRemoteUrl(contenu)) {
                return new Image(contenu, true);
            }
            Path path = resolveLocalPath(contenu);
            if (path != null) {
                return new Image(path.toUri().toString(), true);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String resolveAccessUrl(resources resource) {
        String content = resource.getContenu();
        if ("image".equalsIgnoreCase(resource.getType())) {
            return resolveImageOcrViewerUrl(resource);
        }
        if (isRemoteUrl(content)) {
            return content;
        }
        if (content == null || content.isBlank()) {
            return null;
        }

        try {
            Path source = resolveLocalPath(content);
            if (source == null) {
                return null;
            }
            if (cloudinaryStorageService.isEnabled()) {
                String cloudinaryUrl = "image".equalsIgnoreCase(resource.getType())
                        ? cloudinaryStorageService.uploadImage(source)
                        : cloudinaryStorageService.uploadVideo(source);
                resource.setContenu(cloudinaryUrl);
                resourceService.update(resource);
                return cloudinaryUrl;
            }
            return source.toUri().toString();
        } catch (Exception e) {
            Path source = resolveLocalPath(content);
            return source == null ? null : source.toUri().toString();
        }
    }

    private String resolveImageOcrViewerUrl(resources resource) {
        String content = resource.getContenu();
        try {
            String imageUrl = content;
            if (!isRemoteUrl(imageUrl)) {
                Path source = resolveLocalPath(content);
                if (source == null) {
                    return null;
                }
                if (cloudinaryStorageService.isEnabled()) {
                    imageUrl = cloudinaryStorageService.uploadImage(source);
                    resource.setContenu(imageUrl);
                    resourceService.update(resource);
                } else {
                    imageUrl = source.toUri().toString();
                }
            }
            return ocrViewerPageService.createViewerUrl(resource.getTitre(), imageUrl);
        } catch (Exception e) {
            return isRemoteUrl(content) ? content : null;
        }
    }

    private Path resolveLocalPath(String raw) {
        try {
            Path p = Paths.get(raw).toAbsolutePath().normalize();
            if (Files.exists(p)) {
                return p;
            }
        } catch (Exception ignored) {
        }
        try {
            Path p = Paths.get("").toAbsolutePath().resolve(raw).normalize();
            if (Files.exists(p)) {
                return p;
            }
        } catch (Exception ignored) {
        }
        try {
            Path file = Paths.get(raw).getFileName();
            if (file != null) {
                Path p = Paths.get("storage", "resources").toAbsolutePath().resolve(file.toString()).normalize();
                if (Files.exists(p)) {
                    return p;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
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
