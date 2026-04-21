package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.Chapitre;
import tn.esprit.services.ChapitreService;

import java.awt.*;
import java.io.File;
import java.util.List;

public class ChapitreList {

    @FXML
    private FlowPane chapitreContainer;

    @FXML
    private TextField searchField;

    @FXML
    private Label coursTitle;

    private final ChapitreService service = new ChapitreService();

    private int coursId;

    public void setCoursId(int id) {
        this.coursId = id;
        loadChapitres();

        if (coursTitle != null) {
            coursTitle.setText("Chapitres du cours: " + id);
        }
    }

    private void loadChapitres() {
        chapitreContainer.getChildren().clear();

        List<Chapitre> list = service.getAll(coursId);

        for (Chapitre ch : list) {
            chapitreContainer.getChildren().add(createCard(ch));
        }
    }
    @FXML
    void searchChapitre() {

        String keyword = searchField.getText();

        chapitreContainer.getChildren().clear();

        for (Chapitre ch : service.getAll(coursId)) {

            if (keyword == null || keyword.isEmpty()
                    || ch.getTitre().toLowerCase().contains(keyword.toLowerCase())) {

                chapitreContainer.getChildren().add(createCard(ch));
            }
        }
    }
    private VBox createCard(Chapitre chapitre) {

        VBox card = new VBox(10);
        card.setStyle("""
            -fx-background-color:white;
            -fx-padding:15;
            -fx-background-radius:15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 5);
        """);

        Label titre = new Label(chapitre.getTitre());
        titre.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");

        Label type = new Label("Type : " + chapitre.getTypeContenu());

        Label contenu = new Label();
        if (chapitre.getContenuTexte() != null && !chapitre.getContenuTexte().isEmpty()) {
            contenu.setText(chapitre.getContenuTexte());
            contenu.setWrapText(true);
        }

        Hyperlink fileLink = new Hyperlink();

        if (chapitre.getContenuFichier() != null && !chapitre.getContenuFichier().isEmpty()) {

            fileLink.setText("Ouvrir fichier");

            fileLink.setOnAction(e -> {
                try {
                    File file = new File(chapitre.getContenuFichier());

                    if (file.exists()) {
                        Desktop.getDesktop().open(file);
                    } else {
                        System.out.println("Fichier introuvable !");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

        } else {
            fileLink.setText("Aucun fichier");
            fileLink.setDisable(true);
        }

        Label info = new Label(
                "Ordre: " + chapitre.getOrdre() +
                        " | Durée: " + chapitre.getDureeEstimee() + " min"
        );
        Label durée = new Label(
                        " Durée: " + chapitre.getDureeEstimee() + " min"
        );
        info.setStyle("-fx-text-fill:#888; -fx-font-size:11px;");

        javafx.scene.control.Button edit = new javafx.scene.control.Button("Modifier");
        edit.setStyle("-fx-border-color:#007bff; -fx-text-fill:#007bff;");
        edit.setOnAction(e -> openForm(chapitre));

        javafx.scene.control.Button delete = new javafx.scene.control.Button("Supprimer");
        delete.setStyle("-fx-border-color:#dc3545; -fx-text-fill:#dc3545;");
        delete.setOnAction(e -> {
            service.supprimer(chapitre.getId());
            loadChapitres();
        });

        HBox actions = new HBox(10, edit, delete);

        card.getChildren().addAll(titre, type, contenu, fileLink, info, actions);

        return card;
    }
    @FXML
    void goToAdd() {
        openForm(null);
    }

    private void openForm(Chapitre ch) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ChapitreForm.fxml"));
            Parent root = loader.load();

            ChapitreForm controller = loader.getController();
            controller.initData(ch, coursId);

            Stage stage = (Stage) chapitreContainer.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CoursList.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) chapitreContainer.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
