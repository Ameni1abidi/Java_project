package tn.esprit.services;

import tn.esprit.entities.Evaluation;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvaluationService {

    Connection cnx = MyDatabase.getInstance().getConnection();

    // CREATE
    public void create(Evaluation e) {
        String sql = "INSERT INTO resultat (note, appreciation, examen_id, eleve_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setDouble(1, e.getNote());
            ps.setString(2, e.getAppreciation());
            ps.setInt(3, e.getExamenId());
            ps.setInt(4, e.getEleveId());

            ps.executeUpdate();
            System.out.println("✔ Evaluation ajoutée");

        } catch (Exception ex) {
            System.out.println("Error CREATE: " + ex.getMessage());
        }
    }

    // READ ALL
    public List<Evaluation> getAll() {
        List<Evaluation> list = new ArrayList<>();
        String sql = "SELECT * FROM resultat";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {

                Evaluation e = new Evaluation(
                        rs.getDouble("note"),
                        rs.getString("appreciation"),
                        rs.getInt("examen_id"),
                        rs.getInt("eleve_id")
                );

                e.setId(rs.getInt("id"));

                list.add(e);
            }

        } catch (Exception ex) {
            System.out.println("Error GET ALL: " + ex.getMessage());
        }

        return list;
    }

    // UPDATE
    public void update(Evaluation e) {
        String sql = "UPDATE resultat SET note=?, appreciation=?, examen_id=?, eleve_id=? WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setDouble(1, e.getNote());
            ps.setString(2, e.getAppreciation());
            ps.setInt(3, e.getExamenId());
            ps.setInt(4, e.getEleveId());
            ps.setInt(5, e.getId());

            ps.executeUpdate();
            System.out.println("✔ Evaluation modifiée");

        } catch (Exception ex) {
            System.out.println("Error UPDATE: " + ex.getMessage());
        }
    }

    // DELETE
    public void delete(int id) {
        String sql = "DELETE FROM resultat WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, id);

            ps.executeUpdate();
            System.out.println("✔ Evaluation supprimée");

        } catch (Exception ex) {
            System.out.println("Error DELETE: " + ex.getMessage());
        }
    }


    public List<Evaluation> findByEleveId(int eleveId) {

        List<Evaluation> list = new ArrayList<>();

        String sql = "SELECT * FROM resultat WHERE eleve_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, eleveId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Evaluation e = new Evaluation(
                        rs.getDouble("note"),
                        rs.getString("appreciation"),
                        rs.getInt("examen_id"),
                        rs.getInt("eleve_id")
                );

                e.setId(rs.getInt("id"));

                list.add(e);
            }

        } catch (Exception ex) {
            System.out.println("Error FIND BY ELEVE: " + ex.getMessage());
        }

        return list;
    }
}
