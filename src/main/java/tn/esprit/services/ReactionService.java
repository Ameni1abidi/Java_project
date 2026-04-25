package tn.esprit.services;

import tn.esprit.utils.MyDatabase; // 🔥 IMPORTANT

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ReactionService {

    private Connection cnx = MyDatabase.getInstance().getConnection();

    public void ajouter(int commentaireId, String type) {
        try {
            String sql = "INSERT INTO reaction (commentaire_id, type) VALUES (?, ?)";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, commentaireId);
            ps.setString(2, type);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, Integer> getCounts(int commentaireId) {
        Map<String, Integer> map = new HashMap<>();

        try {
            String sql = "SELECT type, COUNT(*) as total FROM reaction WHERE commentaire_id=? GROUP BY type";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, commentaireId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                map.put(rs.getString("type"), rs.getInt("total"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }
}