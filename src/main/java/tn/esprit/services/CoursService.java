package tn.esprit.services;

import tn.esprit.entities.Cours;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CoursService {

    Connection cnx = MyDatabase.getInstance().getConnection();

    public int ajouter(Cours c) {
        int id = -1;

        try {
            String sql = "INSERT INTO cours (titre, description, niveau, date_creation, titre_traduit, description_traduit, badge) VALUES (?,?,?,?,?,?,?)";

            PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, c.getTitre());
            ps.setString(2, c.getDescription());
            ps.setString(3, c.getNiveau());
            ps.setDate(4, c.getDateCreation());
            ps.setString(5, c.getTitreTraduit());
            ps.setString(6, c.getDescriptionTraduit());
            ps.setString(7, c.getBadge());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
                c.setId(id);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return id;
    }
    public List<Cours> getAll() {
        List<Cours> list = new ArrayList<>();

        try {
            String sql = "SELECT * FROM cours";
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Cours c = new Cours();
                c.setId(rs.getInt("id"));
                c.setTitre(rs.getString("titre"));
                c.setDescription(rs.getString("description"));
                c.setNiveau(rs.getString("niveau"));
                c.setDateCreation(rs.getDate("date_creation"));
                c.setTitreTraduit(rs.getString("titre_traduit"));
                c.setDescriptionTraduit(rs.getString("description_traduit"));
                c.setBadge(rs.getString("badge"));
                c.setEtat(rs.getString("etat") == null ? "PUBLIE" : rs.getString("etat"));

                list.add(c);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    // 👇 حطها هنا
    public List<Cours> getArchived() {
        List<Cours> list = new ArrayList<>();
        String sql = "SELECT * FROM cours WHERE UPPER(TRIM(etat)) = 'ARCHIVE'";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // لازم تكون موجودة
    private Cours mapRow(ResultSet rs) throws SQLException {
        Cours c = new Cours();
        c.setId(rs.getInt("id"));
        c.setTitre(rs.getString("titre"));
        c.setDescription(rs.getString("description"));
        c.setEtat(rs.getString("etat"));
        return c;
    }


    public void modifier(Cours c) {
        try {
            String sql = "UPDATE cours SET titre=?, description=?, niveau=?, date_creation=?, titre_traduit=?, description_traduit=?, badge=? WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setString(1, c.getTitre());
            ps.setString(2, c.getDescription());
            ps.setString(3, c.getNiveau());
            ps.setDate(4, c.getDateCreation());
            ps.setString(5, c.getTitreTraduit());
            ps.setString(6, c.getDescriptionTraduit());
            ps.setString(7, c.getBadge());
            ps.setInt(8, c.getId());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void supprimer(int id) {
        try {
            String sql = "DELETE FROM cours WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public int getLastInsertedId() {
        try {
            String sql = "SELECT MAX(id) FROM cours";
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int getChapitreCount(int coursId) {
        try {
            String sql = "SELECT COUNT(*) FROM chapitre WHERE cours_id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, coursId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    public int countByBadge(String badge) {
        try {
            String sql = "SELECT COUNT(*) FROM cours WHERE badge=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, badge);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String computeBadge(Cours c, int chapCount, boolean isRecent) {
        if (chapCount >= 10) {
            return "populaire";
        } else if (isRecent) {
            return "tendance";
        } else {
            return "a la une";
        }
    }

    public Cours getById(int id) {
        Cours c = null;

        try {
            String sql = "SELECT * FROM cours WHERE id = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                c = new Cours();
                c.setId(rs.getInt("id"));
                c.setTitre(rs.getString("titre"));
                c.setDescription(rs.getString("description"));
                c.setNiveau(rs.getString("niveau"));
                c.setDateCreation(rs.getDate("date_creation"));
                c.setTitreTraduit(rs.getString("titre_traduit"));
                c.setDescriptionTraduit(rs.getString("description_traduit"));
                c.setBadge(rs.getString("badge"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return c;
    }


    public boolean isRecentCours(java.util.Date dateCreation) {
        if (dateCreation == null) return false;

        long diff = System.currentTimeMillis() - dateCreation.getTime();
        long days = diff / (1000 * 60 * 60 * 24);

        return days <= 7;
    }


}

