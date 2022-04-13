package fr.rosstail.nodewar.empires;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.territory.zonehandlers.WorldTerritoryManager;
import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Empire
{
    private final String name;
    private final FileConfiguration config;
    private String display;
    private BarColor barColor;
    private final Map<World, ArrayList<Territory>> worldTerritories = new HashMap<>();
    private boolean friendlyFire;
    private String ownerUUID;
    
    Empire(final FileConfiguration config, final String key) {
        this.name = key;
        this.config = config;
        String display = config.getString(key + ".display");
        if (display == null) {
            display = "&7" + key;
        }
        this.display = ChatColor.translateAlternateColorCodes('&', display);
        if (config.getString(key + ".boss-bar-color") != null) {
            this.barColor = BarColor.valueOf(config.getString(key + ".boss-bar-color"));
        }
        else {
            this.barColor = BarColor.WHITE;
        }
        if (config.getString(key + ".friendly-fire") != null) {
            this.friendlyFire = config.getBoolean(key + ".friendly-fire");
        }
        else {
            this.friendlyFire = true;
        }
        this.ownerUUID = config.getString(key + ".owner-uuid");
    }

    Empire(Player player, String name) {
        this.name = name;
        this.config = new YamlConfiguration();
        this.display = AdaptMessage.empireMessage(this, "&7" + name);
        this.friendlyFire = true;
        this.barColor = BarColor.WHITE;
        this.ownerUUID = player.getUniqueId().toString();
        saveConfigFile();
    }

    /**
     * No Empire
     */
    Empire() {
        this.name = null;
        this.config = null;
        this.display = ChatColor.translateAlternateColorCodes('&', "&7None");
        this.barColor = BarColor.WHITE;
        this.friendlyFire = true;
    }
    
    public String getName() {
        return this.name;
    }

    public String getDisplay() {
        return this.display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(String ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public ArrayList<Territory> getWorldTerritories(final World world) {
        if (this.worldTerritories.containsKey(world)) {
            return new ArrayList<>(this.worldTerritories.get(world));
        }
        return new ArrayList<>();
    }
    
    public BarColor getBarColor() {
        return this.barColor;
    }

    public void setBarColor(BarColor barColor) {
        this.barColor = barColor;
    }

    public void applyTerritories() {
        final List<World> worlds = new ArrayList<>(WorldTerritoryManager.getUsedWorlds().keySet());
        for (final World world : worlds) {
            final ArrayList<Territory> territories = new ArrayList<>();
            for (final WorldTerritoryManager manager : WorldTerritoryManager.getUsedWorlds().values()) {
                for (final Territory territory : manager.getTerritories().values()) {
                    if (territory.getWorld().equals(world) && territory.getEmpire() != null && territory.getEmpire().equals(this)) {
                        if (!worldTerritories.containsKey(world)) {
                            worldTerritories.put(world, new ArrayList<>());
                        }
                        worldTerritories.get(world).add(territory);
                        territories.add(territory);
                        final ProtectedRegion region = territory.getRegion();
                        region.getMembers().removeAll();
                        region.getMembers().addGroup(name);
                    }
                }
            }
            if (this.worldTerritories.containsKey(world)) {
                worldTerritories.replace(world, territories);
            } else {
                this.worldTerritories.put(world, territories);
            }
        }
    }
    
    public boolean isFriendlyFire() {
        return this.friendlyFire;
    }

    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }
    private void saveConfigFile() {
        config.createSection(name);
        config.set(name + ".display", display);
        config.set(name + ".friendly-fire", friendlyFire);
        config.set(name + ".boss-bar-color", barColor.toString());
        config.set(name + ".owner-uuid", ownerUUID);
        try {
            File file = new File(Nodewar.getInstance().getDataFolder(), "empires/" + name + ".yml");
            config.save(file);
            System.out.println("New empire created : " + display);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
