package fr.rosstail.nodewar.team.relation;

public class TeamRelationModel {
    private int id;
    private int firstTeamId;
    private int secondTeamId;
    private int relationTypeID;

    public TeamRelationModel(int firstTeamId, int secondTeamId, int relationTypeID) {
        this.firstTeamId = firstTeamId;
        this.secondTeamId = secondTeamId;
        this.relationTypeID = relationTypeID;
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

    public int getRelationTypeID() {
        return relationTypeID;
    }

    public void setRelationTypeID(int relationTypeID) {
        this.relationTypeID = relationTypeID;
    }
}
