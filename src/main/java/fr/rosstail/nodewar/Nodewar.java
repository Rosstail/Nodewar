package fr.rosstail.nodewar;

import com.rosstail.karma.Karma;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import fr.rosstail.nodewar.commandhandlers.NodewarCommands;
import fr.rosstail.nodewar.eventhandler.KarmaListener;
import fr.rosstail.nodewar.eventhandler.PlayerEventHandler;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.required.DataBase;
import fr.rosstail.nodewar.required.FileResourcesUtils;
import fr.rosstail.nodewar.required.lang.LangManager;
import fr.rosstail.nodewar.required.lang.PAPIExpansion;
import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import fr.rosstail.nodewar.territory.eventhandlers.NodewarEventsListener;
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
    private static Chat chat;
    private static Nodewar instance;
    private static String dimName;
    DataBase database;
    
    public Nodewar() {
        this.database = DataBase.gets(this);
    }
    
    public void onLoad() {
    }
    
    public static Nodewar getInstance() {
        return Nodewar.instance;
    }
    
    public static String getDimName() {
        return Nodewar.dimName;
    }
    
    public void onEnable() {
        fr.rosstail.nodewar.Nodewar.instance = this;
        fr.rosstail.nodewar.Nodewar.dimName = fr.rosstail.nodewar.Nodewar.instance.getName().toLowerCase();
        if (!new File("plugins/Nodewar/config.yml").exists()) {
            System.out.println("Preparing default config.yml");
            this.saveDefaultConfig();
        }

        Karma karma = this.getKarmaPlugin();
        if (karma != null) {
            KarmaListener listener = new KarmaListener(karma);
            this.getServer().getPluginManager().registerEvents(listener, karma);
            System.out.println("[NODEWAR] Karma is OK");
        } else {
            System.out.println("[NODEWAR] Karma is NULL");
        }

        WorldGuardPlugin wgPlugin = this.getWGPlugin();
        if (wgPlugin != null) {
            WGRegionEventsListener wgRegionEventsListener = new WGRegionEventsListener(this);
            NodewarEventsListener nodewarEventsListener = new NodewarEventsListener();
            this.getServer().getPluginManager().registerEvents(wgRegionEventsListener, wgPlugin);
            this.getServer().getPluginManager().registerEvents(nodewarEventsListener, this);
        }
        this.initDefaultConfigs();
        LangManager.initCurrentLang();
        if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PAPIExpansion(this).register();
            Bukkit.getPluginManager().registerEvents(this, this);
        }
        if (this.database.isConnexionEnabled()) {
            this.database.prepareConnection();
        }
        Empire.init(this);
        if (this.setupEconomy()) {
            System.out.println("[" + this.getName() + "] Hooked with Vault !");
            this.setupPermissions();
        }
        else {
            fr.rosstail.nodewar.Nodewar.log.severe(String.format("[" + this.getName() + "] Didn't found Vault.", this.getDescription().getName()));
        }
        Bukkit.getPluginManager().registerEvents(new PlayerEventHandler(this), this);
        if (Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            Territory.initWorldTerritories(this);
            for (final WorldTerritoryManager manager : WorldTerritoryManager.getUsedWorlds().values()) {
                for (final Territory territory : manager.getTerritories().values()) {
                    territory.initCanAttack();
                }
            }
            for (final Empire empire : Empire.getEmpires().values()) {
                empire.applyTerritories();
            }
        }
        this.getCommand(fr.rosstail.nodewar.Nodewar.dimName).setExecutor(new NodewarCommands(this));
    }
    
    private void initDefaultConfigs() {
        try {
            FileResourcesUtils.main("worlds", this);
            FileResourcesUtils.main("empires", this);
            FileResourcesUtils.main("gui", this);
            FileResourcesUtils.main("lang", this);
            FileResourcesUtils.main("playerdata", this);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void onDisable() {
        this.database.closeConnection();
    }
    
    private boolean setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        final RegisteredServiceProvider<Economy> rsp = (RegisteredServiceProvider<Economy>)this.getServer().getServicesManager().getRegistration((Class)Economy.class);
        if (rsp == null) {
            return false;
        }
        fr.rosstail.nodewar.Nodewar.econ = rsp.getProvider();
        return fr.rosstail.nodewar.Nodewar.econ != null;
    }
    
    private boolean setupPermissions() {
        final RegisteredServiceProvider<Permission> rsp = (RegisteredServiceProvider<Permission>)this.getServer().getServicesManager().getRegistration((Class)Permission.class);
        fr.rosstail.nodewar.Nodewar.perms = rsp.getProvider();
        return fr.rosstail.nodewar.Nodewar.perms != null;
    }
    
    public static Economy getEconomy() {
        return fr.rosstail.nodewar.Nodewar.econ;
    }
    
    public static Permission getPermissions() {
        return fr.rosstail.nodewar.Nodewar.perms;
    }
    
    public static Chat getChat() {
        return fr.rosstail.nodewar.Nodewar.chat;
    }
    
    private Karma getKarmaPlugin() {
        final Plugin plugin = this.getServer().getPluginManager().getPlugin("Karma");
        if (plugin == null) {
            return null;
        }
        return (Karma) plugin;
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
        fr.rosstail.nodewar.Nodewar.econ = null;
        fr.rosstail.nodewar.Nodewar.perms = null;
        fr.rosstail.nodewar.Nodewar.chat = null;
    }

    public YamlConfiguration getCustomConfig() {
        return YamlConfiguration.loadConfiguration(new File("plugins/Nodewar/config.yml"));
    }
}
