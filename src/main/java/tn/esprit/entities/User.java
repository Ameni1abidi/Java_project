package tn.esprit.entities;
public class User {

    public enum Role {
        ROLE_ADMIN, ROLE_PROF, ROLE_ETUDIANT, ROLE_PARENT;

        public static Role fromString(String s) {
            return switch (s.toUpperCase()) {
                case "ROLE_ADMIN"                -> ROLE_ADMIN;
                case "ROLE_PROF", "ROLE_TEACHER" -> ROLE_PROF;
                case "ROLE_ETUDIANT", "ROLE_STUDENT", "ROLE_USER" -> ROLE_ETUDIANT;
                case "ROLE_PARENT"               -> ROLE_PARENT;
                case "ADMIN"                     -> ROLE_ADMIN;
                case "PROF", "TEACHER"           -> ROLE_PROF;
                case "ETUDIANT", "STUDENT", "USER" -> ROLE_ETUDIANT;
                case "PARENT"                    -> ROLE_PARENT;
                default                          -> ROLE_ETUDIANT;

            };
        }
    }

    private int    id;
    private String nom;
    private String password;
    private String email;
    private Role   role;
    private String telephone;

    // ── Constructeurs ────────────────────────────────────────────────────────
    public User() {}

    public User(String nom, String password, String email, String telephone,Role role) {
        this.nom      = nom;
        this.password = password;
        this.email    = email;
        this.telephone = telephone;
        this.role     = role;
    }

    public User(int id, String nom, String password, String email,String telephone,Role role) {
        this(nom, password, email, telephone, role);
        this.id = id;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────
    public int    getId()       { return id; }
    public String getNom()      { return nom; }
    public String getPassword() { return password; }
    public String getEmail()    { return email; }
    public Role   getRole()     { return role; }
    public String getTelephone() { return telephone; }

    public void setId(int id)             { this.id = id; }
    public void setNom(String nom)        { this.nom = nom; }
    public void setPassword(String pw)    { this.password = pw; }
    public void setEmail(String email)    { this.email = email; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setRole(Role role)        { this.role = role; }

    @Override
    public String toString() {
        return "User{id=%d, nom='%s', email='%s', telephone='%s', role=%s}".formatted(id, nom, email, telephone,role);
    }
}
