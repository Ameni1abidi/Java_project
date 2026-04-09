package tn.esprit.services;

import tn.esprit.entities.resources;
import tn.esprit.utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResourceService {
    private final Connection connection = MyDatabase.getInstance().getConnection();

    public void add(resources resource) {
        String sql = "INSERT INTO ressource(titre, contenu, categorie_id) VALUES(?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, resource.getTitre());
            ps.setString(2, resource.getContenu());
            ps.setInt(3, resource.getCategorieId());
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
        String sql = "SELECT id, titre, contenu, categorie_id FROM ressource";
        List<resources> resourceList = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resourceList.add(new resources(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("contenu"),
                        rs.getInt("categorie_id")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recuperation des ressources", e);
        }

        return resourceList;
    }

    public resources getById(int id) {
        String sql = "SELECT id, titre, contenu, categorie_id FROM ressource WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new resources(
                            rs.getInt("id"),
                            rs.getString("titre"),
                            rs.getString("contenu"),
                            rs.getInt("categorie_id")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recuperation de la ressource", e);
        }

        return null;
    }

    public boolean update(resources resource) {
        String sql = "UPDATE ressource SET titre = ?, contenu = ?, categorie_id = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, resource.getTitre());
            ps.setString(2, resource.getContenu());
            ps.setInt(3, resource.getCategorieId());
            ps.setInt(4, resource.getId());
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
        String sql = "SELECT id, titre, contenu, categorie_id FROM ressource WHERE categorie_id = ?";
        List<resources> resourceList = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, categorieId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resourceList.add(new resources(
                            rs.getInt("id"),
                            rs.getString("titre"),
                            rs.getString("contenu"),
                            rs.getInt("categorie_id")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recuperation des ressources par categorie", e);
        }

        return resourceList;
    }

}
