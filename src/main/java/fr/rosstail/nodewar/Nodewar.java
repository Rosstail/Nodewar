package fr.rosstail.conquest;

import com.rosstail.karma.Karma;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import fr.rosstail.conquest.character.commandhandlers.ConquestCommands;
import fr.rosstail.conquest.character.eventhandler.KarmaListener;
import fr.rosstail.conquest.character.eventhandler.PlayerEventHandler;
import fr.rosstail.conquest.character.empires.Empire;
import fr.rosstail.conquest.required.DataBase;
import fr.rosstail.conquest.required.FileResourcesUtils;
import fr.rosstail.conquest.required.lang.LangManager;
import fr.rosstail.conquest.required.lang.PAPIExpansion;
import fr.rosstail.conquest.territory.zonehandlers.Territory;
import fr.rosstail.conquest.territory.eventhandlers.ConquestEventsListener;
import fr.rosstail.conquest.territory.eventhandlers.WGRegionEventsListener;
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

public class Conquest extends JavaPlugin implements Listener
{
    private static final Logger log;
    private static Economy econ;
    private static Permission perms;
    private static Chat chat;
    private static Conquest instance;
    private static String dimName;
    DataBase database;
    
    public Conquest() {
        this.database = DataBase.gets(this);
    }
    
    public void onLoad() {
    }
    
    public static Conquest getInstance() {
        return Conquest.instance;
    }
    
    public static String getDimName() {
        return Conquest.dimName;
    }
    
    public void onEnable() {
        Conquest.instance = this;
        Conquest.dimName = Conquest.instance.getName().toLowerCase();
        if (!new File("plugins/Conquest/config.yml").exists()) {
            System.out.println("Preparing default config.yml");
            this.saveDefaultConfig();
        }

        Karma karma = this.getKarmaPlugin();
        if (karma != null) {
            KarmaListener listener = new KarmaListener(karma);
            this.getServer().getPluginManager().registerEvents(listener, karma);
            System.out.println("[CONQUEST] Karma is OK");
        } else {
            System.out.println("[CONQUEST] Karma is NULL");
        }

        WorldGuardPlugin wgPlugin = this.getWGPlugin();
        if (wgPlugin != null) {
            WGRegionEventsListener wgRegionEventsListener = new WGRegionEventsListener(this);
            ConquestEventsListener conquestEventsListener = new ConquestEventsListener();
            this.getServer().getPluginManager().registerEvents(wgRegionEventsListener, wgPlugin);
            this.getServer().getPluginManager().registerEvents(conquestEventsListener, this);
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
            Conquest.log.severe(String.format("[" + this.getName() + "] Didn't found Vault.", this.getDescription().getName()));
        }
        Bukkit.getPluginManager().registerEvents(new PlayerEventHandler(this), this);
        if (Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            Territory.initWorldTerritories(this);
            for (final Territory territory : Territory.getAllTerritories()) {
                territory.initCanAttack();
            }
            for (final Empire empire : Empire.getEmpires().values()) {
                empire.applyTerritories();
            }
        }
        this.getCommand(Conquest.dimName).setExecutor(new ConquestCommands(this));
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
        Conquest.econ = rsp.getProvider();
        return Conquest.econ != null;
    }
    
    private boolean setupPermissions() {
        final RegisteredServiceProvider<Permission> rsp = (RegisteredServiceProvider<Permission>)this.getServer().getServicesManager().getRegistration((Class)Permission.class);
        Conquest.perms = rsp.getProvider();
        return Conquest.perms != null;
    }
    
    public static Economy getEconomy() {
        return Conquest.econ;
    }
    
    public static Permission getPermissions() {
        return Conquest.perms;
    }
    
    public static Chat getChat() {
        return Conquest.chat;
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
        Conquest.econ = null;
        Conquest.perms = null;
        Conquest.chat = null;
    }

    public YamlConfiguration getCustomConfig() {
        return YamlConfiguration.loadConfiguration(new File("plugins/Conquest/config.yml"));
    }
}
