package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.entities.Chapitre;
import tn.esprit.entities.Cours;
import tn.esprit.entities.User;
import tn.esprit.entities.resources;
import tn.esprit.services.ChapitreService;
import tn.esprit.services.CoursService;
import tn.esprit.services.ResourceService;
import tn.esprit.services.SensitiveResourceAccessService;
import tn.esprit.services.CloudinaryStorageService;
import tn.esprit.services.OcrService;
import tn.esprit.services.OcrViewerPageService;
import tn.esprit.services.CoursService;
import tn.esprit.services.QrCodeService;
import tn.esprit.services.RessourceDashboardService;
import tn.esprit.services.SensitiveResourceAccessService;
import tn.esprit.services.TranslationService;
import tn.esprit.services.UserService;
import tn.esprit.services.YouTubeLinkService;
import tn.esprit.utils.ResourceNavigationContext;
import tn.esprit.utils.UserSession;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudentChapterResourcesController {

    @FXML
    private HBox rootContainer;
    @FXML
    private VBox sidebarContainer;
    @FXML
    private VBox profileCard;
    @FXML
    private VBox mainContainer;
    @FXML
    private Label studentInitialsLabel;
    @FXML
    private Label studentNameLabel;
    @FXML
    private Label chapterTitleLabel;
    @FXML
    private Label chapterMetaLabel;
    @FXML
    private FlowPane cardsContainer;
    @FXML
    private Button favoritesOnlyButton;
    @FXML
    private Button themeToggleButton;

    private ResourceService resourceService;
    private ChapitreService chapitreService;
    private CoursService coursService;
    private UserService userService;
    private SensitiveResourceAccessService sensitiveAccessService;
    private YouTubeLinkService youTubeLinkService;
    private RessourceDashboardService ressourceDashboardService;
    private QrCodeService qrCodeService;
    private CloudinaryStorageService cloudinaryStorageService;
    private OcrService ocrService;
    private OcrViewerPageService ocrViewerPageService;
    private TranslationService translationService;

    private int chapitreId = -1;
    private int currentUserId = -1;
    private boolean favoritesOnly = false;
    private int favoriteCount = 0;
    private boolean darkMode = false;
    private User currentUser;

    @FXML
    public void initialize() {
        resourceService = new ResourceService();
        chapitreService = new ChapitreService();
        coursService = new CoursService();
        userService = new UserService();
        sensitiveAccessService = new SensitiveResourceAccessService();
        youTubeLinkService = new YouTubeLinkService();
        ressourceDashboardService = new RessourceDashboardService();
        qrCodeService = new QrCodeService();
        cloudinaryStorageService = new CloudinaryStorageService();
        ocrService = new OcrService();
        ocrViewerPageService = new OcrViewerPageService(cloudinaryStorageService);
        translationService = new TranslationService();

        setupStudentIdentity();
        loadContext();
        applyTheme();
        loadResources();
    }

    private void setupStudentIdentity() {
        User user = UserSession.getCurrentUser();
        if (user == null || user.getId() <= 0) {
            user = resolveFallbackStudent();
        }
        if (user == null) {
            studentInitialsLabel.setText("ET");
            studentNameLabel.setText("Etudiant");
            return;
        }

        currentUser = user;
        currentUserId = user.getId();
        String nom = user.getNom() == null || user.getNom().isBlank() ? "Etudiant" : user.getNom();
        studentNameLabel.setText(nom);
        studentInitialsLabel.setText(buildInitials(nom));
    }

    private User resolveFallbackStudent() {
        try {
            var student = userService.getFirstByRole(User.Role.ROLE_ETUDIANT);
            if (student.isPresent()) {
                UserSession.setCurrentUser(student.get());
                return student.get();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String buildInitials(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 0) {
            return "ET";
        }
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    private void loadContext() {
        Integer id = ResourceNavigationContext.getChapitreId();
        if (id == null || id <= 0) {
            chapterTitleLabel.setText("Ressources du chapitre");
            chapterMetaLabel.setText("Aucun chapitre selectionne.");
            return;
        }
        chapitreId = id;

        Chapitre chapitre = chapitreService.getById(chapitreId);
        if (chapitre == null) {
            chapterTitleLabel.setText("Ressources du chapitre");
            chapterMetaLabel.setText("Chapitre introuvable.");
            return;
        }

        Cours cours = coursService.getById(chapitre.getCoursId());
        String coursTitre = cours != null ? cours.getTitre() : "N/A";
        chapterTitleLabel.setText("Ressources du chapitre");
        chapterMetaLabel.setText("Cours: " + coursTitre + " | Chapitre: " + chapitre.getTitre());
    }

    private void loadResources() {
        cardsContainer.getChildren().clear();

        if (chapitreId <= 0) {
            cardsContainer.getChildren().add(new Label("Aucune ressource."));
            return;
        }

        List<resources> list = resourceService.getByChapitreId(chapitreId);
        for (resources r : list) {
            if (currentUserId > 0) {
                r.setFavori(resourceService.isFavorite(currentUserId, r.getId()));
            }
        }
        favoriteCount = (int) list.stream().filter(resources::isFavori).count();
        updateFavoritesButtonLabel();

        if (favoritesOnly) {
            list = list.stream().filter(resources::isFavori).toList();
        }

        if (list.isEmpty()) {
            Label empty = new Label(favoritesOnly
                    ? "Aucune ressource favorite pour ce chapitre."
                    : "Aucune ressource pour ce chapitre.");
            empty.setStyle(darkMode
                    ? "-fx-text-fill:#cbd5e1; -fx-font-size:14;"
                    : "-fx-text-fill:#667085; -fx-font-size:14;");
            cardsContainer.getChildren().add(empty);
            return;
        }

        for (resources r : list) {
            cardsContainer.getChildren().add(buildCard(r));
        }
    }

    private VBox buildCard(resources resource) {
        VBox card = new VBox(10);
        card.setPrefWidth(360);
        card.setPadding(new Insets(12));
        card.setStyle(darkMode
                ? "-fx-background-color:#1f2937; -fx-background-radius:14; -fx-border-color:#334155; -fx-border-radius:14;"
                : "-fx-background-color:#fffaff; -fx-background-radius:14; -fx-border-color:#dfd4f2; -fx-border-radius:14;");


        HBox topRow = new HBox(8);
        Label titre = new Label(safe(resource.getTitre()));
        titre.setStyle(darkMode
                ? "-fx-font-size:18; -fx-font-weight:bold; -fx-text-fill:#f8fafc;"
                : "-fx-font-size:18; -fx-font-weight:bold; -fx-text-fill:#1d2939;");
        Label typeBadge = new Label(safeType(resource.getType()));
        typeBadge.setStyle(darkMode
                ? "-fx-background-color:#374151; -fx-text-fill:#e5e7eb; -fx-padding:3 8; -fx-background-radius:8; -fx-font-weight:bold;"
                : "-fx-background-color:#f1eaff; -fx-text-fill:#4b3b78; -fx-padding:3 8; -fx-background-radius:8; -fx-font-weight:bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        topRow.getChildren().addAll(titre, spacer, typeBadge);

        HBox chips = new HBox(8);
        boolean disponible = resourceService.isDisponible(resource);
        Label dispoChip = new Label(disponible ? "Disponible" : "Disponible bientot");
        dispoChip.setStyle(disponible
                ? "-fx-background-color:#dcfce7; -fx-text-fill:#166534; -fx-padding:4 10; -fx-background-radius:10;"
                : "-fx-background-color:#fef3c7; -fx-text-fill:#92400e; -fx-padding:4 10; -fx-background-radius:10;");
        chips.getChildren().add(dispoChip);
        if (resource.isSensitive()) {
            Label sensitiveChip = new Label("Sensible");
            sensitiveChip.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#991b1b; -fx-padding:4 10; -fx-background-radius:10; -fx-font-weight:bold;");
            chips.getChildren().add(sensitiveChip);
        }

        if (!disponible) {
            VBox warningBox = new VBox();
            warningBox.setStyle("-fx-background-color:#fff7ed; -fx-border-color:#f59e0b; -fx-border-radius:10; -fx-background-radius:10; -fx-padding:10;");
            String when = formatDisplayDate(resource.getDisponibleLe());
            Label warning = new Label("Cette ressource sera disponible le " + when + ".");
            warning.setWrapText(true);
            warning.setStyle(darkMode
                    ? "-fx-text-fill:#fed7aa; -fx-font-size:13; -fx-font-weight:bold;"
                    : "-fx-text-fill:#9a3412; -fx-font-size:13; -fx-font-weight:bold;");
            warningBox.getChildren().add(warning);
            card.getChildren().addAll(topRow, chips, warningBox, buildActionButtons(resource, disponible));
            return card;
        }

        if ("image".equalsIgnoreCase(resource.getType())) {
            TextArea extractedTextArea = buildOcrTextArea();
            card.getChildren().addAll(topRow, chips, buildProtectedMediaPreview(resource), buildActionButtons(resource, true), buildOcrBox(resource, extractedTextArea));
        } else if ("video".equalsIgnoreCase(resource.getType())) {
            card.getChildren().addAll(topRow, chips, buildProtectedMediaPreview(resource), buildActionButtons(resource, true));
        } else if ("lien".equalsIgnoreCase(resource.getType())) {
            Button openLink = new Button("Ouvrir le lien");
            openLink.setStyle("-fx-background-color:#bbf7d0; -fx-text-fill:#166534; -fx-font-size:14; -fx-font-weight:bold; -fx-background-radius:10;");
            openLink.setOnAction(e -> openResource(resource));
            Label urlLabel = new Label(resource.isSensitive() ? "Lien protege" : safe(resource.getContenu()));
            urlLabel.setWrapText(true);
            urlLabel.setStyle(darkMode ? "-fx-text-fill:#cbd5e1;" : "-fx-text-fill:#667085;");
            card.getChildren().addAll(topRow, chips, openLink, urlLabel, buildActionButtons(resource, true));
        } else {
            Label content = new Label(safe(resource.getContenu()));
            content.setWrapText(true);
            content.setStyle(darkMode ? "-fx-text-fill:#cbd5e1;" : "-fx-text-fill:#667085;");
            card.getChildren().addAll(topRow, chips, content, buildActionButtons(resource, true));
        }

        return card;
    }

    private StackPane buildProtectedMediaPreview(resources resource) {
        StackPane preview = new StackPane();
        preview.setPrefSize(334, 210);
        preview.setMinSize(334, 210);
        preview.setMaxSize(334, 210);
        preview.setAlignment(Pos.CENTER);
        preview.setStyle(darkMode
                ? "-fx-background-color:#111827; -fx-background-radius:12;"
                : "-fx-background-color:#ede7f7; -fx-background-radius:12;");

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
                fallback.setStyle(darkMode
                        ? "-fx-text-fill:#cbd5e1; -fx-font-size:18; -fx-font-weight:bold;"
                        : "-fx-text-fill:#475467; -fx-font-size:18; -fx-font-weight:bold;");
                preview.getChildren().add(fallback);
            }
        } else {
            Label videoPlaceholder = new Label("VIDEO");
            videoPlaceholder.setStyle(darkMode
                    ? "-fx-text-fill:#e5e7eb; -fx-font-size:34; -fx-font-weight:bold;"
                    : "-fx-text-fill:#475467; -fx-font-size:34; -fx-font-weight:bold;");
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
        box.setStyle(darkMode
                ? "-fx-background-color:rgba(15,23,42,0.88); -fx-background-radius:14; -fx-border-color:#475569; -fx-border-radius:14;"
                : "-fx-background-color:rgba(255,250,255,0.94); -fx-background-radius:14; -fx-border-color:#dfd4f2; -fx-border-radius:14;");

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
        hint.setStyle(darkMode
                ? "-fx-text-fill:#e5e7eb; -fx-font-size:12; -fx-font-weight:bold;"
                : "-fx-text-fill:#2f2855; -fx-font-size:12; -fx-font-weight:bold;");
        box.getChildren().addAll(qrView, hint);
        return box;
    }

    private VBox buildQrBox(resources resource) {
        VBox box = new VBox(6);
        box.setStyle(darkMode
                ? "-fx-background-color:#111827; -fx-background-radius:12; -fx-padding:10;"
                : "-fx-background-color:#f8fafc; -fx-background-radius:12; -fx-padding:10;");

        Label label = new Label("QR code d'acces securise");
        label.setStyle(darkMode
                ? "-fx-text-fill:#e5e7eb; -fx-font-weight:bold;"
                : "-fx-text-fill:#334155; -fx-font-weight:bold;");

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
        hint.setStyle(darkMode ? "-fx-text-fill:#9ca3af;" : "-fx-text-fill:#64748b;");
        box.getChildren().addAll(label, qrView, hint);
        return box;
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

    private HBox buildActionButtons(resources resource, boolean disponible) {
        HBox actions = new HBox(10);
        boolean hasQrPreview = resource != null
                && ("image".equalsIgnoreCase(resource.getType()) || "video".equalsIgnoreCase(resource.getType()));

        if (!hasQrPreview) {
            Button open = new Button("Voir / Telecharger");
            open.setDisable(!disponible);
            open.setStyle(darkMode
                    ? "-fx-background-color:#1e3a8a; -fx-text-fill:#dbeafe; -fx-font-weight:bold; -fx-background-radius:10;"
                    : "-fx-background-color:#e9e2fb; -fx-text-fill:#493286; -fx-font-weight:bold; -fx-background-radius:10;");
            open.setOnAction(e -> openResource(resource));
            actions.getChildren().add(open);
        }

        Button favori = new Button(resource.isFavori() ? "Retire" : "Favori");
        favori.setStyle(resource.isFavori()
                ? (darkMode
                    ? "-fx-background-color:#14532d; -fx-text-fill:#dcfce7; -fx-font-weight:bold; -fx-background-radius:10;"
                    : "-fx-background-color:#e4f8ec; -fx-text-fill:#17643a; -fx-font-weight:bold; -fx-background-radius:10;")
                : (darkMode
                    ? "-fx-background-color:#334155; -fx-text-fill:#e2e8f0; -fx-font-weight:bold; -fx-background-radius:10;"
                    : "-fx-background-color:#eee8f8; -fx-text-fill:#4b3b78; -fx-font-weight:bold; -fx-background-radius:10;"));
        favori.setOnAction(e -> {
            toggleFavorite(resource);
            loadResources();
        });

        actions.getChildren().add(favori);
        return actions;
    }

    private VBox buildOcrBox(resources resource, TextArea extractedTextArea) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));
        box.setStyle(darkMode
                ? "-fx-background-color:#111827; -fx-background-radius:12; -fx-border-color:#334155; -fx-border-radius:12;"
                : "-fx-background-color:#fbf8ff; -fx-background-radius:12; -fx-border-color:#d9cdee; -fx-border-radius:12;");

        Label title = new Label("OCR intelligent");
        title.setStyle(darkMode
                ? "-fx-text-fill:#e5e7eb; -fx-font-weight:bold; -fx-font-size:14;"
                : "-fx-text-fill:#251a45; -fx-font-weight:bold; -fx-font-size:14;");

        Label status = new Label("Image -> texte extrait -> copier, traduire ou rechercher.");
        status.setWrapText(true);
        status.setStyle(darkMode ? "-fx-text-fill:#9ca3af;" : "-fx-text-fill:#756b8e;");

        Button scanButton = new Button("Scanner l'image");
        scanButton.setStyle("-fx-background-color:#6f5cc2; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:10;");
        scanButton.setOnAction(e -> runOcr(resource, extractedTextArea, status, scanButton));

        Button copyButton = new Button("Copy");
        copyButton.setStyle("-fx-background-color:#e4f8ec; -fx-text-fill:#17643a; -fx-font-weight:bold; -fx-background-radius:10;");
        copyButton.setOnAction(e -> copyExtractedText(extractedTextArea, status));

        ComboBox<String> languageBox = new ComboBox<>();
        languageBox.getItems().addAll("Francais", "Arabe", "Anglais");
        languageBox.setValue("Francais");
        languageBox.setPrefWidth(112);

        Button translateButton = new Button("Traduire");
        translateButton.setStyle("-fx-background-color:#fff0c9; -fx-text-fill:#855b00; -fx-font-weight:bold; -fx-background-radius:10;");
        translateButton.setOnAction(e -> translateExtractedText(extractedTextArea, languageBox, status, translateButton));

        Button searchButton = new Button("Rechercher");
        searchButton.setStyle("-fx-background-color:#e9e2fb; -fx-text-fill:#493286; -fx-font-weight:bold; -fx-background-radius:10;");
        searchButton.setOnAction(e -> searchExtractedText(extractedTextArea, status));

        HBox firstRow = new HBox(8, scanButton, copyButton);
        firstRow.setAlignment(Pos.CENTER_LEFT);
        HBox secondRow = new HBox(8, languageBox, translateButton, searchButton);
        secondRow.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().addAll(title, status, extractedTextArea, firstRow, secondRow);
        return box;
    }

    private TextArea buildOcrTextArea() {
        TextArea area = new TextArea();
        area.setPromptText("Le texte extrait de l'image apparait ici.");
        area.setWrapText(true);
        area.setPrefRowCount(5);
        area.setMinHeight(96);
        area.setVisible(false);
        area.setManaged(false);
        area.setStyle(darkMode
                ? "-fx-control-inner-background:#0f172a; -fx-text-fill:#e5e7eb; -fx-prompt-text-fill:#94a3b8;"
                : "-fx-control-inner-background:#fffaff; -fx-text-fill:#251a45; -fx-prompt-text-fill:#9b8cb8;");
        return area;
    }

    private void runOcr(resources resource, TextArea target, Label status, Button scanButton) {
        if (!sensitiveAccessService.canAccess(currentUser, resource)) {
            showError("Acces refuse a cette ressource sensible.");
            return;
        }

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                String content = resource.getContenu();
                if (isRemoteUrl(content)) {
                    return ocrService.extractTextFromUrl(content);
                }
                Path source = resolveLocalPath(content);
                if (source == null) {
                    throw new IOException("Image introuvable.");
                }
                if (cloudinaryStorageService.isEnabled()) {
                    String cloudinaryUrl = cloudinaryStorageService.uploadImage(source);
                    resource.setContenu(cloudinaryUrl);
                    resourceService.update(resource);
                    return ocrService.extractTextFromUrl(cloudinaryUrl);
                }
                return ocrService.extractTextFromFile(source);
            }
        };

        scanButton.setDisable(true);
        status.setText("Analyse OCR en cours...");
        task.setOnSucceeded(e -> {
            String text = task.getValue();
            target.setText(text == null || text.isBlank() ? "Aucun texte detecte dans cette image." : text);
            target.setVisible(true);
            target.setManaged(true);
            status.setText(text == null || text.isBlank() ? "OCR termine: aucun texte detecte." : "OCR termine: texte extrait.");
            scanButton.setDisable(false);
        });
        task.setOnFailed(e -> {
            status.setText("OCR indisponible: " + task.getException().getMessage());
            scanButton.setDisable(false);
        });
        Thread thread = new Thread(task, "resource-ocr-" + resource.getId());
        thread.setDaemon(true);
        thread.start();
    }

    private void copyExtractedText(TextArea area, Label status) {
        String text = area.getText();
        if (text == null || text.isBlank()) {
            status.setText("Aucun texte a copier.");
            return;
        }
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
        status.setText("Texte copie.");
    }

    private void translateExtractedText(TextArea area, ComboBox<String> languageBox, Label status, Button translateButton) {
        String text = area.getText();
        if (text == null || text.isBlank()) {
            status.setText("Aucun texte a traduire.");
            return;
        }

        String targetLang = switch (languageBox.getValue()) {
            case "Arabe" -> "ar";
            case "Anglais" -> "en";
            default -> "fr";
        };

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return translationService.traduire(text, targetLang);
            }
        };

        translateButton.setDisable(true);
        status.setText("Traduction en cours...");
        task.setOnSucceeded(e -> {
            area.setText(task.getValue());
            status.setText("Traduction terminee.");
            translateButton.setDisable(false);
        });
        task.setOnFailed(e -> {
            status.setText("Traduction indisponible: " + task.getException().getMessage());
            translateButton.setDisable(false);
        });
        Thread thread = new Thread(task, "resource-translation-" + targetLang);
        thread.setDaemon(true);
        thread.start();
    }

    private void searchExtractedText(TextArea area, Label status) {
        String text = area.getText();
        if (text == null || text.isBlank()) {
            status.setText("Aucun texte a rechercher.");
            return;
        }
        try {
            String query = text.length() > 220 ? text.substring(0, 220) : text;
            URI uri = URI.create("https://www.google.com/search?q=" + java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8));
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(uri);
                status.setText("Recherche ouverte.");
            } else {
                status.setText("Navigateur indisponible sur cet appareil.");
            }
        } catch (Exception e) {
            status.setText("Recherche impossible: " + e.getMessage());
        }
    }

    @FXML
    private void toggleFavoritesOnly() {
        favoritesOnly = !favoritesOnly;
        updateFavoritesButtonLabel();
        loadResources();
    }

    @FXML
    private void toggleTheme() {
        darkMode = !darkMode;
        applyTheme();
        loadResources();
    }

    private void toggleFavorite(resources resource) {
        if (currentUserId <= 0) {
            showError("Session utilisateur invalide.");
            return;
        }
        if (resource == null || resource.getId() <= 0) {
            showError("Ressource invalide.");
            return;
        }

        boolean newFavoriteState = !resource.isFavori();
        if (!resourceService.setFavorite(currentUserId, resource.getId(), newFavoriteState)) {
            showError("Impossible de mettre a jour les favoris.");
            return;
        }
        resource.setFavori(newFavoriteState);
        favoriteCount += newFavoriteState ? 1 : -1;
        if (favoriteCount < 0) {
            favoriteCount = 0;
        }
        updateFavoritesButtonLabel();
    }

    private void updateFavoritesButtonLabel() {
        if (favoritesOnlyButton == null) {
            return;
        }
        favoritesOnlyButton.setText(
                favoritesOnly
                        ? "Favoris (" + favoriteCount + ") - ON"
                        : "Favoris (" + favoriteCount + ")"
        );
        favoritesOnlyButton.setStyle(favoritesOnly
                ? (darkMode
                    ? "-fx-background-color:#14532d; -fx-text-fill:#dcfce7; -fx-font-size:13; -fx-font-weight:bold; -fx-background-radius:999; -fx-padding:8 14;"
                    : "-fx-background-color:#dcfce7; -fx-text-fill:#166534; -fx-font-size:13; -fx-font-weight:bold; -fx-background-radius:999; -fx-padding:8 14;")
                : (darkMode
                    ? "-fx-background-color:#1f2937; -fx-text-fill:#c7d2fe; -fx-font-size:13; -fx-font-weight:bold; -fx-background-radius:999; -fx-padding:8 14;"
                    : "-fx-background-color:#eef2ff; -fx-text-fill:#3730a3; -fx-font-size:13; -fx-font-weight:bold; -fx-background-radius:999; -fx-padding:8 14;"));
    }

    private void applyTheme() {
        if (rootContainer == null) {
            return;
        }

        if (darkMode) {
            rootContainer.setStyle("-fx-background-color:#0f172a;");
            sidebarContainer.setStyle("-fx-background-color:#1e293b; -fx-padding:16; -fx-spacing:12;");
            profileCard.setStyle("-fx-background-color:#334155; -fx-background-radius:18; -fx-padding:14;");
            mainContainer.setStyle("-fx-padding:16;");
            chapterTitleLabel.setStyle("-fx-font-size:32; -fx-font-weight:bold; -fx-text-fill:#f8fafc;");
            chapterMetaLabel.setStyle("-fx-font-size:15; -fx-text-fill:#cbd5e1;");
            studentInitialsLabel.setStyle("-fx-background-color:#64748b; -fx-text-fill:white; -fx-font-size:28; -fx-font-weight:bold; -fx-background-radius:18; -fx-padding:14 20;");
            studentNameLabel.setStyle("-fx-font-size:20; -fx-font-weight:bold; -fx-text-fill:#f8fafc;");
            themeToggleButton.setText("Mode clair");
            themeToggleButton.setStyle("-fx-background-color:#e5e7eb; -fx-text-fill:#111827; -fx-font-size:13; -fx-font-weight:bold; -fx-background-radius:999; -fx-padding:8 14;");
        } else {
            rootContainer.setStyle("-fx-background-color:#eef1ff;");
            sidebarContainer.setStyle("-fx-background-color:#b7a5da; -fx-padding:16; -fx-spacing:12;");
            profileCard.setStyle("-fx-background-color:#c6b6e4; -fx-background-radius:18; -fx-padding:14;");
            mainContainer.setStyle("-fx-padding:16;");
            chapterTitleLabel.setStyle("-fx-font-size:32; -fx-font-weight:bold; -fx-text-fill:#1f2937;");
            chapterMetaLabel.setStyle("-fx-font-size:15; -fx-text-fill:#475467;");
            studentInitialsLabel.setStyle("-fx-background-color:#dbd2ee; -fx-text-fill:white; -fx-font-size:28; -fx-font-weight:bold; -fx-background-radius:18; -fx-padding:14 20;");
            studentNameLabel.setStyle("-fx-font-size:20; -fx-font-weight:bold; -fx-text-fill:white;");
            themeToggleButton.setText("Mode sombre");
            themeToggleButton.setStyle("-fx-background-color:#111827; -fx-text-fill:#f9fafb; -fx-font-size:13; -fx-font-weight:bold; -fx-background-radius:999; -fx-padding:8 14;");
        }

        updateFavoritesButtonLabel();
    }

    private Image buildImage(String contenu) {
        if (contenu == null || contenu.isBlank()) {
            return null;
        }
        try {
            if (contenu.startsWith("http://") || contenu.startsWith("https://")) {
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

    private void openResource(resources resource) {
        if (resource == null) {
            showError("Ressource invalide.");
            return;
        }
        if (!sensitiveAccessService.canAccess(currentUser, resource)) {
            sensitiveAccessService.logAccess(currentUser, resource, false, "ACCESS_DENIED");
            showError("Acces refuse a cette ressource sensible.");
            return;
        }

        String accessToken = sensitiveAccessService.issueToken(resource, currentUser);
        if (resource.isSensitive() && !sensitiveAccessService.validateToken(resource, currentUser, accessToken)) {
            sensitiveAccessService.logAccess(currentUser, resource, false, "TOKEN_INVALID");
            showError("Jeton d'acces securise invalide.");
            return;
        }

        String contenu = resource.getContenu();
        if (contenu == null || contenu.isBlank()) {
            showError("Ressource invalide.");
            return;
        }

        try {
            ressourceDashboardService.recordView(resource.getId(), currentUserId);
            if (contenu.startsWith("http://") || contenu.startsWith("https://")) {
                if (youTubeLinkService.isYoutubeUrl(contenu)) {
                    contenu = youTubeLinkService.normalizeForOpen(contenu);
                }
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(URI.create(contenu));
                }
                sensitiveAccessService.logAccess(currentUser, resource, true, "REMOTE_OPEN");
                return;
            }

            Path source = resolveLocalPath(contenu);
            if (source == null) {
                showError("Fichier introuvable.");
                return;
            }

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Telecharger la ressource");
            chooser.setInitialFileName(source.getFileName().toString());
            File target = chooser.showSaveDialog(cardsContainer.getScene().getWindow());
            if (target == null) {
                return;
            }
            Files.copy(source, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            sensitiveAccessService.logAccess(currentUser, resource, true, "LOCAL_COPY");
        } catch (Exception e) {
            sensitiveAccessService.logAccess(currentUser, resource, false, "ERROR:" + e.getClass().getSimpleName());
            showError("Erreur d'ouverture: " + e.getMessage());
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

    private String safe(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }

    private boolean isRemoteUrl(String value) {
        return value != null && (value.startsWith("http://") || value.startsWith("https://"));
    }
    private String safeType(String type) {
        if (type == null || type.isBlank()) {
            return "TYPE";
        }
        return type.toUpperCase();
    }

    private String formatDisplayDate(String raw) {
        try {
            LocalDate parsed = LocalDate.parse(raw);
            return parsed.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return safe(raw);
        }
    }

    @FXML
    private void backToCourses() {
        try {
            ResourceNavigationContext.clear();
            Parent root = FXMLLoader.load(getClass().getResource("/StudentCours.fxml"));
            Stage stage = (Stage) cardsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goDashboard() {
        loadPage("/EtudiantDashboard.fxml");
    }

    @FXML
    private void goForum() {
        loadPage("/forum.fxml");
    }

    @FXML
    private void goMesCours() {
        loadPage("/StudentCours.fxml");
    }

    @FXML
    private void goExamens() {
        loadPage("/ExamenView.fxml");
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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
