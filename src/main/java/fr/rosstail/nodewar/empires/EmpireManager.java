package fr.rosstail.nodewar.empires;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.lang.AdaptMessage;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EmpireManager {
    private static EmpireManager empireManager;
    private final Map<String, Empire> empires = new HashMap<>();
    private Empire noEmpire;
    private final Nodewar plugin;

    public static void initEmpireManager(Nodewar plugin) {
        if (empireManager == null) {
            empireManager = new EmpireManager(plugin);
        }
    }

    EmpireManager(Nodewar plugin) {
        this.plugin = plugin;
    }

    public Empire getSet(final FileConfiguration config, final String key) {
        if (!empires.containsKey(key)) {
            empires.put(key, new Empire(config, key));
        }
        return empires.get(key);
    }

    public Empire getSet(Player player, final String key) {
        if (!empires.containsKey(key)) {
            empires.put(key, new Empire(player, key));
        }
        return empires.get(key);
    }

    public Map<String, Empire> getEmpires() {
        return empires;
    }

    public void setNoEmpire() {
        noEmpire = new Empire();
    }

    public Empire getNoEmpire() {
        return noEmpire;
    }

    public void init() {
        setNoEmpire();
        final File folder = new File(plugin.getDataFolder(), "empires/");
        if (folder.listFiles() != null) {
            for (final File empireFile : Objects.requireNonNull(folder.listFiles())) {
                if (empireFile.isFile()) {
                    addEmpire(empireFile);
                }
                else {
                    AdaptMessage.print(empireFile + " is not a file", AdaptMessage.prints.WARNING);
                }
            }
        }
    }

    private static void addEmpire(final File empireFile) {
        final FileConfiguration customConfig = new YamlConfiguration();
        try {
            customConfig.load(empireFile);
            for (final String key : customConfig.getKeys(false)) {
                empireManager.getSet(customConfig, key);
            }
        }
        catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static EmpireManager getEmpireManager() {
        return empireManager;
    }
}
