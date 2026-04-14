package tn.esprit.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;

public class MainFX extends Application {

    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/listeRessources.fxml"));
        //FXMLLoader loader = new FXMLLoader(getClass().getResource("/CategorieList.fxml"));

        //FXMLLoader loader = new FXMLLoader(getClass().getResource("/CoursList.fxml"));
        //FXMLLoader loader = new FXMLLoader(getClass().getResource("/CategorieList.fxml"));
        //FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterCours.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("EduFlex - Ressources");
        stage.setScene(scene);
        stage.show();


        //FXMLLoader loader1 = new FXMLLoader(getClass().getResource("/forum.fxml"));
        //Scene scene2 = new Scene(loader1.load(), 600, 500);

        //stage.setTitle("EduFlex Forum");
        //stage.setScene(scene2);
        //stage.show();


        //FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/EvaluationView.fxml"));
        //FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/ExamenView.fxml"));
        //FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/home.fxml"));
        //Scene scene3 = new Scene(loader2.load());
        //scene3.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        //stage.setTitle("EduFlex - Résultats");
        //stage.setScene(scene3);
        //stage.show();


        /*FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionUsers.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("EduFlex — Connexion");
        stage.setScene(scene);
        stage.show();*/

    }

    public static void main(String[] args) {
        launch();
    }
}