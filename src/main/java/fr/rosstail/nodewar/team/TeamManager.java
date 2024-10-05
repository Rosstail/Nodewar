package fr.rosstail.nodewar.team;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.territoryevents.TerritoryOwnerNeutralizeEvent;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.permissionmannager.PermissionManager;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.relation.NwTeamRelation;
import fr.rosstail.nodewar.team.relation.NwTeamRelationRequest;
import fr.rosstail.nodewar.team.teammanagers.NwTeamManager;
import fr.rosstail.nodewar.team.teammanagers.TownyTeamManager;
import fr.rosstail.nodewar.team.teammanagers.UcTeamManager;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.webmap.WebmapManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class TeamManager {

    public static Map<String, Class<? extends NwITeamManager>> iTeamManagerMap = new HashMap<>();

    private NwITeamManager iManager = null;

    static {
        iTeamManagerMap.put("Towny", TownyTeamManager.class);
        iTeamManagerMap.put("UltimateClans", UcTeamManager.class);
        iTeamManagerMap.put("nodewar", NwTeamManager.class); // last, failsafe for AUTO
    }

    /**
     * Add custom objective from add-ons
     *
     * @param name
     * @param customTeamManagerClass
     * @return
     */
    public static boolean addCustomManager(String name, Class<? extends NwITeamManager> customTeamManagerClass) {
        if (!iTeamManagerMap.containsKey(name)) {
            iTeamManagerMap.put(name, customTeamManagerClass);
            AdaptMessage.print("[Nodewar] Custom team " + name + " added to the list !", AdaptMessage.prints.OUT);
            return true;
        }
        return false;
    }

    private static TeamManager manager;

    private final HashSet<NwTeamInvite> teamInviteHashSet = new HashSet<>();

    private TeamManager(Nodewar plugin) {
        String usedSystem = getUsedSystem();

        if (usedSystem != null) {
            Class<? extends NwITeamManager> managerClass = iTeamManagerMap.get(usedSystem);
            Constructor<? extends NwITeamManager> managerConstructor;

            try {
                managerConstructor = managerClass.getDeclaredConstructor();
                iManager = managerConstructor.newInstance();
                AdaptMessage.print("[Nodewar] Using " + usedSystem + " team", AdaptMessage.prints.OUT);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Missing appropriate constructor in TeamManager class.", e);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            AdaptMessage.print("[Nodewar] Using default " + usedSystem + " team", AdaptMessage.prints.OUT);
            iManager = new NwTeamManager();
        }

        if (iManager instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) iManager, plugin);
        }
    }

    public void tryInitialize() {
        iManager.tryInitialize();
    }

    public static void init(Nodewar plugin) {
        if (manager == null) {
            manager = new TeamManager(plugin);
        }
    }

    public String getUsedSystem() {
        String system = ConfigData.getConfigData().team.system;
        if (iTeamManagerMap.containsKey(system) && Bukkit.getServer().getPluginManager().getPlugin(system) != null) {
            return system;
        } else if (system.equalsIgnoreCase("auto")) {
            for (Map.Entry<String, Class<? extends NwITeamManager>> entry : iTeamManagerMap.entrySet()) {
                String s = entry.getKey();
                if (Bukkit.getServer().getPluginManager().getPlugin(s) != null) {
                    return s;
                }
            }
        }

        return null;
    }

    public void loadTeams() {
        iManager.loadTeams();
    }

    public Map<NwITeam, TeamIRelation> getTeamsIRelations(NwITeam nwITeam) {
        return iManager.getRelationMap(nwITeam);
    }

    public TeamIRelation getTeamRelation(NwITeam firstITeam, NwITeam secondITeam) {
        return iManager.getRelation(firstITeam, secondITeam);
    }

    public RelationType getTeamRelationType(NwITeam firstITeam, NwITeam secondITeam) {
        TeamIRelation teamIRelation = getTeamRelation(firstITeam, secondITeam);
        if (teamIRelation == null) {
            return ConfigData.getConfigData().team.defaultRelation;
        }
        return teamIRelation.getType();
    }

    public void addNewTeam(NwITeam nwITeam) {
        PermissionManager.getManager().createGroup(nwITeam.getName());
        iManager.addITeam(nwITeam.getName(), nwITeam);
    }

    public void renameTeam(String newName, String oldName) {
        NwITeam team = getStringTeamMap().get(oldName);
        iManager.addITeam(newName, team);
        StorageManager.getManager().updateTeamName(newName, team.getID());

        TerritoryManager.getTerritoryManager().getTerritoryMap().entrySet().stream().filter(
                stringTerritoryEntry -> stringTerritoryEntry.getValue().getOwnerITeam() == team
        ).forEach(stringTerritoryEntry -> {
            stringTerritoryEntry.getValue().setOwnerITeam(team);
            stringTerritoryEntry.getValue().updateAllBossBar();
        });

        // remove group after to avoid WG instant repercussions
        iManager.removeITeam(oldName);

        TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream().filter(territory -> (
            territory.getOwnerITeam() == team
        )).forEach(territory -> {
            WebmapManager.getManager().addTerritoryToEdit(territory);
        });
    }

    public void deleteTeam(String teamName) {
        NwITeam team = getStringTeamMap().get(teamName);
        iManager.removeITeam(teamName);

        PlayerDataManager.getPlayerDataMap().values().stream().filter(playerData ->
                (playerData.getTeam() == team)).forEach(playerData -> {
            TeamManager.getManager().deleteOnlineTeamMember(team, Bukkit.getPlayer(playerData.getUsername()), true);
        });

        team.getRelations().forEach((nwITeam, teamIRelation) -> {
            NwTeamRelation nwTeamRelation = (NwTeamRelation) teamIRelation;
            StorageManager.getManager().deleteTeamRelationModel(nwTeamRelation.getModel().getId());
        });

        TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream().filter(territory -> territory.getOwnerITeam() == team).collect(Collectors.toList()).forEach(territory -> {
            Bukkit.getServer().getPluginManager().callEvent(new TerritoryOwnerNeutralizeEvent(territory, null));
        });

        PermissionManager.getManager().deleteGroup(teamName);
        StorageManager.getManager().deleteTeamModel(team.getID());
    }

    public void createOnlineTeamMember(NwITeam nwITeam, Player player) {
        iManager.addOnlineTeamMember(nwITeam, player);
    }

    public void createTeamMember(NwITeam nwITeam, String playerName) {
        iManager.addTeamMember(nwITeam, playerName);
    }

    public void deleteOnlineTeamMember(NwITeam nwITeam, Player player, boolean disband) {
        iManager.deleteOnlineTeamMember(nwITeam, player, disband);
    }

    public void deleteTeamMember(NwITeam nwITeam, String playerName, boolean disband) {
        iManager.deleteTeamMember(nwITeam, playerName, disband);
    }

    public Map<String, NwITeam> getStringTeamMap() {
        return iManager.getStringITeamMap();
    }

    public static TeamManager getManager() {
        return manager;
    }

    public NwITeam getPlayerTeam(Player player) {
        return iManager.getPlayerTeam(player);
    }

    public NwITeam getTeam(String name) {
        return iManager.getTeam(name);
    }

    public HashSet<NwTeamInvite> getTeamInviteHashSet() {
        return teamInviteHashSet;
    }

    public void invitePlayerToTeam(@NotNull NwITeam nwITeam, Player sender, @NotNull Player receiver) {
        iManager.addTeamInvite(nwITeam, sender, receiver);
    }

    public String generateRandomColor() {
        StringBuilder randomHexColor;
        List<ChatColor> colorList = Arrays.stream(ChatColor.values()).filter(ChatColor::isColor).collect(Collectors.toList());

        if (AdaptMessage.getAdaptMessage().getVersionNumbers().get(1) < 16) {
            return colorList.get((int) (Math.random() * colorList.size())).name();
        } else {
            randomHexColor = new StringBuilder("#");

            for (int i = 0; i < 6; i++) {
                Random random = new Random();

                randomHexColor.append(Integer.toHexString(random.nextInt(16)));
            }

        }
        return randomHexColor.toString().toUpperCase();
    }

    public void createRelation(NwITeam originITeam, NwITeam targetITeam, RelationType type) {
        iManager.createRelation(originITeam, targetITeam, type);
    }

    public NwTeamRelationRequest getTeamRelationRequest(NwITeam sender, NwITeam target) {
        return iManager.getTeamRelationRequest(sender, target);
    }

    public Set<NwTeamRelationRequest> getTeamRelationRequestSet() {
        return  iManager.getTeamRelationRequestSet();
    }

    public void createRelationRequest(NwITeam sender, NwITeam target, RelationType type) {
        iManager.createRelationRequest(sender, target, type);
    }

    public void deleyeRelationRequest(NwITeam sender, NwITeam target) {
        iManager.deleteRelationRequest(sender, target);
    }
}
