package tn.esprit.entities;
public class User {

    public enum Role {
        ADMIN, PROF, ETUDIANT, PARENT;

        public static Role fromString(String s) {
            return switch (s.toUpperCase()) {
                case "ADMIN"    -> ADMIN;
                case "PROF"     -> PROF;
                case "ETUDIANT" -> ETUDIANT;
                case "PARENT"   -> PARENT;
                default -> throw new IllegalArgumentException("Rôle inconnu : " + s);
            };
        }
    }

    private int    id;
    private String nom;
    private String password;
    private String email;
    private Role   role;

    // ── Constructeurs ────────────────────────────────────────────────────────
    public User() {}

    public User(String nom, String password, String email, Role role) {
        this.nom      = nom;
        this.password = password;
        this.email    = email;
        this.role     = role;
    }

    public User(int id, String nom, String password, String email, Role role) {
        this(nom, password, email, role);
        this.id = id;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────
    public int    getId()       { return id; }
    public String getNom()      { return nom; }
    public String getPassword() { return password; }
    public String getEmail()    { return email; }
    public Role   getRole()     { return role; }

    public void setId(int id)             { this.id = id; }
    public void setNom(String nom)        { this.nom = nom; }
    public void setPassword(String pw)    { this.password = pw; }
    public void setEmail(String email)    { this.email = email; }
    public void setRole(Role role)        { this.role = role; }

    @Override
    public String toString() {
        return "User{id=%d, nom='%s', email='%s', role=%s}".formatted(id, nom, email, role);
    }
}
