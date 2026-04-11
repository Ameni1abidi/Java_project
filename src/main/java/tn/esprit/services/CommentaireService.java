package tn.esprit.services;

import tn.esprit.entities.commentaire;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService {

    private Connection cnx;

    public CommentaireService() {
        cnx = MyDatabase.getInstance().getConnection();
    }

    // 🔹 CREATE
    public void ajouter(commentaire c) {
        try {
            String sql = "INSERT INTO commentaire (contenu, forum_id, date_envoi) VALUES (?, ?, ?)";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, c.getContenu());
            ps.setInt(2, c.getForumId());
            ps.setTimestamp(3, c.getDateEnvoi());

            ps.executeUpdate();
            System.out.println("Commentaire ajouté !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🔹 READ
    public List<commentaire> afficher() {
        List<commentaire> list = new ArrayList<>();

        try {
            String sql = "SELECT * FROM commentaire";
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                commentaire c = new commentaire(
                        rs.getInt("id"),
                        rs.getString("contenu"),
                        rs.getInt("forum_id"),
                        rs.getTimestamp("date_envoi")
                );
                list.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // 🔹 DELETE
    public void supprimer(int id) {
        try {
            String sql = "DELETE FROM commentaire WHERE id = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🔹 UPDATE
    public void modifier(commentaire c) {
        try {
            String sql = "UPDATE commentaire SET contenu = ? WHERE id = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, c.getContenu());
            ps.setInt(2, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}