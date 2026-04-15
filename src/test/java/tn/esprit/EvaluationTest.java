package tn.esprit;

import org.junit.jupiter.api.*;
import tn.esprit.entities.Evaluation;
import tn.esprit.services.EvaluationService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EvaluationTest {

    static EvaluationService service;
    static int idEvaluationTest;

    // ================= INIT =================
    @BeforeAll
    static void setup() {
        service = new EvaluationService();
    }

    // ================= TEST 1 : AJOUT =================
    @Test
    @Order(1)
    void testAjouterEvaluation() {

        Evaluation e = new Evaluation();
        e.setNote(15);
        e.setAppreciation("Test Evaluation");
        e.setExamenId(1);
        e.setEleveId(1);

        service.create(e);

        List<Evaluation> list = service.getAll();

        assertFalse(list.isEmpty());

        Evaluation found = list.stream()
                .filter(ev -> ev.getAppreciation().equals("Test Evaluation"))
                .findFirst()
                .orElse(null);

        assertNotNull(found);

        idEvaluationTest = found.getId();
    }

    // ================= TEST 2 : MODIFIER =================
    @Test
    @Order(2)
    void testModifierEvaluation() {

        assertTrue(idEvaluationTest > 0); // 🔥 sécurité

        Evaluation e = new Evaluation();
        e.setId(idEvaluationTest);
        e.setNote(18);
        e.setAppreciation("Modifié");
        e.setExamenId(1);
        e.setEleveId(1);

        service.update(e);

        List<Evaluation> list = service.getAll();

        boolean updated = list.stream()
                .anyMatch(ev ->
                        ev.getId() == idEvaluationTest &&
                                ev.getNote() == 18 &&
                                ev.getAppreciation().equals("Modifié")
                );

        assertTrue(updated);
    }

    // ================= TEST 3 : SUPPRIMER =================
    @Test
    @Order(3)
    void testSupprimerEvaluation() {

        service.delete(idEvaluationTest);

        List<Evaluation> list = service.getAll();

        boolean exists = list.stream()
                .anyMatch(ev -> ev.getId() == idEvaluationTest);

        assertFalse(exists);
    }

    // ================= CLEAN FINAL =================
    @AfterAll
    static void cleanUpAll() {
        try {
            List<Evaluation> list = service.getAll();

            list.stream()
                    .filter(e -> "Test Evaluation".equals(e.getAppreciation()))
                    .forEach(e -> service.delete(e.getId()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}