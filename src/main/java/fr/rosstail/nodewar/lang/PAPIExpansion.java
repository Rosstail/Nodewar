package fr.rosstail.nodewar.lang;

import fr.rosstail.nodewar.datahandlers.PlayerInfo;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PAPIExpansion extends PlaceholderExpansion
{
    private final fr.rosstail.nodewar.Nodewar plugin;
    
    public boolean canRegister() {
        return Bukkit.getPluginManager().getPlugin("Nodewar") != null;
    }
    
    public PAPIExpansion(final fr.rosstail.nodewar.Nodewar plugin) {
        this.plugin = plugin;
    }
    
    public boolean register() {
        return this.canRegister() && this.plugin != null && super.register();
    }
    
    public String getAuthor() {
        return "Rosstail";
    }
    
    public String getIdentifier() {
        return fr.rosstail.nodewar.Nodewar.getDimName();
    }
    
    public String getRequiredPlugin() {
        return this.plugin.getName();
    }
    
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    public String onPlaceholderRequest(final Player player, final String identifier) {
        if (player != null) {

            if (identifier.startsWith("player")) {
                if (identifier.equals("player")) {
                    return player.getName();
                }

                return playerMessage(player, "%" + fr.rosstail.nodewar.Nodewar.getDimName() + "_" + identifier + "%");
            }
        }

        return "%" + fr.rosstail.nodewar.Nodewar.getDimName() + "_" + identifier + "%";
    }

    public String playerEmpireMessage(final Player player, String message) {
        if (message == null) {
            return null;
        }
        if (message.contains(PlaceHolders.PLAYER_EMPIRE_STARTER.getText()) || message.contains(PlaceHolders.PLAYER_EMPIRE_DISPLAY.getText())) {
            final PlayerInfo playerInfo = PlayerInfo.gets(player);
            if (message.contains(PlaceHolders.PLAYER_EMPIRE_DISPLAY.getText())) {
                message = message.replaceAll(PlaceHolders.PLAYER_EMPIRE_DISPLAY.getText(), playerInfo.getEmpire().getDisplay());
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
            if (message.contains(PlaceHolders.PLAYER_EMPIRE_STARTER.getText()) || message.contains(PlaceHolders.PLAYER_EMPIRE_DISPLAY.getText())) {
                final PlayerInfo playerInfo = PlayerInfo.gets(player);
                if (message.contains(PlaceHolders.PLAYER_EMPIRE.getText())) {
                    message = message.replaceAll(PlaceHolders.PLAYER_EMPIRE.getText(), playerInfo.getEmpire().getName());
                }
                if (message.contains(PlaceHolders.PLAYER_EMPIRE_DISPLAY.getText())) {
                    message = message.replaceAll(PlaceHolders.PLAYER_EMPIRE_DISPLAY.getText(), playerInfo.getEmpire().getDisplay());
                }
            }
            message = playerEmpireMessage(player, message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
