package fr.rosstail.nodewar.territory;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.events.regionevents.RegionEnteredEvent;
import fr.rosstail.nodewar.events.regionevents.RegionLeftEvent;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.territory.type.TerritoryType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TerritoryManager {

    private static TerritoryManager territoryManager;
    private Nodewar plugin;
    private Map<String, TerritoryType> territoryTypeMap = new HashMap<>();
    private TerritoryType defaultTerritoryType;
    private Map<String, Territory> territoryMap = new HashMap<>();

    public TerritoryManager(Nodewar plugin) {
        this.plugin = plugin;
    }

    public static void init(Nodewar plugin) {
        if (territoryManager == null) {
            territoryManager = new TerritoryManager(plugin);
        }
    }

    public void loadTerritoryConfigs(String folder) {
        File territoriesDirectory = new File(folder);
        List<File> territoryFolderList = new ArrayList<>();
        if (territoriesDirectory.isDirectory()) {
            for (File file : territoriesDirectory.listFiles()) {
                if (file.isDirectory()) {
                    territoryFolderList.add(file);
                } else if (file.getName().endsWith(".yml")) {
                    YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
                    yamlConfiguration.getKeys(false).forEach(s -> {

                        ConfigurationSection section = yamlConfiguration.getConfigurationSection(s);
                        Territory territory = new Territory(section);
                        territoryMap.put(territory.getModel().getName(), territory);
                    });
                }
            }

            territoryFolderList.forEach(file -> {
                loadTerritoryConfigs(file.getPath());
            });
        }
    }

    public void loadTerritoryTypeConfig() {
        defaultTerritoryType = new TerritoryType();

        File fileConfig = new File("plugins/" + plugin.getName() + "/conquest/territory-types.yml");
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(fileConfig);
        yamlConfiguration.getKeys(false).forEach(s -> {
            ConfigurationSection section = yamlConfiguration.getConfigurationSection(s);
            TerritoryType territoryType = new TerritoryType(section);
            territoryTypeMap.put(territoryType.getName(), territoryType);
        });
    }

    public void setupTerritoriesOwner() {
        Map<String, NwTeam> stringTeamMap = TeamDataManager.getTeamDataManager().getStringTeamMap();
        List<TerritoryModel> territoryOwnerMap = StorageManager.getManager().selectAllTerritoryModel();
        getTerritoryMap().forEach((s, territory) -> {
            List<TerritoryModel> models = territoryOwnerMap.stream().filter(model ->
                model.getWorldName().equalsIgnoreCase(territory.getModel().getWorldName())
            ).filter(model ->
                model.getName().equalsIgnoreCase(territory.getModel().getName())
            ).collect(Collectors.toList());

            if (!models.isEmpty()) {
                String ownerName = models.get(0).getOwnerName();
                if (ownerName != null && stringTeamMap.containsKey(ownerName)) {
                    territory.setOwnerTeam(stringTeamMap.get(ownerName));
                }
            } else {
                StorageManager.getManager().insertTerritoryOwner(territory.getModel());
            }
        });
    }

    public void setupTerritoriesObjective() {
        getTerritoryMap().forEach((s, territory) -> {
            territory.setupObjective();
        });
    }

    public void setupTerritoriesAttackRequirements() {
        getTerritoryMap().forEach((s, territory) -> {
            territory.setupAttackRequirements();
        });
    }

    public static TerritoryManager getTerritoryManager() {
        return territoryManager;
    }

    public Map<String, Territory> getTerritoryMap() {
        return territoryMap;
    }

    public Map<String, TerritoryType> getTerritoryTypeMap() {
        return territoryTypeMap;
    }

    public List<Territory> getTerritoryListPerWorld(World world) {
        return getTerritoryMap().values().stream().filter(territory -> (
                territory.getWorld().equals(world)
                )).collect(Collectors.toList());
    }

    public TerritoryType getTerritoryTypeFromMap(String type) {
        if (type == null || !territoryTypeMap.containsKey(type)) {
            return defaultTerritoryType;
        }
        return territoryTypeMap.get(type);
    }

    public List<World> getUsedWorldList() {
        return getTerritoryMap().values().stream().map(Territory::getWorld).distinct().collect(Collectors.toList());
    }

    public void setTerritoryTypeMap(Map<String, TerritoryType> territoryTypeMap) {
        this.territoryTypeMap = territoryTypeMap;
    }

    public void addRegionToTerritory(String worldName, ProtectedRegion region) {
        System.out.println(territoryMap.entrySet().stream()
                .filter(
                        x -> x.getValue().getModel().getRegionStringList().contains(region.getId())
                ).filter(
                        x -> x.getValue().getModel().getWorldName().equalsIgnoreCase(worldName)
                ).count());
    }

    public void playerRegionPresenceManager(Player player, Location newLocation) {
        com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(newLocation);

        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player.getName());

        List<ProtectedRegion> currentProtectedRegionList = playerData.getProtectedRegionList();

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(wgLocation);
        List<ProtectedRegion> newProtectedRegionList = new ArrayList<>(set.getRegions());


        List<ProtectedRegion> leftRegionList = currentProtectedRegionList.stream()
                .filter(region -> !newProtectedRegionList.contains(region))
                .collect(Collectors.toList());
        List<ProtectedRegion> enteredRegionList = newProtectedRegionList.stream()
                .filter(region -> !currentProtectedRegionList.contains(region))
                .collect(Collectors.toList());

        playerData.getProtectedRegionList().removeAll(leftRegionList);
        playerData.getProtectedRegionList().addAll(enteredRegionList);

        leftRegionList.forEach(region -> {
            RegionLeftEvent leftEvent = new RegionLeftEvent(region, player.getWorld(), player, null);
            Bukkit.getPluginManager().callEvent(leftEvent);
        });

        enteredRegionList.forEach(region -> {
            RegionEnteredEvent enteredEvent = new RegionEnteredEvent(region, newLocation.getWorld(), player, null);
            Bukkit.getPluginManager().callEvent(enteredEvent);
        });

    }

    public void stopAllObjective() {
        getTerritoryMap().forEach((s, territory) -> {
            territory.getObjective().stopObjective();
        });
    }
}
