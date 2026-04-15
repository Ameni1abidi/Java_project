package tn.esprit;

import org.junit.jupiter.api.*;
import tn.esprit.entities.Cours;
import tn.esprit.services.CoursService;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CoursTest {

    static CoursService service = new CoursService();
    static int idCoursTest;

    // =========================
    // TEST 1 : AJOUTER
    // =========================
    @Test
    @Order(1)
    void testAjouterCours() throws SQLException {

        Cours c = new Cours();
        c.setTitre("Cours Test");
        c.setDescription("Description Test");

        // ✅ niveau champ simple
        c.setNiveau("Débutant");

        // ✅ date obligatoire
        c.setDateCreation(new Date(System.currentTimeMillis()));

        service.ajouter(c);

        idCoursTest = service.ajouter(c);

        List<Cours> list = service.getAll();

        assertFalse(list.isEmpty());

        assertTrue(
                list.stream()
                        .anyMatch(co -> co.getTitre().equals("Cours Test"))
        );
    }

    // =========================
    // TEST 2 : MODIFIER
    // =========================
    @Test
    @Order(2)
    void testModifierCours() throws SQLException {

        Cours c = new Cours();
        c.setId(idCoursTest);
        c.setTitre("Cours Modifie");
        c.setDescription("Desc Modifiee");

        // ❗ لازمهم زادة
        c.setNiveau("Avancé");
        c.setDateCreation(new Date(System.currentTimeMillis()));

        service.modifier(c);

        List<Cours> list = service.getAll();

        boolean trouve = list.stream()
                .anyMatch(co -> co.getTitre().equals("Cours Modifie"));

        assertTrue(trouve);
    }

    // =========================
    // TEST 3 : SUPPRIMER
    // =========================
    @Test
    @Order(3)
    void testSupprimerCours() throws SQLException {

        service.supprimer(idCoursTest);

        List<Cours> list = service.getAll();

        boolean existe = list.stream()
                .anyMatch(co -> co.getId() == idCoursTest);

        assertFalse(existe);
    }

    // =========================
    // CLEAN
    // =========================
    @AfterAll
    static void cleanUp() throws SQLException {

        if (idCoursTest != 0) {
            service.supprimer(idCoursTest);
        }
    }
}
