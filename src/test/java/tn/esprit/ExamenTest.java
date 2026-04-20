package tn.esprit;

import org.junit.jupiter.api.*;
import tn.esprit.entities.Examen;
import tn.esprit.services.ExamenService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ExamenTest {

    static ExamenService service;
    static int idExamenTest;

    // ================= INIT =================
    @BeforeAll
    static void setup() {
        service = new ExamenService();
    }

    // ================= TEST 1 : AJOUT =================
    @Test
    @Order(1)
    void testAjouterExamen() {

        Examen e = new Examen();
        e.setTitre("Test Examen");
        e.setContenu("Contenu test");
        e.setType("Final");
        e.setDateExamen(LocalDate.now());
        e.setDuree(60);
        e.setCoursId(1);
        e.setEnseignantId(1);

        service.create(e);

        List<Examen> list = service.getAll();

        assertFalse(list.isEmpty());

        Examen found = list.stream()
                .filter(ex -> ex.getTitre().equals("Test Examen"))
                .findFirst()
                .orElse(null);

        assertNotNull(found);

        idExamenTest = found.getId();
    }

    // ================= TEST 2 : MODIFIER =================
    @Test
    @Order(2)
    void testModifierExamen() {

        assertTrue(idExamenTest > 0);

        Examen e = new Examen();
        e.setId(idExamenTest);
        e.setTitre("Examen Modifié");
        e.setContenu("Contenu modifié");
        e.setType("Final");
        e.setDateExamen(java.time.LocalDate.now());
        e.setDuree(90);
        e.setCoursId(1);
        e.setEnseignantId(1);

        service.update(e);

        List<Examen> list = service.getAll();

        boolean updated = list.stream()
                .anyMatch(ex ->
                        ex.getId() == idExamenTest &&
                                ex.getTitre().equals("Examen Modifié")
                );

        assertTrue(updated);
    }

    // ================= TEST 3 : SUPPRIMER =================
    @Test
    @Order(3)
    void testSupprimerExamen() {

        service.delete(idExamenTest);

        List<Examen> list = service.getAll();

        boolean exists = list.stream()
                .anyMatch(ex -> ex.getId() == idExamenTest);

        assertFalse(exists);
    }

    // ================= CLEAN FINAL =================
    @AfterAll
    static void cleanUpAll() {
        try {
            List<Examen> list = service.getAll();

            list.stream()
                    .filter(e -> "Test Examen".equals(e.getTitre()))
                    .forEach(e -> service.delete(e.getId()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}