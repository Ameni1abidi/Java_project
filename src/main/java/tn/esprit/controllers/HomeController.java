package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

public class HomeController {

    @FXML
    private TextField searchField;

    @FXML
    private Button btnHome;

    @FXML
    private Button btnServices;

    @FXML
    private Button btnCourses;

    @FXML
    private Button btnTeam;

    @FXML
    private Button btnEvents;

    @FXML
    private Button btnInscription;

    @FXML
    private Button btnConnexion;

    @FXML
    private Button btnLogin;

    @FXML
    private ImageView imageView;

    @FXML
    public void initialize() {

        Rectangle clip = new Rectangle(1100, 500); // même taille que FXML
        clip.setArcWidth(30);
        clip.setArcHeight(30);

        imageView.setClip(clip);

        System.out.println("Home page loaded !");
    }

    @FXML
    private void handleSearch() {
        String text = searchField.getText();
        System.out.println("Search: " + text);
    }

    @FXML
    private void goHome() { System.out.println("Home clicked"); }

    @FXML
    private void goServices() { System.out.println("Services clicked"); }

    @FXML
    private void goCourses() { System.out.println("Courses clicked"); }

    @FXML
    private void goTeam() { System.out.println("Team clicked"); }

    @FXML
    private void goEvents() { System.out.println("Events clicked"); }

    @FXML
    private void goInscription() { System.out.println("Inscription clicked"); }

    @FXML
    private void goConnexion() { System.out.println("Connexion clicked"); }

    @FXML
    private void goLogin() { System.out.println("Login clicked"); }
}