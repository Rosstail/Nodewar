package fr.rosstail.nodewar.team.teammanagers;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.permissionmannager.PermissionManager;
import fr.rosstail.nodewar.team.*;
import fr.rosstail.nodewar.team.relation.NwTeamRelationRequest;
import fr.rosstail.nodewar.team.type.UcTeam;
import me.ulrich.clans.Clans;
import me.ulrich.clans.api.ClanAPIManager;
import me.ulrich.clans.data.ClanData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class UcTeamManager implements NwITeamManager {
    private final Map<String, UcTeam> stringTeamMap = new HashMap<>();

    private final Clans clans;
    private final ClanAPIManager clanAPI;


    UcTeamManager() {
        final Plugin plugin = Nodewar.getInstance().getServer().getPluginManager().getPlugin("UltimateClans");
        if (!(plugin instanceof Clans)) {
            throw new Error("Ultimate Clans is not in the server.");
        }
        clans = (Clans) plugin;
        clanAPI = clans.getClanAPI();
    }

    @Override
    public void loadTeams() {
        clanAPI.getAllClansData().forEach(clanData -> {
            UcTeam ucTeam = new UcTeam(clanData);
            stringTeamMap.put(ucTeam.getName(), ucTeam);
        });
    }

    @Override
    public NwITeam getPlayerTeam(Player player) {
        ClanData playerClanData =  clanAPI.getAllClansData().stream().filter(clanData -> clanData.getMembers().contains(player.getUniqueId())).findFirst().orElse(null);
        UcTeam ucTeam;

        if (playerClanData == null) {
            return null;
        }

        if (stringTeamMap.values().stream().noneMatch(elementUcTeam -> (elementUcTeam.getClanData() == playerClanData))) {
            ucTeam = new UcTeam(playerClanData);
            stringTeamMap.put(ucTeam.getName(), ucTeam);
            return ucTeam;
        }

        return stringTeamMap.values().stream().filter(elementUcTeam -> (elementUcTeam.getClanData() == playerClanData)).findFirst().get();
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
        UcTeam ucTeam = (UcTeam) team;
        stringTeamMap.put(name, ucTeam);

        PermissionManager.getManager().createGroup(name);

        Player player;
        if (((UcTeam) team).getClanData().getLeader() != null) {
            player = Bukkit.getPlayer(((UcTeam) team).getClanData().getLeader());
            PermissionManager.getManager().setPlayerGroup(player, ucTeam);
        }
    }

    @Override
    public void removeITeam(String name) {
        NwITeam nwITeamToDelete = stringTeamMap.get(name);

        PermissionManager.getManager().deleteGroup(name);
        nwITeamToDelete.getOnlineMemberMap().forEach((player, teamMember) -> {
            NwITeam currentPlayerTeam = TeamManager.getManager().getPlayerTeam(player);

            PermissionManager.getManager().removePlayerGroup(player,
                    currentPlayerTeam != null
                            ? "nw_" + currentPlayerTeam.getName()
                            : null
            );
        });
        stringTeamMap.remove(name);
    }

    @Override
    public HashSet<Object> getInviteHashSet() {
        return new HashSet<>();
    }

    @Override
    public void addTeamInvite(NwITeam nwITeam, Player sender, @NotNull Player receiver) {
    }

    @Override
    public void removeTeamInvite(List<NwTeamInvite> inviteList) {
    }

    @Override
    public void addTeamMember(NwITeam nwITeam, Player player) {
        PermissionManager.getManager().setPlayerGroup(player, nwITeam);
    }

    @Override
    public void deleteTeamMember(NwITeam nwITeam, Player player, boolean disband) {
        PermissionManager.getManager().removePlayerGroup(player, null);
    }

    @Override
    public TeamIRelation getRelation(NwITeam firstTeam, NwITeam secondTeam) {
        if (firstTeam == null) {
            return null;
        }
        return firstTeam.getIRelation(secondTeam);
    }

    @Override
    public Map<NwITeam, TeamIRelation> getRelationMap(NwITeam nwITeam) {
        return nwITeam.getRelations();
    }

    @Override
    public void createRelation(NwITeam originITeam, NwITeam targetITeam, RelationType type) {
        UcTeam originUcTeam = (UcTeam) originITeam;
        ClanData originClan = originUcTeam.getClanData();
        UcTeam targetUcTeam = (UcTeam) targetITeam;
        ClanData targetClan = targetUcTeam.getClanData();
        switch (type) {
            case ALLY:
                clanAPI.allyAdd(originClan.getId(), targetClan.getId());
                break;
            case TRUCE:
            case NEUTRAL:
                clanAPI.allyRemove(originClan.getId(), targetClan.getId());
                clanAPI.rivalRemove(originClan.getId(), targetClan.getId());
                break;
            case ENEMY:
                clanAPI.rivalAdd(originClan.getId(), targetClan.getId());
                break;
        }
    }

    @Override
    public void deleteRelation(NwITeam originITeam, NwITeam targetITeam) {
        UcTeam originUcTeam = (UcTeam) originITeam;
        ClanData originClan = originUcTeam.getClanData();
        UcTeam targetUcTeam = (UcTeam) targetITeam;
        ClanData targetClan = targetUcTeam.getClanData();
        clanAPI.allyRemove(originClan.getId(), targetClan.getId());
        clanAPI.rivalRemove(originClan.getId(), targetClan.getId());
    }

    @Override
    public NwTeamRelationRequest getTeamRelationRequest(NwITeam firstTeam, NwITeam secondTeam) {
        throw new RuntimeException("UltimateClans shoud use its own relation request system");
    }

    @Override
    public Set<NwTeamRelationRequest> getTeamRelationRequestSet() {
        throw new RuntimeException("UltimateClans shoud use its own relation request system");
    }

    @Override
    public void createRelationRequest(NwITeam originITeam, NwITeam targetITeam, RelationType type) {
        throw new RuntimeException("UltimateClans shoud use its own relation request system");
    }

    @Override
    public void deleteRelationRequest(NwITeam originTeam, NwITeam targetITeam) {
        throw new RuntimeException("UltimateClans shoud use its own relation request system");
    }
}
