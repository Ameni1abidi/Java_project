package tn.esprit;

import tn.esprit.entities.forum;
import tn.esprit.entities.commentaire;
import tn.esprit.services.ForumService;
import tn.esprit.services.CommentaireService;

import java.sql.Timestamp;

public class Main {
    public static void main(String[] args) {

        ForumService fs = new ForumService();
        CommentaireService cs = new CommentaireService();

        // 🔹 AJOUT FORUM
        forum f = new forum(
                0,
                "Sport",
                "Real Madrid best club",
                "football",
                new Timestamp(System.currentTimeMillis())
        );
        fs.ajouter(f);

        // 🔹 AFFICHAGE FORUMS
        System.out.println("---- Forums ----");
        fs.afficher().forEach(System.out::println);

        // 🔹 AJOUT COMMENTAIRE (⚠️ ID doit exister)
        commentaire c = new commentaire(
                0,
                "Très bon sujet 🔥",
                5,
                new Timestamp(System.currentTimeMillis())
        );
        cs.ajouter(c);

        // 🔹 AFFICHAGE COMMENTAIRES
        System.out.println("---- Commentaires ----");
        cs.afficher().forEach(System.out::println);
    }
}