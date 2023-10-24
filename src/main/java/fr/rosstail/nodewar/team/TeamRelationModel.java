package fr.rosstail.nodewar.team;

public class TeamRelationModel {
    private int id;
    private int firstTeamId;
    private int secondTeamId;
    private int relation;

    public TeamRelationModel(int firstTeamId, int secondTeamId, int relation) {
        this.firstTeamId = firstTeamId;
        this.secondTeamId = secondTeamId;
        this.relation = relation;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFirstTeamId() {
        return firstTeamId;
    }

    public void setFirstTeamId(int firstTeamId) {
        this.firstTeamId = firstTeamId;
    }

    public int getSecondTeamId() {
        return secondTeamId;
    }

    public void setSecondTeamId(int secondTeamId) {
        this.secondTeamId = secondTeamId;
    }

    public int getRelation() {
        return relation;
    }

    public void setRelation(int relation) {
        this.relation = relation;
    }
}
