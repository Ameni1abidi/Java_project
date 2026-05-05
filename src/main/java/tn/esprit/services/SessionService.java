package tn.esprit.services;

import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Persistent sessions (multi-device).
 * - create session on login (classic/OAuth)
 * - validate on app screens (expiration / revoked)
 * - list active sessions
 * - revoke current / revoke all
 */
public final class SessionService {
    public static final Duration TTL_DEFAULT = Duration.ofHours(8);
    public static final Duration TTL_REMEMBER_ME = Duration.ofDays(30);

    private final Connection cnx = MyDatabase.getInstance().getConnection();

    public SessionService() {
        ensureTable();
    }

    public record SessionInfo(
            String token,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            LocalDateTime lastSeenAt,
            String ip,
            String fingerprint,
            boolean revoked
    ) {}

    public String createSession(int userId, String ip, String fingerprint, boolean rememberMe) throws SQLException {
        if (userId <= 0) throw new IllegalArgumentException("userId invalide");
        String token = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime exp = now.plus(rememberMe ? TTL_REMEMBER_ME : TTL_DEFAULT);

        String sql = """
                INSERT INTO user_session (user_id, token, created_at, expires_at, last_seen_at, ip, fingerprint)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, token);
            ps.setTimestamp(3, Timestamp.valueOf(now));
            ps.setTimestamp(4, Timestamp.valueOf(exp));
            ps.setTimestamp(5, Timestamp.valueOf(now));
            ps.setString(6, safe(ip));
            ps.setString(7, safe(fingerprint));
            ps.executeUpdate();
        }
        return token;
    }

    public boolean isValid(String token) throws SQLException {
        if (token == null || token.isBlank()) return false;
        String sql = """
                SELECT expires_at, revoked_at
                FROM user_session
                WHERE token = ?
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, token.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                Timestamp exp = rs.getTimestamp("expires_at");
                Timestamp rev = rs.getTimestamp("revoked_at");
                if (rev != null) return false;
                if (exp != null && exp.toLocalDateTime().isBefore(LocalDateTime.now())) return false;
                return true;
            }
        }
    }

    public void touch(String token) throws SQLException {
        if (token == null || token.isBlank()) return;
        String sql = "UPDATE user_session SET last_seen_at = ? WHERE token = ? AND revoked_at IS NULL";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(2, token.trim());
            ps.executeUpdate();
        }
    }

    public List<SessionInfo> listActiveSessions(int userId) throws SQLException {
        List<SessionInfo> out = new ArrayList<>();
        if (userId <= 0) return out;
        String sql = """
                SELECT token, created_at, expires_at, last_seen_at, ip, fingerprint, revoked_at
                FROM user_session
                WHERE user_id = ?
                  AND (revoked_at IS NULL)
                  AND (expires_at IS NULL OR expires_at >= ?)
                ORDER BY last_seen_at DESC, created_at DESC
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new SessionInfo(
                            rs.getString("token"),
                            toLdt(rs.getTimestamp("created_at")),
                            toLdt(rs.getTimestamp("expires_at")),
                            toLdt(rs.getTimestamp("last_seen_at")),
                            rs.getString("ip"),
                            rs.getString("fingerprint"),
                            rs.getTimestamp("revoked_at") != null
                    ));
                }
            }
        }
        return out;
    }

    public void revoke(String token) throws SQLException {
        if (token == null || token.isBlank()) return;
        String sql = "UPDATE user_session SET revoked_at = ? WHERE token = ? AND revoked_at IS NULL";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(2, token.trim());
            ps.executeUpdate();
        }
    }

    public void revokeAllForUser(int userId) throws SQLException {
        if (userId <= 0) return;
        String sql = "UPDATE user_session SET revoked_at = ? WHERE user_id = ? AND revoked_at IS NULL";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    private void ensureTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS user_session (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  user_id INT NOT NULL,
                  token VARCHAR(64) NOT NULL,
                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  expires_at TIMESTAMP NULL,
                  last_seen_at TIMESTAMP NULL,
                  revoked_at TIMESTAMP NULL,
                  ip VARCHAR(64) NULL,
                  fingerprint VARCHAR(255) NULL,
                  UNIQUE KEY uq_user_session_token (token),
                  INDEX idx_user_session_user (user_id),
                  INDEX idx_user_session_exp (expires_at),
                  INDEX idx_user_session_rev (revoked_at)
                )
                """;
        try (Statement st = cnx.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.err.println("user_session init failed: " + e.getMessage());
        }
    }

    private static LocalDateTime toLdt(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }

    private static String safe(String s) {
        if (s == null) return null;
        String v = s.replace("\n", " ").replace("\r", " ").trim();
        return v.isBlank() ? null : v;
    }
}

