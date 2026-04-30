package tn.esprit.services;

import tn.esprit.entities.forum;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ForumService {

    private Connection cnx;

    public ForumService() {
        cnx = MyDatabase.getInstance().getConnection();
    }

    // ================= CREATE =================
    public void ajouter(forum f) {
        String sql = "INSERT INTO forum (titre, contenu, type, date_creation) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, f.getTitre());
            ps.setString(2, f.getContenu());
            ps.setString(3, f.getType());
            ps.setTimestamp(4, f.getDateCreation());
            ps.executeUpdate();
            System.out.println("Forum ajouté !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= READ =================
    public List<forum> getPaginated(int page, int size) {
        List<forum> list = new ArrayList<>();
        String sql = "SELECT * FROM forum LIMIT ? OFFSET ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, size);
            ps.setInt(2, (page - 1) * size);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new forum(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("contenu"),
                        rs.getString("type"),
                        rs.getTimestamp("date_creation")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ================= GET ALL (pour export Excel) =================
    public List<forum> getAll() {
        List<forum> list = new ArrayList<>();
        String sql = "SELECT * FROM forum ORDER BY date_creation DESC";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new forum(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("contenu"),
                        rs.getString("type"),
                        rs.getTimestamp("date_creation")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ================= DELETE =================
    public void supprimer(int id) {
        String sql = "DELETE FROM forum WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= UPDATE =================
    public void modifier(forum f) {
        String sql = "UPDATE forum SET contenu = ? WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, f.getContenu());
            ps.setInt(2, f.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= COUNT =================
    public int countForums() {
        String sql = "SELECT COUNT(*) FROM forum";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ================= AFFICHER =================
    public List<forum> afficher() {
        List<forum> list = new ArrayList<>();
        String sql = "SELECT * FROM forum";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                list.add(new forum(
                        rs.getInt("id"),
                        rs.getString("titre"),
                        rs.getString("contenu"),
                        rs.getString("type"),
                        rs.getTimestamp("date_creation")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}