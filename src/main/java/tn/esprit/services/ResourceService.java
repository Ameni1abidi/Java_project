package tn.esprit.services;

import tn.esprit.entities.resources;
import tn.esprit.utils.MyDatabase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceService {
    private final Connection connection = MyDatabase.getInstance().getConnection();

    public ResourceService() {
        ensureSchema();
    }

    private void ensureSchema() {
        try {
            if (!hasColumn("categorie_nom") && hasColumn("categorie_id")) {
                try {
                    for (String fkName : getForeignKeyNamesOnColumn("ressource", "categorie_id")) {
                        try {
                            executeUpdate("ALTER TABLE ressource DROP FOREIGN KEY " + fkName);
                        } catch (SQLException ignored) {
                        }
                    }
                    executeUpdate("ALTER TABLE ressource CHANGE categorie_id categorie_nom VARCHAR(255)");
                } catch (SQLException ignored) {
                }
            }

            if (hasColumn("categorie_nom") && !hasForeignKey("fk_ressource_categorie")) {
                try {
                    executeUpdate("ALTER TABLE ressource ADD CONSTRAINT fk_ressource_categorie FOREIGN KEY (categorie_nom) REFERENCES categorie(nom)");
                } catch (SQLException ignored) {
                }
            }

            if (!hasColumn("type")) {
                try {
                    executeUpdate("ALTER TABLE ressource ADD COLUMN type VARCHAR(50)");
                } catch (SQLException ignored) {
                }
            }

            if (!hasColumn("disponible_le")) {
                try {
                    executeUpdate("ALTER TABLE ressource ADD COLUMN disponible_le VARCHAR(100)");
                } catch (SQLException ignored) {
                }
            }

            if (!hasColumn("chapitre_id")) {
                try {
                    executeUpdate("ALTER TABLE ressource ADD COLUMN chapitre_id INT NULL");
                } catch (SQLException ignored) {
                }
            }

            if (!hasColumn("is_sensitive")) {
                try {
                    executeUpdate("ALTER TABLE ressource ADD COLUMN is_sensitive TINYINT(1) NOT NULL DEFAULT 0");
                } catch (SQLException ignored) {
                }
            }

            if (hasColumn("chapitre_id") && !hasForeignKey("fk_ressource_chapitre")) {
                try {
                    executeUpdate("ALTER TABLE ressource ADD CONSTRAINT fk_ressource_chapitre FOREIGN KEY (chapitre_id) REFERENCES chapitre(id) ON DELETE SET NULL ON UPDATE CASCADE");
                } catch (SQLException ignored) {
                }
            }

            executeUpdate("CREATE TABLE IF NOT EXISTS ressource_favori (" +
                    "user_id INT NOT NULL, " +
                    "ressource_id INT NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "PRIMARY KEY (user_id, ressource_id), " +
                    "CONSTRAINT fk_fav_ressource FOREIGN KEY (ressource_id) REFERENCES ressource(id) ON DELETE CASCADE" +
                    ")");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'initialisation du schema des ressources", e);
        }
    }

    private boolean hasForeignKey(String constraintName) throws SQLException {
        String sql = "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS " +
                "WHERE TABLE_NAME = 'ressource' AND CONSTRAINT_TYPE = 'FOREIGN KEY' AND CONSTRAINT_NAME = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, constraintName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private List<String> getForeignKeyNamesOnColumn(String tableName, String columnName) throws SQLException {
        List<String> fkNames = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getImportedKeys(connection.getCatalog(), null, tableName)) {
            while (rs.next()) {
                String fkColumn = rs.getString("FKCOLUMN_NAME");
                String fkName = rs.getString("FK_NAME");
                if (columnName.equalsIgnoreCase(fkColumn) && fkName != null && !fkName.isBlank()) {
                    fkNames.add(fkName);
                }
            }
        }
        return fkNames;
    }

    private boolean hasColumn(String columnName) throws SQLException {
        String sql = "SHOW COLUMNS FROM ressource LIKE ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void executeUpdate(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    public void add(resources r) {
        String sql = "INSERT INTO ressource(titre, contenu, categorie_nom, type, disponible_le, chapitre_id, is_sensitive) VALUES(?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, r.getTitre());
            ps.setString(2, r.getContenu());
            ps.setString(3, r.getCategorieNom());
            ps.setString(4, r.getType());
            ps.setString(5, r.getDisponibleLe());
            if (r.getChapitreId() > 0) {
                ps.setInt(6, r.getChapitreId());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            ps.setBoolean(7, r.isSensitive());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<resources> getAll() {
        String sql = "SELECT id, titre, contenu, categorie_nom, type, disponible_le, chapitre_id, is_sensitive FROM ressource";

        List<resources> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    public resources getById(int id) {
        String sql = "SELECT id, titre, contenu, categorie_nom, type, disponible_le, chapitre_id, is_sensitive FROM ressource WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public boolean update(resources r) {
        String sql = "UPDATE ressource SET titre=?, contenu=?, categorie_nom=?, type=?, disponible_le=?, chapitre_id=?, is_sensitive=? WHERE id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, r.getTitre());
            ps.setString(2, r.getContenu());
            ps.setString(3, r.getCategorieNom());
            ps.setString(4, r.getType());
            ps.setString(5, r.getDisponibleLe());
            if (r.getChapitreId() > 0) {
                ps.setInt(6, r.getChapitreId());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            ps.setBoolean(7, r.isSensitive());
            ps.setInt(8, r.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM ressource WHERE id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<resources> getByCategoryNom(String categorieNom) {
        String sql = "SELECT id, titre, contenu, categorie_nom, type, disponible_le, chapitre_id, is_sensitive FROM ressource WHERE categorie_nom = ?";
        List<resources> resourceList = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, categorieNom);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resourceList.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recuperation des ressources par categorie", e);
        }

        return resourceList;
    }

    public List<resources> getByChapitreId(int chapitreId) {
        String sql = "SELECT id, titre, contenu, categorie_nom, type, disponible_le, chapitre_id, is_sensitive FROM ressource WHERE chapitre_id = ? ORDER BY id DESC";
        List<resources> resourceList = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, chapitreId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resourceList.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recuperation des ressources par chapitre", e);
        }

        return resourceList;
    }

    public List<resources> search(String keyword) {
        String sql = "SELECT id, titre, contenu, categorie_nom, type, disponible_le, chapitre_id, is_sensitive FROM ressource WHERE titre LIKE ? OR contenu LIKE ?";

        List<resources> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            ps.setString(1, k);
            ps.setString(2, k);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de ressources", e);
        }

        return list;
    }

    public boolean isDisponible(resources resource) {
        if (resource == null || resource.getDisponibleLe() == null || resource.getDisponibleLe().isBlank()) {
            return false;
        }
        try {
            LocalDate dateDispo = LocalDate.parse(resource.getDisponibleLe());
            return !LocalDate.now().isBefore(dateDispo);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isFavorite(int userId, int resourceId) {
        if (userId <= 0) {
            return false;
        }
        ensureFavoriteTableSafe();
        String sql = "SELECT 1 FROM ressource_favori WHERE user_id = ? AND ressource_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public void setFavorite(int userId, int resourceId, boolean favorite) {
        if (userId <= 0) {
            return;
        }
        ensureFavoriteTableSafe();
        String sqlInsert = "INSERT IGNORE INTO ressource_favori(user_id, ressource_id) VALUES(?, ?)";
        String sqlDelete = "DELETE FROM ressource_favori WHERE user_id = ? AND ressource_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(favorite ? sqlInsert : sqlDelete)) {
            ps.setInt(1, userId);
            ps.setInt(2, resourceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            // No crash in UI if favoris table is not ready; resources page must stay usable.
        }
    }

    public List<resources> getFavoritesByUserId(int userId) {
        if (userId <= 0) {
            return new ArrayList<>();
        }
        ensureFavoriteTableSafe();
        String sql = "SELECT r.id, r.titre, r.contenu, r.categorie_nom, r.type, r.disponible_le, r.chapitre_id, r.is_sensitive " +
                "FROM ressource r INNER JOIN ressource_favori rf ON r.id = rf.ressource_id " +
                "WHERE rf.user_id = ? ORDER BY rf.created_at DESC";
        List<resources> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recuperation des favoris", e);
        }
        return list;
    }

    public Map<Integer, String> getChapitreTitles() {
        Map<Integer, String> chapitreTitles = new HashMap<>();
        String sql = "SELECT id, titre FROM chapitre";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                chapitreTitles.put(rs.getInt("id"), rs.getString("titre"));
            }
        } catch (SQLException ignored) {
        }
        return chapitreTitles;
    }

    private void ensureFavoriteTableSafe() {
        try {
            executeUpdate("CREATE TABLE IF NOT EXISTS ressource_favori (" +
                    "user_id INT NOT NULL, " +
                    "ressource_id INT NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "PRIMARY KEY (user_id, ressource_id), " +
                    "CONSTRAINT fk_fav_ressource FOREIGN KEY (ressource_id) REFERENCES ressource(id) ON DELETE CASCADE" +
                    ")");
        } catch (SQLException ignored) {
            // Keep app running even if migration cannot be applied now.
        }
    }

    private resources mapRow(ResultSet rs) throws SQLException {
        return new resources(
                rs.getInt("id"),
                rs.getString("titre"),
                rs.getString("contenu"),
                rs.getString("categorie_nom"),
                rs.getString("type"),
                rs.getString("disponible_le"),
                rs.getInt("chapitre_id"),
                rs.getBoolean("is_sensitive")
        );
    }
}
