package fr.rosstail.nodewar.team;

import java.sql.Timestamp;

public class TeamRelationModel {
    private int Id;
    private int firstTeamId;
    private int secondTeamId;
    private int relation;

    public TeamRelationModel(int firstTeamId, int secondTeamId, int relation) {
        this.firstTeamId = firstTeamId;
        this.secondTeamId = secondTeamId;
        this.relation = relation;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
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
