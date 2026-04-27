package tn.esprit.services;

import tn.esprit.utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
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

    public List<ResourceEngagement> getTopResourcesByScore(int limit) {
        return getEngagementByResource().stream()
                .sorted((a, b) -> Integer.compare(b.score(), a.score()))
                .limit(limit)
                .toList();
    }

    public Map<String, Integer> getResourcesByCategory() {
        String sql = """
                SELECT COALESCE(NULLIF(type, ''), NULLIF(categorie_nom, ''), 'Non classe') AS label,
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

    public List<DailyInteraction> getEvolutionLast30Days() {
        String sql = """
                SELECT day,
                       SUM(views) AS views,
                       SUM(likes) AS likes,
                       SUM(favorites) AS favorites
                FROM (
                    SELECT DATE(created_at) AS day,
                           SUM(CASE WHEN interaction_type = 'VIEW' THEN 1 ELSE 0 END) AS views,
                           SUM(CASE WHEN interaction_type = 'LIKE' THEN 1 ELSE 0 END) AS likes,
                           0 AS favorites
                    FROM ressource_interaction
                    WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 29 DAY)
                    GROUP BY DATE(created_at)
                    UNION ALL
                    SELECT DATE(created_at) AS day,
                           0 AS views,
                           0 AS likes,
                           COUNT(*) AS favorites
                    FROM ressource_favori
                    WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 29 DAY)
                    GROUP BY DATE(created_at)
                ) t
                GROUP BY day
                ORDER BY day
                """;
        Map<LocalDate, DailyInteraction> byDay = new LinkedHashMap<>();
        LocalDate start = LocalDate.now().minusDays(29);
        for (int i = 0; i < 30; i++) {
            LocalDate day = start.plusDays(i);
            byDay.put(day, new DailyInteraction(day, 0, 0, 0));
        }
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LocalDate day = rs.getDate("day").toLocalDate();
                byDay.put(day, new DailyInteraction(
                        day,
                        rs.getInt("views"),
                        rs.getInt("likes"),
                        rs.getInt("favorites")
                ));
            }
        } catch (SQLException e) {
            return new ArrayList<>(byDay.values());
        }
        return new ArrayList<>(byDay.values());
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
        public int score() {
            return views + (likes * 3) + (favorites * 2);
        }
    }

    public record DailyInteraction(LocalDate day, int views, int likes, int favorites) {
    }
}
