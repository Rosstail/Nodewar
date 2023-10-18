package fr.rosstail.nodewar;

import fr.rosstail.nodewar.lang.AdaptMessage;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigData {
    private final Nodewar plugin = Nodewar.getInstance();
    private static ConfigData configData;

    public final ConfigStorage storage;
    public final ConfigGeneral general;
    public final ConfigLocale locale;

    public final ConfigTeam team;

    public final ConfigBossBar bossbar;

    public class ConfigStorage {
        public final FileConfiguration configFile;
        public final String storageType;
        public final String storageHost;
        public final short storagePort;
        public final String storageDatabase;
        public final String storageUser;
        public final String storagePass;
        public final int saveDelay;

        ConfigStorage(FileConfiguration config) {
            this.configFile = config;
            storageType = config.getString("storage.type", "LocalStorage");
            storageHost = config.getString("storage.host");
            storagePort = (short) config.getInt("storage.port", 3306);
            storageDatabase = config.getString("storage.database");
            storageUser = config.getString("storage.username");
            storagePass = config.getString("storage.password");

            int saveDelay = config.getInt("storage.save-delay", 300);
            if (saveDelay <= 0) {
                saveDelay = 300;
            }
            this.saveDelay = saveDelay * 1000;
        }
    }

    public class ConfigLocale {
        public final FileConfiguration configFile;

        public final String lang;
        public final int decNumber;
        public final int titleFadeIn;
        public final int titleStay;
        public final int titleFadeOut;
        //public final String dateTimeFormat;
        //public final String countDownFormat;

        ConfigLocale(FileConfiguration config) {
            this.configFile = config;

            lang = config.getString("locale.lang");
            decNumber = config.getInt("locale.decimal-display");
            titleFadeIn = config.getInt("locale.title.fade-in");
            titleStay = config.getInt("locale.title.stay");
            titleFadeOut = config.getInt("locale.title.fade-out");
            //dateTimeFormat = config.getString("locale.datetime-format");
            //countDownFormat = config.getString("locale.countdown-format");
        }

        /*public String getDateTimeFormat() {
            if (dateTimeFormat == null) {
                return "yyyy-MM-dd HH:mm:ss";
            }
            return dateTimeFormat;
        }

        public String getCountdownFormat() {
            if (countDownFormat == null) {
                return "{dd} {HH}:{mm}:{ss}";
            }
            return countDownFormat;
        }*/
    }

    public class ConfigGeneral {
        public FileConfiguration configFile;

        public final float configVersion;
        public final int topScoreLimit;
        public final boolean useWorldGuard;

        ConfigGeneral(FileConfiguration config) {
            configFile = config;

            configVersion = (float) config.getDouble("general.config-version", 1.0F);
            topScoreLimit = config.getInt("general.topscore-limit", 10);
            useWorldGuard = Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard") && config.getBoolean("general.use-worldguard", false);

        }
    }

    public class ConfigTeam {
        public FileConfiguration configFile;

        public final String teamSystem;

        ConfigTeam(FileConfiguration config) {
            configFile = config;

            teamSystem = config.getString("team.system", "nodewar");
        }
    }

    public class ConfigBossBar {
        public FileConfiguration configFile;

        public final String[] relations = new String[]{"neutral", "team", "ally", "truce", "enemy"};
        public final Map<String, BarColor> stringBarColorMap = new HashMap<>();

        ConfigBossBar(FileConfiguration config) {
            configFile = config;

            for (String relation : relations) {
                try {
                    stringBarColorMap.put(relation, BarColor.valueOf(config.getString("bossbar.color." + relation)));
                } catch (NullPointerException | IllegalArgumentException e) {
                    AdaptMessage.print(
                            "The color " + (config.getString("bossbar.color.") + relation) + " does not exist for relation "
                                    + relation + ". Use PINK color instead"
                            , AdaptMessage.prints.ERROR);
                    stringBarColorMap.put(relation, BarColor.PINK);
                }
            }
        }
    }

    public final FileConfiguration config;

    ConfigData(FileConfiguration config) {
        this.config = config;

        this.storage = new ConfigStorage(readConfig(config, "storage"));
        this.locale = new ConfigLocale(readConfig(config, "locale"));
        this.general = new ConfigGeneral(readConfig(config, "general"));
        this.team = new ConfigTeam(readConfig(config, "team"));
        this.bossbar = new ConfigBossBar(readConfig(config, "bossbar"));
    }

    private FileConfiguration readConfig(FileConfiguration baseConfig, String item) {
        try {
            File file = new File("plugins/" + plugin.getName() + "/" + baseConfig.getString(item) + ".yml");
            if (!(file.exists())) {
                return baseConfig;
            }
            return YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            e.printStackTrace();
            //If error such a ConfigurationSection instead of String
            return baseConfig;
        }
    }

    public static void init(FileConfiguration config) {
        configData = new ConfigData(config);
    }

    public static ConfigData getConfigData() {
        return configData;
    }

}
