package fr.rosstail.nodewar.team;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.permissionmannager.PermissionManagerHandler;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.relation.TeamRelation;
import fr.rosstail.nodewar.team.relation.TeamRelationManager;
import fr.rosstail.nodewar.team.relation.TeamRelationModel;
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
                team.getModel().getTeamMemberModelMap().put(s1, teamMemberModel);
            });

            Map<String, TeamRelationModel> teamRelationModelMap =
                    StorageManager.getManager().selectTeamRelationModelByTeamUuid(s);
            teamRelationModelMap.forEach((s1, teamRelationModel) -> {
                team.getModel().getTeamRelationModelMap().put(teamRelationModel.getSecondTeamId(), teamRelationModel);
            });
        });
    }

    public Map<String, TeamRelation> getTeamsRelations(NwTeam team) {
        return TeamRelationManager.getTeamRelationManager().getTeamRelationMap(team);
    }

    public void addNewTeam(NwTeam nwTeam) {
        PermissionManagerHandler.createGroup(nwTeam.getModel().getName());
        getStringTeamMap().put(nwTeam.getModel().getName(), nwTeam);
    }

    public void deleteTeam(String teamName) {
        NwTeam team = getStringTeamMap().get(teamName);
        getStringTeamMap().remove(teamName);

        PlayerDataManager.getPlayerDataMap().values().stream().filter(playerData ->
                (playerData.getTeam() == team)).forEach(this::deleteTeamMember);

        // TeamRelationManager;
        PermissionManagerHandler.deleteGroup(teamName);
        StorageManager.getManager().deleteTeamModel(team.getModel().getId());
    }

    public void deleteTeamMember(PlayerData playerData) {
        StorageManager.getManager().deleteTeamMemberModel(playerData.getId());
        playerData.setTeam(null);
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
                team.getModel().getTeamMemberModelMap().containsKey(playerData.getId())
        ).collect(Collectors.toList());

        if (!nwTeams.isEmpty()) {
            if (nwTeams.size() > 1) {
                AdaptMessage.print("The player with data id " + playerData.getId() +
                        " is in multiple teams. using the first one only", AdaptMessage.prints.WARNING);
            } else {
                AdaptMessage.print("The player " + player.getName() + " has a team !", AdaptMessage.prints.OUT);
            }
            return nwTeams.get(0);
        } else {
            AdaptMessage.print("PLayer " + player.getName() + " is in no team", AdaptMessage.prints.WARNING);
        }

        return null;
    }
}
