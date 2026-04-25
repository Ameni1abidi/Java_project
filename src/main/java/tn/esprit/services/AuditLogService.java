package tn.esprit.services;

import tn.esprit.utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class AuditLogService {
    private final Connection cnx = MyDatabase.getInstance().getConnection();

    public AuditLogService() {
        ensureTable();
    }

    public void log(String actorEmail, String actionType, String details) {
        String sql = "INSERT INTO audit_log (actor_email, action_type, details) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, actorEmail);
            ps.setString(2, actionType);
            ps.setString(3, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Audit log failed: " + e.getMessage());
        }
    }

    private void ensureTable() {
        String sql = "CREATE TABLE IF NOT EXISTS audit_log (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "actor_email VARCHAR(255), " +
                "action_type VARCHAR(100) NOT NULL, " +
                "details TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try (Statement st = cnx.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.err.println("Audit table init failed: " + e.getMessage());
        }
    }
}
