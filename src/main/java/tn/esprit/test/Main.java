package tn.esprit.test;

import tn.esprit.entities.Examen;
import tn.esprit.entities.Evaluation;
import tn.esprit.services.ExamenService;
import tn.esprit.services.EvaluationService;

import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        // ===== EXAMEN =====
        ExamenService es = new ExamenService();
        //Examen e = new Examen("Examen BD", "SQL", "Final", LocalDate.now(), 90);
        //es.create(e);
        //es.getAll().forEach(System.out::println);
        //e.setTitre("Examen Update");
        //es.update(e);
        //es.delete(e.getId());
        // ===== EVALUATION =====
        EvaluationService evs = new EvaluationService();
        //Evaluation ev = new Evaluation(16, "Bien");
        //evs.create(ev);
        evs.getAll().forEach(System.out::println);
        //ev.setNote(19);
        //ev.setAppreciation("Excellent");
        //evs.update(ev);
        //evs.delete(ev.getId());
    }


}