package fr.rosstail.nodewar.team;

import java.sql.Timestamp;

public class TeamMemberModel {
    private int id;
    private int teamId;
    private int playerId;
    private int rank; //1 OWNER, 2 Admin, 3 Lieutenant, 4 Member, 5 Recruit
    private Timestamp joinDate;

    public TeamMemberModel(int teamId, int playerId, int rank, Timestamp joinDate) {
        this.teamId = teamId;
        this.playerId = playerId;
        this.rank = rank;
        this.joinDate = joinDate;
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

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public Timestamp getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Timestamp joinDate) {
        this.joinDate = joinDate;
    }
}
