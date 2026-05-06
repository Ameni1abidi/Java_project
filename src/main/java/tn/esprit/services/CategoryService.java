package tn.esprit.services;

import tn.esprit.entities.categorie;
import tn.esprit.utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoryService {
    private final Connection connection = MyDatabase.getInstance().getConnection();

    public CategoryService() {
        ensureCategorieTable();
    }

    public void add(categorie categorie) {
        String nom = normalizeName(categorie.getNom());
        String sql = "INSERT INTO categorie(nom) VALUES(?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de la categorie", e);
        }
    }

    public List<categorie> getAll() {
        List<categorie> categories = new ArrayList<>();
        String sql = "SELECT nom FROM categorie ORDER BY nom";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String nom = rs.getString("nom");
                if (nom != null && !nom.isBlank()) {
                    categories.add(new categorie(nom));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recuperation des categories", e);
        }
        return categories;
    }

    public categorie getByNom(String nom) {
        nom = normalizeName(nom);
        String sql = "SELECT nom FROM categorie WHERE nom = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nom);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new categorie(rs.getString("nom"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recuperation de la categorie", e);
        }

        return null;
    }

    public boolean update(String ancienNom, String nouveauNom) {
        ancienNom = normalizeName(ancienNom);
        nouveauNom = normalizeName(nouveauNom);

        if (ancienNom.equals(nouveauNom)) {
            return true;
        }

        boolean autoCommit = true;
        try {
            autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            ensureCategorieExists(nouveauNom);
            updateResourcesCategory(ancienNom, nouveauNom);
            boolean deletedOldName = deleteCategorieOnly(ancienNom);

            connection.commit();
            return deletedOldName;
        } catch (SQLException e) {
            rollbackQuietly();
            throw new RuntimeException("Erreur lors de la mise a jour de la categorie", e);
        } finally {
            restoreAutoCommit(autoCommit);
        }
    }

    public boolean delete(String nom) {
        nom = normalizeName(nom);
        String sql = "DELETE FROM categorie WHERE nom = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nom);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la categorie", e);
        }
    }

    public boolean existsByName(String nom) {
        nom = normalizeName(nom);
        String sql = "SELECT COUNT(*) FROM categorie WHERE nom = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nom);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la verification du nom", e);
        }

        return false;
    }

    public int countResourcesByCategory(String nom) {
        if (!hasTable("ressource")) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM ressource WHERE categorie_nom = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, normalizeName(nom));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des ressources de la categorie", e);
        }

        return 0;
    }

    private void ensureCategorieTable() {
        String sql = "CREATE TABLE IF NOT EXISTS categorie (nom VARCHAR(255) PRIMARY KEY)";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'initialisation de la table categorie", e);
        }
    }

    private void ensureCategorieExists(String nom) throws SQLException {
        String sql = "INSERT INTO categorie(nom) VALUES(?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.executeUpdate();
        }
    }

    private void updateResourcesCategory(String ancienNom, String nouveauNom) throws SQLException {
        if (!hasTable("ressource")) {
            return;
        }

        String sql = "UPDATE ressource SET categorie_nom = ? WHERE categorie_nom = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nouveauNom);
            ps.setString(2, ancienNom);
            ps.executeUpdate();
        }
    }

    private boolean deleteCategorieOnly(String nom) throws SQLException {
        String sql = "DELETE FROM categorie WHERE nom = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nom);
            return ps.executeUpdate() > 0;
        }
    }

    private boolean hasTable(String tableName) {
        try (ResultSet rs = connection.getMetaData().getTables(connection.getCatalog(), null, tableName, null)) {
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la verification de la table " + tableName, e);
        }
    }

    private String normalizeName(String nom) {
        if (nom == null) {
            return "";
        }
        return nom.trim();
    }

    private void rollbackQuietly() {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void restoreAutoCommit(boolean autoCommit) {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException ignored) {
        }
    }
}
