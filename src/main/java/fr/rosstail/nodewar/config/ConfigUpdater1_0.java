package fr.rosstail.nodewar.config;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.battlefield.BattlefieldManager;
import org.bukkit.configuration.ConfigurationSection;
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

public class ConfigUpdater1_0 extends ConfigUpdater {

    @Override
    public float getVersion() {
        return 1.0f;
    }

    @Override
    public void update(@NotNull File configFile) {
        YamlConfiguration baseConfig = YamlConfiguration.loadConfiguration(configFile);
        File generalFile = ConfigData.readConfig(configFile, "general");
        File mapFile = ConfigData.readConfig(configFile, "map");
        File battlefieldFile = ConfigData.readConfig(configFile, "battlefield");
        YamlConfiguration generalConfig;
        YamlConfiguration mapConfig;
        YamlConfiguration battlefieldConfig;

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

        if (configFile == mapFile) {
            mapConfig = baseConfig;
            if (updateMapConfig(mapConfig)) {
                try {
                    mapConfig.save(mapFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            generalFile.delete();
        }

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

        try {
            baseConfig.set("config-version", getVersion());
            baseConfig.save(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateGeneralConfig(YamlConfiguration yamlConfig) {
        ConfigurationSection generalSection = yamlConfig.getConfigurationSection("general");
        if (generalSection != null) {
            generalSection.set("delay-between-database-updates", null);
            generalSection.set("use-action-bar-on-actions", null);
            generalSection.set("use-calendar", null);
        }
        return true;
    }

    public boolean updateMapConfig(YamlConfiguration yamlConfig) {
        ConfigurationSection mapSection = yamlConfig.getConfigurationSection("map");
        if (mapSection != null) {
            yamlConfig.set("map", null);
        }
        return true;
    }

    public boolean updateBattlefieldConfig(YamlConfiguration yamlConfig) {
        ConfigurationSection battlefielAlertsSection = yamlConfig.getConfigurationSection("battlefield.alerts");
        if (battlefielAlertsSection == null) {
            yamlConfig.set("alerts", new ArrayList<String>());
        }
        return true;
    }
}
