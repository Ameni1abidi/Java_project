package tn.esprit.entities;

import java.time.LocalDateTime;

public class StudentChapitreProgress {

    private int id;

    private int utilisateurId;
    private int chapitreId;

    private LocalDateTime startedAt;
    private LocalDateTime lastViewedAt;
    private LocalDateTime completedAt;

    private int timeSpentSeconds;

    // ===== GETTERS & SETTERS =====

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }

    public int getChapitreId() { return chapitreId; }
    public void setChapitreId(int chapitreId) { this.chapitreId = chapitreId; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getLastViewedAt() { return lastViewedAt; }
    public void setLastViewedAt(LocalDateTime lastViewedAt) { this.lastViewedAt = lastViewedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public int getTimeSpentSeconds() { return timeSpentSeconds; }
    public void setTimeSpentSeconds(int timeSpentSeconds) {
        this.timeSpentSeconds = Math.max(0, timeSpentSeconds);
    }

    // ===== HELPERS =====
    public boolean isCompleted() {
        return completedAt != null;
    }
}
