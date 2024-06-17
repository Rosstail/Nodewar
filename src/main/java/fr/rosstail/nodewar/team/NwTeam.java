package fr.rosstail.nodewar.team;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.permissionmannager.PermissionManagerHandler;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.relation.TeamRelation;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.security.Permission;
import java.util.HashMap;
import java.util.Map;

public class NwTeam {

    private final TeamModel teamModel;
    private final Map<Player, TeamMember> memberMap = new HashMap<>();

    public NwTeam(TeamModel teamModel) {
        this.teamModel = teamModel;
        PermissionManagerHandler.createGroup(teamModel.getName());
    }

    public TeamModel getModel() {
        return teamModel;
    }

    public Map<Player, TeamMember> getMemberMap() {
        return memberMap;
    }

    public Map<String, TeamRelation> getRelations() {
        return TeamDataManager.getTeamDataManager().getTeamsRelations(this);
    }
}