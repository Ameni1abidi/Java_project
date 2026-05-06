package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import tn.esprit.entities.forum;
import tn.esprit.services.ForumService;
import tn.esprit.services.OllamaService;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.List;

public class AjoutForum {

    @FXML private FlowPane forumContainer;
    @FXML private VBox     formPane;
    @FXML private StackPane formOverlay;
    @FXML private TextField titreField;
    @FXML private TextField typeField;
    @FXML private TextArea  contenuField;
    @FXML private Label     pageLabel;

    private final ForumService  fs     = new ForumService();
    private final OllamaService ollama = new OllamaService();

    private int currentPage = 1;
    private final int pageSize = 3;
    private int totalPages;

    private static final String BTN_MODIFIER =
            "-fx-background-color:#d1fae5; -fx-text-fill:#065f46;" +
                    "-fx-background-radius:15; -fx-font-size:12px;" +
                    "-fx-border-color:#6ee7b7; -fx-border-radius:15; -fx-border-width:1;";

    private static final String BTN_SUPPRIMER =
            "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;" +
                    "-fx-background-radius:15; -fx-font-size:12px;" +
                    "-fx-border-color:#fca5a5; -fx-border-radius:15; -fx-border-width:1;";

    private static final String BTN_IA =
            "-fx-background-color:#ede9fe; -fx-text-fill:#5b21b6;" +
                    "-fx-background-radius:15; -fx-font-size:12px;" +
                    "-fx-border-color:#c4b5fd; -fx-border-radius:15; -fx-border-width:1;";

    private static final String BTN_IA_ACTIVE =
            "-fx-background-color:#7c3aed; -fx-text-fill:white;" +
                    "-fx-background-radius:15; -fx-font-size:12px;";

    @FXML
    public void initialize() { loadForums(); }

    @FXML public void showCreateForm() { formOverlay.setVisible(true);  }
    @FXML public void showList()       { formOverlay.setVisible(false); }

    @FXML
    public void ajouterForum() {
        forum f = new forum(
                0,
                titreField.getText(),
                contenuField.getText(),
                typeField.getText(),
                new Timestamp(System.currentTimeMillis())
        );
        String erreur = f.valider();
        if (erreur != null) {
            new Alert(Alert.AlertType.ERROR, erreur).showAndWait();
            return;
        }
        fs.ajouter(f);
        titreField.clear();
        contenuField.clear();
        typeField.clear();
        showList();
        loadForums();
    }

