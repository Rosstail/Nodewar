package fr.rosstail.nodewar.team;

import java.util.HashMap;
import java.util.Map;

public class Team {

    private TeamModel teamModel;
    private final Map<String, TeamMemberModel> memberModelMap = new HashMap<>();
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

    public Map<String, TeamMemberModel> getMemberModelMap() {
        return memberModelMap;
    }
}
