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
                Chapitre c = mapRow(rs);
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

    public int countByCoursId(int coursId) {
        int count = 0;
        try {
            String sql = "SELECT COUNT(*) FROM chapitre WHERE cours_id = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, coursId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public List<Chapitre> getByCoursId(int coursId) {
        List<Chapitre> list = new ArrayList<>();
        try {
            String sql = "SELECT * FROM chapitre WHERE cours_id = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, coursId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Chapitre> getAllChapitres() {
        List<Chapitre> list = new ArrayList<>();
        String sql = "SELECT * FROM chapitre ORDER BY cours_id, ordre, id";
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Chapitre getById(int chapitreId) {
        String sql = "SELECT * FROM chapitre WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, chapitreId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getCourseProgress(int userId, int coursId) {
        try {
            String sql = """
            SELECT COALESCE(AVG(progress),0) as avg_progress
            FROM student_chapitre_progress p
            JOIN chapitre c ON c.id = p.chapitre_id
            WHERE p.utilisateur_id = ? AND c.cours_id = ?
        """;
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, coursId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("avg_progress");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Chapitre mapRow(ResultSet rs) throws SQLException {
        Chapitre ch = new Chapitre();
        ch.setId(rs.getInt("id"));
        ch.setTitre(rs.getString("titre"));
        ch.setOrdre(rs.getInt("ordre"));
        ch.setTypeContenu(rs.getString("type_contenu"));
        ch.setContenuTexte(rs.getString("contenu_texte"));
        ch.setContenuFichier(rs.getString("contenu_fichier"));
        ch.setDureeEstimee(rs.getInt("duree_estimee"));
        ch.setCoursId(rs.getInt("cours_id"));
        ch.setResume(rs.getString("resume"));
        return ch;
    }
}
