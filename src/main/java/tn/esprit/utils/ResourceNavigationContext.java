package tn.esprit.utils;

public final class ResourceNavigationContext {
    private static Integer chapitreId;
    private static String chapitreTitre;
    private static boolean studentMode;

    private ResourceNavigationContext() {
    }

    public static void openForChapitre(int chapitreIdValue, String titre, boolean isStudentMode) {
        chapitreId = chapitreIdValue;
        chapitreTitre = titre;
        studentMode = isStudentMode;
    }

    public static Integer getChapitreId() {
        return chapitreId;
    }

    public static String getChapitreTitre() {
        return chapitreTitre;
    }

    public static boolean isStudentMode() {
        return studentMode;
    }

    public static void clear() {
        chapitreId = null;
        chapitreTitre = null;
        studentMode = false;
    }
}
