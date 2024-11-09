package fr.rosstail.nodewar.team.teammanagers;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.TownyEventHandler;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.permissionmannager.PermissionManager;
import fr.rosstail.nodewar.team.*;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.relation.NwTeamRelationRequest;
import fr.rosstail.nodewar.team.type.TownTeam;
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

public class TownyTeamManager implements NwITeamManager, Listener {
    private final Map<String, TownTeam> stringTeamMap = new HashMap<>();
    private TownyAPI townyAPI;
    private TownyEventHandler townyEventHandler;


    public TownyTeamManager() {
    }

    @Override
    public void tryInitialize() {
        final JavaPlugin townyPlugin = (Towny) Nodewar.getInstance().getServer().getPluginManager().getPlugin("Towny");
        if (townyPlugin.isEnabled()) {
            initialize(townyPlugin);
        }
    }

    @Override
    public void initialize(JavaPlugin javaPlugin) {
        AdaptMessage.print("TownyTeamManager.initialize", AdaptMessage.prints.DEBUG);
        townyAPI = TownyAPI.getInstance();
        townyEventHandler = new TownyEventHandler();
        Bukkit.getPluginManager().registerEvents(townyEventHandler, Nodewar.getInstance());
        loadTeams();
        TerritoryManager.getTerritoryManager().initialize();
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        JavaPlugin plugin = (JavaPlugin) event.getPlugin();
        if (plugin.getName().equals("Towny")) {
            initialize(plugin);
        }
    }

    @Override
    public void loadTeams() {
        townyAPI.getTowns().forEach(town -> {
            TownTeam townTeam = new TownTeam(town);
            stringTeamMap.put(townTeam.getName(), townTeam);
        });
    }

    @Override
    public NwITeam getPlayerTeam(Player player) {
        Resident resident = townyAPI.getResident(player.getUniqueId());
        Town playerTown;
        TownTeam townTeam;
        if (resident == null) {
            return null;
        }

        playerTown = resident.getTownOrNull();
        if (playerTown == null) {
            return null;
        }

        if (stringTeamMap.values().stream().noneMatch(elementTownTeam -> (elementTownTeam.getTown() == playerTown))) {
            townTeam = new TownTeam(playerTown);
            stringTeamMap.put(townTeam.getName(), townTeam);
            return townTeam;
        }

        return stringTeamMap.values().stream().filter(elementTownTeam -> (elementTownTeam.getTown() == playerTown)).findFirst().get();
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
        TownTeam townTeam = (TownTeam) team;
        stringTeamMap.put(name, townTeam);

        PermissionManager.getManager().createGroup(name);

        Player player;
        if (((TownTeam) team).getTown().getMayor() != null) {
            player = ((TownTeam) team).getTown().getMayor().getPlayer();
            PermissionManager.getManager().setPlayerGroup(player.getName(), player.getUniqueId(), townTeam);
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
        TownTeam originTownTeam = (TownTeam) originITeam;
        Town originTown = originTownTeam.getTown();
        TownTeam targetTownTeam = (TownTeam) targetITeam;
        Town targetTown = targetTownTeam.getTown();
        switch (type) {
            case ALLY:
                originTown.addAlly(targetTown);
                break;
            case TRUCE:
                originTown.addTrustedTown(targetTown);
                break;
            case NEUTRAL:
                deleteRelation(originITeam, targetITeam);
                break;
            case ENEMY:
                originTown.addEnemy(targetTown);
                break;
        }
    }

    @Override
    public void deleteRelation(NwITeam originITeam, NwITeam targetITeam) {
        TownTeam originTownTeam = (TownTeam) originITeam;
        Town originTown = originTownTeam.getTown();
        TownTeam targetTownTeam = (TownTeam) targetITeam;
        Town targetTown = targetTownTeam.getTown();
        originTown.removeAlly(targetTown);
        originTown.removeTrustedTown(targetTown);
        originTown.removeEnemy(targetTown);
    }

    @Override
    public NwTeamRelationRequest getTeamRelationRequest(NwITeam firstTeam, NwITeam secondTeam) {
        throw new RuntimeException("Towny shoud use its own relation request system");
    }

    @Override
    public Set<NwTeamRelationRequest> getTeamRelationRequestSet() {
        throw new RuntimeException("Towny shoud use its own relation request system");
    }

    @Override
    public void createRelationRequest(NwITeam originITeam, NwITeam targetITeam, RelationType type) {
        throw new RuntimeException("Towny shoud use its own relation request system");
    }

    @Override
    public void deleteRelationRequest(NwITeam originTeam, NwITeam targetITeam) {
        throw new RuntimeException("Towny shoud use its own relation request system");
    }
}
