package tn.esprit.entities;

public class Reaction {
    private int id;
    private int commentaireId;
    private String type;

    public Reaction(int id, int commentaireId, String type) {
        this.id = id;
        this.commentaireId = commentaireId;
        this.type = type;
    }

    public int getCommentaireId() { return commentaireId; }
    public String getType() { return type; }
}