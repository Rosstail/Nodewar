package fr.rosstail.nodewar.territory.zonehandlers;

import java.util.HashMap;
import java.util.List;
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

public class ConquestWorlds
{
    private static final Map<World, ConquestWorlds> worlds = new HashMap<>();
    private static final ArrayList<File> territoryFiles = new ArrayList<>();
    private static final ArrayList<FileConfiguration> territoryConfigs = new ArrayList<>();
    private final ArrayList<Territory> worldTerritories;
    
    public static ConquestWorlds gets(final File folder) {
        final World world = Bukkit.getWorld(folder.getName());
        if (world != null) {
            if (!ConquestWorlds.worlds.containsKey(world)) {
                ConquestWorlds.worlds.put(world, new ConquestWorlds(folder));
            }
            return ConquestWorlds.worlds.get(world);
        }
        return null;
    }
    
    public ConquestWorlds(final File worldFolder) {
        this.worldTerritories = new ArrayList<>();
        if (worldFolder.listFiles() != null) {
            final World world = Bukkit.getWorld(worldFolder.getName());
            if (world != null) {
                for (final File file : Objects.requireNonNull(worldFolder.listFiles())) {
                    territoryFiles.add(file);
                    final FileConfiguration customConfig = new YamlConfiguration();
                    try {
                        customConfig.load(file);
                        territoryConfigs.add(customConfig);
                        System.out.println(file.getPath() + " " + file.getName());
                        for (final String key : customConfig.getKeys(false)) {
                            final Territory territory = Territory.gets(territoryFiles.size() - 1, world, key);
                            this.worldTerritories.add(territory);
                        }
                    }
                    catch (IOException | InvalidConfigurationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        Territory.getAllTerritories().forEach(Territory::enableTickCheckIfVulnerable);
    }
    
    public static void setUsedWorlds() {
        final List<World> worldList = Bukkit.getWorlds();
        final ArrayList<World> finWorldList = new ArrayList<World>();
        for (final World world : worldList) {
            if (ConquestWorlds.worlds.containsKey(world)) {
                finWorldList.add(world);
            }
        }
        WorldGuardInteractions.setPlayersDataForWorlds(finWorldList);
        worldList.retainAll(finWorldList);
    }
    
    public static List<World> getUsedWorlds() {
        return new ArrayList<World>(ConquestWorlds.worlds.keySet());
    }
    
    public ArrayList<Territory> getWorldTerritories() {
        return this.worldTerritories;
    }
    
    public static ArrayList<Territory> getWorldTerritories(final World world) {
        return ConquestWorlds.worlds.get(world).getWorldTerritories();
    }

    public static ArrayList<File> getTerritoryFiles() {
        return territoryFiles;
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
