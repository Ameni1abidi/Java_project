package tn.esprit.services;

import tn.esprit.entities.commentaire;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService {

    Connection cnx;

    public CommentaireService() {
        cnx = MyDatabase.getInstance().getConnection();
    }

    // CREATE
    public void ajouter(commentaire c) {
        String sql = "INSERT INTO commentaire (contenu, forum_id, date_envoi) VALUES (?, ?, ?)";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, c.getContenu());
            ps.setInt(2, c.getForumId());
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));

            ps.executeUpdate();
            System.out.println("Commentaire ajouté !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ
    public List<commentaire> afficher() {
        List<commentaire> list = new ArrayList<>();
        String sql = "SELECT * FROM commentaire";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                commentaire c = new commentaire();
                c.setId(rs.getInt("id"));
                c.setContenu(rs.getString("contenu"));
                c.setForumId(rs.getInt("forum_id"));
                c.setDateEnvoi(rs.getTimestamp("date_envoi"));

                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}