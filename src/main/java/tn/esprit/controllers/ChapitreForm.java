package tn.esprit.controllers;

import tn.esprit.entities.Chapitre;

public class ChapitreForm {

    private Chapitre chapitre;
    private int coursId;

    public void initData(Chapitre ch, int coursId) {

        this.chapitre = ch;
        this.coursId = coursId;

        if (ch == null) {
            System.out.println("MODE AJOUT");
        } else {
            System.out.println("MODE EDIT");
        }

        System.out.println("Cours ID = " + coursId);
    }
}
