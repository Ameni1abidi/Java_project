package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.services.AIExamService;

public class AIController {

    @FXML
    private TextField courseField;

    @FXML
    private ComboBox<String> levelBox;

    @FXML
    private TextArea resultArea;

    private final AIExamService service = new AIExamService();

    @FXML
    public void initialize() {

        levelBox.getItems().addAll(
                "débutant",
                "intermédiaire",
                "avancé"
        );

        levelBox.setValue("intermédiaire");
    }

    @FXML
    private void handleGenerate() {

        String course = courseField.getText();
        String level = levelBox.getValue();

        if (course == null || course.isEmpty()) {
            showAlert("Erreur", "Cours obligatoire");
            return;
        }

        String result = service.generateExam(course, level);

        resultArea.setText(result);
    }

    private void showAlert(String title, String msg) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.show();
    }
}