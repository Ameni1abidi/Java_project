package tn.esprit;

import org.junit.jupiter.api.*;
import tn.esprit.entities.categorie;
import tn.esprit.services.CategoryService;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CategoryServiceTest {

    private static CategoryService service = new CategoryService();
    private static int idCategorieTest;

    @Test
    @Order(1)
    void testAjouterCategorie() throws SQLException {
        categorie c = new categorie("TestNom");
        service.add(c);
        List<categorie> categories = service.getAll();
        assertFalse(categories.isEmpty());
        assertTrue(
            categories.stream().anyMatch(cat ->
                cat.getNom().equals("TestNom"))
        );
        // Set id for next tests
        idCategorieTest = categories.stream()
            .filter(cat -> cat.getNom().equals("TestNom"))
            .findFirst().get().getId();
    }

    @Test
    @Order(2)
    void testModifierCategorie() throws SQLException {
        categorie c = new categorie();
        c.setId(idCategorieTest);
        c.setNom("NomModifie");
        service.update(c);
        List<categorie> categories = service.getAll();
        boolean trouve = categories.stream()
            .anyMatch(cat ->
                cat.getNom().equals("NomModifie"));
        assertTrue(trouve);
    }

    @Test
    @Order(3)
    void testSupprimerCategorie() throws SQLException {
        service.delete(idCategorieTest);
        List<categorie> categories = service.getAll();
        boolean existe = categories.stream().anyMatch(c -> c.getId() == idCategorieTest);
        assertFalse(existe);
    }

    @AfterEach
    void cleanUp() throws SQLException {
        List<categorie> categories = service.getAll();
        if (!categories.isEmpty()) {
            categorie last = categories.get(categories.size() - 1);
            service.delete(last.getId());
        }
    }
}