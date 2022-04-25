package fr.rosstail.nodewar.lang;

import fr.rosstail.nodewar.datahandlers.PlayerInfo;
import fr.rosstail.nodewar.datahandlers.PlayerInfoManager;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.empires.EmpireManager;
import fr.rosstail.nodewar.territory.zonehandlers.Territory;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.bukkit.Bukkit.getLogger;

public class AdaptMessage
{
    private static final Pattern hexPattern = Pattern.compile("\\{(#[a-fA-F0-9]{6})}");

    public enum prints {
        OUT,
        WARNING,
        SEVERE,
        ERROR;
    }

    public static void sendActionBar(final Player player, final String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(setPlaceholderMessage(player, message)));
    }

    public static String empireMessage(Empire empire, String message) {
        if (message == null) {
            return null;
        }
        if (empire == null) {
            empire = EmpireManager.getEmpireManager().getNoEmpire();
        }

        if (message.contains(PlaceHolders.EMPIRE_STARTER.getText())) {
            if (message.contains(PlaceHolders.EMPIRE_NAME.getText())) {
                message = message.replaceAll(PlaceHolders.EMPIRE_NAME.getText(), empire.getName());
            }
            if (message.contains(PlaceHolders.EMPIRE_DISPLAY.getText())) {
                message = message.replaceAll(PlaceHolders.EMPIRE_DISPLAY.getText(), empire.getDisplay());
            }
            if (message.contains(PlaceHolders.EMPIRE_FRIENDLY_FIRE.getText())) {
                message = message.replaceAll(PlaceHolders.EMPIRE_FRIENDLY_FIRE.getText(), String.valueOf(empire.isFriendlyFire()));
            }
        }

        return adapt(setPlaceholderMessage(null, message));
    }

    public static String worldMessage(World world, String message) {
        if (message == null) {
            return null;
        }
        if (world == null) {
            return message;
        }

        if (message.contains(PlaceHolders.WORLD_STARTER.getText())) {
            if (message.contains(PlaceHolders.WORLD_NAME.getText())) {
                message = message.replaceAll(PlaceHolders.WORLD_NAME.getText(), world.getName());
            }
        }
        return adapt(setPlaceholderMessage(null, message));
    }

    public static String territoryMessage(Territory territory, String message) {
        if (message == null) {
            return null;
        }
        if (territory == null) {
            return message;
        }

        if (message.contains(PlaceHolders.TERRITORY_STARTER.getText())) {
            if (message.contains(PlaceHolders.TERRITORY_NAME.getText())) {
                message = message.replaceAll(PlaceHolders.TERRITORY_NAME.getText(), territory.getName());
            }
            if (message.contains(PlaceHolders.TERRITORY_DISPLAY.getText())) {
                message = message.replaceAll(PlaceHolders.TERRITORY_DISPLAY.getText(), territory.getDisplay());
            }
            if (message.contains(PlaceHolders.TERRITORY_WORLD.getText())) {
                message = message.replaceAll(PlaceHolders.TERRITORY_WORLD.getText(), territory.getWorld().getName());
            }
            if (message.contains(PlaceHolders.TERRITORY_ON_ATTACK.getText())) {
                message = message.replaceAll(PlaceHolders.TERRITORY_ON_ATTACK.getText(), String.valueOf(territory.isUnderAttack()));
            }
            if (message.contains(PlaceHolders.TERRITORY_EMPIRE_OWNER.getText())) {
                Empire owner = territory.getEmpire();
                message = message.replaceAll(PlaceHolders.TERRITORY_EMPIRE_OWNER.getText(), owner != null ? owner.getDisplay() : EmpireManager.getEmpireManager().getNoEmpire().getDisplay());
            }
            if (message.contains(PlaceHolders.TERRITORY_EMPIRE_ADVANTAGE.getText())) {
                message = message.replaceAll(PlaceHolders.TERRITORY_EMPIRE_ADVANTAGE.getText(), String.valueOf(territory.getObjective().getAdvantage().getDisplay()));
            }
            if (message.contains(PlaceHolders.TERRITORY_REGION.getText())) {
                message = message.replaceAll(PlaceHolders.TERRITORY_REGION.getText(), territory.getRegion().getId());
            }
        }

        return adapt(setPlaceholderMessage(null, message));
    }

    public static String playerEmpireMessage(final Player player, String message) {
        if (message == null) {
            return null;
        }
        if (message.contains(PlaceHolders.PLAYER_EMPIRE_STARTER.getText()) || message.contains(PlaceHolders.PLAYER_EMPIRE_DISPLAY.getText())) {
            final PlayerInfo playerInfo = PlayerInfoManager.getPlayerInfoManager().getPlayerInfoMap().get(player);
            if (message.contains(PlaceHolders.PLAYER_EMPIRE.getText())) {
                message = message.replaceAll(PlaceHolders.PLAYER_EMPIRE.getText(), playerInfo.getEmpire().getName());
            }
            if (message.contains(PlaceHolders.PLAYER_EMPIRE_DISPLAY.getText())) {
                message = message.replaceAll(PlaceHolders.PLAYER_EMPIRE_DISPLAY.getText(), playerInfo.getEmpire().getDisplay());
            }
        }
        return adapt(setPlaceholderMessage(player, message));
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
        return adapt(setPlaceholderMessage(player, message));
    }

    private static String adapt(String message) {
        if (message == null) {
            return null;
        }
        if (Integer.parseInt(Bukkit.getVersion().split("\\.")[1].replaceAll("\\)", "")) >= 16) {
            Matcher matcher = hexPattern.matcher(message);
            while (matcher.find()) {
                try {
                    String matched = matcher.group(0);
                    String color = matcher.group(1);
                    message = message.replace(matched, String.valueOf(ChatColor.of(color)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    private static String setPlaceholderMessage(final Player player, final String message) {
        if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return PlaceholderAPI.setPlaceholders(player, message);
        }
        return message;
    }

    public static void print(String string, prints cause) {
        if (cause.equals(prints.ERROR)) {
            getLogger().severe(string);
        } else if (cause.equals(prints.WARNING)) {
            getLogger().warning(string);
        } else if (cause.equals(prints.SEVERE)) {
            getLogger().severe(string);
        } else{
            getLogger().info(string);
        }
    }
}
