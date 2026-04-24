package tn.esprit.services;

import tn.esprit.entities.StudentChapitreProgress;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;

public class StudentChapitreProgressService {

    private Connection cnx;

    public StudentChapitreProgressService() {
        this.cnx = MyDatabase.getInstance().getConnection();
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

                p.setOpened(rs.getBoolean("opened"));
                p.setViewedResume(rs.getBoolean("viewed_resume"));
                p.setCompleted(rs.getBoolean("completed"));
                p.setProgress(rs.getInt("progress")); // 🔥 FIX IMPORTANT

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

                String sql = """
                INSERT INTO student_chapitre_progress
                (utilisateur_id, chapitre_id, started_at, last_viewed_at, completed_at, progress, opened, viewed_resume, completed)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

                PreparedStatement ps = cnx.prepareStatement(sql);

                ps.setInt(1, p.getUtilisateurId());
                ps.setInt(2, p.getChapitreId());

                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

                ps.setTimestamp(5, p.getCompletedAt() != null
                        ? Timestamp.valueOf(p.getCompletedAt())
                        : null);

                ps.setInt(6, p.getProgress());
                ps.setBoolean(7, p.isOpened());
                ps.setBoolean(8, p.isViewedResume());
                ps.setBoolean(9, p.isCompleted());

                ps.executeUpdate();

            } else {

                String sql = """
                UPDATE student_chapitre_progress
                SET progress=?, opened=?, viewed_resume=?, completed=?, completed_at=?, last_viewed_at=?
                WHERE id=?
            """;

                PreparedStatement ps = cnx.prepareStatement(sql);

                ps.setInt(1, p.getProgress());
                ps.setBoolean(2, p.isOpened());
                ps.setBoolean(3, p.isViewedResume());
                ps.setBoolean(4, p.isCompleted());

                ps.setTimestamp(5, p.getCompletedAt() != null
                        ? Timestamp.valueOf(p.getCompletedAt())
                        : null);

                ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

                ps.setInt(7, p.getId());

                ps.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public int calculateProgress(StudentChapitreProgress p) {

        int progress = 0;

        if (p.isOpened()) progress += 20;
        if (p.isViewedResume()) progress += 40;
        if (p.isCompleted()) progress = 100;

        return Math.min(progress, 100);
    }
    public int getCourseProgress(int userId, int coursId) {

        String sql = """
        SELECT COUNT(*) as completed_count
        FROM student_chapitre_progress p
        JOIN chapitre c ON p.chapitre_id = c.id
        WHERE p.utilisateur_id = ? 
        AND c.cours_id = ?
        AND p.completed = true
    """;

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, coursId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int completed = rs.getInt("completed_count");

                // 🔥 إذا فمّا chapitre terminé → 100%
                if (completed > 0) return 100;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}
