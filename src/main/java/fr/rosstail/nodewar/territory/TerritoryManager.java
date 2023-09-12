package fr.rosstail.nodewar.territory;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.territory.type.TerritoryType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
        if (territoriesDirectory.isDirectory()) {
            for (File file : territoriesDirectory.listFiles()) {
                if (file.isDirectory()) {
                    loadTerritoryConfigs(file.getPath());
                } else if (file.getName().endsWith(".yml")) {
                    YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
                    yamlConfiguration.getKeys(false).forEach(s -> {
                        ConfigurationSection section = yamlConfiguration.getConfigurationSection(s);
                        Territory territory = new Territory(section);
                        territoryMap.put(territory.getTerritoryModel().getName(), territory);
                    });
                }
            }
        }
    }

    public void loadTerritoryTypeConfig() {
        defaultTerritoryType = new TerritoryType();

        File fileConfig = new File("plugins/" + plugin.getName() + "/conquest/territory-types.yml");
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(fileConfig);
        yamlConfiguration.getKeys(false).forEach(s -> {
            ConfigurationSection section = yamlConfiguration.getConfigurationSection(s);
            TerritoryType territoryType = new TerritoryType(section);
            territoryType.printModel();
            territoryTypeMap.put(territoryType.getName(), territoryType);
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

    public TerritoryType getTerritoryTypeFromMap(String type) {
        if (type == null || !territoryTypeMap.containsKey(type)) {
            return defaultTerritoryType;
        }
        return territoryTypeMap.get(type);
    }

    public void setTerritoryTypeMap(Map<String, TerritoryType> territoryTypeMap) {
        this.territoryTypeMap = territoryTypeMap;
    }
}
