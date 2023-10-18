package fr.rosstail.nodewar.team;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.storage.StorageManager;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TeamDataManager {

    private static TeamDataManager teamDataManager;
    private final Nodewar plugin;

    private final Map<String, Team> stringTeamMap = new HashMap<>();
    private TeamDataManager(Nodewar plugin) {
        this.plugin = plugin;
    }

    public static void init(Nodewar plugin) {
        if (teamDataManager == null) {
            teamDataManager = new TeamDataManager(plugin);
        }
    }

    public void loadTeams() {
        stringTeamMap.clear();
        StorageManager.getManager().selectAllTeamModel().forEach((s, teamModel) -> {
            Team team = new Team(teamModel);
            stringTeamMap.put(s, team);
        });

        stringTeamMap.forEach((s, team) -> {
            Map<String, TeamMemberModel> teamMemberModelMap =
                    StorageManager.getManager().selectTeamMemberModelByTeamUuid(s);
            teamMemberModelMap.forEach((s1, teamMemberModel) -> {
                team.getMemberModelMap().put(s1, teamMemberModel);
            });

            Map<String, TeamRelationModel> teamRelationModelMap =
                    StorageManager.getManager().selectTeamRelationModelByTeamUuid(s);
            teamRelationModelMap.forEach((s1, teamRelationModel) -> {
                team.getRelationModelMap().put(s1, teamRelationModel);
            });
        });
    }

    public void addNewTeam(Team team) {
        getStringTeamMap().put(team.getTeamModel().getName(), team);
    }

    public void removeDeletedTeam(String teamName) {
        getStringTeamMap().remove(teamName);
    }

    public Map<String, Team> getStringTeamMap() {
        return stringTeamMap;
    }

    public static TeamDataManager getTeamDataManager() {
        return teamDataManager;
    }

    public List<Team> getTeamOfPlayer(String playerUuid) {
        return TeamDataManager.getTeamDataManager().getStringTeamMap().values().stream().filter(team ->
                team.getMemberModelMap().containsKey(playerUuid)
        ).collect(Collectors.toList());
    }
}
