package fr.rosstail.nodewar;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import fr.rosstail.nodewar.calendar.CalendarManager;
import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.datahandlers.PlayerInfoManager;
import fr.rosstail.nodewar.empires.EmpireManager;
import fr.rosstail.nodewar.eventhandler.PlayerEventHandler;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.required.DataBaseInteractions;
import fr.rosstail.nodewar.required.FileResourcesUtils;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.PAPIExpansion;
import fr.rosstail.nodewar.territory.WorldGuardInteractions;
import fr.rosstail.nodewar.territory.zonehandlers.DynmapHandler;
import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import fr.rosstail.nodewar.territory.eventhandlers.NodeWarEventsListener;
import fr.rosstail.nodewar.territory.eventhandlers.WGRegionEventsListener;
import fr.rosstail.nodewar.territory.zonehandlers.WorldTerritoryManager;
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
import java.util.logging.Logger;

public class Nodewar extends JavaPlugin implements Listener
{
    private static final Logger log;
    private static Economy econ;
    private static Permission perms;
    private static final Chat chat;
    private static Nodewar instance;
    private static String dimName;
    
    public void onLoad() {
    }
    
    public static Nodewar getInstance() {
        return Nodewar.instance;
    }
    
    public static String getDimName() {
        return Nodewar.dimName;
    }
    
    public void onEnable() {
        instance = this;
        dimName = instance.getName().toLowerCase();
        if (!new File("plugins/" + getName() + "/config.yml").exists()) {
            AdaptMessage.print("Preparing default config.yml", AdaptMessage.prints.OUT);
            this.saveDefaultConfig();
        }

        WorldGuardPlugin wgPlugin = this.getWGPlugin();
        if (wgPlugin != null) {
            WGRegionEventsListener wgRegionEventsListener = new WGRegionEventsListener(this);
            NodeWarEventsListener nodewarEventsListener = new NodeWarEventsListener();
            getServer().getPluginManager().registerEvents(wgRegionEventsListener, wgPlugin);
            getServer().getPluginManager().registerEvents(nodewarEventsListener, this);
        }
        this.initDefaultConfigs();
        LangManager.initCurrentLang();
        if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PAPIExpansion(this).register();
            Bukkit.getPluginManager().registerEvents(this, this);
        }
        EmpireManager.initEmpireManager(this);
        EmpireManager.getEmpireManager().init();
        if (setupEconomy()) {
            AdaptMessage.print("[" + this.getName() + "] Hooked with Vault !", AdaptMessage.prints.OUT);
            setupPermissions();
        } else {
            log.severe(String.format("[" + this.getName() + "] Didn't found Vault.", this.getDescription().getName()));
        }
        Bukkit.getPluginManager().registerEvents(new PlayerEventHandler(this), this);
        if (Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            Territory.initWorldTerritories(this);
            for (final WorldTerritoryManager manager : WorldTerritoryManager.getUsedWorlds().values()) {
                for (final Territory territory : manager.getTerritories().values()) {
                    territory.setupSubTerritories();
                    WorldTerritoryManager.setUpObjective(territory);
                    territory.initTargets();
                }
            }
            for (final Empire empire : EmpireManager.getEmpireManager().getEmpires().values()) {
                empire.applyTerritories();
            }

            DynmapHandler.init(this);
            DynmapHandler dynmapHandler = DynmapHandler.getDynmapHandler();
            dynmapHandler.enable();

        }
        if (getCustomConfig().getBoolean("general.use-calendar")) {
            CalendarManager.init(this);
        }
        String connectorStr = getCustomConfig().getString("mysql.connector");
        if (connectorStr != null && !connectorStr.equalsIgnoreCase("none")) {
            DataBaseInteractions.init(instance);
        }
        PlayerInfoManager.init(this);
        PlayerInfoManager.getPlayerInfoManager().startTimer();
        this.getCommand(dimName).setExecutor(new CommandManager());
        //this.getCommand(dimName).setExecutor(new NodewarCommands(this));
    }
    
    private void initDefaultConfigs() {
        try {
            FileResourcesUtils.main("worlds", this, false);
            FileResourcesUtils.main("empires", this, false);
            FileResourcesUtils.main("gui", this, false);
            FileResourcesUtils.main("lang", this, false);
            FileResourcesUtils.main("playerdata", this, false);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void onDisable() {
        WorldGuardInteractions.stopTimer();
        if (getCustomConfig().getBoolean("general.use-calendar")) {
            CalendarManager.getCalendarManager().stopCalenderSchedule();
        }
        if (DynmapHandler.getDynmapHandler() != null) {
            DynmapHandler.getDynmapHandler().disable();
        }
        PlayerInfoManager playerInfoManager = PlayerInfoManager.getPlayerInfoManager();
        playerInfoManager.stopTimer();
        playerInfoManager.getPlayerInfoMap().forEach((player, playerInfo) -> {
            playerInfo.updateAll(false);
        });
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
    
    private boolean setupPermissions() {
        final RegisteredServiceProvider<Permission> rsp = this.getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return true;
    }
    
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
        return (WorldGuardPlugin)plugin;
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
