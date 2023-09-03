package fr.rosstail.nodewar.team;

import org.bukkit.scoreboard.Team;

import java.sql.Timestamp;

public class TeamMemberModel {
    private int Id;
    private int teamId;
    private String memberUuid;
    private int rank; //1 OWNER, 2 Admin, 3 Lieutenant, 4 Member, 5 Recruit
    private Timestamp joinDate;

    public TeamMemberModel(int teamId, String memberUuid, int rank, Timestamp joinDate) {
        this.teamId = teamId;
        this.memberUuid = memberUuid;
        this.rank = rank;
        this.joinDate = joinDate;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public String getMemberUuid() {
        return memberUuid;
    }

    public void setMemberUuid(String memberUuid) {
        this.memberUuid = memberUuid;
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
