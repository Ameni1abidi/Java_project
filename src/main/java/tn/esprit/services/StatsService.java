package tn.esprit.services;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import tn.esprit.utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StatsService {

    private final Connection cnx = MyDatabase.getInstance().getConnection();


    public int getCountCoursRecent() {
        return getCount("""
            SELECT COUNT(*) FROM cours
            WHERE date_creation >= NOW() - INTERVAL 7 DAY
        """);
    }

    public int getCountCoursAncien() {
        return getCount("""
            SELECT COUNT(*) FROM cours
            WHERE date_creation < NOW() - INTERVAL 30 DAY
        """);
    }

    public int getCoursPopulaires() {
        return getCount("""
            SELECT COUNT(*) FROM cours
            WHERE started_by_cours >= 20
        """);
    }

    public int getCoursSansChapitres() {
        return getCount("""
            SELECT COUNT(*) 
            FROM cours c
            LEFT JOIN chapitre ch ON ch.cours_id = c.id_cours
            WHERE ch.id IS NULL
        """);
    }

    public int getCoursRiches() {
        try {
            String sql = """
                SELECT COUNT(*) FROM (
                    SELECT c.id_cours
                    FROM cours c
                    JOIN chapitre ch ON ch.cours_id = c.id_cours
                    GROUP BY c.id_cours
                    HAVING COUNT(ch.id) >= 5
                ) t
            """;
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getCoursSansDescription() {
        return getCount("""
            SELECT COUNT(*) 
            FROM cours 
            WHERE description IS NULL OR description = ''
        """);
    }

    private int getCount(String sql) {
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    private int executeCount(String sql) {
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    private final CoursService coursService = new CoursService();

    @FXML
    private Label lblPopulaire;
    @FXML private Label lblALaUne;
    @FXML private Label lblTendance;

    private void loadCoursStats() {

        int populaire = coursService.countByBadge("populaire");
        int tendance = coursService.countByBadge("tendance");
        int alaune = coursService.countByBadge("a la une");

        lblPopulaire.setText(String.valueOf(populaire));
        lblTendance.setText(String.valueOf(tendance));
        lblALaUne.setText(String.valueOf(alaune));
    }

}
