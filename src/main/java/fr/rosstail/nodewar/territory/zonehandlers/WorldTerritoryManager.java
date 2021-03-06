package fr.rosstail.nodewar.territory.zonehandlers;

import fr.rosstail.nodewar.territory.WorldGuardInteractions;
import fr.rosstail.nodewar.territory.zonehandlers.objective.Objective;
import fr.rosstail.nodewar.territory.zonehandlers.objective.objectives.ControlPoint;
import fr.rosstail.nodewar.territory.zonehandlers.objective.objectives.KingOfTheHill;
import fr.rosstail.nodewar.territory.zonehandlers.objective.objectives.Struggle;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WorldTerritoryManager {
    private static final Map<World, WorldTerritoryManager> worlds = new HashMap<>();
    private static final ArrayList<File> territoryFiles = new ArrayList<>();
    private static final ArrayList<FileConfiguration> territoryConfigs = new ArrayList<>();
    private final Map<String, Territory> territories = new HashMap<>();

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
                    } catch (IOException | InvalidConfigurationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void setUpObjective(Territory territory) {
        FileConfiguration config = territory.getConfig();
        ConfigurationSection objectiveSection = config.getConfigurationSection(territory.getName() + ".options.objective");
        Objective objective = null;
        if (objectiveSection != null) {
            String objectiveType = objectiveSection.getString(".type");
            if (objectiveType != null) {
                if (objectiveType.equalsIgnoreCase("KOTH")) {
                    objective = new KingOfTheHill(territory);
                    objective.start();
                } else if (objectiveType.equalsIgnoreCase("STRUGGLE")) {
                    objective = new Struggle(territory);
                    objective.start();
                } else if (objectiveType.equalsIgnoreCase("CONTROL")) {
                    objective = new ControlPoint(territory);
                    objective.start();
                }
            }
        }
        territory.setObjective(objective);
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
}
