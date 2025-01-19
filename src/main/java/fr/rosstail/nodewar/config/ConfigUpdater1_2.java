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

public class ConfigUpdater1_2 extends ConfigUpdater {

    @Override
    public float getVersion() {
        return 1.2f;
    }

    @Override
    public void update(@NotNull File configFile) {
        YamlConfiguration baseConfig = YamlConfiguration.loadConfiguration(configFile);
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





        try {
            baseConfig.set("config-version", getVersion());
            baseConfig.save(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateBattlefieldConfig(YamlConfiguration yamlConfig) {
        ConfigurationSection battlefieldListSection = yamlConfig.getConfigurationSection("battlefield.list");
        if (battlefieldListSection == null) {
            return false;
        }

        battlefieldListSection.getKeys(false).forEach(s -> {
            ConfigurationSection battlefieldSection = battlefieldListSection.getConfigurationSection(s);
            ConfigurationSection fromSection = battlefieldSection.getConfigurationSection("from");
            List<String> fromDays = new ArrayList<>();
            List<String> fromTime = new ArrayList<>();
            List<String> toDays = new ArrayList<>();
            List<String> toTime = new ArrayList<>();
            long startTime;
            long endTime;
            long duration;

            ConfigurationSection toSection = battlefieldSection.getConfigurationSection("to");

            if (fromSection != null) {
                fromDays = fromSection.getStringList("days");
                if (!fromDays.isEmpty()) {
                    battlefieldSection.set("start-days", fromDays);
                }

                String fromTimeStr = fromSection.getString("time");
                if (fromTimeStr != null) {
                    fromTime = List.of(fromTimeStr);
                }

                if (!fromTime.isEmpty()) {
                    battlefieldSection.set("start-times", fromTime);
                }
                battlefieldSection.set("from", null);
            }

            startTime = BattlefieldManager.getManager().getDateTimeMillisAfterDate(LocalDateTime.now(), new HashSet<>(fromDays), new HashSet<>(fromTime));

            if (toSection != null) {
                toDays = toSection.getStringList("days");

                String toTimeStr = toSection.getString("time");
                if (toTimeStr != null) {
                    toTime = List.of(toTimeStr);
                }

                battlefieldSection.set("to", null);
            }
            LocalDateTime startLocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), TimeZone.getDefault().toZoneId());

            endTime = BattlefieldManager.getManager().getDateTimeMillisAfterDate(startLocalDateTime, new HashSet<>(toDays), new HashSet<>(toTime));

            duration = endTime - startTime;

            if (duration > 0L) {
                long day = duration / 1000L / 86400L;
                long hour = (duration / 1000L - (day * 86400L)) / 3600L;
                long minute = (duration / 1000L - (day * 86400L) - (hour * 3600L)) / 60L;

                battlefieldSection.set("duration",
                        (((day > 0) ? (day + "d ") : "")
                                + ((hour > 0) ? (hour + "h ") : "")
                                + ((minute > 0) ? (minute + "m") : "")).trim()
                );
            }
        });
        return true;
    }
}
