package tn.esprit.services;

import tn.esprit.entities.categorie;
import tn.esprit.utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryService {
    private final Connection connection = MyDatabase.getInstance().getConnection();

    public void add(categorie categorie) {
        String sql = "INSERT INTO categorie(nom) VALUES(?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, categorie.getNom());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    categorie.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de la categorie", e);
        }
    }

    public List<categorie> getAll() {
        String sql = "SELECT id, nom FROM categorie";
        List<categorie> categories = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categories.add(new categorie(rs.getInt("id"), rs.getString("nom")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recuperation des categories", e);
        }

        return categories;
    }

    public categorie getById(int id) {
        String sql = "SELECT id, nom FROM categorie WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new categorie(rs.getInt("id"), rs.getString("nom"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recuperation de la categorie", e);
        }

        return null;
    }

    public boolean update(categorie categorie) {
        String sql = "UPDATE categorie SET nom = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, categorie.getNom());
            ps.setInt(2, categorie.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise a jour de la categorie", e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM categorie WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la categorie", e);
        }
    }

    public boolean existsByName(String nom, Integer excludeId) {
        String sql = "SELECT COUNT(*) FROM categorie WHERE nom = ?";
        if (excludeId != null) {
            sql += " AND id != ?";
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nom);
            if (excludeId != null) {
                ps.setInt(2, excludeId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification du nom", e);
        }

        return false;
    }
}
