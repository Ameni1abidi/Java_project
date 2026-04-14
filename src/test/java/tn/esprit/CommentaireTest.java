package tn.esprit;

import org.junit.jupiter.api.*;

import tn.esprit.entities.commentaire;
import tn.esprit.entities.forum;
import tn.esprit.services.CommentaireService;
import tn.esprit.services.ForumService;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommentaireTest {

    static CommentaireService service;
    static ForumService forumService;

    static int idCommentaireTest;
    static int forumIdTest;

    // ================= INIT =================
    @BeforeAll
    static void setup() {

        service = new CommentaireService();
        forumService = new ForumService();

        // 🔥 créer forum obligatoire
        forum f = new forum(
                0,
                "Forum Test Commentaire",
                "Contenu forum",
                "Test",
                new Timestamp(System.currentTimeMillis())
        );

        forumService.ajouter(f);

        // récupérer ID
        List<forum> list = forumService.afficher();

        forum found = list.stream()
                .filter(fr -> fr.getTitre().equals("Forum Test Commentaire"))
                .findFirst()
                .orElse(null);

        assertNotNull(found);

        forumIdTest = found.getId();
    }

    // ================= TEST 1 : AJOUT =================
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

        commentaire found = list.stream()
                .filter(cm -> cm.getContenu().equals("Commentaire test"))
                .findFirst()
                .orElse(null);

        assertNotNull(found);

        idCommentaireTest = found.getId();
    }

    // ================= TEST 2 : MODIFIER =================
    @Test
    @Order(2)
    void testModifierCommentaire() {

        assertTrue(idCommentaireTest > 0);

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

    // ================= TEST 3 : SUPPRIMER =================
    @Test
    @Order(3)
    void testSupprimerCommentaire() {

        service.supprimer(idCommentaireTest);

        List<commentaire> list = service.afficher();

        boolean exists = list.stream()
                .anyMatch(cm -> cm.getId() == idCommentaireTest);

        assertFalse(exists);
    }

    // ================= CLEAN FINAL =================
    @AfterAll
    static void cleanUpAll() {

        try {
            // supprimer commentaires
            List<commentaire> commentaires = service.afficher();
            commentaires.stream()
                    .filter(c -> c.getContenu().contains("Commentaire"))
                    .forEach(c -> service.supprimer(c.getId()));

            // supprimer forum
            forumService.supprimer(forumIdTest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}