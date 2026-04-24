package tn.esprit.entities;

import java.time.LocalDateTime;

public class StudentChapitreProgress {

    private int id;
    private int utilisateurId;
    private int chapitreId;

    private LocalDateTime startedAt;
    private LocalDateTime lastViewedAt;
    private LocalDateTime completedAt;

    private boolean opened;
    private boolean viewedResume;
    private boolean completed;

    private int progress;

    // GETTERS / SETTERS

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

    public boolean isOpened() { return opened; }
    public void setOpened(boolean opened) { this.opened = opened; }

    public boolean isViewedResume() { return viewedResume; }
    public void setViewedResume(boolean viewedResume) { this.viewedResume = viewedResume; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getProgress() { return progress; }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
    }
}