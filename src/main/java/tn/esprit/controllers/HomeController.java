package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;   // 🔥 IMPORTANT
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
public class HomeController {

    @FXML private TextField searchField;
    @FXML private ImageView imageView;

    @FXML
    public void initialize() {
        Rectangle clip = new Rectangle(1100, 500);
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        imageView.setClip(clip);

        System.out.println("Home page loaded !");
    }


    private void loadPage(ActionEvent event, String page) {
        try {
            var url = getClass().getResource("/" + page);
            System.out.println("URL = " + url);

            if (url == null) {
                System.out.println("❌ Fichier introuvable: " + page);
                return;
            }

            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 🔎 SEARCH
    @FXML
    private void handleSearch() {
        String text = searchField.getText();
        System.out.println("Search: " + text);
    }

    // 🚀 NAVIGATION
    @FXML
    private void goHome(ActionEvent event) {
        loadPage(event, "Home.fxml");
    }

    @FXML
    private void goServices(ActionEvent event) {
        loadPage(event, "GestionUsers.fxml"); // change selon ton besoin
    }

    @FXML
    private void goCourses(ActionEvent event) {
        loadPage(event, "CoursList.fxml");
    }

    @FXML
    private void goTeam(ActionEvent event) {
        loadPage(event, "forum.fxml");
    }

    @FXML
    private void goEvents(ActionEvent event) {
        loadPage(event, "EvaluationView.fxml");
    }

    @FXML
    private void goInscription(ActionEvent event) {
        loadPage(event, "Register.fxml");
    }

    @FXML
    private void goConnexion(ActionEvent event) {
        loadPage(event, "Login.fxml");
    }

    @FXML
    private void goLogin(ActionEvent event) {
        loadPage(event, "Login.fxml");
    }
    @FXML
    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}