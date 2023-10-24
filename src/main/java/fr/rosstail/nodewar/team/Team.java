package fr.rosstail.nodewar.team;

import java.util.HashMap;
import java.util.Map;

public class Team {

    private TeamModel teamModel;
    private final Map<Integer, TeamMemberModel> memberModelMap = new HashMap<>();
    private final Map<String, TeamRelationModel> relationModelMap = new HashMap<>();
    public Team(String name, String display) {
        teamModel = new TeamModel(name, display);
    }

    public Team(TeamModel teamModel) {
        this.teamModel = teamModel;
    }

    public TeamModel getTeamModel() {
        return teamModel;
    }

    public void setTeamModel(TeamModel teamModel) {
        this.teamModel = teamModel;
    }

    public Map<Integer, TeamMemberModel> getMemberModelMap() {
        return memberModelMap;
    }

    public Map<String, TeamRelationModel> getRelationModelMap() {
        return relationModelMap;
    }
}
