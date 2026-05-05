package tn.esprit.services;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BadWordsService {

    private static final Set<String> BAD_WORDS = new HashSet<>(Arrays.asList(
            // Anglais
            "fuck", "shit", "bitch", "asshole", "bastard", "damn", "crap",
            "idiot", "stupid", "hate", "kill", "ugly", "dumb", "loser",
            "moron", "fool", "jerk", "freak", "dick", "cock", "pussy",
            "nigger", "faggot", "retard", "whore", "slut",
            // Français
            "merde", "putain", "connard", "salaud", "ordure", "con",
            "idiot", "imbécile", "nul", "stupide", "crétin", "abruti",
            "ferme ta gueule", "casse toi", "je te hais", "naze", "dégage",
            "enculé", "fils de pute", "ta gueule", "va te faire",
            // Arabe translittéré
            "ahbal", "kelb", "hmar", "gahba", "weld el kahba", "sharmouta",
            "ibn el sharmouta", "kess", "zebi", "ayir"
    ));

    public boolean contientBadWord(String texte) {
        if (texte == null || texte.isBlank()) return false;
        String texteLower = texte.toLowerCase();
        for (String word : BAD_WORDS) {
            if (texteLower.contains(word.toLowerCase())) {
                System.out.println("Bad word détecté : " + word); // ← pour debug
                return true;
            }
        }
        return false;
    }

    public String nettoyerTexte(String texte) {
        if (texte == null || texte.isBlank()) return texte;
        String resultat = texte;
        for (String word : BAD_WORDS) {
            String stars = "*".repeat(word.length());
            resultat = resultat.replaceAll("(?i)" + word, stars);
        }
        return resultat;
    }
}