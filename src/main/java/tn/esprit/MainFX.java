package tn.esprit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterCours.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setTitle("Ajouter Cours");
        stage.setScene(scene);
        stage.show();
    }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/forum.fxml"));
        Scene scene = new Scene(loader.load(), 600, 500);

        stage.setTitle("EduFlex Forum");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}