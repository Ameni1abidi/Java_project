package tn.esprit.utils;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) {

        try {
            System.out.println("STEP 1");

            var url = getClass().getResource("/EvaluationView.fxml");
            System.out.println("STEP 2");

            FXMLLoader loader = new FXMLLoader(url);
            System.out.println("STEP 3");

            Object root = loader.load(); // 👈 ça bloque ici
            System.out.println("STEP 4");

            Scene scene = new Scene((javafx.scene.Parent) root);

            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            System.out.println("❌ ERROR DETECTED");
            e.printStackTrace();
        }
    }}