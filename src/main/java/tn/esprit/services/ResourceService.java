package tn.esprit.services;

import tn.esprit.entities.resources;
import tn.esprit.utils.MyDatabase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ResourceService {
    private final Connection connection = MyDatabase.getInstance().getConnection();

    public ResourceService() {
        ensureSchema();
    }

    private void ensureSchema() {
        try {
            if (!hasColumn("categorie_nom") && hasColumn("categorie_id")) {
                // Drop any foreign key constraints on categorie_id before renaming the column
                try {
                    for (String fkName : getForeignKeyNamesOnColumn("ressource", "categorie_id")) {
                        try {
                            executeUpdate("ALTER TABLE ressource DROP FOREIGN KEY " + fkName);
                        } catch (SQLException ignored) {
                            // FK might not exist
                        }
                    }
                    executeUpdate("ALTER TABLE ressource CHANGE categorie_id categorie_nom VARCHAR(255)");
                } catch (SQLException ignored) {
                    // Column might already be renamed or not exist
                }
            }
            
            // Ensure foreign key constraint
            if (hasColumn("categorie_nom") && !hasForeignKey("fk_ressource_categorie")) {
                try {
                    executeUpdate("ALTER TABLE ressource ADD CONSTRAINT fk_ressource_categorie FOREIGN KEY (categorie_nom) REFERENCES categorie(nom)");
                } catch (SQLException ignored) {
                    // FK constraint might already exist
                }
            }
            
            if (!hasColumn("type")) {
                try {
                    executeUpdate("ALTER TABLE ressource ADD COLUMN type VARCHAR(50)");
                } catch (SQLException ignored) {
                    // Column might already exist
                }
            }
            if (!hasColumn("disponible_le")) {
                try {
                    executeUpdate("ALTER TABLE ressource ADD COLUMN disponible_le VARCHAR(100)");
                } catch (SQLException ignored) {
                    // Column might already exist
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'initialisation du schéma des ressources", e);
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
        String sql = "INSERT INTO ressource(titre, contenu, categorie_nom, type, disponible_le) " +
                "VALUES(?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, r.getTitre());
            ps.setString(2, r.getContenu());
            ps.setString(3, r.getCategorieNom());
            ps.setString(4, r.getType());
            ps.setString(5, r.getDisponibleLe());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<resources> getAll() {
        String sql = "SELECT id, titre, contenu, categorie_nom, type, disponible_le FROM ressource";

        List<resources> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new resources(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("contenu"),
                        rs.getString("categorie_nom"),
                        rs.getString("type"),
                        rs.getString("disponible_le")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    public resources getById(int id) {
        String sql = "SELECT id, titre, contenu, categorie_nom, type, disponible_le FROM ressource WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new resources(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("contenu"),
                        rs.getString("categorie_nom"),
                        rs.getString("type"),
                        rs.getString("disponible_le")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public boolean update(resources r) {
        String sql = "UPDATE ressource SET titre=?, contenu=?, categorie_nom=?, type=?, disponible_le=? WHERE id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, r.getTitre());
            ps.setString(2, r.getContenu());
            ps.setString(3, r.getCategorieNom());
            ps.setString(4, r.getType());
            ps.setString(5, r.getDisponibleLe());
            ps.setInt(6, r.getId());

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
        String sql = "SELECT id, titre, contenu, categorie_nom, type, disponible_le FROM ressource WHERE categorie_nom = ?";
        List<resources> resourceList = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, categorieNom);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resourceList.add(new resources(
                            rs.getInt("id"),
                            rs.getString("titre"),
                            rs.getString("contenu"),
                            rs.getString("categorie_nom"),
                            rs.getString("type"),
                            rs.getString("disponible_le")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recuperation des ressources par categorie", e);
        }

        return resourceList;
    }

    public List<resources> search(String keyword) {
        String sql = "SELECT id, titre, contenu, categorie_nom, type, disponible_le " +
                "FROM ressource WHERE titre LIKE ? OR contenu LIKE ?";

        List<resources> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            ps.setString(1, k);
            ps.setString(2, k);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new resources(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("contenu"),
                        rs.getString("categorie_nom"),
                        rs.getString("type"),
                        rs.getString("disponible_le")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de ressources", e);
        }

        return list;
    }
}
