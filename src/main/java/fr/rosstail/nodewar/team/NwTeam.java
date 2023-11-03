package fr.rosstail.nodewar.team;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class NwTeam {

    private final TeamModel teamModel;
    private final Map<Player, TeamMember> memberMap = new HashMap<>();
    private final Map<String, TeamRelation> relationMap = new HashMap<>();

    public NwTeam(String name, String display) {
        teamModel = new TeamModel(name, display);
    }

    public NwTeam(TeamModel teamModel) {
        this.teamModel = teamModel;
    }

    public TeamModel getModel() {
        return teamModel;
    }

    public Map<Player, TeamMember> getMemberMap() {
        return memberMap;
    }

    public Map<String, TeamRelation> getRelationMap() {
        return relationMap;
    }
}
