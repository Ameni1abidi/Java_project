package tn.esprit.utils;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        //FXMLLoader loader = new FXMLLoader(getClass().getResource("/EtudiantDashboard.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionUsers.fxml"));
        Scene scene = new Scene(loader.load());
        PopupStyleInstaller.install();

        stage.setTitle("EDUFLEX");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
