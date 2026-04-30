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
        String sql = "INSERT INTO examen (titre, contenu, type, date_examen, duree, cours_id, enseignant_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, e.getTitre());
            ps.setString(2, e.getContenu());
            ps.setString(3, e.getType());
            ps.setDate(4, Date.valueOf(e.getDateExamen()));
            ps.setInt(5, e.getDuree());
            ps.setInt(6, e.getCoursId());        // 🔥 important
            ps.setInt(7, e.getEnseignantId());   // 🔥 important

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                e.setId(rs.getInt(1));
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

                Examen e = new Examen();

                e.setId(rs.getInt("id"));
                e.setTitre(rs.getString("titre"));
                e.setContenu(rs.getString("contenu"));
                e.setType(rs.getString("type"));
                e.setDateExamen(rs.getDate("date_examen").toLocalDate());
                e.setDuree(rs.getInt("duree"));
                e.setCoursId(rs.getInt("cours_id"));
                e.setEnseignantId(rs.getInt("enseignant_id"));

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
        String sql = "UPDATE examen SET titre=?, contenu=?, type=?, date_examen=?, duree=?, cours_id=?, enseignant_id=? WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, e.getTitre());
            ps.setString(2, e.getContenu());
            ps.setString(3, e.getType());
            ps.setDate(4, Date.valueOf(e.getDateExamen()));
            ps.setInt(5, e.getDuree());
            ps.setInt(6, e.getCoursId());        // 🔥 ajout
            ps.setInt(7, e.getEnseignantId());   // 🔥 ajout
            ps.setInt(8, e.getId());

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

    public List<Examen> rechercherParTitre(String titre) {
        String sql = "SELECT * FROM examen WHERE titre LIKE ?";
        List<Examen> list = new ArrayList<>();

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, "%" + titre + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Examen e = new Examen();
                e.setId(rs.getInt("id"));
                e.setTitre(rs.getString("titre"));
                e.setContenu(rs.getString("contenu"));
                e.setType(rs.getString("type"));
                e.setDateExamen(rs.getDate("date_examen").toLocalDate());
                e.setDuree(rs.getInt("duree"));
                e.setCoursId(rs.getInt("cours_id"));
                e.setEnseignantId(rs.getInt("enseignant_id"));

                list.add(e);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
    public Examen getById(int id) {

        String sql = "SELECT * FROM examen WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Examen e = new Examen();

                e.setId(rs.getInt("id"));
                e.setTitre(rs.getString("titre"));
                e.setContenu(rs.getString("contenu"));
                e.setType(rs.getString("type"));
                e.setDateExamen(rs.getDate("date_examen").toLocalDate());
                e.setDuree(rs.getInt("duree"));
                e.setCoursId(rs.getInt("cours_id"));
                e.setEnseignantId(rs.getInt("enseignant_id"));

                return e;
            }

        } catch (Exception ex) {
            System.out.println("Error GET BY ID: " + ex.getMessage());
        }

        return null;
    }
}