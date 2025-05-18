package fr.rosstail.nodewar.config;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.battlefield.BattlefieldManager;
import fr.rosstail.nodewar.territory.TerritoryManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

public class ConfigUpdater1_3 extends ConfigUpdater {

    @Override
    public float getVersion() {
        return 1.3f;
    }

    @Override
    public void update(@NotNull File configFile) {
        YamlConfiguration baseConfig = YamlConfiguration.loadConfiguration(configFile);
        File permissionFile = ConfigData.readConfig(configFile, "permission");
        YamlConfiguration permissionConfig;

        if (configFile == permissionFile) {
            permissionConfig = baseConfig;
        } else {
            permissionConfig = YamlConfiguration.loadConfiguration(permissionFile);
        }

        if (updatePermissionConfig(permissionConfig)) {
            try {
                permissionConfig.save(permissionFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        File generalFile = ConfigData.readConfig(configFile, "general");
        YamlConfiguration generalConfig;

        if (configFile == generalFile) {
            generalConfig = baseConfig;
        } else {
            generalConfig = YamlConfiguration.loadConfiguration(generalFile);
        }

        if (updateGeneralConfig(generalConfig)) {
            try {
                generalConfig.save(generalFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        File webmapFile = ConfigData.readConfig(configFile, "webmap");
        YamlConfiguration webmapConfig;

        if (configFile == webmapFile) {
            webmapConfig = baseConfig;
        } else {
            webmapConfig = YamlConfiguration.loadConfiguration(webmapFile);
        }

        if (updateWebmapConfig(webmapConfig)) {
            try {
                webmapConfig.save(webmapFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        File battlefieldFile = ConfigData.readConfig(configFile, "battlefield");
        YamlConfiguration battlefieldConfig;

        if (configFile == battlefieldFile) {
            battlefieldConfig = baseConfig;
        } else {
            battlefieldConfig = YamlConfiguration.loadConfiguration(battlefieldFile);
        }

        if (updateBattlefieldConfig(battlefieldConfig)) {
            try {
                battlefieldConfig.save(battlefieldFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        File territoryTypesFile = new File("plugins/" + Nodewar.getInstance().getName() + "/conquest/territories-types.yml");
        File territoryPresetsFile = new File("plugins/" + Nodewar.getInstance().getName() + "/conquest/territories-presets.yml");

        if (territoryTypesFile.exists()) {
            YamlConfiguration territoryTypesFileConfig = YamlConfiguration.loadConfiguration(territoryTypesFile);
            if (updateTerritoryConfig(territoryTypesFileConfig)) {
                try {
                    territoryTypesFileConfig.save(territoryPresetsFile);
                    territoryTypesFile.delete();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        TerritoryManager.getTerritoryManager()
                .getTerritoryFiles("plugins/" + Nodewar.getInstance().getName() + "/conquest/territories")
                .forEach(file -> {
                    YamlConfiguration territoryFileConfig = YamlConfiguration.loadConfiguration(file);

                    if (updateTerritoryConfig(territoryFileConfig)) {
                        try {
                            territoryFileConfig.save(file);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

        // FINALLY

        try {
            baseConfig.set("config-version", getVersion());
            baseConfig.save(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * New section
     *
     * @param yamlConfig
     * @return
     */
    public boolean updatePermissionConfig(YamlConfiguration yamlConfig) {
        if (yamlConfig.isConfigurationSection("permission")) {
            return true;
        }

        Nodewar plugin = Nodewar.getInstance();


        File generalFile = ConfigData.readConfig(plugin.getCustomConfigFile(), "general");
        YamlConfiguration generalConfig = YamlConfiguration.loadConfiguration(generalFile);

        String permPluginName = generalConfig.getString("general.permission-plugin");

        yamlConfig.set("permission", Nodewar.getInstance().getConfig().getConfigurationSection("permission"));

        ConfigurationSection permissionSection = yamlConfig.getConfigurationSection("permission");
        permissionSection.set("plugin", permPluginName);

        return true;
    }

    public boolean updateGeneralConfig(YamlConfiguration yamlConfig) {
        ConfigurationSection generalSection = yamlConfig.getConfigurationSection("permission");

        generalSection.set("permission-plugin", null);

        return true;
    }

    public boolean updateWebmapConfig(YamlConfiguration yamlConfig) {
        ConfigurationSection webmapSection = yamlConfig.getConfigurationSection("webmap");
        FileConfiguration defaultConfig = Nodewar.getInstance().getConfig();

        if (webmapSection.get("territory-target-height-delta") == null) {
            webmapSection.set("territory-target-height-delta", defaultConfig.getInt("webmap.territory-target-height-delta"));
        }
        if (webmapSection.get("territory-protected-border-color") == null) {
            webmapSection.set("territory-protected-border-color", defaultConfig.getString("webmap.territory-protected-border-color"));
        }
        if (webmapSection.get("territory-vulnerable-border-color") == null) {
            webmapSection.set("territory-vulnerable-border-color", defaultConfig.getString("webmap.territory-vulnerable-border-color"));
        }

        return true;
    }

    public boolean updateBattlefieldConfig(YamlConfiguration yamlConfig) {
        ConfigurationSection battlefieldListSection = yamlConfig.getConfigurationSection("battlefield.list");
        if (battlefieldListSection == null) {
            return false;
        }

        battlefieldListSection.getKeys(false).forEach(s -> {
            ConfigurationSection battlefieldSection = battlefieldListSection.getConfigurationSection(s);
            if (battlefieldSection.isList("territory-types")) {
                battlefieldSection.set("territory-presets", battlefieldSection.getStringList("territory-types"));
                battlefieldSection.set("territory-types", null);
            }
        });
        return true;
    }

    public boolean updateTerritoryConfig(YamlConfiguration yamlConfig) {
        for (String key : yamlConfig.getKeys(false)) {
            if (yamlConfig.getString(key + ".type") != null) {
                yamlConfig.set(key + ".preset", yamlConfig.getString(key + ".type"));
                yamlConfig.set(key + ".type", null);
            }
            if (yamlConfig.getString(key + ".type-display") != null) {
                yamlConfig.set(key + ".preset-display", yamlConfig.getString(key + ".type-display"));
                yamlConfig.set(key + ".type-display", null);
            }
        }

        return true;
    }
}
