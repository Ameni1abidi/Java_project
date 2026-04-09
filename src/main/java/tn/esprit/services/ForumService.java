package tn.esprit.services;

import tn.esprit.entities.forum;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ForumService {

    Connection cnx;

    public ForumService() {
        cnx = MyDatabase.getInstance().getConnection();
    }

    // CREATE
    public void ajouter(forum f) {
        String sql = "INSERT INTO forum (titre, contenu, type, date_creation) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, f.getTitre());
            ps.setString(2, f.getContenu());
            ps.setString(3, f.getType());
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            ps.executeUpdate();
            System.out.println("Forum ajouté !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ
    public List<forum> afficher() {
        List<forum> list = new ArrayList<>();
        String sql = "SELECT * FROM forum";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                forum f = new forum();
                f.setId(rs.getInt("id"));
                f.setTitre(rs.getString("titre"));
                f.setContenu(rs.getString("contenu"));
                f.setType(rs.getString("type"));
                f.setDateCreation(rs.getTimestamp("date_creation"));

                list.add(f);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // UPDATE
    public void modifier(forum f) {
        String sql = "UPDATE forum SET titre=?, contenu=?, type=? WHERE id=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, f.getTitre());
            ps.setString(2, f.getContenu());
            ps.setString(3, f.getType());
            ps.setInt(4, f.getId());

            ps.executeUpdate();
            System.out.println("Forum modifié !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public void supprimer(int id) {
        String sql = "DELETE FROM forum WHERE id=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Forum supprimé !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}