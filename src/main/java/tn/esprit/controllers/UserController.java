package tn.esprit.controllers;

import javafx.animation.PauseTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.entities.User.Role;
import tn.esprit.services.AuditLogService;
import tn.esprit.services.UserService;
import tn.esprit.utils.OllamaClient;
import tn.esprit.utils.UserSession;
import javafx.util.Duration;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserController {
    private static final int PAGE_SIZE = 10;
    private static final DateTimeFormatter UI_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Formulaire ───────────────────────────────────────────────────────────
    @FXML private TextField        nameField;
    @FXML private TextField        emailField;
    @FXML private PasswordField    passwordField;
    @FXML private ComboBox<String> roleAddCombo;

    // ── Recherche ────────────────────────────────────────────────────────────
    @FXML private TextField searchField;

    // ── TableView ────────────────────────────────────────────────────────────
    @FXML private TableView<User>            table;
    @FXML private TableColumn<User, String>  colAvatar;
    @FXML private TableColumn<User, String>  colName;
    @FXML private TableColumn<User, String>  colEmail;
    @FXML private TableColumn<User, String>  colRole;
    @FXML private TableColumn<User, String>  colStatus;
    @FXML private TableColumn<User, String>  colCreated;
    @FXML private TableColumn<User, String>  colLastLogin;
    @FXML private TableColumn<User, String>  colActions;

    // ── Filtre + compteur ────────────────────────────────────────────────────
    @FXML private ComboBox<String> filterRoleCombo;
    @FXML private ComboBox<String> filterStatusCombo;
    @FXML private ComboBox<String> sortCombo;
    @FXML private ComboBox<String> bulkRoleCombo;
    @FXML private Label            countLabel;
    @FXML private Label            totalUsersLabel;
    @FXML private Label            totalAdminsLabel;
    @FXML private Label            totalProfsLabel;
    @FXML private Label            totalEtudiantsLabel;
    @FXML private Label            totalParentsLabel;
    @FXML private Label            newUsersMonthLabel;
    @FXML private Label            activeUsersLabel;
    @FXML private Pagination       pagination;
    @FXML private PieChart         rolePieChart;
    @FXML private BarChart<String, Number> roleBarChart;
    @FXML private LineChart<String, Number> monthlyLineChart;
    @FXML private AreaChart<String, Number> weeklyAreaChart;
    @FXML private BarChart<String, Number> topClassesChart;

    // ── Admin UI extras ──────────────────────────────────────────────────────
    @FXML private Label avatarLabel;
    @FXML private ToggleButton darkModeToggle;
    @FXML private TextField copilotQuestionField;
    @FXML private TextArea  copilotAnswerArea;
    @FXML private Label     copilotSourceLabel;

    // ── Service + données ────────────────────────────────────────────────────
    private final UserService userService = new UserService();
    private final AuditLogService auditLogService = new AuditLogService();
    private ObservableList<User> allUsers = FXCollections.observableArrayList();
    private final PauseTransition searchDebounce = new PauseTransition(Duration.millis(300));
    private List<User> processedUsers = new ArrayList<>();
    private final Map<Integer, String> userStatusOverrides = new HashMap<>();

    // ── Initialisation ───────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        if (table != null) {
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }
        colAvatar.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(avatarFor(cell.getValue())));
        colName.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue() == null || cell.getValue().getRole() == null ? "" : cell.getValue().getRole().name()
        ));
        colStatus.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(statusFor(cell.getValue())));
        colCreated.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(createdDateFor(cell.getValue()).format(UI_DATE)));
        colLastLogin.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(lastLoginFor(cell.getValue()).format(UI_DATE)));
        colActions.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(""));
        colActions.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= table.getItems().size()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                User rowUser = table.getItems().get(getIndex());
                MenuItem viewItem = new MenuItem("Voir");
                MenuItem activeItem = new MenuItem("Activer");
                MenuItem deactiveItem = new MenuItem("Désactiver");
                MenuItem blockItem = new MenuItem("Bloquer");
                MenuItem unblockItem = new MenuItem("Débloquer");

                // Lightweight icons (no extra deps)
                viewItem.setGraphic(new Label("👁"));
                activeItem.setGraphic(new Label("✅"));
                deactiveItem.setGraphic(new Label("⏸"));
                blockItem.setGraphic(new Label("⛔"));
                unblockItem.setGraphic(new Label("🔓"));

                viewItem.setOnAction(e -> {
                    table.getSelectionModel().select(rowUser);
                    selectUser();
                });
                activeItem.setOnAction(e -> setUserStatus(rowUser, "Active"));
                deactiveItem.setOnAction(e -> setUserStatus(rowUser, "Deactive"));
                blockItem.setOnAction(e -> setUserStatus(rowUser, "Blocked"));
                unblockItem.setOnAction(e -> setUserBlocked(rowUser, false));

                MenuButton actions = new MenuButton("Actions", null,
                        viewItem,
                        new SeparatorMenuItem(),
                        activeItem,
                        deactiveItem,
                        blockItem,
                        unblockItem
                );
                actions.setGraphic(new Label("⋯"));
                actions.getStyleClass().add("btn-table-menu");
                actions.setPopupSide(javafx.geometry.Side.BOTTOM);

                setGraphic(actions);
                setText(null);
            }
        });
        colRole.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("role-badge-admin", "role-badge-prof", "role-badge-student", "role-badge-parent");
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item.replace("ROLE_", ""));
                switch (item) {
                    case "ROLE_ADMIN" -> getStyleClass().add("role-badge-admin");
                    case "ROLE_PROF" -> getStyleClass().add("role-badge-prof");
                    case "ROLE_ETUDIANT" -> getStyleClass().add("role-badge-student");
                    case "ROLE_PARENT" -> getStyleClass().add("role-badge-parent");
                    default -> {
                    }
                }
            }
        });
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("status-badge-active", "status-badge-inactive", "status-badge-suspended");
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item);
                switch (item) {
                    case "Active" -> getStyleClass().add("status-badge-active");
                    case "Deactive" -> getStyleClass().add("status-badge-inactive");
                    default -> getStyleClass().add("status-badge-suspended");
                }
            }
        });

        roleAddCombo.getItems().addAll(
                "ROLE_PROF", "ROLE_ETUDIANT", "ROLE_PARENT"
        );
        roleAddCombo.setValue("ROLE_ETUDIANT");

        filterRoleCombo.getItems().addAll(
                "Tous", "ROLE_ADMIN", "ROLE_PROF", "ROLE_ETUDIANT", "ROLE_PARENT"
        );
        filterRoleCombo.setValue("Tous");
        if (filterStatusCombo != null) {
            filterStatusCombo.getItems().addAll("Tous statuts", "Active", "Deactive", "Blocked");
            filterStatusCombo.setValue("Tous statuts");
        }
        filterRoleCombo.setOnAction(e -> applyFilter());

        sortCombo.getItems().addAll(
                "Nom A-Z",
                "Nom Z-A",
                "Role A-Z",
                "Plus récents",
                "Plus anciens"
        );
        sortCombo.setValue("Nom A-Z");
        sortCombo.setOnAction(e -> applySorting());

        bulkRoleCombo.getItems().addAll("ROLE_ADMIN", "ROLE_PROF", "ROLE_ETUDIANT", "ROLE_PARENT");
        bulkRoleCombo.setValue("ROLE_ETUDIANT");

        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.setSortPolicy(tv -> {
            applySearchFilterSort();
            return true;
        });

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> searchDebounce.playFromStart());
        }
        searchDebounce.setOnFinished(event -> applySearchFilterSort());

        pagination.setPageFactory(pageIndex -> {
            updateTablePage(pageIndex);
            return new Region();
        });

        loadUsers();
        table.setOnMouseClicked(e -> selectUser());

        // Avatar (top-right)
        try {
            String email = currentActorEmail();
            if (avatarLabel != null && email != null && !email.isBlank()) {
                avatarLabel.setText(String.valueOf(Character.toUpperCase(email.charAt(0))));
            }
        } catch (Exception ignored) {
        }

        if (copilotSourceLabel != null) {
            copilotSourceLabel.setText("Source: Fallback local");
        }
        Platform.runLater(() -> {
            System.out.println("Users loaded = " + allUsers.size());
            System.out.println("Table rows = " + table.getItems().size());

            table.setVisible(true);
            table.setManaged(true);
            table.setPrefHeight(700);
            table.setMinHeight(600);
        });
    }

    // ── Charger tous les users ───────────────────────────────────────────────
    @FXML
    public void loadUsers() {
        try {
            allUsers = FXCollections.observableArrayList(userService.getAllUsers());
            filterRoleCombo.setValue("Tous");
            if (filterStatusCombo != null) filterStatusCombo.setValue("Tous statuts");
            if (sortCombo != null) sortCombo.setValue("Nom A-Z");
            if (searchField != null) searchField.clear();
            applySearchFilterSort();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les utilisateurs : " + e.getMessage());
        }
        System.out.println("ALL USERS = " + allUsers.size());
        System.out.println("PROCESSED USERS = " + processedUsers.size());
        System.out.println("TABLE ITEMS = " + table.getItems().size());
    }

    // ── Recherche par nom ────────────────────────────────────────────────────
    @FXML
    public void handleSearch() {
        applySearchFilterSort();
    }

    // ── Filtrage par rôle ────────────────────────────────────────────────────
    @FXML
    public void applyFilter() {
        applySearchFilterSort();
    }

    @FXML
    public void applySorting() {
        applySearchFilterSort();
    }

    // ── Ajouter user ─────────────────────────────────────────────────────────
    @FXML
    public void addUser() {
        String nom   = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pw    = passwordField.getText();
        Role   role  = Role.fromString(roleAddCombo.getValue());

        if (nom.isEmpty() || email.isEmpty() || pw.isEmpty()) {
            showAlert("Erreur", "Tous les champs sont obligatoires !");
            return;
        }
        if (!email.matches("^[\\w.+\\-]+@[\\w\\-]+\\.[a-z]{2,}$")) {
            showAlert("Erreur", "Format d'email invalide !");
            return;
        }
        if (pw.length() < 6) {
            showAlert("Erreur", "Mot de passe trop court (min. 6 caractères) !");
            return;
        }

        User user = new User(0, nom, pw, email, role);

        try {
            boolean ok = userService.register(user);
            if (!ok) {
                showAlert("Erreur", "Cet email est déjà utilisé !");
                return;
            }
            showAlert("Succès", "Utilisateur ajouté avec succès !");
            String actor = currentActorEmail();
            auditLogService.log(actor, "ADMIN_ADD_USER", "Added user " + email + " with role " + role.name());
            clearFields();
            loadUsers();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Problème lors de l'ajout : " + e.getMessage());
        }
    }

    // ── Supprimer user ───────────────────────────────────────────────────────
    @FXML
    public void deleteUser() {
        User selected = table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Erreur", "Sélectionnez un utilisateur dans le tableau !");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer " + selected.getNom() + " ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userService.deleteUser(selected.getId());
                    showAlert("Succès", "Utilisateur supprimé !");
                    String actor = currentActorEmail();
                    auditLogService.log(actor, "ADMIN_DELETE_USER", "Deleted user id=" + selected.getId() + ", email=" + selected.getEmail());
                    loadUsers();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Suppression échouée : " + e.getMessage());
                }
            }
        });
    }

    // ── Sélection dans la table ──────────────────────────────────────────────
    public void selectUser() {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            nameField.setText(selected.getNom());
            emailField.setText(selected.getEmail());
            passwordField.clear();
            roleAddCombo.setValue(selected.getRole().name());
        }
    }

    @FXML
    public void updateSelectedUser() {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Erreur", "Sélectionnez un utilisateur à modifier.");
            return;
        }

        String nom = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pw = passwordField.getText();
        Role role = Role.fromString(roleAddCombo.getValue());

        if (nom.isEmpty() || email.isEmpty()) {
            showAlert("Erreur", "Nom et email sont obligatoires.");
            return;
        }

        String newPassword = (pw == null || pw.isBlank()) ? selected.getPassword() : pw;
        User updated = new User(selected.getId(), nom, newPassword, email, role);

        try {
            if (userService.updateUser(updated)) {
                showAlert("Succès", "Utilisateur modifié.");
                String actor = currentActorEmail();
                auditLogService.log(actor, "ADMIN_UPDATE_USER", "Updated user id=" + updated.getId() + ", email=" + updated.getEmail());
                clearFields();
                loadUsers();
            } else {
                showAlert("Erreur", "Modification impossible.");
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur modification: " + e.getMessage());
        }
    }

    @FXML
    public void deleteSelectedUsers() {
        List<User> selectedUsers = new ArrayList<>(table.getSelectionModel().getSelectedItems());
        if (selectedUsers.isEmpty()) {
            showAlert("Erreur", "Sélectionnez un ou plusieurs utilisateurs.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer " + selectedUsers.size() + " utilisateur(s) ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                int deleted = 0;
                for (User user : selectedUsers) {
                    try {
                        if (userService.deleteUser(user.getId())) deleted++;
                    } catch (SQLException ignored) {
                    }
                }
                String actor = currentActorEmail();
                auditLogService.log(actor, "ADMIN_BULK_DELETE", "Bulk deleted users count=" + deleted);
                showAlert("Succès", deleted + " utilisateur(s) supprimé(s).");
                loadUsers();
            }
        });
    }

    @FXML
    public void applyBulkRole() {
        List<User> selectedUsers = new ArrayList<>(table.getSelectionModel().getSelectedItems());
        if (selectedUsers.isEmpty()) {
            showAlert("Erreur", "Sélectionnez un ou plusieurs utilisateurs.");
            return;
        }
        Role role = Role.fromString(bulkRoleCombo.getValue());
        int updatedCount = 0;
        for (User user : selectedUsers) {
            try {
                User updated = new User(user.getId(), user.getNom(), user.getPassword(), user.getEmail(), role);
                if (userService.updateUser(updated)) updatedCount++;
            } catch (SQLException ignored) {
            }
        }
        String actor = currentActorEmail();
        auditLogService.log(actor, "ADMIN_BULK_ROLE_UPDATE", "Set role=" + role.name() + " for " + updatedCount + " users");
        showAlert("Succès", "Rôle appliqué à " + updatedCount + " utilisateur(s).");
        loadUsers();
    }

    // ── Vider les champs ─────────────────────────────────────────────────────
    @FXML
    public void clearFields() {
        nameField.clear();
        emailField.clear();
        passwordField.clear();
        roleAddCombo.setValue("ROLE_ETUDIANT");
    }

    @FXML
    private void exportVisibleUsers() {
        List<User> visibleUsers = table.getItems();
        if (visibleUsers == null || visibleUsers.isEmpty()) {
            showAlert("Information", "Aucun utilisateur a exporter.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les utilisateurs (CSV)");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV files", "*.csv")
        );
        String exportDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
        fileChooser.setInitialFileName("users_export_" + exportDate + ".csv");

        Stage stage = (Stage) table.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) return;

        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(file),
                java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write('\uFEFF');
            writer.write("ID;Nom complet;Email;Role;Statut;Date creation;Derniere connexion\n");
            for (User user : visibleUsers) {
                writer.write(user.getId() + ";"
                        + csv(user.getNom()) + ";"
                        + csv(user.getEmail()) + ";"
                        + csv(user.getRole().name()) + ";"
                        + statusFor(user) + ";"
                        + createdDateFor(user).format(UI_DATE) + ";"
                        + lastLoginFor(user).format(UI_DATE) + "\n");
            }
            showAlert("Succes", "Export termine : " + file.getAbsolutePath());
        } catch (IOException e) {
            showAlert("Erreur", "Echec export CSV : " + e.getMessage());
        }
    }

    @FXML
    private void exportExcelUsers() {
        List<User> visibleUsers = table.getItems();
        if (visibleUsers == null || visibleUsers.isEmpty()) {
            showAlert("Information", "Aucun utilisateur a exporter.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter les utilisateurs (Tableau Excel stylisé)");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel files", "*.xls"));
        chooser.setInitialFileName("users_export_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd")) + ".xls");
        File file = chooser.showSaveDialog((Stage) table.getScene().getWindow());
        if (file == null) return;
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write("<html><head><meta charset='UTF-8'>");
            writer.write("<style>");
            writer.write("body{font-family:Segoe UI,Arial,sans-serif;background:#F8F9FC;margin:0;padding:12px;}");
            writer.write("table{border-collapse:collapse;width:100%;background:#FFFFFF;border:1px solid #C7C3F5;}");
            writer.write("th{background:#D7D2FF;color:#4C1D95;padding:10px 12px;border:1px solid #C5BEFA;text-align:left;font-weight:800;}");
            writer.write("td{padding:9px 12px;border:1px solid #E6E4FA;color:#1F2937;}");
            writer.write("tr:nth-child(odd){background:#FFFFFF;}");
            writer.write("tr:nth-child(even){background:#F7F5FF;}");
            writer.write(".status-active{background:#D1FAE5;color:#065F46;font-weight:700;}");
            writer.write(".status-deactive{background:#FEF3C7;color:#92400E;font-weight:700;}");
            writer.write(".status-blocked{background:#FEE2E2;color:#991B1B;font-weight:700;}");
            writer.write("</style></head><body>");
            writer.write("<table>");
            writer.write("<thead><tr>");
            writer.write("<th>ID</th><th>Nom complet</th><th>Email</th><th>Role</th><th>Statut</th><th>Date creation</th><th>Derniere connexion</th>");
            writer.write("</tr></thead><tbody>");
            for (User user : visibleUsers) {
                String status = statusFor(user);
                String statusClass = "status-active";
                if ("Deactive".equals(status)) statusClass = "status-deactive";
                if ("Blocked".equals(status)) statusClass = "status-blocked";
                writer.write("<tr>");
                writer.write("<td>" + user.getId() + "</td>");
                writer.write("<td>" + html(user.getNom()) + "</td>");
                writer.write("<td>" + html(user.getEmail()) + "</td>");
                writer.write("<td>" + html(user.getRole().name()) + "</td>");
                writer.write("<td class='" + statusClass + "'>" + html(status) + "</td>");
                writer.write("<td>" + createdDateFor(user).format(UI_DATE) + "</td>");
                writer.write("<td>" + lastLoginFor(user).format(UI_DATE) + "</td>");
                writer.write("</tr>");
            }
            writer.write("</tbody></table></body></html>");
            showAlert("Succès", "Export Excel généré : " + file.getAbsolutePath());
        } catch (IOException e) {
            showAlert("Erreur", "Echec export Excel : " + e.getMessage());
        }
    }

    @FXML
    private void exportPdfUsers() {
        List<User> visibleUsers = table.getItems();
        if (visibleUsers == null || visibleUsers.isEmpty()) {
            showAlert("Information", "Aucun utilisateur a exporter.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter les utilisateurs (PDF texte)");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        chooser.setInitialFileName("users_export_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd")) + ".pdf");
        File file = chooser.showSaveDialog((Stage) table.getScene().getWindow());
        if (file == null) return;
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write("EduFlex Users Report\n\n");
            writer.write("ID | Full Name | Email | Role | Status | Created | Last Login\n");
            writer.write("-------------------------------------------------------------\n");
            for (User user : visibleUsers) {
                writer.write(user.getId() + " | " + csv(user.getNom()) + " | " + csv(user.getEmail()) + " | "
                        + user.getRole().name() + " | " + statusFor(user) + " | "
                        + createdDateFor(user).format(UI_DATE) + " | " + lastLoginFor(user).format(UI_DATE) + "\n");
            }
            showAlert("Succès", "Export PDF généré : " + file.getAbsolutePath());
        } catch (IOException e) {
            showAlert("Erreur", "Echec export PDF : " + e.getMessage());
        }
    }
    @FXML
    private void goBack(ActionEvent event) {
        try {
            String actor = currentActorEmail();
            auditLogService.log(actor, "LOGOUT", "Admin session ended from user management screen");
            UserSession.clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Utilitaires ──────────────────────────────────────────────────────────
    private void updateCount(int count) {
        if (countLabel != null)
            countLabel.setText(count + " utilisateur(s)");
    }

    private void applySearchFilterSort() {
        String keyword = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        String roleFilter = filterRoleCombo != null ? filterRoleCombo.getValue() : "Tous";
        String statusFilter = filterStatusCombo != null ? filterStatusCombo.getValue() : "Tous statuts";
        String sortMode = sortCombo != null ? sortCombo.getValue() : "Nom A-Z";

        Comparator<User> comparator = switch (sortMode) {
            case "Nom Z-A" -> Comparator.comparing(User::getNom, String.CASE_INSENSITIVE_ORDER).reversed();
            case "Role A-Z" -> Comparator.comparing(u -> u.getRole().name(), String.CASE_INSENSITIVE_ORDER);
            case "Plus récents" -> Comparator.comparingInt(User::getId).reversed();
            case "Plus anciens" -> Comparator.comparingInt(User::getId);
            default -> Comparator.comparing(User::getNom, String.CASE_INSENSITIVE_ORDER);
        };

        List<User> processed = allUsers.stream()
                .filter(u -> keyword.isEmpty() || u.getNom().toLowerCase().contains(keyword))
                .filter(u -> roleFilter == null || roleFilter.equals("Tous")
                        || u.getRole().name().equals(roleFilter))
                .filter(u -> statusFilter == null || statusFilter.equals("Tous statuts")
                        || statusFor(u).equals(statusFilter))
                .sorted(comparator)
                .collect(Collectors.toList());

        if (table.getComparator() != null) {
            processed = processed.stream().sorted(table.getComparator()).collect(Collectors.toList());
        }

        processedUsers = processed;
        updateCount(processedUsers.size());
        updateStatistics(processedUsers);
        updatePaginationBounds();
        updateTablePage(pagination.getCurrentPageIndex());
    }

    private void updatePaginationBounds() {
        int pageCount = Math.max(1, (int) Math.ceil((double) processedUsers.size() / PAGE_SIZE));
        pagination.setPageCount(pageCount);
        if (pagination.getCurrentPageIndex() >= pageCount) {
            pagination.setCurrentPageIndex(0);
        }
    }

    private void updateTablePage(int pageIndex) {
        if (processedUsers == null) {
            table.setItems(FXCollections.observableArrayList());
            return;
        }
        int from = pageIndex * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, processedUsers.size());
        if (from > to) from = to;
        table.setItems(FXCollections.observableArrayList(processedUsers.subList(from, to)));
    }

    private void updateStatistics(List<User> users) {
        if (users == null) return;
        long admins = users.stream().filter(u -> u.getRole() == Role.ROLE_ADMIN).count();
        long profs = users.stream().filter(u -> u.getRole() == Role.ROLE_PROF).count();
        long etudiants = users.stream().filter(u -> u.getRole() == Role.ROLE_ETUDIANT).count();
        long parents = users.stream().filter(u -> u.getRole() == Role.ROLE_PARENT).count();
        long active = users.stream().filter(u -> "Active".equals(statusFor(u))).count();
        long newThisMonth = users.stream().filter(u -> createdDateFor(u).getMonth() == LocalDate.now().getMonth()
                && createdDateFor(u).getYear() == LocalDate.now().getYear()).count();

        animateCounter(totalUsersLabel, users.size());
        animateCounter(totalAdminsLabel, admins);
        animateCounter(totalProfsLabel, profs);
        animateCounter(totalEtudiantsLabel, etudiants);
        animateCounter(totalParentsLabel, parents);
        animateCounter(newUsersMonthLabel, newThisMonth);
        animateCounter(activeUsersLabel, active);

        if (rolePieChart != null) {
            rolePieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Admins", admins),
                    new PieChart.Data("Professeurs", profs),
                    new PieChart.Data("Etudiants", etudiants),
                    new PieChart.Data("Parents", parents)
            ));
        }

        if (roleBarChart != null) {
            XYChart.Series<String, Number> roleSeries = new XYChart.Series<>();
            roleSeries.setName("Users");
            roleSeries.getData().add(new XYChart.Data<>("Admins", admins));
            roleSeries.getData().add(new XYChart.Data<>("Teachers", profs));
            roleSeries.getData().add(new XYChart.Data<>("Students", etudiants));
            roleSeries.getData().add(new XYChart.Data<>("Parents", parents));
            roleBarChart.getData().setAll(roleSeries);
        }

        if (monthlyLineChart != null) {
            XYChart.Series<String, Number> monthSeries = new XYChart.Series<>();
            monthSeries.setName("Signups");
            for (int i = 5; i >= 0; i--) {
                LocalDate m = LocalDate.now().minusMonths(i);
                long count = users.stream().filter(u -> createdDateFor(u).getMonthValue() == m.getMonthValue()).count();
                monthSeries.getData().add(new XYChart.Data<>(m.getMonth().name().substring(0, 3), count));
            }
            monthlyLineChart.getData().setAll(monthSeries);
        }

        if (weeklyAreaChart != null) {
            XYChart.Series<String, Number> weekSeries = new XYChart.Series<>();
            weekSeries.setName("Activity");
            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            int base = Math.max(3, users.size() / 4);
            for (int i = 0; i < days.length; i++) {
                weekSeries.getData().add(new XYChart.Data<>(days[i], base + ((i * 3) % 9)));
            }
            weeklyAreaChart.getData().setAll(weekSeries);
        }

        if (topClassesChart != null) {
            XYChart.Series<String, Number> classSeries = new XYChart.Series<>();
            classSeries.setName("Enrolled");
            int base = Math.max(5, users.size() / 6);
            classSeries.getData().add(new XYChart.Data<>("3A Info", base + 8));
            classSeries.getData().add(new XYChart.Data<>("2B Math", base + 5));
            classSeries.getData().add(new XYChart.Data<>("1C Sci", base + 3));
            classSeries.getData().add(new XYChart.Data<>("4A Eco", base + 2));
            topClassesChart.getData().setAll(classSeries);
        }

        Platform.runLater(this::enhanceChartPresentation);
    }

    private void enhanceChartPresentation() {
        styleBarChart(roleBarChart);
        styleBarChart(topClassesChart);
        styleLineChart(monthlyLineChart);
        styleAreaChart(weeklyAreaChart);
        stylePieChart(rolePieChart);
    }

    private void styleBarChart(BarChart<String, Number> chart) {
        if (chart == null) return;
        chart.setAnimated(true);
        for (XYChart.Series<String, Number> s : chart.getData()) {
            for (XYChart.Data<String, Number> d : s.getData()) {
                if (d.getNode() != null) {
                    d.getNode().setStyle("-fx-background-radius: 12 12 0 0;");
                    Tooltip tip = new Tooltip(d.getXValue() + " : " + d.getYValue());
                    tip.setStyle("-fx-background-color: #6C63FF; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8;");
                    Tooltip.install(d.getNode(), tip);
                }
            }
        }
    }

    private void styleLineChart(LineChart<String, Number> chart) {
        if (chart == null) return;
        chart.setAnimated(true);
        for (XYChart.Series<String, Number> s : chart.getData()) {
            for (XYChart.Data<String, Number> d : s.getData()) {
                if (d.getNode() != null) {
                    Tooltip tip = new Tooltip(d.getXValue() + " : " + d.getYValue());
                    tip.setStyle("-fx-background-color: #8B5CF6; -fx-text-fill: white; -fx-background-radius: 8;");
                    Tooltip.install(d.getNode(), tip);
                }
            }
        }
    }

    private void styleAreaChart(AreaChart<String, Number> chart) {
        if (chart == null) return;
        chart.setAnimated(true);
        for (XYChart.Series<String, Number> s : chart.getData()) {
            for (XYChart.Data<String, Number> d : s.getData()) {
                if (d.getNode() != null) {
                    Tooltip tip = new Tooltip(d.getXValue() + " : " + d.getYValue());
                    tip.setStyle("-fx-background-color: #7C7BF8; -fx-text-fill: white; -fx-background-radius: 8;");
                    Tooltip.install(d.getNode(), tip);
                }
            }
        }
    }

    private void stylePieChart(PieChart chart) {
        if (chart == null) return;
        chart.setAnimated(true);
        for (PieChart.Data d : chart.getData()) {
            if (d.getNode() != null) {
                Tooltip tip = new Tooltip(d.getName() + " : " + (int) d.getPieValue());
                tip.setStyle("-fx-background-color: #6C63FF; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8;");
                Tooltip.install(d.getNode(), tip);
            }
        }
    }

    private void animateCounter(Label label, long target) {
        if (label == null) return;
        long current;
        try {
            current = Long.parseLong(label.getText().replaceAll("[^0-9]", ""));
        } catch (Exception ignored) {
            current = 0;
        }
        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(label.textProperty(), String.valueOf(current))),
                new KeyFrame(Duration.millis(450), event -> label.setText(String.valueOf(target)))
        );
        t.play();
    }

    private String avatarFor(User user) {
        if (user == null || user.getNom() == null || user.getNom().isBlank()) return "?";
        return String.valueOf(Character.toUpperCase(user.getNom().charAt(0)));
    }

    private String statusFor(User user) {
        if (user == null) return "Deactive";
        if (user.isBlocked()) return "Blocked";
        String s = user.getStatus();
        if (s == null || s.isBlank()) return "Deactive";
        return s.trim();
    }

    private void setUserStatus(User user, String status) {
        if (user == null || status == null || status.isBlank()) return;
        try {
            userService.setStatus(user.getId(), status);
            String actor = currentActorEmail();
            auditLogService.log(actor, "ADMIN_STATUS_CHANGE", "User id=" + user.getId() + " status=" + status);
            loadUsers();
        } catch (SQLException e) {
            showAlert("Erreur", "Changement de statut impossible: " + e.getMessage());
        }
    }

    private void setUserBlocked(User user, boolean blocked) {
        if (user == null) return;
        try {
            userService.setBlocked(user.getId(), blocked);
            String actor = currentActorEmail();
            auditLogService.log(actor, "ADMIN_BLOCK_CHANGE", "User id=" + user.getId() + " blocked=" + blocked);
            loadUsers();
        } catch (SQLException e) {
            showAlert("Erreur", "Blocage/déblocage impossible: " + e.getMessage());
        }
    }

    private LocalDate createdDateFor(User user) {
        if (user != null && user.getCreatedAt() != null) {
            return user.getCreatedAt().toLocalDate();
        }
        int days = user == null ? 0 : Math.max(0, user.getId() % 180);
        return LocalDate.now().minusDays(days);
    }

    private LocalDate lastLoginFor(User user) {
        if (user != null && user.getLastLoginAt() != null) {
            return user.getLastLoginAt().toLocalDate();
        }
        int days = user == null ? 0 : Math.max(0, user.getId() % 30);
        return LocalDate.now().minusDays(days);
    }

    private String csv(String value) {
        if (value == null) return "";
        return value.replace(";", ",").replace("\n", " ").replace("\r", " ").trim();
    }

    private String html(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .trim();
    }

    private String currentActorEmail() {
        User current = UserSession.getCurrentUser();
        return current != null ? current.getEmail() : "admin@system";
    }

    @FXML
    private void toggleDarkMode() {
        if (table == null || table.getScene() == null) return;
        Parent root = table.getScene().getRoot();
        if (root == null) return;
        if (darkModeToggle != null && darkModeToggle.isSelected()) {
            if (!root.getStyleClass().contains("theme-dark")) root.getStyleClass().add("theme-dark");
        } else {
            root.getStyleClass().remove("theme-dark");
        }
    }

    // ── Sidebar navigation (safe) ───────────────────────────────────────────
    @FXML private void goDashboardAdmin(ActionEvent event) { navigateIfExists(event, "/Home.fxml"); }
    @FXML private void goForum(ActionEvent event) { navigateIfExists(event, "/forum.fxml"); }
    @FXML private void goUsers(ActionEvent event) { /* already here */ }
    @FXML private void goClasses(ActionEvent event) { navigateIfExists(event, "/CoursList.fxml"); }
    @FXML private void goMatieres(ActionEvent event) { navigateIfExists(event, "/CategorieList.fxml"); }
    @FXML private void goAccess(ActionEvent event) { navigateIfExists(event, "/GestionUsers.fxml"); }
    @FXML private void goStats(ActionEvent event) { navigateIfExists(event, "/VoirEvaluation.fxml"); }
    @FXML private void goSettings(ActionEvent event) { navigateIfExists(event, "/ParentDashboard.fxml"); }

    @FXML
    private void goCopiloteIA(ActionEvent event) {
        if (copilotQuestionField != null) {
            copilotQuestionField.requestFocus();
        }
    }

    @FXML
    private void logoutAdmin(ActionEvent event) {
        try {
            String actor = currentActorEmail();
            auditLogService.log(actor, "LOGOUT", "Admin session ended from admin sidebar");
        } catch (Exception ignored) {}
        UserSession.clear();
        navigateIfExists(event, "/Home.fxml");
    }

    private void navigateIfExists(ActionEvent event, String fxmlPath) {
        try {
            var url = getClass().getResource(fxmlPath);
            if (url == null) {
                showAlert("Information", "Vue non disponible: " + fxmlPath);
                return;
            }
            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Navigation impossible: " + e.getMessage());
        }
    }

    // ── Copilote IA (fallback local) ─────────────────────────────────────────
    @FXML
    private void analyzeWithCopilot(ActionEvent event) {
        runCopilot("");
    }

    @FXML
    private void askCopilot(ActionEvent event) {
        String q = copilotQuestionField != null ? copilotQuestionField.getText() : "";
        runCopilot(q);
    }

    @FXML
    private void exportCopilotReport(ActionEvent event) {
        if (copilotAnswerArea == null) return;
        String content = copilotAnswerArea.getText();
        if (content == null || content.isBlank()) {
            content = buildLocalInsights();
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter rapport Copilote (TXT)");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text files", "*.txt")
        );
        fileChooser.setInitialFileName("copilote_rapport.txt");
        Stage stage = (Stage) table.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) return;

        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(file),
                java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write(content);
            showAlert("Succès", "Rapport exporté : " + file.getAbsolutePath());
        } catch (IOException e) {
            showAlert("Erreur", "Echec export: " + e.getMessage());
        }
    }


    private String buildLocalInsights() {
        List<User> base = processedUsers != null ? processedUsers : allUsers;
        if (base == null) base = List.of();

        long admins = base.stream().filter(u -> u.getRole() == Role.ROLE_ADMIN).count();
        long profs = base.stream().filter(u -> u.getRole() == Role.ROLE_PROF).count();
        long etudiants = base.stream().filter(u -> u.getRole() == Role.ROLE_ETUDIANT).count();
        long parents = base.stream().filter(u -> u.getRole() == Role.ROLE_PARENT).count();

        StringBuilder sb = new StringBuilder();
        sb.append("Brief IA Gestion Utilisateurs\n");
        sb.append("Resume\n");
        sb.append("- Parc utilisateurs: ").append(base.size()).append(" comptes (vue actuelle)\n");
        sb.append("- Repartition roles: admin=").append(admins)
                .append(", prof=").append(profs)
                .append(", etudiant=").append(etudiants)
                .append(", parent=").append(parents).append("\n\n");

        sb.append("Anomalies\n");
        sb.append("- Donnees de verification/blocage non disponibles dans ce module (champs manquants).\n\n");

        sb.append("Actions prioritaires\n");
        sb.append("- Completer le modele User (statut, blocage) si tu veux les badges APPROVED/REJECTED/BLOCKED.\n");
        sb.append("- Ajouter des colonnes Actions (Voir/Modifier/Approuver/Rejeter/Bloquer) sur la table.\n");
        return sb.toString();
    }

    private String ollamaModel() {
        String fromEnv = System.getenv("OLLAMA_MODEL");
        if (fromEnv != null && !fromEnv.isBlank()) return fromEnv.trim();
        return "llama3";
    }

    private void runCopilot(String questionOrBlank) {
        if (copilotAnswerArea == null) return;

        final String preferredModel = ollamaModel();
        final String context = buildLocalInsights();
        final String question = questionOrBlank == null ? "" : questionOrBlank.trim();

        copilotAnswerArea.setText("Connexion à Ollama... (ça peut prendre ~1 min au premier message)");
        if (copilotSourceLabel != null) copilotSourceLabel.setText("Source: Ollama (local)");

        final String[] usedModel = {preferredModel};
        Task<String> t = new Task<>() {
            @Override
            protected String call() throws Exception {
                OllamaClient client = OllamaClient.localDefault();
                usedModel[0] = client.pickAvailableModel(preferredModel);
                String system = "Tu es un copilote admin pour une application JavaFX (EduFlex). " +
                        "Réponds en français, de façon concise et actionnable.";
                String userPrompt = (question.isBlank()
                        ? "Analyse ces infos et propose un bref résumé + actions.\n\n" + context
                        : "Question: " + question + "\n\nContexte:\n" + context);

                return client.chatOnce(usedModel[0], system, userPrompt);
            }
        };

        t.setOnSucceeded(e -> Platform.runLater(() -> {
            if (copilotSourceLabel != null) copilotSourceLabel.setText("Source: Ollama (local) · " + usedModel[0]);
            copilotAnswerArea.setText(t.getValue());
        }));
        t.setOnFailed(e -> Platform.runLater(() -> {
            if (copilotSourceLabel != null) copilotSourceLabel.setText("Source: Fallback local");
            String err = t.getException() != null ? t.getException().getMessage() : "Erreur inconnue";
            copilotAnswerArea.setText(
                    "Ollama indisponible. (Astuce: vérifie `ollama serve` et exécute `ollama list`)\n" +
                            "Détail: " + err + "\n\n" +
                            buildLocalInsights()
            );
        }));

        Thread th = new Thread(t, "ollama-copilot");
        th.setDaemon(true);
        th.start();
    }

    public void showAlert(String title, String message) {
        Alert.AlertType type = Alert.AlertType.INFORMATION;
        if (title != null && title.toLowerCase().contains("erreur")) type = Alert.AlertType.ERROR;
        if (title != null && title.toLowerCase().contains("confirmation")) type = Alert.AlertType.CONFIRMATION;
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
