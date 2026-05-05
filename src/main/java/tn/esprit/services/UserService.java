package tn.esprit.services;

import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.entities.User;
import tn.esprit.entities.User.Role;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserService {

    private final Connection cnx = MyDatabase.getInstance().getConnection();
    private volatile Boolean cachedHasTelephoneColumn;
    private final LoginSecurityService loginSecurityService = new LoginSecurityService();
    private final AuditLogService auditLogService = new AuditLogService();
    private final EmailService emailService = new EmailService();

    // ─────────────────────────────────────────────────────────────────────────
    //  REGISTER
    // ─────────────────────────────────────────────────────────────────────────
    public boolean register(User user) throws SQLException {
        if (emailExists(user.getEmail())) return false;

        boolean hasTel = hasTelephoneColumn();
        String sql = hasTel
                ? "INSERT INTO utilisateur (nom, password, email, telephone, role, is_verified, is_blocked, status) VALUES (?, ?, ?, ?, ?, 0, 0, 'PENDING')"
                : "INSERT INTO utilisateur (nom, password, email, role, is_verified, is_blocked, status) VALUES (?, ?, ?, ?, 0, 0, 'PENDING')";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            ps.setString(1, user.getNom());
            ps.setString(2, hashedPassword);
            ps.setString(3, user.getEmail());
            if (hasTel) {
                String tel = user.getTelephone();
                ps.setString(4, (tel == null || tel.isBlank()) ? null : tel.trim());
                ps.setString(5, user.getRole().name());
            } else {
                ps.setString(4, user.getRole().name());
            }
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
        return login(email, password, null);
    }

    public Optional<User> login(String email, String password, LoginSecurityService.LoginContext ctx) throws SQLException {
        loginSecurityService.enforceNotLocked(email);
        String sql = "SELECT * FROM utilisateur WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Optional.empty();

            User user = mapRow(rs);
            enforceLoginAllowed(user);
            String stored = user.getPassword() == null ? "" : user.getPassword().trim();
            String input = password == null ? "" : password.trim();

            // Supports migrated bcrypt and legacy plain passwords.
            boolean valid;
            if (isBcryptHash(stored)) {
                try {
                    valid = BCrypt.checkpw(input, stored);
                } catch (IllegalArgumentException ex) {
                    // Some rows may contain malformed/partial bcrypt strings; fallback to legacy plain match.
                    valid = stored.equals(input);
                }
            } else {
                valid = stored.equals(input);
            }
            if (!valid) {
                try {
                    loginSecurityService.recordFailure(email, ctx);
                } catch (SQLException ignored) {
                }
                return Optional.empty();
            }

            // Auto-migrate legacy plain text password after successful login.
            if (!isBcryptHash(stored)) {
                updatePasswordHash(user.getId(), BCrypt.hashpw(input, BCrypt.gensalt()));
            }
            markLastLoginNowIfPossible(user.getId());

            boolean suspicious = false;
            try {
                suspicious = loginSecurityService.recordSuccessAndDetectSuspicious(email, ctx);
            } catch (SQLException ignored) {
            }
            if (suspicious) {
                String ip = ctx == null ? "" : String.valueOf(ctx.ip());
                auditLogService.log(user.getEmail(), "LOGIN_SUSPICIOUS", "New device detected. ip=" + ip);
                try {
                    String body = """
                            Alerte sécurité EduFlex
                            
                            Une connexion à votre compte a été détectée depuis un nouvel appareil.
                            
                            Email: %s
                            IP (local): %s
                            
                            Si ce n'est pas vous, changez votre mot de passe immédiatement.
                            """.formatted(user.getEmail(), ip).trim();
                    emailService.sendTextEmail(user.getEmail(), "EduFlex — Alerte connexion suspecte", body);
                } catch (Exception ignored) {
                }
            }
            return Optional.of(user);
        }
    }

    public void onExternalLoginSuccess(User user, LoginSecurityService.LoginContext ctx) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) return;
        try {
            boolean suspicious = loginSecurityService.recordSuccessAndDetectSuspicious(user.getEmail(), ctx);
            if (suspicious) {
                String ip = ctx == null ? "" : String.valueOf(ctx.ip());
                auditLogService.log(user.getEmail(), "LOGIN_SUSPICIOUS", "New device detected (external login). ip=" + ip);
                try {
                    String body = """
                            Alerte sécurité EduFlex
                            
                            Une connexion à votre compte a été détectée depuis un nouvel appareil (Google/GitHub).
                            
                            Email: %s
                            IP (local): %s
                            
                            Si ce n'est pas vous, changez votre mot de passe immédiatement.
                            """.formatted(user.getEmail(), ip).trim();
                    emailService.sendTextEmail(user.getEmail(), "EduFlex — Alerte connexion suspecte", body);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
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

    public Optional<User> getFirstByRole(Role role) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE role = ? ORDER BY id ASC LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
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
        User u = new User(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("password"),
                rs.getString("email"),
                Role.fromString(rs.getString("role"))
        );
        if (hasColumn(rs, "is_verified")) u.setVerified(rs.getBoolean("is_verified"));
        if (hasColumn(rs, "is_blocked")) u.setBlocked(rs.getBoolean("is_blocked"));
        if (hasColumn(rs, "status")) u.setStatus(rs.getString("status"));
        if (hasColumn(rs, "created_at")) {
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) u.setCreatedAt(ts.toLocalDateTime());
        }
        if (hasColumn(rs, "last_login_at")) {
            Timestamp ts = rs.getTimestamp("last_login_at");
            if (ts != null) u.setLastLoginAt(ts.toLocalDateTime());
        } else if (hasColumn(rs, "last_login")) {
            Timestamp ts = rs.getTimestamp("last_login");
            if (ts != null) u.setLastLoginAt(ts.toLocalDateTime());
        }
        if (hasColumn(rs, "telephone")) {
            u.setTelephone(rs.getString("telephone"));
        }
        return u;
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

    public User findOrCreateGithubUser(String email, String displayName) throws SQLException {
        Optional<User> existing = findByEmail(email);
        if (existing.isPresent()) {
            return existing.get();
        }

        String name = (displayName == null || displayName.isBlank()) ? "GitHub User" : displayName.trim();
        String generatedPassword = "github-" + UUID.randomUUID();
        register(new User(name, generatedPassword, email, Role.ROLE_ETUDIANT));
        return findByEmail(email).orElseThrow(() ->
                new SQLException("Creation du compte GitHub echouee pour " + email));
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

    public void setBlocked(int userId, boolean blocked) throws SQLException {
        String sql = "UPDATE utilisateur SET is_blocked = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setBoolean(1, blocked);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
        if (blocked) {
            setStatus(userId, "Blocked");
        }
    }

    public void setStatus(int userId, String status) throws SQLException {
        String sql = "UPDATE utilisateur SET status = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
        if ("Blocked".equalsIgnoreCase(status)) {
            try {
                String sql2 = "UPDATE utilisateur SET is_blocked = 1 WHERE id = ?";
                try (PreparedStatement ps2 = cnx.prepareStatement(sql2)) {
                    ps2.setInt(1, userId);
                    ps2.executeUpdate();
                }
            } catch (SQLException ignored) {}
        }
        if ("Active".equalsIgnoreCase(status)) {
            try {
                String sql2 = "UPDATE utilisateur SET is_blocked = 0 WHERE id = ?";
                try (PreparedStatement ps2 = cnx.prepareStatement(sql2)) {
                    ps2.setInt(1, userId);
                    ps2.executeUpdate();
                }
            } catch (SQLException ignored) {}
        }
    }

    private void enforceLoginAllowed(User user) {
        if (user == null) return;
        if (user.isBlocked()) {
            throw new IllegalStateException("Compte bloque par un administrateur.");
        }
        if (!user.isVerified()) {
            throw new IllegalStateException("Compte non verifie. Verifiez votre email.");
        }
        String status = user.getStatus() == null ? "" : user.getStatus().trim();
        if (!status.isBlank()) {
            String s = status.toLowerCase();
            if (s.contains("block")) throw new IllegalStateException("Compte bloque.");
            if (s.contains("archiv")) throw new IllegalStateException("Compte archive.");
            if (s.contains("deactiv") || s.contains("inactiv")) throw new IllegalStateException("Compte desactive.");
            if (s.contains("pending")) throw new IllegalStateException("Compte en attente de validation.");
        }
    }

    public void markUserVerified(String email) throws SQLException {
        String sql = "UPDATE utilisateur SET is_verified = 1, status = 'Active', is_blocked = 0 WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
    }

    public void resetPasswordByEmail(String email, String newPassword) throws SQLException {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email manquant");
        if (newPassword == null || newPassword.isBlank()) throw new IllegalArgumentException("Nouveau mot de passe manquant");
        String sql = "UPDATE utilisateur SET password = ? WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, BCrypt.hashpw(newPassword.trim(), BCrypt.gensalt()));
            ps.setString(2, email.trim());
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Aucun utilisateur trouvé pour cet email");
            }
        }
    }

    private void markLastLoginNowIfPossible(int userId) {
        // Optional: only works if column exists.
        // We try both names commonly used in schemas.
        try {
            String sql = "UPDATE utilisateur SET last_login_at = ? WHERE id = ?";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(2, userId);
                ps.executeUpdate();
                return;
            }
        } catch (SQLException ignored) {
        }
        try {
            String sql = "UPDATE utilisateur SET last_login = ? WHERE id = ?";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(2, userId);
                ps.executeUpdate();
            }
        } catch (SQLException ignored) {
        }
    }

    private static boolean hasColumn(ResultSet rs, String name) {
        if (rs == null || name == null || name.isBlank()) return false;
        try {
            ResultSetMetaData md = rs.getMetaData();
            int n = md.getColumnCount();
            for (int i = 1; i <= n; i++) {
                String label = md.getColumnLabel(i);
                if (label != null && label.equalsIgnoreCase(name)) return true;
                String col = md.getColumnName(i);
                if (col != null && col.equalsIgnoreCase(name)) return true;
            }
        } catch (SQLException ignored) {
        }
        return false;
    }

    private boolean hasTelephoneColumn() {
        Boolean cached = cachedHasTelephoneColumn;
        if (cached != null) return cached;

        boolean exists = false;
        try {
            DatabaseMetaData md = cnx.getMetaData();
            try (ResultSet rs = md.getColumns(null, null, "utilisateur", "telephone")) {
                exists = rs.next();
            }
        } catch (SQLException ignored) {
            exists = false;
        }
        cachedHasTelephoneColumn = exists;
        return exists;
    }
}
