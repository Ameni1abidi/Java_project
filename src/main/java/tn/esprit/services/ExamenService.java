package tn.esprit.services;

import tn.esprit.entities.Examen;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamenService implements IService<Examen> {

    Connection cnx = MyDatabase.getInstance().getConnection();

    // ================= CREATE =================
    @Override
    public void create(Examen e) {
        String sql = "INSERT INTO examen (titre, contenu, type, date_examen, duree) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, e.getTitre());
            ps.setString(2, e.getContenu());
            ps.setString(3, e.getType());
            ps.setDate(4, Date.valueOf(e.getDateExamen()));
            ps.setInt(5, e.getDuree());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    e.setId(rs.getInt(1));
                }
            }

            System.out.println("✔ Examen ajouté");

        } catch (Exception ex) {
            System.out.println("Error CREATE: " + ex.getMessage());
        }
    }

    // ================= READ ALL =================
    @Override
    public List<Examen> getAll() {
        List<Examen> list = new ArrayList<>();
        String sql = "SELECT * FROM examen";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {

                Examen e = new Examen(
                        rs.getString("titre"),
                        rs.getString("contenu"),
                        rs.getString("type"),
                        rs.getDate("date_examen").toLocalDate(),
                        rs.getInt("duree")
                );

                e.setId(rs.getInt("id"));
                list.add(e);
            }

        } catch (Exception ex) {
            System.out.println("Error GET ALL: " + ex.getMessage());
        }

        return list;
    }

    // ================= UPDATE =================
    @Override
    public void update(Examen e) {
        String sql = "UPDATE examen SET titre=?, contenu=?, type=?, date_examen=?, duree=? WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, e.getTitre());
            ps.setString(2, e.getContenu());
            ps.setString(3, e.getType());
            ps.setDate(4, Date.valueOf(e.getDateExamen()));
            ps.setInt(5, e.getDuree());
            ps.setInt(6, e.getId());

            ps.executeUpdate();

            System.out.println("✔ Examen modifié");

        } catch (Exception ex) {
            System.out.println("Error UPDATE: " + ex.getMessage());
        }
    }

    // ================= DELETE =================
    @Override
    public void delete(int id) {
        String sql = "DELETE FROM examen WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, id);

            ps.executeUpdate();

            System.out.println("✔ Examen supprimé");

        } catch (Exception ex) {
            System.out.println("Error DELETE: " + ex.getMessage());
        }
    }
}