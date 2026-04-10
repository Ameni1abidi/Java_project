package tn.esprit.services;

import tn.esprit.entities.Evaluation;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvaluationService implements IService<Evaluation> {
    Connection cnx = MyDatabase.getInstance().getConnection();

    @Override
    public void create(Evaluation e) {
        String sql = "INSERT INTO resultat (note, appreciation) VALUES (?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setDouble(1, e.getNote());
            ps.setString(2, e.getAppreciation());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                e.setId(rs.getInt(1));
            }

            System.out.println("✔ Evaluation ajoutée");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public List<Evaluation> getAll() {
        List<Evaluation> list = new ArrayList<>();
        String sql = "SELECT * FROM resultat";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Evaluation e = new Evaluation(
                        rs.getDouble("note"),
                        rs.getString("appreciation")
                );
                e.setId(rs.getInt("id"));
                list.add(e);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        return list;
    }

    @Override
    public void update(Evaluation e) {
        String sql = "UPDATE resultat SET note=?, appreciation=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setDouble(1, e.getNote());
            ps.setString(2, e.getAppreciation());
            ps.setInt(3, e.getId());

            ps.executeUpdate();
            System.out.println("✔ Evaluation modifiée");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM resultat WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✔ Evaluation supprimée");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
