package tn.esprit.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;

public class MainFX extends Application {

    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/EvaluationView.fxml")
        );

        Scene scene = new Scene(loader.load());
        stage.setTitle("EduFlex - Résultats");
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
