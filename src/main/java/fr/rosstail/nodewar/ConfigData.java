package fr.rosstail.nodewar;

import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.team.RelationType;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class ConfigData {
    private final Nodewar plugin = Nodewar.getInstance();
    private static ConfigData configData;

    public final ConfigStorage storage;
    public final ConfigGeneral general;
    public final ConfigPerm perm;
    public final ConfigLocale locale;

    public final ConfigTeam team;
    public final ConfigBattlefield battlefield;

    public final ConfigBossBar bossbar;

    public final ConfigWebmap webmap;

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
        public final boolean debugMode;

        ConfigGeneral(FileConfiguration config) {
            configFile = config;

            configVersion = (float) config.getDouble("config-version", 1.0F); // Not in general cause top of file
            debugMode = config.getBoolean("general.debug-mode", false);
        }
    }

    public class ConfigPerm {
        public FileConfiguration configFile;

        public final String defaultPermissionPlugin;
        public final String groupCreateCommand;
        public final String groupJoinCommand;
        public final String groupLeaveCommand;
        public final String groupDeleteCommand;

        ConfigPerm(FileConfiguration config) {
            configFile = config;

            defaultPermissionPlugin = config.getString("permission.plugin", "auto");
            groupCreateCommand = config.getString("permisison.create-group");
            groupJoinCommand = config.getString("permisison.join-group");
            groupLeaveCommand = config.getString("permisison.leave-group");
            groupDeleteCommand = config.getString("permisison.delete-group");
        }
    }

    public class ConfigTeam {
        public FileConfiguration configFile;

        public final String system;
        public final double creationCost;
        public final RelationType defaultRelation;
        public final String noneDisplay;
        public final String noneColor;
        public final int maximumMembers;
        public final long deployTimer;
        public final long deployCooldown;
        public final boolean canCounterAttack;

        public final short minimumNameLength;
        public final short maximumNameLength;
        public final short minimumShortnameLength;
        public final short maximumShortNameLength;

        ConfigTeam(FileConfiguration config) {
            configFile = config;

            system = config.getString("team.system", "auto");
            creationCost = config.getDouble("team.creation-cost");
            String relationTypeStr = config.getString("team.default-relation", "neutral");
            defaultRelation = RelationType.valueOf(relationTypeStr.toUpperCase());
            canCounterAttack = config.getBoolean("general.can-counter-attack", false);
            noneDisplay = config.getString("team.none-display", "None");
            noneColor = config.getString("team.none-color", "#CACACA");
            maximumMembers = config.getInt("team.maximum-members", 50);
            deployTimer = config.getLong("team.deploy-timer") * 1000;
            deployCooldown = config.getLong("team.deploy-cooldown") * 1000;
            minimumNameLength = (short) Math.min(config.getInt("team.name-min-length", 5), 33);
            maximumNameLength = (short) Math.min(config.getInt("team.name-max-length", 20), 33);
            minimumShortnameLength = (short) Math.min(config.getInt("team.shortname-min-length", 3), 40);
            maximumShortNameLength = (short) Math.min(config.getInt("team.shortname-max-length", 5), 40);
        }
    }

    public class ConfigBattlefield {
        public FileConfiguration configFile;

        public final List<String> alertTimers;

        ConfigBattlefield(FileConfiguration config) {
            configFile = config;
            alertTimers = config.getStringList("battlefield.alerts");
        }
    }

    public class ConfigBossBar {
        public FileConfiguration configFile;

        public final boolean enabled;
        public final String[] relations = new String[]{"neutral", "team", "ally", "truce", "enemy"};
        public final Map<String, BarColor> stringBarColorMap = new HashMap<>();

        ConfigBossBar(FileConfiguration config) {
            configFile = config;

            enabled = config.getBoolean("bossbar.enabled", true);
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

    public class ConfigWebmap {
        public FileConfiguration configFile;
        public final Set<String> pluginList = new HashSet<>();

        public final int layerPriority = 20;
        public final boolean simpleLine;
        public final int lineThickness;
        public final boolean hideByDefault;
        public final boolean use3DRegions;
        public final String backgroundColor;
        public final int minimumZoom = 0;
        public final int maximumDepth = 16;
        public final int tickPerUpdate;

        public final int mapUpdateDelay;

        public final float fillOpacity;
        public final float lineOpacity;


        public final float territoryTargetHeightDelta;

        public final String protectedTerritoryBorderColor;
        public final String vulnerableTerritoryBorderColor;

        ConfigWebmap(FileConfiguration config) {
            configFile = config;
            hideByDefault = configFile.getBoolean("webmap.hide-by-default", false);
            use3DRegions = configFile.getBoolean("webmap.use-3d-region", false);
            backgroundColor = configFile.getString("webmap.background-color", "#FFFFFF");
            tickPerUpdate = Math.max(1, configFile.getInt("webmap.tick-per-update", 20));
            mapUpdateDelay = Math.max(1, configFile.getInt("webmap.many-update-delay", 1));
            fillOpacity = Math.max(0f, (float) configFile.getInt("webmap.fill-opacity", 100) / 100);
            lineOpacity = Math.max(0f, (float) configFile.getInt("webmap.line-opacity", 100) / 100);
            simpleLine = config.getBoolean("webmap.simple-line", false);
            lineThickness = Math.max(config.getInt("webmap.line-thickness", 4), 1);
            pluginList.addAll(config.getStringList("webmap.plugins"));
            pluginList.addAll(config.getStringList("webmap.plugins"));
            territoryTargetHeightDelta = (float) config.getDouble("webmap.territory-target-height-delta");
            protectedTerritoryBorderColor = config.getString("webmap.territory-protected-border-color", "00AA00");
            vulnerableTerritoryBorderColor = config.getString("webmap.territory-vulnerable-border-color", "AA0000");
        }
    }

    public final FileConfiguration config;

    ConfigData(File configFile) {
        this.config = YamlConfiguration.loadConfiguration(configFile);

        this.storage = new ConfigStorage(YamlConfiguration.loadConfiguration(readConfig(configFile, "storage")));
        this.locale = new ConfigLocale(YamlConfiguration.loadConfiguration(readConfig(configFile, "locale")));
        this.general = new ConfigGeneral(YamlConfiguration.loadConfiguration(readConfig(configFile, "general")));
        this.perm = new ConfigPerm(YamlConfiguration.loadConfiguration(readConfig(configFile, "permission")));
        this.team = new ConfigTeam(YamlConfiguration.loadConfiguration(readConfig(configFile, "team")));
        this.battlefield = new ConfigBattlefield(YamlConfiguration.loadConfiguration(readConfig(configFile, "battlefield")));
        this.bossbar = new ConfigBossBar(YamlConfiguration.loadConfiguration(readConfig(configFile, "bossbar")));
        this.webmap = new ConfigWebmap(YamlConfiguration.loadConfiguration(readConfig(configFile, "webmap")));
    }

    public static File readConfig(File configFile, String item) {
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(configFile);
        try {
            File otherFile = new File("plugins/" + Nodewar.getInstance().getName() + "/" + configuration.getString(item) + ".yml");
            if (!(otherFile.exists())) {
                return configFile;
            }
            return otherFile;
        } catch (Exception e) {
            e.printStackTrace();
            //If error such a ConfigurationSection instead of String
            return configFile;
        }
    }

    public static void init(File file) {
        configData = new ConfigData(file);
    }

    public static ConfigData getConfigData() {
        return configData;
    }

}
