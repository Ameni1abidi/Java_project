package tn.esprit.services;

import tn.esprit.entities.resources;
import tn.esprit.utils.MyDatabase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ResourceService {
    private final Connection connection = MyDatabase.getInstance().getConnection();

    public ResourceService() {
        ensureSchema();
    }

    private void ensureSchema() {
        try {
            if (!hasColumn("type")) {
                executeUpdate("ALTER TABLE ressource ADD COLUMN type VARCHAR(50)");
            }
            if (!hasColumn("disponible_le")) {
                executeUpdate("ALTER TABLE ressource ADD COLUMN disponible_le VARCHAR(100)");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'initialisation du schéma des ressources", e);
        }
    }

    private boolean hasColumn(String columnName) throws SQLException {
        String sql = "SHOW COLUMNS FROM ressource LIKE ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void executeUpdate(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    public void add(resources resource) {
        String sql = "INSERT INTO ressource(titre, contenu, categorie_id, type, disponible_le) VALUES(?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, resource.getTitre());
            ps.setString(2, resource.getContenu());
            ps.setInt(3, resource.getCategorieId());
            ps.setString(4, resource.getType());
            ps.setString(5, resource.getDisponibleLe());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    resource.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de la ressource", e);
        }
    }

    public List<resources> getAll() {
        String sql = "SELECT id, titre, contenu, categorie_id, type, disponible_le FROM ressource";
        List<resources> resourceList = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resourceList.add(new resources(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("contenu"),
                        rs.getInt("categorie_id"),
                        rs.getString("type"),
                        rs.getString("disponible_le")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recuperation des ressources", e);
        }

        return resourceList;
    }

    public resources getById(int id) {
        String sql = "SELECT id, titre, contenu, categorie_id, type, disponible_le FROM ressource WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new resources(
                            rs.getInt("id"),
                            rs.getString("titre"),
                            rs.getString("contenu"),
                            rs.getInt("categorie_id"),
                            rs.getString("type"),
                            rs.getString("disponible_le")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recuperation de la ressource", e);
        }

        return null;
    }

    public boolean update(resources resource) {
        String sql = "UPDATE ressource SET titre = ?, contenu = ?, categorie_id = ?, type = ?, disponible_le = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, resource.getTitre());
            ps.setString(2, resource.getContenu());
            ps.setInt(3, resource.getCategorieId());
            ps.setString(4, resource.getType());
            ps.setString(5, resource.getDisponibleLe());
            ps.setInt(6, resource.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise a jour de la ressource", e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM ressource WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la ressource", e);
        }
    }

    public List<resources> getByCategoryId(int categorieId) {
        String sql = "SELECT id, titre, contenu, categorie_id, type, disponible_le FROM ressource WHERE categorie_id = ?";
        List<resources> resourceList = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, categorieId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resourceList.add(new resources(
                            rs.getInt("id"),
                            rs.getString("titre"),
                            rs.getString("contenu"),
                            rs.getInt("categorie_id"),
                            rs.getString("type"),
                            rs.getString("disponible_le")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recuperation des ressources par categorie", e);
        }

        return resourceList;
    }
}
