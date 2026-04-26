package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class SidebarController {

    @FXML private Button btnDashboard;
    @FXML private Button btnForum;
    @FXML private Button btnCours;
    @FXML private Button btnRessources;
    @FXML private Button btnCategories;
    @FXML private Button btnExamens;
    @FXML private Button btnEvaluations;
    @FXML private Button btnLogout;

    @FXML
    public void initialize() {
        resetAll();
    }

    // ================= NAVIGATION =================

    @FXML
    void goDashboard(ActionEvent e) {
        load(e, "/ProfDashboard.fxml");
    }

    @FXML
    void goForum(ActionEvent e) {
        load(e, "/forum.fxml");
    }

    @FXML
    void goCours(ActionEvent e) {
        load(e, "/CoursList.fxml");
    }

    @FXML
    void goRessources(ActionEvent e) {
        load(e, "/listeRessources.fxml");
    }

    @FXML
    void goCategories(ActionEvent e) {
        load(e, "/CategorieList.fxml");
    }

    @FXML
    void goExamens(ActionEvent e) {
        load(e, "/ExamenView.fxml");
    }

    @FXML
    void goEvaluations(ActionEvent e) {
        load(e, "/EvaluationView.fxml");
    }

    @FXML
    void goLogout(ActionEvent e) {
        load(e, "/Login.fxml");
    }

    // ================= LOAD PAGE =================

    private void load(ActionEvent event, String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ================= STYLE ACTIVE =================

    public void setActive(String page) {
        resetAll();

        switch (page) {
            case "dashboard" -> active(btnDashboard);
            case "forum" -> active(btnForum);
            case "cours" -> active(btnCours);
            case "ressources" -> active(btnRessources);
            case "categories" -> active(btnCategories);
            case "examens" -> active(btnExamens);
            case "evaluations" -> active(btnEvaluations);
        }
    }

    private void active(Button btn) {
        btn.setStyle("""
            -fx-background-color: #b39ddb;
            -fx-text-fill: #333;
            -fx-alignment: CENTER_LEFT;
            -fx-padding: 8 12;
            -fx-background-radius: 8;
        """);
    }

    private void resetAll() {

        String base = """
            -fx-background-color: transparent;
            -fx-text-fill: #333;
            -fx-alignment: CENTER_LEFT;
            -fx-padding: 8 12;
            -fx-background-radius: 8;
        """;

        if (btnDashboard != null) btnDashboard.setStyle(base);
        if (btnForum != null) btnForum.setStyle(base);
        if (btnCours != null) btnCours.setStyle(base);
        if (btnRessources != null) btnRessources.setStyle(base);
        if (btnCategories != null) btnCategories.setStyle(base);
        if (btnExamens != null) btnExamens.setStyle(base);
        if (btnEvaluations != null) btnEvaluations.setStyle(base);
        if (btnLogout != null) btnLogout.setStyle(base);
    }
}