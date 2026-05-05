package tn.esprit.controllers;

import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.Cours;
import tn.esprit.services.CoursService;

import javafx.event.ActionEvent;
import tn.esprit.services.EmailService;
import tn.esprit.services.UserService;
import tn.esprit.utils.FlashSession;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class CoursForm {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private TextField niveauField;
    @FXML private DatePicker dateField;
    @FXML
    private ComboBox<String> badgeCombo;

    @FXML private Label titreError;
    @FXML private Label descriptionError;
    @FXML private Label niveauError;
    @FXML private Label dateError;
    @FXML private Label badgeError;


    private CoursService service = new CoursService();
    private Cours currentCours;

    public void setCours(Cours c) {
        this.currentCours = c;

        if (c != null) {
            titreField.setText(c.getTitre());
            descriptionField.setText(c.getDescription());
            niveauField.setText(c.getNiveau());
            badgeCombo.setValue(c.getBadge());
            dateField.setValue(c.getDateCreation().toLocalDate());
        }
    }

    @FXML
    void saveCours() {

        // RESET ERRORS
        titreError.setText("");
        descriptionError.setText("");
        niveauError.setText("");
        dateError.setText("");
        badgeError.setText("");

        boolean valid = true;

        String titre = titreField.getText();
        String description = descriptionField.getText();
        String niveau = niveauField.getText();
        String badge = badgeCombo.getValue();
        var date = dateField.getValue();


        if (titre == null || titre.trim().isEmpty()) {
            titreError.setText("Titre obligatoire");
            valid = false;
        } else if (titre.length() < 3) {
            titreError.setText("Min 3 caractères");
            valid = false;
        }

        if (description == null || description.trim().isEmpty()) {
            descriptionError.setText("Description obligatoire");
            valid = false;
        } else if (description.length() < 10) {
            descriptionError.setText("Min 10 caractères");
            valid = false;
        }

        if (niveau == null || niveau.trim().isEmpty()) {
            niveauError.setText("Niveau obligatoire");
            valid = false;
        }

        if (date == null) {
            dateError.setText("Date obligatoire");
            valid = false;
        }


        if (badge == null || badge.isEmpty()) {
            badgeError.setText("Veuillez choisir un badge");
            valid = false;
        }

        if (!valid) return;

        Cours savedCours;

        if (currentCours == null) {

            savedCours = new Cours();

            savedCours.setTitre(titre);
            savedCours.setDescription(description);
            savedCours.setNiveau(niveau);
            savedCours.setBadge(badge);
            savedCours.setDateCreation(Date.valueOf(date));

            service.ajouter(savedCours);

        } else {

            currentCours.setTitre(titre);
            currentCours.setDescription(description);
            currentCours.setNiveau(niveau);
            currentCours.setBadge(badge);
            currentCours.setDateCreation(Date.valueOf(date));

            service.modifier(currentCours);

            savedCours = currentCours;
        }
        boolean isNew = (currentCours == null);
        // 📩 EMAIL REAL (MailHog)
        EmailService emailService = new EmailService();

        UserService userService = new UserService();

        List<String> students = new ArrayList<>();

        try {
            students = userService.getStudentEmails();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String message;

        String link = "file:///C:/EduFlex/login.exe";

        if (isNew) {
            message =
                    "<h3>Bonjour Assil,</h3>" +

                            "<p>Un nouveau cours vient d'être ajouté :</p>" +
                            "<b>" + savedCours.getTitre() + "</b><br><br>" +

                            "<p><b>Description :</b> " + savedCours.getDescription() + "</p>" +

                            "<a href='" + link + "' " +
                            "style='display:inline-block;padding:10px 15px;background:#4CAF50;color:white;text-decoration:none;border-radius:5px;'>"
                            + "📚 Voir le cours</a><br><br>" +

                            "<p>EduFlex</p>";

        } else {
            message =
                    "<h3>Bonjour Assil,</h3>" +

                            "<p>Le cours suivant a été modifié :</p>" +
                            "<b>" + savedCours.getTitre() + "</b><br><br>" +

                            "<p><b>Nouvelle description :</b> " + savedCours.getDescription() + "</p>" +

                            "<a href='" + link + "' " +
                            "style='display:inline-block;padding:10px 15px;background:#2196F3;color:white;text-decoration:none;border-radius:5px;'>"
                            + "✏️ Voir le cours</a><br><br>" +

                            "<p>EduFlex</p>";
        }

        int sent = 0;

        for (String email : students) {
            emailService.sendEmail(email, "📚 Notification Cours", message);
            sent++;
        }

        // 🔔 FLASH MESSAGE (IMPORTANT)
        FlashSession.setFlash(
                "📩 Cours enregistré avec succès. " + sent + " email(s) envoyé(s).",
                "success"
        );

        goToList();
    }
    public void initialize() {

        badgeCombo.getItems().addAll(
                "🔥 Populaire",
                "📈 Tendance",
                "⭐ À la une"
        );
    }

    @FXML
    void goToList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CoursList.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goDashboard(ActionEvent event) {
        loadPage(event, "/ProfDashboard.fxml");
    }

    @FXML
    private void goForum(ActionEvent event) {
        loadPage(event, "/forum.fxml");
    }

    @FXML
    private void goRessources(ActionEvent event) {
        loadPage(event, "/listeRessources.fxml");
    }

    @FXML
    private void goCategories(ActionEvent event) {
        loadPage(event, "/CategorieList.fxml");
    }

    @FXML
    private void goExamens(ActionEvent event) {
        loadPage(event, "/ExamenView.fxml");
    }

    @FXML
    private void goEvaluations(ActionEvent event) {
        loadPage(event, "/EvaluationView.fxml");
    }

    @FXML
    private void goResultats(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Resultats");
        alert.setHeaderText(null);
        alert.setContentText("La page resultats sera bientot disponible.");
        alert.showAndWait();
    }

    @FXML
    private void goLogout(ActionEvent event) {
        loadPage(event, "/Login.fxml");
    }

    private void loadPage(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