    @FXML
    private void exporterExcel() {
        System.out.println("=== exporterExcel() appelé ===");
        List<forum> forums = fs.getAll();

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Forums");

            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(
                    org.apache.poi.ss.usermodel.IndexedColors.VIOLET.getIndex());
            headerStyle.setFillPattern(
                    org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(
                    org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);

            org.apache.poi.ss.usermodel.CellStyle evenStyle = workbook.createCellStyle();
            evenStyle.setFillForegroundColor(
                    org.apache.poi.ss.usermodel.IndexedColors.LAVENDER.getIndex());
            evenStyle.setFillPattern(
                    org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            evenStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            evenStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            evenStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            evenStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            evenStyle.setWrapText(true);

            org.apache.poi.ss.usermodel.CellStyle oddStyle = workbook.createCellStyle();
            oddStyle.setFillForegroundColor(
                    org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
            oddStyle.setFillPattern(
                    org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            oddStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            oddStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            oddStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            oddStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            oddStyle.setWrapText(true);

            String[] colonnes = {"ID", "Titre", "Type", "Contenu", "Date de creation"};
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
            header.setHeight((short) 500);
            for (int i = 0; i < colonnes.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = header.createCell(i);
                cell.setCellValue(colonnes[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (forum f : forums) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIndex);
                row.setHeight((short) 600);
                org.apache.poi.ss.usermodel.CellStyle style =
                        (rowIndex % 2 == 0) ? evenStyle : oddStyle;

                org.apache.poi.ss.usermodel.Cell c0 = row.createCell(0);
                c0.setCellValue(f.getId()); c0.setCellStyle(style);

                org.apache.poi.ss.usermodel.Cell c1 = row.createCell(1);
                c1.setCellValue(f.getTitre()); c1.setCellStyle(style);

                org.apache.poi.ss.usermodel.Cell c2 = row.createCell(2);
                c2.setCellValue(f.getType()); c2.setCellStyle(style);

                org.apache.poi.ss.usermodel.Cell c3 = row.createCell(3);
                c3.setCellValue(f.getContenu()); c3.setCellStyle(style);

                org.apache.poi.ss.usermodel.Cell c4 = row.createCell(4);
                c4.setCellValue(f.getDateCreation() != null
                        ? f.getDateCreation().toString() : "");
                c4.setCellStyle(style);

                rowIndex++;
            }

            for (int i = 0; i < colonnes.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1024);
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le fichier Excel");
            fileChooser.setInitialFileName("forums_export.xlsx");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichier Excel (*.xlsx)", "*.xlsx"));

            Stage owner = (Stage) forumContainer.getScene().getWindow();
            owner.toFront();
            File fichier = fileChooser.showSaveDialog(owner);

            if (fichier != null) {
                try (FileOutputStream out = new FileOutputStream(fichier)) {
                    workbook.write(out);
                    new Alert(Alert.AlertType.INFORMATION,
                            "Export reussi !\n" + fichier.getAbsolutePath()).showAndWait();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Erreur lors de l'export : " + e.getMessage()).showAndWait();
        }
    }

    private void loadForums() {
        forumContainer.getChildren().clear();

        int totalItems = fs.countForums();
        totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages == 0) totalPages = 1;

        fs.getPaginated(currentPage, pageSize).forEach(f -> {
            try {
                // ── Card ──────────────────────────────────────────────────
                VBox card = new VBox(10);
                card.setPrefWidth(260);
                card.setMaxWidth(260);
                card.setStyle(
                        "-fx-background-color:white;" +
                                "-fx-padding:15;" +
                                "-fx-background-radius:12;" +
                                "-fx-border-color:#ebebeb;" +
                                "-fx-border-radius:12;" +
                                "-fx-border-width:1;" +
                                "-fx-effect: dropshadow(gaussian,#e0e0e0,6,0,0,2);");

                // ── Titre ─────────────────────────────────────────────────
                Label titre = new Label(f.getTitre());
                titre.setStyle(
                        "-fx-font-size:17px; -fx-font-weight:bold;" +
                                "-fx-text-fill:#4c1d95;");

                // ── Info ──────────────────────────────────────────────────
                Label info = new Label("Type: " + f.getType() + " | " + f.getDateCreation());
                info.setStyle("-fx-text-fill:#a78bfa; -fx-font-size:11px;");

                // ── Contenu ───────────────────────────────────────────────
                Label contenu = new Label(f.getContenu());
                contenu.setWrapText(true);
                contenu.setStyle("-fx-font-size:13px; -fx-text-fill:#374151;");

                // ── Boutons Modifier / Supprimer ──────────────────────────
                Button edit   = new Button("Modifier");
                Button delete = new Button("Supprimer");
                edit.setStyle(BTN_MODIFIER);
                delete.setStyle(BTN_SUPPRIMER);

                delete.setOnAction(e -> { fs.supprimer(f.getId()); loadForums(); });
                edit.setOnAction(e -> {
                    TextInputDialog dialog = new TextInputDialog(f.getContenu());
                    dialog.setTitle("Modifier forum");
                    dialog.showAndWait().ifPresent(newText -> {
                        f.setContenu(newText);
                        fs.modifier(f);
                        loadForums();
                    });
                });

                HBox actions = new HBox(10, edit, delete);

                // ── Bouton IA ─────────────────────────────────────────────
                Button iaBtn = new Button("Demander a l'IA");
                iaBtn.setStyle(BTN_IA);

                Label iaLoading = new Label("L'IA reflechit...");
                iaLoading.setVisible(false);
                iaLoading.setManaged(false);
                iaLoading.setStyle(
                        "-fx-font-size:12px; -fx-text-fill:#7c3aed;" +
                                "-fx-font-style:italic;");

                Label iaResponse = new Label();
                iaResponse.setWrapText(true);
                iaResponse.setVisible(false);
                iaResponse.setManaged(false);
                iaResponse.setStyle(
                        "-fx-background-color:#f5f3ff;" +
                                "-fx-border-color:#c4b5fd; -fx-border-width:1;" +
                                "-fx-border-radius:8; -fx-background-radius:8;" +
                                "-fx-padding:10; -fx-font-size:12px; -fx-text-fill:#3b0764;");

                iaBtn.setOnAction(e -> {
                    if (iaResponse.isVisible()) {
                        iaResponse.setVisible(false);
                        iaResponse.setManaged(false);
                        iaBtn.setText("Demander a l'IA");
                        iaBtn.setStyle(BTN_IA);
                        return;
                    }
                    iaBtn.setDisable(true);
                    iaBtn.setText("Reflexion...");
                    iaBtn.setStyle(BTN_IA_ACTIVE);
                    iaLoading.setVisible(true);
                    iaLoading.setManaged(true);

                    String question = f.getTitre() + " : " + f.getContenu();
                    new Thread(() -> {
                        String reponse = ollama.poserQuestion(question);
                        Platform.runLater(() -> {
                            iaLoading.setVisible(false);
                            iaLoading.setManaged(false);
                            iaBtn.setDisable(false);
                            iaBtn.setText("Masquer la reponse IA");
                            iaBtn.setStyle(BTN_IA_ACTIVE);
                            iaResponse.setText("IA : " + reponse);
                            iaResponse.setVisible(true);
                            iaResponse.setManaged(true);
                        });
                    }).start();
                });

                // ── Commentaires ──────────────────────────────────────────
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/commentaire.fxml"));
                Parent commentUI = loader.load();
                AjoutCommentaire cc = loader.getController();
                cc.setForumId(f.getId());

                // ── Ajout dans la card (SANS DOUBLONS) ────────────────────
                card.getChildren().addAll(
                        titre, info, contenu, actions,
                        iaBtn, iaLoading, iaResponse,
                        commentUI
                );

                forumContainer.getChildren().add(card);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        pageLabel.setText("Page " + currentPage + " / " + totalPages);
    }

    @FXML void nextPage(ActionEvent event) {
        if (currentPage < totalPages) { currentPage++; loadForums(); }
    }

    @FXML void previousPage(ActionEvent event) {
        if (currentPage > 1) { currentPage--; loadForums(); }
    }

    @FXML private void goDashboard(ActionEvent event)  { loadPage(event, "/ProfDashboard.fxml"); }
    @FXML private void goCours(ActionEvent event)      { loadPage(event, "/CoursList.fxml"); }
    @FXML private void goRessources(ActionEvent event) { loadPage(event, "/listeRessources.fxml"); }
    @FXML private void goCategories(ActionEvent event) { loadPage(event, "/CategorieList.fxml"); }
    @FXML private void goExamens(ActionEvent event)    { loadPage(event, "/ExamenView.fxml"); }
    @FXML private void goEvaluations(ActionEvent event){ loadPage(event, "/EvaluationView.fxml"); }

    @FXML private void goResultats(ActionEvent event) {
        new Alert(Alert.AlertType.INFORMATION,
                "La page resultats sera bientot disponible.").showAndWait();
    }

    @FXML private void goLogout(ActionEvent event) {
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
}