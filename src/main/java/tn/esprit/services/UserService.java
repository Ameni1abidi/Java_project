package tn.esprit.services;

import tn.esprit.entities.User;
import tn.esprit.entities.User.Role;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserService {

    private final Connection cnx = MyDatabase.getInstance().getConnection();

    // ─────────────────────────────────────────────────────────────────────────
    //  REGISTER
    // ─────────────────────────────────────────────────────────────────────────
    public boolean register(User user) throws SQLException {
        if (emailExists(user.getEmail())) return false;

        String sql = "INSERT INTO utilisateur (nom, password, email, role, is_verified, is_blocked, status) " +
                "VALUES (?, ?, ?, ?, 0, 0, 'PENDING')";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole().name());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) user.setId(keys.getInt(1));
            return true;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LOGIN
    // ─────────────────────────────────────────────────────────────────────────
    public Optional<User> login(String email, String password) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE email = ? AND password = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
            return Optional.empty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  GET ALL
    // ─────────────────────────────────────────────────────────────────────────
    public List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  GET BY ID
    // ─────────────────────────────────────────────────────────────────────────
    public Optional<User> getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
            return Optional.empty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────────────────────────────────
    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE utilisateur SET nom=?, password=?, email=?, role=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole().name());
            ps.setInt(5, user.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────────────────────────────────
    public boolean deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM utilisateur WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UTILITAIRES
    // ─────────────────────────────────────────────────────────────────────────
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("password"),
                rs.getString("email"),
                Role.fromString(rs.getString("role"))
        );
    }
}