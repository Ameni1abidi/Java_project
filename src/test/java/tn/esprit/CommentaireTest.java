package tn.esprit;

import org.junit.jupiter.api.*;
import tn.esprit.entities.commentaire;
import tn.esprit.services.CommentaireService;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommentaireTest {

    static CommentaireService service;
    static int idCommentaireTest;
    static int forumIdTest = 1; // ⚠️ doit exister dans DB

    @BeforeAll
    static void setup() {
        service = new CommentaireService();
    }

    // ================= AJOUT =================
    @Test
    @Order(1)
    void testAjouterCommentaire() {

        commentaire c = new commentaire(
                0,
                "Commentaire test",
                forumIdTest,
                new Timestamp(System.currentTimeMillis())
        );

        service.ajouter(c);

        List<commentaire> list = service.afficher();

        assertFalse(list.isEmpty());

        commentaire found = list.stream()
                .filter(cm -> cm.getContenu().equals("Commentaire test"))
                .findFirst()
                .orElse(null);

        assertNotNull(found);

        idCommentaireTest = found.getId();
    }

    // ================= MODIFIER =================
    @Test
    @Order(2)
    void testModifierCommentaire() {

        commentaire c = new commentaire(
                idCommentaireTest,
                "Commentaire modifié",
                forumIdTest,
                new Timestamp(System.currentTimeMillis())
        );

        service.modifier(c);

        List<commentaire> list = service.afficher();

        boolean updated = list.stream()
                .anyMatch(cm ->
                        cm.getId() == idCommentaireTest &&
                                cm.getContenu().equals("Commentaire modifié")
                );

        assertTrue(updated);
    }

    // ================= SUPPRIMER =================
    @Test
    @Order(3)
    void testSupprimerCommentaire() {

        service.supprimer(idCommentaireTest);

        List<commentaire> list = service.afficher();

        boolean exists = list.stream()
                .anyMatch(cm -> cm.getId() == idCommentaireTest);

        assertFalse(exists);
    }

    // ================= CLEANUP =================
    @AfterEach
    void cleanUp() {

        List<commentaire> list = service.afficher();

        if (!list.isEmpty()) {
            commentaire last = list.get(list.size() - 1);
            service.supprimer(last.getId());
        }
    }
}