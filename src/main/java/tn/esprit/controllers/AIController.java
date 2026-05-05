package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Examen;
import tn.esprit.services.AIExamService;

public class AIController {

    // ===================== FXML BINDINGS =====================

    @FXML private TextField courseField;
    @FXML private ComboBox<String> levelBox;
    @FXML private TextArea resultArea;
    @FXML private Button generateBtn;
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;
    @FXML private VBox questionsContainer;
    @FXML private Label examInfoLabel;
    @FXML private TabPane tabPane;

    private final AIExamService service = new AIExamService();
    private Examen currentExamen;

    // ===================== INIT =====================

    @FXML
    public void initialize() {

        levelBox.getItems().addAll("débutant", "intermédiaire", "avancé");
        levelBox.setValue("intermédiaire");

        progressBar.setVisible(false);
        statusLabel.setText("Prêt");

        resultArea.setPromptText("L'examen généré apparaîtra ici...");
    }

    // ===================== GENERATE =====================

    @FXML
    private void handleGenerate() {

        String course = courseField.getText().trim();
        String level = levelBox.getValue();

        if (course.isEmpty()) {
            showAlert("Champ requis", "Veuillez saisir le nom du cours.");
            return;
        }

        // UI: loading state
        generateBtn.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        statusLabel.setText("⏳ Génération en cours avec Ollama...");
        resultArea.setText("");
        examInfoLabel.setText("");

        // Background task
        Task<Examen> task = new Task<>() {
            @Override
            protected Examen call() {
                return service.generateExamEntity(course, level);
            }
        };

        task.setOnSucceeded(e -> {
            currentExamen = task.getValue();
            generateBtn.setDisable(false);
            progressBar.setVisible(false);
            statusLabel.setText("✅ Examen généré avec succès !");
        });

        task.setOnFailed(e -> {
            generateBtn.setDisable(false);
            progressBar.setVisible(false);
            statusLabel.setText("❌ Erreur lors de la génération.");
            resultArea.setText("Erreur: " + task.getException().getMessage());
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // ===================== DISPLAY =====================

    private void displayExam(String cours, String niveau, String contenuBrut) {

        // Affichage texte brut
        resultArea.setText(contenuBrut);

        // Info label
        examInfoLabel.setText(String.format(
                "📚 Cours: %s  |  🎯 Niveau: %s",
                cours, niveau
        ));
    }


    // ===================== CLEAR =====================

    @FXML
    private void handleClear() {
        courseField.clear();
        resultArea.clear();
        examInfoLabel.setText("");
        statusLabel.setText("Prêt");
        currentExamen = null;
        if (questionsContainer != null) questionsContainer.getChildren().clear();
    }

    // ===================== COPY =====================

    @FXML
    private void handleCopy() {
        if (resultArea.getText().isEmpty()) return;
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(resultArea.getText());
        clipboard.setContent(content);
        statusLabel.setText("📋 Copié dans le presse-papier !");
    }

    // ===================== ALERT =====================

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}