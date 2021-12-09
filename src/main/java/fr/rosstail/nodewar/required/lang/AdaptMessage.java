package fr.rosstail.conquest.required.lang;

import fr.rosstail.conquest.character.datahandlers.PlayerInfo;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AdaptMessage
{

    public static void sendActionBar(final Player player, final String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(setPlaceholderMessage(player, message)));
    }
    
    public static String playerEmpireMessage(final Player player, String message) {
        if (message == null) {
            return null;
        }
        if (message.contains(PlaceHolders.PLAYER_EMPIRE_STARTER.getText()) || message.contains(PlaceHolders.PLAYER_EMPIRE.getText())) {
            final PlayerInfo playerInfo = PlayerInfo.gets(player);
            if (message.contains(PlaceHolders.PLAYER_EMPIRE.getText())) {
                message = message.replaceAll(PlaceHolders.PLAYER_EMPIRE.getText(), playerInfo.getEmpire().getDisplay());
            }
        }
        return ChatColor.translateAlternateColorCodes('&', setPlaceholderMessage(player, message));
    }
    
    public static String playerMessage(final Player player, String message) {
        if (message == null) {
            return null;
        }
        if (player != null) {
            if (message.contains(PlaceHolders.PLAYER_NAME.getText())) {
                message = message.replaceAll(PlaceHolders.PLAYER_NAME.getText(), player.getName());
            }
            message = playerEmpireMessage(player, message);
        }
        return ChatColor.translateAlternateColorCodes('&', setPlaceholderMessage(player, message));
    }
    
    private static String setPlaceholderMessage(final Player player, final String message) {
        if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return PlaceholderAPI.setPlaceholders(player, message);
        }
        return message;
    }
    
    static {
    }
}
