package fr.rosstail.nodewar.team;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
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
            System.out.println("Team loaded " + s);
            stringTeamMap.put(s, team);
        });

        stringTeamMap.forEach((s, team) -> {
            Map<Integer, TeamMemberModel> teamMemberModelMap =
                    StorageManager.getManager().selectTeamMemberModelByTeamUuid(s);
            teamMemberModelMap.forEach((s1, teamMemberModel) -> {
                System.out.println("Add member to team " + s + " " + s1 + " rank " + teamMemberModel.getRank() + " player id " + teamMemberModel.getId());
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

    public Team getTeamOfPlayer(Player player) {
        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player.getName());
        List<Team> teams = TeamDataManager.getTeamDataManager().getStringTeamMap().values().stream().filter(team ->
                team.getMemberModelMap().containsKey(playerData.getId())
        ).collect(Collectors.toList());

        if (!teams.isEmpty()) {
            if (teams.size() > 1) {
                AdaptMessage.print("The player with data id " + playerData.getId() +
                        " is in multiple teams. using the first one only", AdaptMessage.prints.WARNING);
            }
            return teams.get(0);
        }

        return null;
    }
}
