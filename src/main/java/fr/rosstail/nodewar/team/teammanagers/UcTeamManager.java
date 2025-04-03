package fr.rosstail.nodewar.team.teammanagers;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.UltimateClansEventHandler;
import fr.rosstail.nodewar.permission.PermissionManager;
import fr.rosstail.nodewar.team.*;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.relation.NwTeamRelationRequest;
import fr.rosstail.nodewar.team.type.UcTeam;
import fr.rosstail.nodewar.territory.TerritoryManager;
import me.ulrich.clans.Clans;
import me.ulrich.clans.api.ClanAPIManager;
import me.ulrich.clans.data.ClanData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class UcTeamManager implements NwITeamManager, Listener {
    private final Map<String, UcTeam> stringTeamMap = new HashMap<>();

    private final Clans clansPlugin;
    private ClanAPIManager clanAPI;
    private UltimateClansEventHandler ultimateClansEventHandler;


    public UcTeamManager() {
        final Plugin uClansPlugin = Nodewar.getInstance().getServer().getPluginManager().getPlugin("UltimateClans");
        clansPlugin = (Clans) uClansPlugin;
    }

    @Override
    public void tryInitialize() {
        if (clansPlugin.isEnabled()) {
            initialize(clansPlugin);
        }
    }

    @Override
    public void initialize(JavaPlugin javaPlugin) {
        clanAPI = clansPlugin.getClanAPI();
        ultimateClansEventHandler = new UltimateClansEventHandler();
        Bukkit.getPluginManager().registerEvents(ultimateClansEventHandler, Nodewar.getInstance());
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        JavaPlugin plugin = (JavaPlugin) event.getPlugin();
        if (plugin.getName().equals("UltimateClans")) {
            initialize(plugin);
        }
    }

    @Override
    public void loadTeams() {
        clanAPI.getAllClansData().forEach(clanData -> {
            UcTeam ucTeam = new UcTeam(clanData);
            stringTeamMap.put(ucTeam.getName(), ucTeam);
        });
        TerritoryManager.getTerritoryManager().initialize();
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
            PermissionManager.getManager().setPlayerGroup(player.getName(), player.getUniqueId(), ucTeam);
        }
    }

    @Override
    public void removeITeam(String name) {
        NwITeam nwITeamToDelete = stringTeamMap.get(name);

        PermissionManager.getManager().deleteGroup(name);
        nwITeamToDelete.getOnlineMemberMap().forEach((player, teamMember) -> {
            NwITeam currentPlayerTeam = TeamManager.getManager().getPlayerTeam(player);

            PermissionManager.getManager().removePlayerGroup(player.getName(), player.getUniqueId(),
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
    public TeamMember addOnlineTeamMember(NwITeam nwITeam, Player player) {
        TeamMemberModel teamMemberModel = addTeamMember(nwITeam, player.getName());
        TeamMember teamMember = new TeamMember(player, nwITeam, teamMemberModel);
        nwITeam.getOnlineMemberMap().put(player, teamMember);
        return teamMember;
    }

    @Override
    public TeamMemberModel addTeamMember(NwITeam nwITeam, String playerName) {
        PermissionManager.getManager().setPlayerGroup(playerName, null, nwITeam);
        return null;
    }

    @Override
    public void deleteOnlineTeamMember(NwITeam nwITeam, Player player, boolean disband) {
        deleteTeamMember(nwITeam, player.getName(), disband);
        nwITeam.getOnlineMemberMap().remove(player);
    }

    @Override
    public void deleteTeamMember(NwITeam nwITeam, String playerName, boolean disband) {
        PermissionManager.getManager().removePlayerGroup(playerName, null, null);
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
