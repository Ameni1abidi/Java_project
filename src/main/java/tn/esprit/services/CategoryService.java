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
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, categorie.getNom());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de la categorie", e);
        }
    }

    public List<categorie> getAll() {
        String sql = "SELECT nom FROM categorie";
        List<categorie> categories = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categories.add(new categorie(rs.getString("nom")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recuperation des categories", e);
        }

        return categories;
    }

    public categorie getByNom(String nom) {
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
        String sql = "UPDATE categorie SET nom = ? WHERE nom = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nouveauNom);
            ps.setString(2, ancienNom);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise a jour de la categorie", e);
        }
    }

    public boolean delete(String nom) {
        String sql = "DELETE FROM categorie WHERE nom = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nom);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la categorie", e);
        }
    }

    public boolean existsByName(String nom) {
        String sql = "SELECT COUNT(*) FROM categorie WHERE nom = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nom);

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
