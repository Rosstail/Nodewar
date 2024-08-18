package fr.rosstail.nodewar;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import fr.rosstail.nodewar.apis.PAPIExpansion;
import fr.rosstail.nodewar.battlefield.BattlefieldManager;
import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.events.MinecraftEventHandler;
import fr.rosstail.nodewar.events.NodewarEventHandler;
import fr.rosstail.nodewar.events.WorldguardEventHandler;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.permissionmannager.PermissionManager;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.player.PlayerModel;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.TeamManager;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.battle.BattleManager;
import fr.rosstail.nodewar.territory.objective.ObjectiveManager;
import fr.rosstail.nodewar.webmap.WebmapManager;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

public class Nodewar extends JavaPlugin implements Listener {
    private YamlConfiguration config;
    private static final Logger log;
    private static Economy econ;
    private static Permission perms;
    private static final Chat chat;
    private static Nodewar instance;
    private static String dimName;
    private MinecraftEventHandler minecraftEventHandler;
    private WorldguardEventHandler worldguardEventHandler;
    private NodewarEventHandler nodewarEventHandler;

    public void onLoad() {
    }

    public static Nodewar getInstance() {
        return Nodewar.instance;
    }

    public void loadCustomConfig() {
        File fileConfig = new File("plugins/" + getName() + "/config.yml");
        if (!(fileConfig.exists())) {
            AdaptMessage.print("[" + this.getName() + "] Preparing default config.yml", AdaptMessage.prints.OUT);
            this.saveDefaultConfig();
        }
        config = YamlConfiguration.loadConfiguration(fileConfig);
        saveResource("conquest/territory-types.yml", false);

        ConfigData.init(getCustomConfig());
        initDefaultLocales();

        LangManager.initCurrentLang(ConfigData.getConfigData().locale.lang);
    }

    private void initDefaultLocales() {
        try {
            FileResourcesUtils.generateYamlFile("lang", this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getDimName() {
        return Nodewar.dimName;
    }

    public void onEnable() {
        instance = this;
        dimName = instance.getName().toLowerCase();

        AdaptMessage.initAdaptMessage(this);
        TeamManager.init();
        TerritoryManager.init(this);
        ObjectiveManager.init(this);
        BattleManager.init(this);
        BattlefieldManager.init(this);


        loadCustomConfig();
        PermissionManager.init();
        PermissionManager.getManager().loadManager();

        WebmapManager.init(this);
        WebmapManager.getManager().loadManager();

        if (PermissionManager.getManager() == null) {
            AdaptMessage.print("[" + this.getName() + "] No permission plugin available. Disabling", AdaptMessage.prints.ERROR);
            this.onDisable();
        }

        this.createDataFolder();
        StorageManager storageManager = StorageManager.initStorageManage(this);
        storageManager.chooseDatabase();

        WorldGuardPlugin wgPlugin = this.getWGPlugin();
        if (wgPlugin != null) {
            AdaptMessage.print("[" + this.getName() + "] Worldguard has been detected", AdaptMessage.prints.OUT);
        } else {
            AdaptMessage.print("[" + this.getName() + "] Worldguard has not been found.", AdaptMessage.prints.ERROR);
            this.onDisable();
        }
        this.initDefaultConfigs();

        if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PAPIExpansion(this).register();
            Bukkit.getPluginManager().registerEvents(this, this);
        }

        if (setupEconomy()) {
            AdaptMessage.print("[" + this.getName() + "] Hooked with Vault !", AdaptMessage.prints.OUT);
            //setupPermissions();
        } else {
            log.severe(String.format("[" + this.getName() + "] Didn't found Vault.", this.getDescription().getName()));
        }

        minecraftEventHandler = new MinecraftEventHandler();
        worldguardEventHandler = new WorldguardEventHandler();
        nodewarEventHandler = new NodewarEventHandler();


        Bukkit.getPluginManager().registerEvents(minecraftEventHandler, this);
        Bukkit.getPluginManager().registerEvents(worldguardEventHandler, this);
        Bukkit.getPluginManager().registerEvents(nodewarEventHandler, this);

        this.getCommand(getName().toLowerCase()).setExecutor(new CommandManager());

        TerritoryManager territoryManager = TerritoryManager.getTerritoryManager();

        territoryManager.loadTerritoryTypeConfig();
        territoryManager.loadTerritoryConfigs("plugins/" + getName() + "/conquest/territories");

        TeamManager.getManager().loadTeams();
        BattlefieldManager.getBattlefieldManager().loadBattlefieldList();
        territoryManager.setupTerritoriesOwner();
        territoryManager.setupTerritoriesSubTerritories();
        territoryManager.setupTerritoriesObjective();
        territoryManager.setupTerritoriesBattle();
        territoryManager.setupTerritoriesAttackRequirements();
        territoryManager.setupTerritoriesRewardScheduler();

        WebmapManager webmapManager = WebmapManager.getManager();
        webmapManager.createMarkerSet();

        webmapManager.addTerritorySetToDraw(new HashSet<>(territoryManager.getTerritoryMap().values()));

        PlayerDataManager.startDeployHandler();
        BattlefieldManager.getBattlefieldManager().startBattlefieldDispatcher();
    }

    private void initDefaultConfigs() {
        try {
            FileResourcesUtils.generateYamlFile("conquest", this);
            FileResourcesUtils.generateYamlFile("conquest/territories", this);
            FileResourcesUtils.generateYamlFile("lang", this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the folder for player's datas
     */
    private void createDataFolder() {
        File folder = new File(this.getDataFolder(), "data/");
        if (!folder.exists()) {
            String message = this.getCustomConfig().getString("messages.creating-data-folder");
            if (message != null) {
                message = AdaptMessage.getAdaptMessage().adaptMessage(message);

                getServer().getConsoleSender().sendMessage(message);
            }
            folder.mkdir();
        }
    }

    public void onDisable() {
        minecraftEventHandler.setClosing(true);
        worldguardEventHandler.setClosing(true);
        nodewarEventHandler.setClosing(true);

        Map<String, PlayerData> playerDataMap = PlayerDataManager.getPlayerDataMap();
        for (Map.Entry<String, PlayerData> entry : playerDataMap.entrySet()) {
            String s = entry.getKey();
            PlayerModel model = entry.getValue();
            StorageManager.getManager().updatePlayerModel(model, false);
        }

        TerritoryManager.getTerritoryManager().stopAllObjective();
        StorageManager.getManager().disconnect();
    }

    private boolean setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        final RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    /*private boolean setupPermissions() {
        final RegisteredServiceProvider<Permission> rsp = this.getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return true;
    }*/

    public static Economy getEconomy() {
        return econ;
    }

    public static Permission getPermissions() {
        return perms;
    }

    public static Chat getChat() {
        return chat;
    }

    private WorldGuardPlugin getWGPlugin() {
        final Plugin plugin = this.getServer().getPluginManager().getPlugin("WorldGuard");
        if (!(plugin instanceof WorldGuardPlugin)) {
            return null;
        }
        return (WorldGuardPlugin) plugin;
    }

    static {
        log = Logger.getLogger("Minecraft");
        econ = null;
        perms = null;
        chat = null;
    }

    public YamlConfiguration getCustomConfig() {
        return YamlConfiguration.loadConfiguration(new File("plugins/" + getName() + "/config.yml"));
    }
}
