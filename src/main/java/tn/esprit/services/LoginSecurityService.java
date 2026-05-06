package tn.esprit.services;

import tn.esprit.utils.MyDatabase;

import java.net.InetAddress;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Login security rules:
 * - rate limit by counting recent failures
 * - lock account temporarily after N failures
 * - store last successful "device fingerprint" and IP for suspicious-login detection
 *
 * Persistence is done in a dedicated table so we don't depend on the utilisateur schema.
 */
public final class LoginSecurityService {
    public static final int DEFAULT_MAX_ATTEMPTS = 5;
    public static final Duration DEFAULT_FAILURE_WINDOW = Duration.ofMinutes(15);
    public static final Duration DEFAULT_LOCK_DURATION = Duration.ofMinutes(10);

    private final Connection cnx = MyDatabase.getInstance().getConnection();

    public LoginSecurityService() {
        ensureTable();
    }

    public record LoginContext(String ip, String fingerprint) {}

    public record LockInfo(LocalDateTime lockedUntil) {
        public long secondsRemaining() {
            if (lockedUntil == null) return 0;
            long sec = Duration.between(LocalDateTime.now(), lockedUntil).getSeconds();
            return Math.max(0, sec);
        }
    }

    public Optional<LockInfo> getActiveLock(String email) throws SQLException {
        if (email == null || email.isBlank()) return Optional.empty();
        String sql = "SELECT locked_until FROM login_security WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Timestamp ts = rs.getTimestamp("locked_until");
                if (ts == null) return Optional.empty();
                LocalDateTime until = ts.toLocalDateTime();
                if (until.isAfter(LocalDateTime.now())) {
                    return Optional.of(new LockInfo(until));
                }
                return Optional.empty();
            }
        }
    }

    public void enforceNotLocked(String email) throws SQLException {
        Optional<LockInfo> lock = getActiveLock(email);
        if (lock.isPresent()) {
            long sec = lock.get().secondsRemaining();
            long min = (long) Math.ceil(sec / 60.0);
            throw new IllegalStateException("Compte temporairement verrouillé. Réessayez dans ~" + min + " minute(s).");
        }
    }

    /**
     * Records a failed login attempt and locks the account if needed.
     */
    public void recordFailure(String email, LoginContext ctx) throws SQLException {
        if (email == null || email.isBlank()) return;
        String normalized = email.trim();
        Row row = getOrCreateRow(normalized);

        LocalDateTime now = LocalDateTime.now();
        int failed = row.failedCount;
        if (row.lastFailedAt == null || row.lastFailedAt.isBefore(now.minus(DEFAULT_FAILURE_WINDOW))) {
            failed = 0;
        }
        failed++;

        LocalDateTime lockedUntil = null;
        int storedFailed = failed;
        if (failed >= DEFAULT_MAX_ATTEMPTS) {
            lockedUntil = now.plus(DEFAULT_LOCK_DURATION);
            storedFailed = 0; // reset counter on lock to avoid permanent growth
        }

        String sql = """
                UPDATE login_security
                SET failed_count = ?, last_failed_at = ?, locked_until = ?,
                    last_ip = ?, last_fingerprint = ?
                WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, storedFailed);
            ps.setTimestamp(2, Timestamp.valueOf(now));
            ps.setTimestamp(3, lockedUntil == null ? null : Timestamp.valueOf(lockedUntil));
            ps.setString(4, safe(ctx == null ? null : ctx.ip()));
            ps.setString(5, safe(ctx == null ? null : ctx.fingerprint()));
            ps.setString(6, normalized);
            ps.executeUpdate();
        }
    }

    /**
     * Records a successful login. Returns true when it looks suspicious
     * (fingerprint changed compared to last success).
     */
    public boolean recordSuccessAndDetectSuspicious(String email, LoginContext ctx) throws SQLException {
        if (email == null || email.isBlank()) return false;
        String normalized = email.trim();
        Row row = getOrCreateRow(normalized);

        String newFp = safe(ctx == null ? null : ctx.fingerprint());
        boolean suspicious = row.lastSuccessFingerprint != null
                && !row.lastSuccessFingerprint.isBlank()
                && newFp != null
                && !newFp.isBlank()
                && !row.lastSuccessFingerprint.equals(newFp);

        String sql = """
                UPDATE login_security
                SET failed_count = 0,
                    last_failed_at = NULL,
                    locked_until = NULL,
                    last_success_at = ?,
                    last_success_fingerprint = ?,
                    last_success_ip = ?
                WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(2, newFp);
            ps.setString(3, safe(ctx == null ? null : ctx.ip()));
            ps.setString(4, normalized);
            ps.executeUpdate();
        }
        return suspicious;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static final class Row {
        final String email;
        final int failedCount;
        final LocalDateTime lastFailedAt;
        final LocalDateTime lockedUntil;
        final String lastSuccessFingerprint;

        private Row(String email, int failedCount, LocalDateTime lastFailedAt, LocalDateTime lockedUntil, String lastSuccessFingerprint) {
            this.email = email;
            this.failedCount = failedCount;
            this.lastFailedAt = lastFailedAt;
            this.lockedUntil = lockedUntil;
            this.lastSuccessFingerprint = lastSuccessFingerprint;
        }
    }

    private Row getOrCreateRow(String email) throws SQLException {
        ensureRow(email);
        String sql = """
                SELECT failed_count, last_failed_at, locked_until, last_success_fingerprint
                FROM login_security
                WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return new Row(email, 0, null, null, null);
                }
                int failed = rs.getInt("failed_count");
                Timestamp lf = rs.getTimestamp("last_failed_at");
                Timestamp lu = rs.getTimestamp("locked_until");
                String fp = rs.getString("last_success_fingerprint");
                return new Row(
                        email,
                        failed,
                        lf == null ? null : lf.toLocalDateTime(),
                        lu == null ? null : lu.toLocalDateTime(),
                        fp
                );
            }
        }
    }

    private void ensureRow(String email) throws SQLException {
        String sql = "INSERT IGNORE INTO login_security (email) VALUES (?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
    }

    private void ensureTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS login_security (
                  email VARCHAR(255) PRIMARY KEY,
                  failed_count INT NOT NULL DEFAULT 0,
                  last_failed_at TIMESTAMP NULL,
                  locked_until TIMESTAMP NULL,
                  last_success_at TIMESTAMP NULL,
                  last_success_ip VARCHAR(64) NULL,
                  last_success_fingerprint VARCHAR(255) NULL,
                  last_ip VARCHAR(64) NULL,
                  last_fingerprint VARCHAR(255) NULL
                )
                """;
        try (Statement st = cnx.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.err.println("login_security init failed: " + e.getMessage());
        }
    }

    private static String safe(String s) {
        if (s == null) return null;
        String v = s.replace("\n", " ").replace("\r", " ").trim();
        return v.isBlank() ? null : v;
    }

    public static String bestEffortLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
