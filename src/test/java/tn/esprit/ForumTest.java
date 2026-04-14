package tn.esprit;

import org.junit.jupiter.api.*;

import tn.esprit.entities.forum;
import tn.esprit.services.ForumService;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ForumTest {

    static ForumService service;
    static int idForumTest;

    @BeforeAll
    static void setup() {
        service = new ForumService();
    }

    // ================= TEST 1 : AJOUT =================
    @Test
    @Order(1)
    void testAjouterForum() {

        forum f = new forum(
                0,
                "Test Forum",
                "Contenu test",
                "Question",
                new Timestamp(System.currentTimeMillis())
        );

        service.ajouter(f);

        List<forum> list = service.afficher();

        assertFalse(list.isEmpty());

        forum found = list.stream()
                .filter(fr -> fr.getTitre().equals("Test Forum"))
                .findFirst()
                .orElse(null);

        assertNotNull(found);

        idForumTest = found.getId();
    }

    // ================= TEST 2 : MODIFIER =================
    @Test
    @Order(2)
    void testModifierForum() {

        forum f = new forum(
                idForumTest,
                "Test Forum",
                "Contenu modifié",
                "Question",
                new Timestamp(System.currentTimeMillis())
        );

        service.modifier(f);

        List<forum> list = service.afficher();

        boolean updated = list.stream()
                .anyMatch(fr ->
                        fr.getId() == idForumTest &&
                                fr.getContenu().equals("Contenu modifié")
                );

        assertTrue(updated);
    }

    // ================= TEST 3 : SUPPRIMER =================
    @Test
    @Order(3)
    void testSupprimerForum() {

        service.supprimer(idForumTest);

        List<forum> list = service.afficher();

        boolean exists = list.stream()
                .anyMatch(fr -> fr.getId() == idForumTest);

        assertFalse(exists);
    }

    // ================= CLEANUP PRO =================
    @AfterEach
    void cleanUp() {

        if (idForumTest != 0) {
            service.supprimer(idForumTest);
        }
    }
}