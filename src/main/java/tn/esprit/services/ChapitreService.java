package tn.esprit.services;

import tn.esprit.entities.Chapitre;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChapitreService {

    Connection cnx = MyDatabase.getInstance().getConnection();

    public void ajouter(Chapitre c) {

        String sql = "INSERT INTO chapitre (titre, ordre, type_contenu, contenu_texte, contenu_fichier, duree_estimee, resume, cours_id) VALUES (?,?,?,?,?,?,?,?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getTitre());
            ps.setInt(2, c.getOrdre());
            ps.setString(3, c.getTypeContenu());
            ps.setString(4, c.getContenuTexte());
            ps.setString(5, c.getContenuFichier());
            ps.setInt(6, c.getDureeEstimee());
            ps.setString(7, c.getResume());
            ps.setInt(8, c.getCoursId());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                c.setId(rs.getInt(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Chapitre> getAll(int coursId) {

        List<Chapitre> list = new ArrayList<>();

        String sql = "SELECT * FROM chapitre WHERE cours_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, coursId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Chapitre c = new Chapitre();

                c.setId(rs.getInt("id"));
                c.setTitre(rs.getString("titre"));
                c.setOrdre(rs.getInt("ordre"));
                c.setTypeContenu(rs.getString("type_contenu"));
                c.setContenuTexte(rs.getString("contenu_texte"));
                c.setContenuFichier(rs.getString("contenu_fichier"));
                c.setDureeEstimee(rs.getInt("duree_estimee"));
                c.setResume(rs.getString("resume"));

                list.add(c);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public void modifier(Chapitre c) {

        String sql = "UPDATE chapitre SET titre=?, ordre=?, type_contenu=?, contenu_texte=?, contenu_fichier=?, duree_estimee=?, resume=?, cours_id=? WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, c.getTitre());
            ps.setInt(2, c.getOrdre());
            ps.setString(3, c.getTypeContenu());
            ps.setString(4, c.getContenuTexte());
            ps.setString(5, c.getContenuFichier());
            ps.setInt(6, c.getDureeEstimee());
            ps.setString(7, c.getResume());

            // relation
            ps.setInt(8, c.getCoursId());

            ps.setInt(9, c.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM chapitre WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public int getLastInsertedId() {
        try {
            String sql = "SELECT LAST_INSERT_ID()";
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
}

