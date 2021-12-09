package fr.rosstail.conquest.required.lang;

import fr.rosstail.conquest.character.datahandlers.PlayerInfo;
import fr.rosstail.conquest.Conquest;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PAPIExpansion extends PlaceholderExpansion
{
    private final Conquest plugin;
    
    public boolean canRegister() {
        return Bukkit.getPluginManager().getPlugin("Conquest") != null;
    }
    
    public PAPIExpansion(final Conquest plugin) {
        this.plugin = plugin;
    }
    
    public boolean register() {
        return this.canRegister() && this.plugin != null && super.register();
    }
    
    public String getAuthor() {
        return "Rosstail";
    }
    
    public String getIdentifier() {
        return Conquest.getDimName();
    }
    
    public String getRequiredPlugin() {
        return this.plugin.getName();
    }
    
    public String getVersion() {
        return "0.1";
    }
    
    public String onPlaceholderRequest(final Player player, final String identifier) {
        if (player != null) {

            if (identifier.startsWith("player")) {
                if (identifier.equals("player")) {
                    return player.getName();
                }

                return playerMessage(player, "%" + Conquest.getDimName() + "_" + identifier + "%");
            }
        }

        return "%" + Conquest.getDimName() + "_" + identifier + "%";
    }

    public String playerEmpireMessage(final Player player, String message) {
        if (message == null) {
            return null;
        }
        if (message.contains(PlaceHolders.PLAYER_EMPIRE_STARTER.getText()) || message.contains(PlaceHolders.PLAYER_EMPIRE.getText())) {
            final PlayerInfo playerInfo = PlayerInfo.gets(player);
            if (message.contains(PlaceHolders.PLAYER_EMPIRE.getText())) {
                message = message.replaceAll(PlaceHolders.PLAYER_EMPIRE.getText(), playerInfo.getEmpire().getDisplay());
            }
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String playerMessage(final Player player, String message) {
        if (message == null) {
            return null;
        }
        if (player != null) {
            if (message.contains(PlaceHolders.PLAYER_NAME.getText())) {
                message = message.replaceAll(PlaceHolders.PLAYER_NAME.getText(), player.getName());
            }
            message = playerEmpireMessage(player, message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
