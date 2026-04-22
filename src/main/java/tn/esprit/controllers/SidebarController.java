package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class SidebarController {

    private void load(ActionEvent event, String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goDashboard(ActionEvent e) { load(e, "/ProfDashboard.fxml"); }

    @FXML
    void goForum(ActionEvent e) { load(e, "/forum.fxml"); }

    @FXML
    void goCours(ActionEvent e) { load(e, "/CoursList.fxml"); }

    @FXML
    void goRessources(ActionEvent e) { load(e, "/listeRessources.fxml"); }

    @FXML
    void goCategories(ActionEvent e) { load(e, "/CategorieList.fxml"); }

    @FXML
    void goEvaluations(ActionEvent e) { load(e, "/EvaluationView.fxml"); }

    @FXML
    void goLogout(ActionEvent e) { load(e, "/Login.fxml"); }
}