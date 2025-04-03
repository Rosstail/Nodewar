package fr.rosstail.nodewar.team.teammanagers;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.FactionsPlugin;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.SaberFactionsEventHandler;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.permission.PermissionManager;
import fr.rosstail.nodewar.team.*;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.relation.NwTeamRelationRequest;
import fr.rosstail.nodewar.team.type.SfTeam;
import fr.rosstail.nodewar.territory.TerritoryManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SfTeamManager implements NwITeamManager, Listener {
    private final Map<String, SfTeam> stringTeamMap = new HashMap<>();

    private final FactionsPlugin saberFactionsPlugin;
    private final Factions saberFactions;
    private final FPlayers fPlayers;
    private SaberFactionsEventHandler sfEventHandler;

    public SfTeamManager() {
        final Plugin saberFactionPlugin = Nodewar.getInstance().getServer().getPluginManager().getPlugin("Factions");
        this.saberFactionsPlugin = (FactionsPlugin) saberFactionPlugin;
        this.saberFactions = Factions.getInstance();
        this.fPlayers = FPlayers.getInstance();
    }

    @Override
    public void tryInitialize() {
        if (this.saberFactionsPlugin.isEnabled()) {
            initialize(this.saberFactionsPlugin);
        }
    }

    @Override
    public void initialize(JavaPlugin javaPlugin) {
        AdaptMessage.print("SfTeamManager.initialize", AdaptMessage.prints.DEBUG);
        this.sfEventHandler = new SaberFactionsEventHandler();
        Bukkit.getPluginManager().registerEvents(this.sfEventHandler, Nodewar.getInstance());
        loadTeams();
        TerritoryManager.getTerritoryManager().initialize();
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        JavaPlugin plugin = (JavaPlugin) event.getPlugin();
        if (plugin.getName().equals("Factions")) {
            initialize(plugin);
        }
    }



    @Override
    public void loadTeams() {
        for (Faction faction : saberFactions.getAllFactions()) {
            SfTeam sfTeam = new SfTeam(faction);
            stringTeamMap.put(sfTeam.getName(), sfTeam);
        }
    }

    @Override
    public NwITeam getPlayerTeam(Player player) {
        Faction playerFaction = fPlayers.getByPlayer(player).getFaction();
        SfTeam sfTeam;

        if (playerFaction == null) {
            return null;
        }

        if (stringTeamMap.values().stream().noneMatch(elementFactionTeam -> (elementFactionTeam.getFaction() == playerFaction))) {
            sfTeam = new SfTeam(playerFaction);
            stringTeamMap.put(sfTeam.getName(), sfTeam);
            return sfTeam;
        }

        return stringTeamMap.values().stream().filter(elementTownTeam -> (elementTownTeam.getFaction() == playerFaction)).findFirst().get();
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
        SfTeam sfTeam = (SfTeam) team;
        stringTeamMap.put(name, sfTeam);

        PermissionManager.getManager().createGroup(name);

        Player player;
        if (((SfTeam) team).getFaction().getFPlayerAdmin().getPlayer() != null) {
            player = ((SfTeam) team).getFaction().getFPlayerAdmin().getPlayer();
            PermissionManager.getManager().setPlayerGroup(player.getName(), player.getUniqueId(), sfTeam);
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
        return null;
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
        throw new RuntimeException("SaberFactions shoud use its own relation request system");
    }

    @Override
    public void deleteRelation(NwITeam originITeam, NwITeam targetITeam) {
        throw new RuntimeException("SaberFactions shoud use its own relation request system");
    }

    @Override
    public NwTeamRelationRequest getTeamRelationRequest(NwITeam firstTeam, NwITeam secondTeam) {
        throw new RuntimeException("SaberFactions shoud use its own relation request system");
    }

    @Override
    public Set<NwTeamRelationRequest> getTeamRelationRequestSet() {
        throw new RuntimeException("SaberFactions shoud use its own relation request system");
    }

    @Override
    public void createRelationRequest(NwITeam originITeam, NwITeam targetITeam, RelationType type) {
        throw new RuntimeException("SaberFactions shoud use its own relation request system");
    }

    @Override
    public void deleteRelationRequest(NwITeam originTeam, NwITeam targetITeam) {
        throw new RuntimeException("SaberFactions shoud use its own relation request system");
    }
}
