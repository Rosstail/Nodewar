package fr.rosstail.nodewar.team.member;

import java.sql.Timestamp;

public class TeamMemberModel {
    private int id;
    private int teamId;
    private int playerId;
    private int numRank;
    private Timestamp joinDate;
    private String username;

    public TeamMemberModel(int teamId, int playerId, int numRank, Timestamp joinDate, String username) {
        this.teamId = teamId;
        this.playerId = playerId;
        this.numRank = numRank;
        this.joinDate = joinDate;
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getNumRank() {
        return numRank;
    }

    public void setNumRank(int numRank) {
        this.numRank = numRank;
    }

    public Timestamp getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Timestamp joinDate) {
        this.joinDate = joinDate;
    }
}
