package tn.esprit.entities;

public class Chapitre {
    private int id;
    private String titre;
    private int ordre;
    private String typeContenu;
    private String contenuTexte;
    private String contenuFichier;
    private int dureeEstimee; // en minutes
    private String resume;
    private int coursId;

    public Chapitre() {}

    public Chapitre(String titre, int ordre, String typeContenu, String contenuTexte,
                    String contenuFichier, int dureeEstimee, String resume, int coursId) {
        this.titre = titre;
        this.ordre = ordre;
        this.typeContenu = typeContenu;
        this.contenuTexte = contenuTexte;
        this.contenuFichier = contenuFichier;
        this.dureeEstimee = dureeEstimee;
        this.resume = resume;
        this.coursId = coursId;
    }

    // getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }
    public String getTypeContenu() { return typeContenu; }
    public void setTypeContenu(String typeContenu) { this.typeContenu = typeContenu; }
    public String getContenuTexte() { return contenuTexte; }
    public void setContenuTexte(String contenuTexte) { this.contenuTexte = contenuTexte; }
    public String getContenuFichier() { return contenuFichier; }
    public void setContenuFichier(String contenuFichier) { this.contenuFichier = contenuFichier; }
    public int getDureeEstimee() { return dureeEstimee; }
    public void setDureeEstimee(int dureeEstimee) { this.dureeEstimee = dureeEstimee; }
    public String getResume() { return resume; }
    public void setResume(String resume) { this.resume = resume; }
    public int getCoursId() { return coursId; }
    public void setCoursId(int coursId) { this.coursId = coursId; }

    @Override
    public String toString() {
        return "Chapitre{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", ordre=" + ordre +
                ", typeContenu='" + typeContenu + '\'' +
                ", contenuTexte='" + contenuTexte + '\'' +
                ", contenuFichier='" + contenuFichier + '\'' +
                ", dureeEstimee=" + dureeEstimee +
                ", resume='" + resume + '\'' +
                ", coursId=" + coursId +
                '}';
    }
}