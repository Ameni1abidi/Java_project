package tn.esprit.services;

import tn.esprit.entities.BulletinRow;
import tn.esprit.entities.Evaluation;
import tn.esprit.entities.Examen;
import tn.esprit.entities.User;
import tn.esprit.utils.BulletinPdfExporter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BulletinService {

    private final EvaluationService evaluationService = new EvaluationService();
    private final ExamenService examenService = new ExamenService();
    UserService userService = new UserService();

    public void exportBulletin(int eleveId) {

        // 1. récupérer données depuis BD
        List<Evaluation> evaluations = evaluationService.findByEleveId(eleveId);
        List<Examen> examens = examenService.getAll();
        try {
            User eleve = userService.getUserById(eleveId)
                    .orElseThrow(() -> new RuntimeException("Élève introuvable"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // 2. transformer
        List<BulletinRow> rows = buildRows(evaluations, examens);

        // 3. générer PDF
        BulletinPdfExporter.exportBulletin(
                "C:/EduFlex/bulletin_" + eleveId + ".pdf",
                "Eleve " + eleveId,
                rows
        );
    }

    private List<BulletinRow> buildRows(List<Evaluation> evaluations, List<Examen> examens) {

        List<BulletinRow> rows = new ArrayList<>();

        for (Evaluation e : evaluations) {
            for (Examen ex : examens) {

                if (e.getExamenId() == ex.getId()) {

                    rows.add(new BulletinRow(
                            ex.getTitre(),
                            e.getNote(),
                            e.getAppreciation()
                    ));
                }
            }
        }

        return rows;
    }
}