package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.Chapitre;
import tn.esprit.services.ChapitreService;

import java.util.List;

public class ChapitreList {

    @FXML
    private FlowPane chapitreContainer;

    private ChapitreService service = new ChapitreService();

    private int coursId;

    public void setCoursId(int coursId) {
        this.coursId = coursId;
        loadChapitres();
    }

    @FXML
    private TextField searchField;
    @FXML
    void searchChapitre() {

        String keyword = searchField.getText().toLowerCase();

        chapitreContainer.getChildren().clear();

        List<Chapitre> list = service.getAll(coursId); // مهم 👈

        for (Chapitre ch : list) {

            if (ch.getTitre().toLowerCase().contains(keyword)) {
                chapitreContainer.getChildren().add(createCard(ch));
            }
        }
    }

    private void loadChapitres() {

        chapitreContainer.getChildren().clear();

        if (coursId == 0) {
            System.out.println("❌ coursId = 0");
            return;
        }

        List<Chapitre> list = service.getAll(coursId); // ✅ IMPORTANT

        System.out.println("SIZE = " + list.size()); // debug

        for (Chapitre ch : list) {
            chapitreContainer.getChildren().add(createCard(ch));
        }
    }

    private VBox createCard(Chapitre ch) {

        VBox card = new VBox(10);
        card.setStyle("-fx-background-color:white; -fx-padding:15; -fx-background-radius:15;");

        Label titre = new Label(ch.getTitre());
        titre.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");

        Label info = new Label("Ordre: " + ch.getOrdre());

        Button edit = new Button("Modifier");
        edit.setStyle("-fx-border-color:#007bff; -fx-text-fill:#007bff;");
        edit.setOnAction(e -> openForm(ch));

        Button delete = new Button("Supprimer");
        delete.setStyle("-fx-border-color:#dc3545; -fx-text-fill:#dc3545;");
        delete.setOnAction(e -> {
            service.supprimer(ch.getId());
            loadChapitres();
        });

// ✅ نحطهم في HBox باش يجو بحذا بعضهم
        HBox actions = new HBox(10);
        actions.getChildren().addAll(edit, delete);

// ✅ نزيدهم للـ card
        card.getChildren().addAll(titre, info, actions);

        return card;
    }

    // 🔥 BUTTON AJOUT
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
}
