package fr.rosstail.nodewar.territory.zonehandlers;

import java.util.HashMap;
import java.util.List;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.territory.WorldGuardInteractions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;
import java.util.Objects;
import org.bukkit.Bukkit;
import java.io.File;
import java.util.ArrayList;
import org.bukkit.World;
import java.util.Map;

public class WorldTerritoryManager
{
    private static final Map<World, WorldTerritoryManager> worlds = new HashMap<>();
    private static final ArrayList<File> territoryFiles = new ArrayList<>();
    private static final ArrayList<FileConfiguration> territoryConfigs = new ArrayList<>();
    private final Map<String, Territory> territories = new HashMap<>();
    private static final int tickScheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Nodewar.getInstance(), () -> {
        worlds.forEach((world, worldTerritoryManager) -> {
            worldTerritoryManager.getTerritories().forEach((s, territory) -> {
                if (territory.isVulnerable()) {
                    territory.countEmpiresPointsOnTerritory();
                    territory.getCapturePoints().forEach((s1, capturePoint) -> {
                        capturePoint.getPlayersOnPoint();
                        capturePoint.countEmpirePlayerOnPoint();
                        capturePoint.updateBossBar();
                    });
                    territory.updateBossBar();
                }
            });
        });

    }, 0L, 1L);;
    private static final int secondScheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(Nodewar.getInstance(), () -> {
        worlds.forEach((world, worldTerritoryManager) -> {
            worldTerritoryManager.getTerritories().forEach((s, territory) -> {
                if (territory.isVulnerable()) {
                    territory.getCapturePoints().forEach((s1, capturePoint) -> {
                        capturePoint.setCaptureTime();
                        capturePoint.checkOwnerChange();
                    });
                    if (territory.isDamaged()) {
                        territory.setCaptureTime();
                        territory.checkChangeOwner();
                    }
                }
            });
        });
    }, 0L, 20L);;
    
    public static WorldTerritoryManager gets(final File folder) {
        final World world = Bukkit.getWorld(folder.getName());
        if (world != null) {
            if (!WorldTerritoryManager.worlds.containsKey(world)) {
                WorldTerritoryManager.worlds.put(world, new WorldTerritoryManager(folder));
            }
            return WorldTerritoryManager.worlds.get(world);
        }
        return null;
    }
    
    public WorldTerritoryManager(final File worldFolder) {
        if (worldFolder.listFiles() != null) {
            final World world = Bukkit.getWorld(worldFolder.getName());
            if (world != null) {
                for (final File file : Objects.requireNonNull(worldFolder.listFiles())) {
                    territoryFiles.add(file);
                    final FileConfiguration customConfig = new YamlConfiguration();
                    try {
                        customConfig.load(file);
                        territoryConfigs.add(customConfig);
                        for (final String key : customConfig.getKeys(false)) {
                            final Territory territory = new Territory(territoryFiles.size() - 1, world, key);
                            this.territories.put(key, territory);
                        }
                    }
                    catch (IOException | InvalidConfigurationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    public static void setUsedWorlds() {
        final List<World> worldList = Bukkit.getWorlds();
        final ArrayList<World> finWorldList = new ArrayList<World>();
        for (final World world : worldList) {
            if (WorldTerritoryManager.worlds.containsKey(world)) {
                finWorldList.add(world);
            }
        }
        WorldGuardInteractions.setPlayersDataForWorlds(finWorldList);
        worldList.retainAll(finWorldList);
    }
    
    public static Map<World, WorldTerritoryManager> getUsedWorlds() {
        return worlds;
    }

    public Map<String, Territory> getTerritories() {
        return territories;
    }

    public static ArrayList<FileConfiguration> getTerritoryConfigs() {
        return territoryConfigs;
    }

    public static void saveTerritoryFile(int fileID) {
        try {
            territoryConfigs.get(fileID).save(territoryFiles.get(fileID));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int getTickScheduler() {
        return tickScheduler;
    }

    public int getSecondScheduler() {
        return secondScheduler;
    }

    public static void stopTimers() {
        Bukkit.getScheduler().cancelTask(tickScheduler);
        Bukkit.getScheduler().cancelTask(secondScheduler);
    }
}
