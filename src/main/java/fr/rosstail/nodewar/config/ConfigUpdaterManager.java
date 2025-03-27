package fr.rosstail.nodewar.config;

import fr.rosstail.nodewar.Nodewar;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigUpdaterManager {

    private final List<ConfigUpdater> configUpdaterList = new ArrayList<>();

    public ConfigUpdaterManager() {
        configUpdaterList.add(new ConfigUpdater1_0());
        configUpdaterList.add(new ConfigUpdater1_1());
        configUpdaterList.add(new ConfigUpdater1_2());
        configUpdaterList.add(new ConfigUpdater1_3());
    }


    public void updateConfig() {
        File fileConfig = new File("plugins/" + Nodewar.getInstance().getName() + "/config.yml");
        File fileConfigBackupStart = new File("plugins/" + Nodewar.getInstance().getName() + "/config.yml");
        YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(Nodewar.getInstance().getCustomConfigFile());
        float currentVersion = (float) yamlConfig.getDouble("config-version", 0f);

        for (ConfigUpdater configUpdater : configUpdaterList) {
            File fileConfigBackupEnd = new File("plugins/" + Nodewar.getInstance().getName() + "/config-bkp-" + currentVersion + ".yml");
            if (currentVersion < configUpdater.getVersion()) {
                configUpdater.update(fileConfig);
                /*try {
                    boolean saveBackup = fileConfigBackupStart.renameTo(fileConfigBackupEnd);
                    if (saveBackup) {
                        yamlConfig.save(fileConfig);
                    }
                    currentVersion = configUpdater.getVersion();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }*/
            }
        }
    }


}
