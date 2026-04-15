package tn.esprit;

import org.junit.jupiter.api.*;
import tn.esprit.entities.Chapitre;
import tn.esprit.entities.Cours;
import tn.esprit.services.ChapitreService;
import tn.esprit.services.CoursService;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChapitreTest {

    static ChapitreService chapitreService = new ChapitreService();
    static CoursService coursService = new CoursService();

    static int coursIdTest;
    static int chapitreIdTest;

    // =========================
    // CREATE COURS FIRST
    // =========================
    @BeforeAll
    static void init() throws SQLException {

        Cours c = new Cours();
        c.setTitre("Cours Chapitre Test");
        c.setDescription("Desc");
        c.setNiveau("Débutant");
        c.setDateCreation(new Date(System.currentTimeMillis()));

        coursService.ajouter(c);

        coursIdTest = coursService.getLastInsertedId();
    }

    // =========================
    // TEST 1 : AJOUT CHAPITRE
    // =========================
    @Test
    @Order(1)
    void testAjouterChapitre() throws SQLException {

        Chapitre ch = new Chapitre();
        ch.setTitre("Chapitre Test");
        ch.setOrdre(1);
        ch.setTypeContenu("texte");
        ch.setContenuTexte("Contenu");
        ch.setCoursId(coursIdTest);

        chapitreService.ajouter(ch);

        chapitreIdTest = ch.getId();

        List<Chapitre> list = chapitreService.getAll(coursIdTest);

        assertTrue(list.stream()
                .anyMatch(c -> c.getTitre().equals("Chapitre Test")));
    }

    // =========================
    // TEST 2 : MODIFY
    // =========================
    @Test
    @Order(2)
    void testModifierChapitre() throws SQLException {

        Chapitre ch = new Chapitre();

        ch.setId(chapitreIdTest);
        ch.setTitre("Chapitre Modifie");
        ch.setOrdre(2);
        ch.setTypeContenu("texte");

        // ✅ IMPORTANT : remettre les champs obligatoires
        ch.setContenuTexte("Contenu modifié");
        ch.setContenuFichier(null);
        ch.setDureeEstimee(10);
        ch.setResume("Résumé modifié");

        ch.setCoursId(coursIdTest);

        chapitreService.modifier(ch);

        List<Chapitre> list = chapitreService.getAll(coursIdTest);

        assertTrue(list.stream()
                .anyMatch(c -> c.getTitre().equals("Chapitre Modifie")));
    }

    // =========================
    // TEST 3 : DELETE
    // =========================
    @Test
    @Order(3)
    void testSupprimerChapitre() throws SQLException {

        chapitreService.supprimer(chapitreIdTest);

        List<Chapitre> list = chapitreService.getAll(coursIdTest);

        assertFalse(list.stream()
                .anyMatch(c -> c.getId() == chapitreIdTest));
    }

    // =========================
    // CLEAN
    // =========================
    @AfterAll
    static void clean() throws SQLException {

        if (coursIdTest != 0) {
            coursService.supprimer(coursIdTest);
        }
    }
}
