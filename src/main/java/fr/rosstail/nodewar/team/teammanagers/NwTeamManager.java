package fr.rosstail.nodewar.team.teammanagers;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.permissionmannager.PermissionManager;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.player.PlayerModel;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.*;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.rank.NwTeamRank;
import fr.rosstail.nodewar.team.relation.NwTeamRelation;
import fr.rosstail.nodewar.team.relation.NwTeamRelationRequest;
import fr.rosstail.nodewar.team.relation.TeamRelationModel;
import fr.rosstail.nodewar.team.type.NwTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.*;

public class NwTeamManager implements NwITeamManager {
    private final Map<String, NwTeam> stringTeamMap = new HashMap<>();

    private int teamInviteExpirationScheduler;
    private final HashSet<NwTeamInvite> teamInviteHashSet = new HashSet<>();
    private final Set<NwTeamRelationRequest> relationRequestHashSet = new HashSet<>();

    public NwTeamManager() {
        startInviteExpirationHandler();
    }

    @Override
    public void loadTeams() {
        stringTeamMap.clear();
        StorageManager.getManager().selectAllTeamModel().forEach((s, teamModel) -> {
            NwTeam nwTeam = new NwTeam(teamModel);
            stringTeamMap.put(s, nwTeam);
        });

        stringTeamMap.forEach((s, team) -> {
            Map<String, TeamMemberModel> teamMemberModelMap =
                    StorageManager.getManager().selectAllTeamMemberModel(s);
            teamMemberModelMap.forEach((s1, teamMemberModel) -> {
                team.getModel().getTeamMemberModelMap().put(teamMemberModel.getId(), teamMemberModel);
            });

            Map<String, TeamRelationModel> teamRelationModelMap =
                    StorageManager.getManager().selectTeamRelationModelByTeamUuid(s);
            teamRelationModelMap.forEach((s1, teamRelationModel) -> {
                team.getModel().getTeamRelationModelMap().put(teamRelationModel.getSecondTeamId(), teamRelationModel);
            });
        });

        StorageManager.getManager().selectAllTeamRelationModel().forEach(model -> {
            NwTeam originTeam = stringTeamMap.values().stream().filter(team -> (team.getID() == model.getFirstTeamId())).findFirst().get();
            NwTeam targetTeam = stringTeamMap.values().stream().filter(team -> (team.getID() == model.getSecondTeamId())).findFirst().get();
            RelationType type = Arrays.stream(RelationType.values()).filter(relationType -> relationType.getWeight() == model.getRelationTypeID()).findFirst().get();
            NwTeamRelation iRelation = new NwTeamRelation(originTeam, targetTeam, type, model);

            originTeam.addRelation(targetTeam, iRelation);

        });
    }

    @Override
    public NwITeam getPlayerTeam(Player player) {
        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player.getName());
        List<NwITeam> nwTeams = new ArrayList<>(TeamManager.getManager().getStringTeamMap().values());

