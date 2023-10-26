package fr.rosstail.nodewar.team;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TeamDataManager {

    private static TeamDataManager teamDataManager;
    private final Nodewar plugin;

    private final Map<String, NwTeam> stringTeamMap = new HashMap<>();

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
            NwTeam nwTeam = new NwTeam(teamModel);
            stringTeamMap.put(s, nwTeam);
        });

        stringTeamMap.forEach((s, team) -> {
            Map<Integer, TeamMemberModel> teamMemberModelMap =
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

    public void addNewTeam(NwTeam nwTeam) {
        getStringTeamMap().put(nwTeam.getTeamModel().getName(), nwTeam);
    }

    public void removeDeletedTeam(String teamName) {
        getStringTeamMap().remove(teamName);
    }

    public Map<String, NwTeam> getStringTeamMap() {
        return stringTeamMap;
    }

    public static TeamDataManager getTeamDataManager() {
        return teamDataManager;
    }

    public NwTeam getTeamOfPlayer(Player player) {
        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player.getName());
        List<NwTeam> nwTeams = TeamDataManager.getTeamDataManager().getStringTeamMap().values().stream().filter(team ->
                team.getMemberModelMap().containsKey(playerData.getId())
        ).collect(Collectors.toList());

        if (!nwTeams.isEmpty()) {
            if (nwTeams.size() > 1) {
                AdaptMessage.print("The player with data id " + playerData.getId() +
                        " is in multiple teams. using the first one only", AdaptMessage.prints.WARNING);
            }
            return nwTeams.get(0);
        }

        return null;
    }
}
