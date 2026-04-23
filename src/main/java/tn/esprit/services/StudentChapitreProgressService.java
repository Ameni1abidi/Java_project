package tn.esprit.services;

import tn.esprit.entities.StudentChapitreProgress;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;

public class StudentChapitreProgressService {

    private Connection cnx;

    public StudentChapitreProgressService() {
        Connection cnx = MyDatabase.getInstance().getConnection(); // حسب مشروعك
    }

    // ================= FIND =================
    public StudentChapitreProgress find(int userId, int chapitreId) {

        try {
            String sql = "SELECT * FROM student_chapitre_progress WHERE utilisateur_id=? AND chapitre_id=?";

            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, chapitreId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                StudentChapitreProgress p = new StudentChapitreProgress();

                p.setId(rs.getInt("id"));
                p.setUtilisateurId(userId);
                p.setChapitreId(chapitreId);

                Timestamp c = rs.getTimestamp("completed_at");
                if (c != null)
                    p.setCompletedAt(c.toLocalDateTime());

                return p;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= SAVE OR UPDATE =================
    public void saveOrUpdate(StudentChapitreProgress p) {

        try {

            if (p.getId() == 0) {
                // INSERT
                String sql = """
                    INSERT INTO student_chapitre_progress
                    (utilisateur_id, chapitre_id, started_at, last_viewed_at, completed_at, time_spent_seconds)
                    VALUES (?, ?, ?, ?, ?, ?)
                """;

                PreparedStatement ps = cnx.prepareStatement(sql);

                ps.setInt(1, p.getUtilisateurId());
                ps.setInt(2, p.getChapitreId());
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

                if (p.getCompletedAt() != null)
                    ps.setTimestamp(5, Timestamp.valueOf(p.getCompletedAt()));
                else
                    ps.setNull(5, Types.TIMESTAMP);

                ps.setInt(6, 0);

                ps.executeUpdate();

            } else {
                // UPDATE
                String sql = """
                    UPDATE student_chapitre_progress
                    SET completed_at = ?, last_viewed_at = ?
                    WHERE id = ?
                """;

                PreparedStatement ps = cnx.prepareStatement(sql);

                if (p.getCompletedAt() != null)
                    ps.setTimestamp(1, Timestamp.valueOf(p.getCompletedAt()));
                else
                    ps.setNull(1, Types.TIMESTAMP);

                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(3, p.getId());

                ps.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
