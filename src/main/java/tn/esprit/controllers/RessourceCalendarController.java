package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.resources;
import tn.esprit.services.ResourceService;
import tn.esprit.utils.ResourceNavigationContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RessourceCalendarController {
    @FXML
    private VBox calendarContainer;

    private ResourceService resourceService;
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        try {
            resourceService = new ResourceService();
            loadCalendar();
        } catch (Exception e) {
            e.printStackTrace();
            showEmptyState("Impossible de charger le calendrier des ressources.");
        }
    }

    private void loadCalendar() {
        calendarContainer.getChildren().clear();

        List<resources> resourceList = resourceService.getAll().stream()
                .sorted(Comparator.comparing(this::sortDate).thenComparing(resources::getTitre, String.CASE_INSENSITIVE_ORDER))
                .toList();

        if (resourceList.isEmpty()) {
            showEmptyState("Aucune ressource ajoutee.");
            return;
        }

        Map<String, VBox> sections = new TreeMap<>(this::compareDateLabels);
        for (resources resource : resourceList) {
            String dateLabel = formatDateLabel(resource.getDisponibleLe());
            VBox section = sections.computeIfAbsent(dateLabel, this::createDateSection);
            section.getChildren().add(createEventCard(resource));
        }

        calendarContainer.getChildren().setAll(sections.values());
    }

    private void showEmptyState(String message) {
        calendarContainer.getChildren().clear();
        Label empty = new Label(message);
        empty.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14;");
        calendarContainer.getChildren().add(empty);
    }

    private VBox createDateSection(String dateLabel) {
        VBox section = new VBox(8);
        boolean today = isTodayLabel(dateLabel);
        boolean past = isPastLabel(dateLabel);
        String border = today ? "#7c3aed" : past ? "#cbd5e1" : "#c4b5fd";
        String background = today ? "#faf5ff" : past ? "#f8fafc" : "white";
        section.setStyle("-fx-background-color:" + background + "; -fx-background-radius: 12; -fx-border-color:" + border + "; -fx-border-width: 1.3; -fx-border-radius: 12; -fx-padding: 14;");

        Label title = new Label(buildDateTitle(dateLabel));
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill:" + (today ? "#6d28d9" : past ? "#64748b" : "#4c1d95") + ";");
        section.getChildren().add(title);
        return section;
    }

    private HBox createEventCard(resources resource) {
        HBox card = new HBox(12);
        card.setPadding(new Insets(10));
        boolean available = resourceService.isDisponible(resource);
        String accent = colorForType(resource.getType());
        String cardBackground = available ? "#f8fafc" : "#fff7ed";
        String border = available ? "#e9d5ff" : "#fed7aa";
        card.setStyle("-fx-background-color:" + cardBackground + "; -fx-background-radius: 10; -fx-border-color:" + border + "; -fx-border-radius: 10; -fx-border-width: 1.2;");

        Label type = new Label(safeType(resource.getType()));
        type.setMinWidth(70);
        type.setStyle("-fx-background-color:" + accent + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 9; -fx-background-radius: 8;");

        VBox details = new VBox(4);
        Label title = new Label(safe(resource.getTitre()));
        title.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label status = new Label(available ? "Disponible" : "Programme");
        status.setStyle(available
                ? "-fx-text-fill:#166534; -fx-font-size:12; -fx-font-weight:bold;"
                : "-fx-text-fill:#9a3412; -fx-font-size:12; -fx-font-weight:bold;");

        Label content = new Label(shortText(resource.getContenu()));
        content.setWrapText(true);
        content.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12;");

        details.getChildren().addAll(title, status, content);
        card.getChildren().addAll(type, details);
        return card;
    }

    private String colorForType(String type) {
        if (type == null) {
            return "#7c3aed";
        }
        return switch (type.toLowerCase()) {
            case "image" -> "#8b5cf6";
            case "video" -> "#ec4899";
            case "pdf" -> "#ef4444";
            case "lien" -> "#0ea5e9";
            default -> "#6366f1";
        };
    }

    private String buildDateTitle(String dateLabel) {
        if (isTodayLabel(dateLabel)) {
            return dateLabel + " - Aujourd'hui";
        }
        if (isPastLabel(dateLabel)) {
            return dateLabel + " - Passe";
        }
        return dateLabel;
    }

    private boolean isTodayLabel(String dateLabel) {
        LocalDate date = parseDisplayDate(dateLabel, null);
        return date != null && date.isEqual(LocalDate.now());
    }

    private boolean isPastLabel(String dateLabel) {
        LocalDate date = parseDisplayDate(dateLabel, null);
        return date != null && date.isBefore(LocalDate.now());
    }

    private LocalDate sortDate(resources resource) {
        return parseDate(resource.getDisponibleLe(), LocalDate.MAX);
    }

    private int compareDateLabels(String left, String right) {
        LocalDate leftDate = parseDisplayDate(left, LocalDate.MAX);
        LocalDate rightDate = parseDisplayDate(right, LocalDate.MAX);
        return leftDate.compareTo(rightDate);
    }

    private String formatDateLabel(String rawDate) {
        LocalDate date = parseDate(rawDate, null);
        return date == null ? "Sans date" : date.format(DISPLAY_DATE);
    }

    private LocalDate parseDate(String rawDate, LocalDate fallback) {
        if (rawDate == null || rawDate.isBlank()) {
            return fallback;
        }
        try {
            return LocalDate.parse(rawDate);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private LocalDate parseDisplayDate(String dateLabel, LocalDate fallback) {
        if ("Sans date".equals(dateLabel)) {
            return fallback;
        }
        try {
            return LocalDate.parse(dateLabel, DISPLAY_DATE);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }

    private String safeType(String value) {
        return value == null || value.isBlank() ? "TYPE" : value.toUpperCase();
    }

    private String shortText(String value) {
        String text = safe(value).replace("\n", " ").trim();
        return text.length() <= 95 ? text : text.substring(0, 92) + "...";
    }

    @FXML
    private void goDashboard(ActionEvent event) {
        loadPage(event, "/ProfDashboard.fxml");
    }

    @FXML
    private void goRessources(ActionEvent event) {
        loadPage(event, "/listeRessources.fxml");
    }

    @FXML
    private void goRessourceDashboard(ActionEvent event) {
        loadPage(event, "/RessourceDashboard.fxml");
    }

    @FXML
    private void goCategories(ActionEvent event) {
        loadPage(event, "/CategorieList.fxml");
    }

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
