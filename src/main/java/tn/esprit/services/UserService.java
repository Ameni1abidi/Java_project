package tn.esprit.services;

import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.entities.User;
import tn.esprit.entities.User.Role;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            ps.setString(1, user.getNom());
            ps.setString(2, hashedPassword);
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
        String sql = "SELECT * FROM utilisateur WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Optional.empty();

            User user = mapRow(rs);
            String stored = user.getPassword() == null ? "" : user.getPassword().trim();
            String input = password == null ? "" : password.trim();

            // Supports migrated bcrypt and legacy plain passwords.
            boolean valid = isBcryptHash(stored) ? BCrypt.checkpw(input, stored) : stored.equals(input);
            if (!valid) return Optional.empty();

            // Auto-migrate legacy plain text password after successful login.
            if (!isBcryptHash(stored)) {
                updatePasswordHash(user.getId(), BCrypt.hashpw(input, BCrypt.gensalt()));
            }
            return Optional.of(user);
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
            String passwordValue = user.getPassword();
            if (passwordValue != null && !passwordValue.isBlank() && !isBcryptHash(passwordValue)) {
                passwordValue = BCrypt.hashpw(passwordValue, BCrypt.gensalt());
            }
            ps.setString(1, user.getNom());
            ps.setString(2, passwordValue);
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

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
            return Optional.empty();
        }
    }

    public User findOrCreateGoogleUser(String email, String displayName) throws SQLException {
        Optional<User> existing = findByEmail(email);
        if (existing.isPresent()) {
            return existing.get();
        }

        String name = (displayName == null || displayName.isBlank()) ? "Google User" : displayName.trim();
        String generatedPassword = "google-" + UUID.randomUUID();
        register(new User(name, generatedPassword, email, Role.ROLE_ETUDIANT));
        return findByEmail(email).orElseThrow(() ->
                new SQLException("Creation du compte Google echouee pour " + email));
    }

    private boolean isBcryptHash(String value) {
        return value != null && value.startsWith("$2a$")
                || value != null && value.startsWith("$2b$")
                || value != null && value.startsWith("$2y$");
    }

    private void updatePasswordHash(int userId, String hash) throws SQLException {
        String sql = "UPDATE utilisateur SET password = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }
}