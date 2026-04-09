package tn.esprit.services;




import tn.esprit.entities.Chapitre;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChapitreService {
    Connection cnx = MyDatabase.getInstance().getConnection();

    // CREATE
    public void ajouter(Chapitre c) {
        try {
            String sql = "INSERT INTO chapitre (titre, ordre, type_contenu, contenu_texte, contenu_fichier, duree_estimee, resume, cours_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setString(1, c.getTitre());
            ps.setInt(2, c.getOrdre());
            ps.setString(3, c.getTypeContenu());
            ps.setString(4, c.getContenuTexte());
            ps.setString(5, c.getContenuFichier());
            ps.setInt(6, c.getDureeEstimee());
            ps.setString(7, c.getResume());
            ps.setInt(8, c.getCoursId());

            ps.executeUpdate();
            System.out.println("Chapitre ajouté !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // READ
    public List<Chapitre> afficher() {
        List<Chapitre> list = new ArrayList<>();
        try {
            String sql = "SELECT * FROM chapitre";
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);

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
                c.setCoursId(rs.getInt("cours_id"));

                list.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    // UPDATE
    public void modifier(Chapitre c) {
        try {
            String sql = "UPDATE chapitre SET titre=?, ordre=?, type_contenu=?, contenu_texte=?, contenu_fichier=?, duree_estimee=?, resume=?, cours_id=? WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);

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
            System.out.println("Chapitre modifié !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // DELETE
    public void supprimer(int id) {
        try {
            String sql = "DELETE FROM chapitre WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);

            ps.executeUpdate();
            System.out.println("Chapitre supprimé !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}

