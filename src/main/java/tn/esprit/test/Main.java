package tn.esprit;

import tn.esprit.entities.forum;
import tn.esprit.services.ForumService;

import java.sql.Timestamp;

public class Main {
    public static void main(String[] args) {

        ForumService fs = new ForumService();

        // 🔹 AJOUT
        forum f = new forum(0, "Sport", "Real Madrid best club", "football", new Timestamp(System.currentTimeMillis()));
        fs.ajouter(f);

        // 🔹 AFFICHAGE
        fs.afficher().forEach(System.out::println);
    }
}