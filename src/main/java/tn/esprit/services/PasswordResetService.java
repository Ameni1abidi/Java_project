package tn.esprit.services;

import tn.esprit.utils.MyDatabase;

import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDateTime;

public final class PasswordResetService {
    private final Connection cnx = MyDatabase.getInstance().getConnection();
    private final EmailService emailService = new EmailService();

    public PasswordResetService() {
        ensureTable();
    }

    public void sendResetCode(String email) throws Exception {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email manquant");
        String code = generateCode6();
        upsert(email.trim(), code, LocalDateTime.now().plusMinutes(15));

        String body = """
                Bonjour,
                
                Voici votre code de réinitialisation du mot de passe EduFlex:
                
                %s
                
                Ce code expire dans 15 minutes.
                Si vous n'avez pas demandé de réinitialisation, ignorez cet email.
                """.formatted(code).trim();

        emailService.sendTextEmail(email.trim(), "EduFlex — Réinitialisation du mot de passe", body);
    }

    public boolean verifyCode(String email, String code) throws SQLException {
        if (email == null || email.isBlank() || code == null || code.isBlank()) return false;
        String sql = "SELECT code, expires_at, consumed_at FROM password_reset_token WHERE email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return false;
            Timestamp exp = rs.getTimestamp("expires_at");
            Timestamp cons = rs.getTimestamp("consumed_at");
            if (cons != null) return false;
            if (exp != null && exp.toLocalDateTime().isBefore(LocalDateTime.now())) return false;
            String stored = rs.getString("code");
            return stored != null && stored.trim().equals(code.trim());
        }
    }

    public void consume(String email) throws SQLException {
        String sql = "UPDATE password_reset_token SET consumed_at = ? WHERE email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(2, email.trim());
            ps.executeUpdate();
        }
    }

    private void ensureTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS password_reset_token (
                  email VARCHAR(255) PRIMARY KEY,
                  code VARCHAR(32) NOT NULL,
                  expires_at TIMESTAMP NULL,
                  consumed_at TIMESTAMP NULL
                )
                """;
        try (Statement st = cnx.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.err.println("password_reset_token init failed: " + e.getMessage());
        }
    }

    private void upsert(String email, String code, LocalDateTime expiresAt) throws SQLException {
        String update = "UPDATE password_reset_token SET code=?, expires_at=?, consumed_at=NULL WHERE email=?";
        try (PreparedStatement ps = cnx.prepareStatement(update)) {
            ps.setString(1, code);
            ps.setTimestamp(2, Timestamp.valueOf(expiresAt));
            ps.setString(3, email);
            int n = ps.executeUpdate();
            if (n > 0) return;
        }
        String insert = "INSERT INTO password_reset_token (email, code, expires_at) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(insert)) {
            ps.setString(1, email);
            ps.setString(2, code);
            ps.setTimestamp(3, Timestamp.valueOf(expiresAt));
            ps.executeUpdate();
        }
    }

    private static String generateCode6() {
        int n = new SecureRandom().nextInt(900_000) + 100_000;
        return String.valueOf(n);
    }
}

