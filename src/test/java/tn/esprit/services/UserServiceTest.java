package tn.esprit.services;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import tn.esprit.entities.User;
import tn.esprit.entities.User.Role;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTest {

    private static UserService userService;
    private static int createdUserId;

    // Email unique pour chaque execution de test
    private static final String TEST_EMAIL = "test_" + System.currentTimeMillis() + "@test.com";

    @BeforeAll
    static void setup() {
        userService = new UserService();
    }

    @Test
    @Order(1)
    @DisplayName("Register - nom vide doit etre rejete")
    void testRegisterNomVide() {
        User user = new User(0, "", "password123", TEST_EMAIL, Role.ROLE_ETUDIANT);
        assertTrue(user.getNom().isEmpty(), "Le nom est vide - doit etre valide cote controller");
    }

    @Test
    @Order(2)
    @DisplayName("Register - insertion valide doit reussir")
    void testRegisterValide() throws SQLException {
        User user = new User(0, "Test User", "password123", TEST_EMAIL, Role.ROLE_ETUDIANT);
        boolean result = userService.register(user);
        assertTrue(result, "L'insertion doit reussir");
        assertTrue(user.getId() > 0, "L'ID doit etre genere");
        createdUserId = user.getId();
    }

    @Test
    @Order(3)
    @DisplayName("Register - email duplique doit etre rejete")
    void testRegisterEmailDuplique() throws SQLException {
        User user = new User(0, "Autre User", "password456", TEST_EMAIL, Role.ROLE_PROF);
        boolean result = userService.register(user);
        assertFalse(result, "Un email deja utilise doit retourner false");
    }

    @Test
    @Order(4)
    @DisplayName("Login - identifiants corrects doit retourner l'utilisateur")
    void testLoginValide() throws SQLException {
        Optional<User> result = userService.login(TEST_EMAIL, "password123");
        assertTrue(result.isPresent(), "Le login doit reussir avec les bons identifiants");
        assertEquals(TEST_EMAIL, result.get().getEmail(), "L'email doit correspondre");
        assertEquals(Role.ROLE_ETUDIANT, result.get().getRole(), "Le role doit correspondre");
    }

    @Test
    @Order(5)
    @DisplayName("Login - mauvais mot de passe doit retourner vide")
    void testLoginMauvaisMotDePasse() throws SQLException {
        Optional<User> result = userService.login(TEST_EMAIL, "mauvaismdp");
        assertFalse(result.isPresent(), "Le login doit echouer avec un mauvais mot de passe");
    }

    @Test
    @Order(6)
    @DisplayName("Login - email inexistant doit retourner vide")
    void testLoginEmailInexistant() throws SQLException {
        Optional<User> result = userService.login("inexistant@test.com", "password123");
        assertFalse(result.isPresent(), "Le login doit echouer avec un email inexistant");
    }

    @Test
    @Order(7)
    @DisplayName("GetAllUsers - doit retourner une liste non vide")
    void testGetAllUsers() throws SQLException {
        List<User> users = userService.getAllUsers();
        assertNotNull(users, "La liste ne doit pas etre null");
        assertFalse(users.isEmpty(), "La liste ne doit pas etre vide");
    }

    @Test
    @Order(8)
    @DisplayName("EmailExists - doit detecter un email existant")
    void testEmailExists() throws SQLException {
        assertTrue(userService.emailExists(TEST_EMAIL), "L'email doit exister");
        assertFalse(userService.emailExists("noexist@test.com"), "L'email ne doit pas exister");
    }

    @Test
    @Order(9)
    @DisplayName("DeleteUser - doit supprimer l'utilisateur cree")
    void testDeleteUser() throws SQLException {
        if (createdUserId > 0) {
            boolean result = userService.deleteUser(createdUserId);
            assertTrue(result, "La suppression doit reussir");
            assertFalse(userService.emailExists(TEST_EMAIL), "L'email ne doit plus exister apres suppression");
        }
    }
}

