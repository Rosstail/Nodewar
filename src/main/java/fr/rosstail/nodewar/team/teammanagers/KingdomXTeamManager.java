package fr.rosstail.nodewar.team.teammanagers;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.KingdomsEventHandler;
import fr.rosstail.nodewar.permission.PermissionManager;
import fr.rosstail.nodewar.team.*;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.relation.NwTeamRelationRequest;
import fr.rosstail.nodewar.team.type.KingdomXTeam;
import fr.rosstail.nodewar.team.type.UltimateClanTeam;
import fr.rosstail.nodewar.territory.TerritoryManager;
import me.ulrich.clans.data.ClanData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.kingdoms.api.KingdomsAPI;
import org.kingdoms.constants.group.Kingdom;
import org.kingdoms.constants.group.model.relationships.KingdomRelation;
import org.kingdoms.main.Kingdoms;

import java.util.*;

public class KingdomXTeamManager implements NwITeamManager, Listener {
    private final Map<String, KingdomXTeam> stringTeamMap = new HashMap<>();

    private final Kingdoms kingdomsPlugin;
    private KingdomsAPI kingdomsAPI;
    private KingdomsEventHandler kingdomsEventHandler;

    public KingdomXTeamManager() {
        final Plugin kingdomsPlugin = Nodewar.getInstance().getServer().getPluginManager().getPlugin("KingdomsX");
        this.kingdomsPlugin = (Kingdoms) kingdomsPlugin;
    }

    @Override
    public void tryInitialize() {
        if (kingdomsPlugin.isEnabled()) {
            initialize(kingdomsPlugin);
        }
    }

    @Override
    public void initialize(JavaPlugin javaPlugin) {
        kingdomsAPI = KingdomsAPI.getApi();
        kingdomsEventHandler = new KingdomsEventHandler();
        Bukkit.getPluginManager().registerEvents(kingdomsEventHandler, Nodewar.getInstance());

    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        JavaPlugin plugin = (JavaPlugin) event.getPlugin();
        if (plugin.getName().equals("KingdomsX")) {
            initialize(plugin);
        }
    }

    @Override
    public void loadTeams() {
        Kingdoms.get().getDataCenter().getKingdomManager().getKingdoms().forEach(kingdom -> {
            KingdomXTeam kingdomXTeam = new KingdomXTeam(kingdom);
            stringTeamMap.put(kingdomXTeam.getName().toLowerCase(), kingdomXTeam);
        });
        TerritoryManager.getTerritoryManager().initialize();
    }

    @Override
    public NwITeam getPlayerTeam(Player player) {
        return stringTeamMap.values().stream()
                .filter(kingdomXTeam -> kingdomXTeam.getKingdom().isMember(player.getUniqueId()))
                .findFirst().orElse(null);
    }

    @Override
    public NwITeam getTeam(String name) {
        return stringTeamMap.get(name);
    }

    @Override
    public Map<String, NwITeam> getStringITeamMap() {
        return Map.of();
    }

    @Override
    public void addITeam(String name, NwITeam team) {
        KingdomXTeam kingdomXTeam = (KingdomXTeam) team;
        stringTeamMap.put(name, kingdomXTeam);

        PermissionManager.getManager().createGroup(name);

        Player player;
        if (kingdomXTeam.getKingdom().getOwnerId() != null) {
            player = Bukkit.getPlayer(kingdomXTeam.getKingdom().getOwnerId());
            PermissionManager.getManager().setPlayerGroup(player.getName(), player.getUniqueId(), kingdomXTeam);
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
        KingdomXTeam originUltimateClanTeam = (KingdomXTeam) originITeam;
        Kingdom originKingdom = originUltimateClanTeam.getKingdom();
        KingdomXTeam targetUltimateClanTeam = (KingdomXTeam) targetITeam;
        Kingdom targetKingdom = targetUltimateClanTeam.getKingdom();
        switch (type) {
            case ALLY:
                originKingdom.getRelations().put(targetKingdom.getId(), KingdomRelation.ALLY);
                break;
            case TRUCE:
                originKingdom.getRelations().put(targetKingdom.getId(), KingdomRelation.TRUCE);
                break;
            case NEUTRAL:
                originKingdom.getRelations().put(targetKingdom.getId(), KingdomRelation.NEUTRAL);
                break;
            case ENEMY:
                originKingdom.getRelations().put(targetKingdom.getId(), KingdomRelation.ENEMY);
                break;
        }
    }

    @Override
    public void deleteRelation(NwITeam originITeam, NwITeam targetITeam) {
        KingdomXTeam originUltimateClanTeam = (KingdomXTeam) originITeam;
        Kingdom originKingdom = originUltimateClanTeam.getKingdom();
        KingdomXTeam targetUltimateClanTeam = (KingdomXTeam) targetITeam;
        Kingdom targetKingdom = targetUltimateClanTeam.getKingdom();
        originKingdom.getRelations().remove(targetKingdom.getId());
    }

    @Override
    public NwTeamRelationRequest getTeamRelationRequest(NwITeam firstTeam, NwITeam secondTeam) {
        throw new RuntimeException("Not implemented / Incompatible");
    }

    @Override
    public Set<NwTeamRelationRequest> getTeamRelationRequestSet() {
        throw new RuntimeException("Not implemented / Incompatible");
    }

    @Override
    public void createRelationRequest(NwITeam originITeam, NwITeam targetITeam, RelationType type) {
        throw new RuntimeException("Not implemented / Incompatible");
    }

    @Override
    public void deleteRelationRequest(NwITeam originTeam, NwITeam targetITeam) {
        throw new RuntimeException("Not implemented / Incompatible");
    }
}
