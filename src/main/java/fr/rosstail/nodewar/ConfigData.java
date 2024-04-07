package fr.rosstail.nodewar;

import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.team.RelationType;
import org.bukkit.boss.BarColor;
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

    public final ConfigDynmap dynmap;

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
        public final String dateTimeFormat;
        public final String countDownFormat;

        ConfigLocale(FileConfiguration config) {
            this.configFile = config;

            lang = config.getString("locale.lang", "en_EN");
            decNumber = config.getInt("locale.decimal-display");
            titleFadeIn = config.getInt("locale.title.fade-in");
            titleStay = config.getInt("locale.title.stay");
            titleFadeOut = config.getInt("locale.title.fade-out");
            dateTimeFormat = config.getString("locale.datetime-format", "YYYY-MM-dd HH:mm:ss");
            countDownFormat = config.getString("locale.countdown-format", "{dd} {HH}:{mm}:{ss}");
        }
    }

    public class ConfigGeneral {
        public FileConfiguration configFile;

        public final float configVersion;
        public final String defaultPermissionPlugin;

        ConfigGeneral(FileConfiguration config) {
            configFile = config;

            configVersion = (float) config.getDouble("general.config-version", 1.0F);
            defaultPermissionPlugin = config.getString("general.permission-plugin", "auto");
        }
    }

    public class ConfigTeam {
        public FileConfiguration configFile;

        public final RelationType defaultRelation;
        public final String noneDisplay;
        public final String noneColor;
        public final long deployTimer;
        public final long deployCooldown;

        ConfigTeam(FileConfiguration config) {
            configFile = config;

            String relationTypeStr = config.getString("team.default-relation", "neutral");
            defaultRelation = RelationType.valueOf(relationTypeStr.toUpperCase());
            noneDisplay = config.getString("team.none-display", "None");
            noneColor = config.getString("team.none-color", "#CACACA");
            deployTimer = config.getLong("team.deploy-timer");
            deployCooldown = config.getLong("team.deploy-cooldown");
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
            try {
                stringBarColorMap.put("controlled", BarColor.valueOf(config.getString("bossbar.color.controlled")));
            } catch (NullPointerException | IllegalArgumentException e) {
                AdaptMessage.print(
                        "The color " + (config.getString("bossbar.color.controlled")
                                + " does not exist for controlled territory. Use PINK color instead")
                        , AdaptMessage.prints.ERROR);
                stringBarColorMap.put("controlled", BarColor.PINK);
            }
        }
    }

    public class ConfigDynmap {
        public FileConfiguration configFile;

        public final int layerPriority = 20;
        public final boolean hideByDefault;
        public final boolean use3DRegions;

        public final String infoWindow;
        public final int minimumZoom = 0;
        public final int maximumDepth = 16;
        public final int tickPerUpdate;

        public final int mapUpdateDelay;

        public final float fillOpacity;

        ConfigDynmap(FileConfiguration config) {
            configFile = config;
            hideByDefault = configFile.getBoolean("dynmap.hide-by-default", false);
            use3DRegions = configFile.getBoolean("dynmap.use-3d-region", false);
            infoWindow = configFile.getString("dynmap-info-window",
                    "<div class=\"infowindow\">" +
                            "<span style=\"font-size:120%;\">%regionname%</span>" +
                            "<br /> Team <span style=\"font-weight:bold;\">%groupmembers%</span>" +
                            "<br /> Flags<br />" +
                            "<span style=\"font-weight:bold;\">  > %flags%</span>" +
                            "</div>");
            tickPerUpdate = Math.max(1, configFile.getInt("dynmap.tick-per-update", 20));
            mapUpdateDelay = Math.max(1, configFile.getInt("dynmap.many-update-delay", 1));
            fillOpacity = Math.max(0f, (float) configFile.getInt("dynmap.fill-opacity", 50) / 100);
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
        this.dynmap = new ConfigDynmap(readConfig(config, "dynmap"));
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