        return nwTeams.stream().filter(team ->
                team.getMemberMap().values().stream().anyMatch(
                        teamMemberModel -> teamMemberModel.getModel().getPlayerId() == playerData.getId())
        ).findFirst().orElse(null);
    }

    @Override
    public NwITeam getTeam(String name) {
        return stringTeamMap.get(name);
    }

    @Override
    public Map<String, NwITeam> getStringITeamMap() {
        return new HashMap<>(stringTeamMap);
    }


    @Override
    public void addITeam(String name, NwITeam team) {
        NwTeam nwTeam = (NwTeam) team;
        stringTeamMap.put(name, nwTeam);
        Player player;

        if (team.getOnlineMemberMap().entrySet().stream().anyMatch(teamMember -> teamMember.getValue().getRank() == NwTeamRank.OWNER)) {
            player = team.getOnlineMemberMap().entrySet().stream().filter(playerTeamMemberEntry -> playerTeamMemberEntry.getValue().getRank() == NwTeamRank.OWNER).findFirst().get().getKey();
            PermissionManager.getManager().setPlayerGroup(player.getName(), player.getUniqueId(), team);
        }
    }

    @Override
    public void removeITeam(String name) {
        NwITeam nwITeam = stringTeamMap.get(name);

        nwITeam.getOnlineMemberMap().forEach((player, teamMember) -> {
            PermissionManager.getManager().removePlayerGroup(player.getName(), player.getUniqueId(), null);
        });
        stringTeamMap.remove(name);
    }

    @Override
    public HashSet<Object> getInviteHashSet() {
        return new HashSet<>();
    }

    @Override
    public void addTeamInvite(NwITeam nwITeam, Player sender, @NotNull Player receiver) {
        teamInviteHashSet.add(new NwTeamInvite((NwTeam) nwITeam, receiver));
    }

    @Override
    public void removeTeamInvite(List<NwTeamInvite> invites) {
        invites.forEach(teamInviteHashSet::remove);
    }

    @Override
    public TeamMember addOnlineTeamMember(NwITeam nwITeam, Player player) {
        TeamMemberModel teamMemberModel = addTeamMember(nwITeam, player.getName());
        TeamMember teamMember = new TeamMember(player, nwITeam, teamMemberModel);
        nwITeam.getOnlineMemberMap().put(player, teamMember);
        return teamMember;
    }

    @Override
    public TeamMemberModel addTeamMember(NwITeam nwITeam, String playerName) {
        NwTeam nwTeam = (NwTeam) nwITeam;
        String playerUUID = PlayerDataManager.getPlayerUUIDFromName(playerName);
        PlayerModel playerModel = StorageManager.getManager().selectPlayerModel(playerUUID);
        TeamMemberModel teamMemberModel = new TeamMemberModel(nwTeam.getID(), playerModel.getId(), 1, new Timestamp(System.currentTimeMillis()), playerName);
        StorageManager.getManager().insertTeamMemberModel(teamMemberModel);
        nwTeam.getModel().getTeamMemberModelMap().put(teamMemberModel.getId(), teamMemberModel);
        PermissionManager.getManager().setPlayerGroup(playerName, UUID.fromString(playerUUID), nwTeam);
        return teamMemberModel;
    }

    @Override
    public void deleteOnlineTeamMember(NwITeam nwITeam, Player player, boolean disband) {
        NwTeam nwTeam = (NwTeam) nwITeam;
        deleteTeamMember(nwITeam, player.getName(), disband);
        nwTeam.getOnlineMemberMap().remove(player);
    }

    @Override
    public void deleteTeamMember(NwITeam nwITeam, String playerName, boolean disband) {
        NwTeam nwTeam = (NwTeam) nwITeam;
        String playerUuid = PlayerDataManager.getPlayerUUIDFromName(playerName);
        int playerModelId = StorageManager.getManager().selectPlayerModel(playerUuid).getId();
        int teamMemberModelID = nwITeam.getMemberMap().get(playerName).getModel().getId();

        nwTeam.getModel().getTeamMemberModelMap().remove(teamMemberModelID);
        if (!disband) {
            StorageManager.getManager().deleteTeamMemberModel(playerModelId);
        }
        PermissionManager.getManager().removePlayerGroup(playerName, null, null);
    }

    @Override
    public TeamIRelation getRelation(NwITeam firstTeam, NwITeam secondTeam) {
        return firstTeam.getIRelation(secondTeam);
    }

    @Override
    public Map<NwITeam, TeamIRelation> getRelationMap(NwITeam nwITeam) {
        Map<NwITeam, TeamIRelation> relations = nwITeam.getRelations();
        stringTeamMap.values().stream().filter(entryTeam -> (
                entryTeam.getIRelation(nwITeam) != null
        )).forEach(filteredTeam -> {
            relations.put(filteredTeam, filteredTeam.getIRelation(nwITeam));
        });

        return relations;
    }

    @Override
    public void createRelation(NwITeam originITeam, NwITeam targetITeam, RelationType type) {
        NwTeam originNwTeam = (NwTeam) originITeam;
        NwTeam targetNwTeam = (NwTeam) targetITeam;

        TeamRelationModel relationModel = new TeamRelationModel(originNwTeam.getID(), targetITeam.getID(), type.getWeight());
        NwTeamRelation teamRelation = new NwTeamRelation(originITeam, targetITeam, type, relationModel);
        originNwTeam.addRelation(targetNwTeam, teamRelation);
        StorageManager.getManager().insertTeamRelationModel(relationModel);
    }

    @Override
    public void deleteRelation(NwITeam originITeam, NwITeam targetITeam) {
        NwTeam originNwTeam = (NwTeam) originITeam;
        NwTeam targetNwTeam = (NwTeam) targetITeam;

        originNwTeam.removeRelation(targetNwTeam);
        StorageManager.getManager().deleteTeamRelationModel(originNwTeam.getIRelation(targetITeam).getID());
    }

    @Override
    public NwTeamRelationRequest getTeamRelationRequest(NwITeam firstTeam, NwITeam secondTeam) {
        return relationRequestHashSet.stream().filter(relationRequest -> (
                relationRequest.getSenderTeam() == firstTeam && relationRequest.getTargetTeam() == secondTeam
        )).findFirst().orElse(null);
    }

    @Override
    public Set<NwTeamRelationRequest> getTeamRelationRequestSet() {
        return relationRequestHashSet;
    }

    @Override
    public void createRelationRequest(NwITeam originITeam, NwITeam targetITeam, RelationType type) {
        NwTeamRelationRequest nwTeamRelationRequest = new NwTeamRelationRequest(originITeam, targetITeam, type);
        relationRequestHashSet.add(nwTeamRelationRequest);
    }

    @Override
    public void deleteRelationRequest(NwITeam originTeam, NwITeam targetITeam) {
        relationRequestHashSet.removeIf(relationRequest -> (
                        relationRequest.getSenderTeam() == originTeam &&
                                relationRequest.getTargetTeam() == targetITeam
                )
        );
    }

    private void startInviteExpirationHandler() {
        Runnable handleRequestExpiration = () -> {

            teamInviteHashSet.removeIf(nwTeamInvite -> nwTeamInvite.getExpirationDateTime() <= System.currentTimeMillis());
            relationRequestHashSet.removeIf(nwTeamRelationInvite -> nwTeamRelationInvite.getExpirationDateTime() <= System.currentTimeMillis());
        };

        teamInviteExpirationScheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Nodewar.getInstance(), handleRequestExpiration, 1L, 1L);
    }
}
