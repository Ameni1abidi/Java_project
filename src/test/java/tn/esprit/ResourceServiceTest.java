package tn.esprit;

import org.junit.jupiter.api.*;
import tn.esprit.entities.categorie;
import tn.esprit.entities.resources;
import tn.esprit.services.CategoryService;
import tn.esprit.services.ResourceService;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResourceServiceTest {

    private static ResourceService service = new ResourceService();
    private static CategoryService categoryService = new CategoryService();
    private static int idRessourceTest;
    private static int idCategorieTest;

    @BeforeAll
    static void setup() throws SQLException {
        // Add a test categorie
        categorie c = new categorie("TestCategorie");
        categoryService.add(c);
        List<categorie> categories = categoryService.getAll();
        idCategorieTest = categories.stream()
            .filter(cat -> cat.getNom().equals("TestCategorie"))
            .findFirst().get().getId();
    }

    @Test
    @Order(1)
    void testAjouterRessource() throws SQLException {
        resources r = new resources("TestTitre", "TestContenu", idCategorieTest, "TestType", "2023-01-01");
        service.add(r);
        List<resources> ressources = service.getAll();
        assertFalse(ressources.isEmpty());
        assertTrue(
            ressources.stream().anyMatch(res ->
                res.getTitre().equals("TestTitre"))
        );
        // Set id for next tests
        idRessourceTest = ressources.stream()
            .filter(res -> res.getTitre().equals("TestTitre"))
            .findFirst().get().getId();
    }

    @Test
    @Order(2)
    void testModifierRessource() throws SQLException {
        resources r = new resources();
        r.setId(idRessourceTest);
        r.setTitre("TitreModifie");
        r.setContenu("ContenuModifie");
        r.setCategorieId(idCategorieTest);
        r.setType("TypeModifie");
        r.setDisponibleLe("2023-02-01");
        service.update(r);
        List<resources> ressources = service.getAll();
        boolean trouve = ressources.stream()
            .anyMatch(res ->
                res.getTitre().equals("TitreModifie"));
        assertTrue(trouve);
    }

    @Test
    @Order(3)
    void testSupprimerRessource() throws SQLException {
        service.delete(idRessourceTest);
        List<resources> ressources = service.getAll();
        boolean existe = ressources.stream().anyMatch(r -> r.getId() == idRessourceTest);
        assertFalse(existe);
    }

    @AfterAll
    static void cleanUp() throws SQLException {
        // Delete the test categorie
        categoryService.delete(idCategorieTest);
    }
}