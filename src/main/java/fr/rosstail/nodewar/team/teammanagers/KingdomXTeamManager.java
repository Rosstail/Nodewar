package fr.rosstail.nodewar.team.teammanagers;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.KingdomsEventHandler;
import fr.rosstail.nodewar.team.*;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.relation.NwTeamRelationRequest;
import fr.rosstail.nodewar.team.type.KingdomXTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.kingdoms.api.KingdomsAPI;
import org.kingdoms.constants.player.KingdomPlayer;
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
        // TODO

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

    }

    @Override
    public void removeITeam(String name) {

    }

    @Override
    public HashSet<Object> getInviteHashSet() {
        return null;
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
        return null;
    }

    @Override
    public void deleteOnlineTeamMember(NwITeam nwITeam, Player player, boolean disband) {

    }

    @Override
    public void deleteTeamMember(NwITeam nwITeam, String playerName, boolean disband) {

    }

    @Override
    public TeamIRelation getRelation(NwITeam firstTeam, NwITeam secondTeam) {
        return null;
    }

    @Override
    public Map<NwITeam, TeamIRelation> getRelationMap(NwITeam nwITeam) {
        return Map.of();
    }

    @Override
    public void createRelation(NwITeam originITeam, NwITeam targetITeam, RelationType type) {

    }

    @Override
    public void deleteRelation(NwITeam originTeam, NwITeam targetITeam) {

    }

    @Override
    public NwTeamRelationRequest getTeamRelationRequest(NwITeam firstTeam, NwITeam secondTeam) {
        return null;
    }

    @Override
    public Set<NwTeamRelationRequest> getTeamRelationRequestSet() {
        return Set.of();
    }

    @Override
    public void createRelationRequest(NwITeam originITeam, NwITeam targetITeam, RelationType type) {

    }

    @Override
    public void deleteRelationRequest(NwITeam originTeam, NwITeam targetITeam) {

    }
}
