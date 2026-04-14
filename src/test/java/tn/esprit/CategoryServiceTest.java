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
    private static String nomCategorieTest = "TestNom";

    @Test
    @Order(1)
    void testAjouterCategorie() throws SQLException {
        categorie c = new categorie(nomCategorieTest);
        service.add(c);
        List<categorie> categories = service.getAll();
        assertFalse(categories.isEmpty());
        assertTrue(
            categories.stream().anyMatch(cat ->
                cat.getNom().equals(nomCategorieTest))
        );
    }

    @Test
    @Order(2)
    void testModifierCategorie() throws SQLException {
        String nouveauNom = "NomModifie";
        service.update(nomCategorieTest, nouveauNom);
        List<categorie> categories = service.getAll();
        boolean trouve = categories.stream()
            .anyMatch(cat ->
                cat.getNom().equals(nouveauNom));
        assertTrue(trouve);
        // Update the test name for cleanup
        nomCategorieTest = nouveauNom;
    }

    @Test
    @Order(3)
    void testSupprimerCategorie() throws SQLException {
        service.delete(nomCategorieTest);
        List<categorie> categories = service.getAll();
        boolean existe = categories.stream().anyMatch(c -> c.getNom().equals(nomCategorieTest));
        assertFalse(existe);
    }

    @AfterEach
    void cleanUp() throws SQLException {
        List<categorie> categories = service.getAll();
        categories.forEach(cat -> {
            try {
                service.delete(cat.getNom());
            } catch (Exception ignored) {
            }
        });
    }
}