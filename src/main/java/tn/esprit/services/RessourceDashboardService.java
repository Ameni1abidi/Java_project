package tn.esprit.services;

import tn.esprit.utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RessourceDashboardService {
    private final Connection connection = MyDatabase.getInstance().getConnection();

    public RessourceDashboardService() {
        ensureSchemaSafe();
    }

    public void recordView(int resourceId, int userId) {
        if (resourceId <= 0) {
            return;
        }
        String sql = "INSERT INTO ressource_interaction(ressource_id, user_id, interaction_type) VALUES(?, ?, 'VIEW')";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            if (userId > 0) {
                ps.setInt(2, userId);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    public List<ResourceEngagement> getEngagementByResource() {
        String sql = """
                SELECT r.id,
                       r.titre,
                       COALESCE(v.views, 0) AS views,
                       COALESCE(l.likes, 0) AS likes,
                       COALESCE(f.favorites, 0) AS favorites
                FROM ressource r
                LEFT JOIN (
                    SELECT ressource_id, COUNT(*) AS views
                    FROM ressource_interaction
                    WHERE interaction_type = 'VIEW'
                    GROUP BY ressource_id
                ) v ON v.ressource_id = r.id
                LEFT JOIN (
                    SELECT ressource_id, COUNT(*) AS likes
                    FROM ressource_interaction
                    WHERE interaction_type = 'LIKE'
                    GROUP BY ressource_id
                ) l ON l.ressource_id = r.id
                LEFT JOIN (
                    SELECT ressource_id, COUNT(*) AS favorites
                    FROM ressource_favori
                    GROUP BY ressource_id
                ) f ON f.ressource_id = r.id
                ORDER BY r.id
                """;
        List<ResourceEngagement> stats = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stats.add(new ResourceEngagement(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getInt("views"),
                        rs.getInt("likes"),
                        rs.getInt("favorites")
                ));
            }
        } catch (SQLException e) {
            return new ArrayList<>();
        }
        return stats;
    }

    public List<ResourceEngagement> getTopResourcesByFavorites(int limit) {
        String sql = """
                SELECT r.id,
                       r.titre,
                       COUNT(*) AS favorites
                FROM ressource r
                INNER JOIN ressource_favori f ON f.ressource_id = r.id
                GROUP BY r.id, r.titre
                HAVING favorites > 0
                ORDER BY favorites DESC, r.titre
                LIMIT ?
                """;
        List<ResourceEngagement> stats = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stats.add(new ResourceEngagement(
                            rs.getInt("id"),
                            rs.getString("titre"),
                            0,
                            0,
                            rs.getInt("favorites")
                    ));
                }
            }
        } catch (SQLException ignored) {
        }
        return stats;
    }

    public int getTotalFavorites() {
        String sql = "SELECT COUNT(*) AS total FROM ressource_favori";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException ignored) {
        }
        return 0;
    }

    public Map<String, Integer> getResourcesByCategory() {
        String sql = """
                SELECT COALESCE(NULLIF(categorie_nom, ''), 'Non classe') AS label,
                       COUNT(*) AS total
                FROM ressource
                GROUP BY label
                ORDER BY total DESC, label
                """;
        Map<String, Integer> data = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                data.put(rs.getString("label"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            return data;
        }
        return data;
    }

    private void ensureSchemaSafe() {
        try {
            ensureSchema();
        } catch (SQLException ignored) {
            tryCreateInteractionTableWithoutForeignKey();
        }
    }

    private void ensureSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ressource_favori (" +
                    "user_id INT NOT NULL, " +
                    "ressource_id INT NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "PRIMARY KEY (user_id, ressource_id)" +
                    ")");
            ensureFavoriteCreatedAtColumn();
            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS ressource_interaction (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        ressource_id INT NOT NULL,
                        user_id INT NULL,
                        interaction_type VARCHAR(20) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        INDEX idx_ressource_interaction_resource (ressource_id),
                        INDEX idx_ressource_interaction_type (interaction_type),
                        CONSTRAINT fk_interaction_ressource
                            FOREIGN KEY (ressource_id) REFERENCES ressource(id)
                            ON DELETE CASCADE
                    )
                    """);
        }
    }

    private void ensureFavoriteCreatedAtColumn() {
        try (PreparedStatement ps = connection.prepareStatement("SHOW COLUMNS FROM ressource_favori LIKE 'created_at'");
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE ressource_favori ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                }
            }
        } catch (SQLException ignored) {
        }
    }

    private void tryCreateInteractionTableWithoutForeignKey() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS ressource_interaction (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        ressource_id INT NOT NULL,
                        user_id INT NULL,
                        interaction_type VARCHAR(20) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        INDEX idx_ressource_interaction_resource (ressource_id),
                        INDEX idx_ressource_interaction_type (interaction_type)
                    )
                    """);
        } catch (SQLException ignored) {
        }
    }

    public record ResourceEngagement(int id, String title, int views, int likes, int favorites) {
    }
}
