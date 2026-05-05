package tn.esprit.entities;

import java.time.LocalDateTime;

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
    private String telephone;
    private Role   role;
    private boolean verified;
    private boolean blocked;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    // ── Constructeurs ────────────────────────────────────────────────────────
    public User() {}

    public User(String nom, String password, String email, Role role) {
        this.nom      = nom;
        this.password = password;
        this.email    = email;
        this.role     = role;
        this.verified = false;
        this.blocked = false;
        this.status = "PENDING";
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
    public String getTelephone() { return telephone; }
    public Role   getRole()     { return role; }
    public boolean isVerified() { return verified; }
    public boolean isBlocked()  { return blocked; }
    public String getStatus()   { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }

    public void setId(int id)             { this.id = id; }
    public void setNom(String nom)        { this.nom = nom; }
    public void setPassword(String pw)    { this.password = pw; }
    public void setEmail(String email)    { this.email = email; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setRole(Role role)        { this.role = role; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    @Override
    public String toString() {
        return "User{id=%d, nom='%s', email='%s', role=%s, status=%s, blocked=%s, verified=%s}"
                .formatted(id, nom, email, role, status, blocked, verified);
    }
}
