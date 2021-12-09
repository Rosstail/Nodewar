package fr.rosstail.nodewar.character.commandhandlers;

import fr.rosstail.nodewar.character.guis.adminguis.nodewarguis.WorldsGUIs;
import fr.rosstail.nodewar.character.guis.adminguis.playerGUIs.PlayerAdminGUI;
import org.bukkit.entity.Player;
import fr.rosstail.nodewar.required.lang.AdaptMessage;
import fr.rosstail.nodewar.character.datahandlers.PlayerInfo;
import fr.rosstail.nodewar.character.empires.Empire;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import fr.rosstail.nodewar.required.lang.LangManager;
import fr.rosstail.nodewar.required.lang.LangMessage;
import fr.rosstail.nodewar.character.commandhandlers.enums.Commands;
import org.bukkit.command.CommandSender;

public class AdminCommands
{
    private final fr.rosstail.nodewar.Nodewar plugin;
    
    AdminCommands(final fr.rosstail.nodewar.Nodewar plugin) {
        this.plugin = plugin;
    }
    
    public void adminCommands(final CommandSender sender, final String command, final String[] args) {
        if (command.startsWith(Commands.COMMAND_ADMIN_PLAYER.getCommand())) {
            this.playerGUICommand(sender, args);
        }
        else if (command.startsWith(Commands.COMMAND_ADMIN_NODEWAR.getCommand())) {
            this.nodewarGUICommand(sender, args);
        }
        else if (command.startsWith(Commands.COMMAND_ADMIN_EMPIRE_PLAYER_SET.getCommand())) {
            this.setEmpireCommand(sender, args);
        }
        else if (command.startsWith(Commands.COMMAND_ADMIN_EMPIRE_PLAYER_REMOVE.getCommand())) {
            this.removeEmpireCommand(sender, args);
        }
        else {
            LangManager.getListMessage(LangMessage.ADMIN_HELP).forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
        }
    }
    
    private void setEmpireCommand(final CommandSender sender, final String[] args) {
        if (sender.hasPermission(Commands.COMMAND_ADMIN_EMPIRE.getPermission())) {
            if (args.length == 5) {
                final Player target = Bukkit.getServer().getPlayerExact(args[3]);
                final Empire empire = Empire.getEmpires().get(args[4]);
                if (target != null) {
                    if (empire != null) {
                        PlayerInfo.gets(target).setEmpire(empire);
                        sender.sendMessage("Set " + empire.getDisplay() + " empire to " + target.getName());
                    }
                    else {
                        AdaptMessage.playerMessage(target, LangManager.getMessage(LangMessage.EMPIRE_DOES_NOT_EXIST));
                    }
                }
                else {
                    NodewarCommands.discPlayer(sender);
                }
            }
            else {
                NodewarCommands.tooFewArguments(sender);
            }
        }
        else {
            NodewarCommands.doesNotHavePermission(sender, Commands.COMMAND_ADMIN_EMPIRE.getPermission());
        }
    }
    
    private void removeEmpireCommand(final CommandSender sender, final String[] args) {
        if (sender.hasPermission(Commands.COMMAND_ADMIN_EMPIRE.getPermission())) {
            if (args.length == 4) {
                final Player target = Bukkit.getServer().getPlayerExact(args[3]);
                if (target != null) {
                    PlayerInfo.gets(target).setEmpire(null);
                    sender.sendMessage("Removed empire for " + target.getName());
                }
                else {
                    NodewarCommands.discPlayer(sender);
                }
            }
            else {
                NodewarCommands.tooFewArguments(sender);
            }
        }
        else {
            NodewarCommands.doesNotHavePermission(sender, Commands.COMMAND_ADMIN_EMPIRE.getPermission());
        }
    }
    
    private void playerGUICommand(final CommandSender sender, final String[] args) {
        if (NodewarCommands.isSenderPlayer(sender)) {
            final Player playerSender = (Player)sender;
            if (playerSender.hasPermission(Commands.COMMAND_ADMIN_PLAYER.getPermission())) {
                if (args.length == 3) {
                    final Player target = Bukkit.getServer().getPlayerExact(args[2]);
                    if (target != null) {
                        PlayerAdminGUI.initGUI(playerSender, this.plugin, target, null);
                    }
                    else {
                        NodewarCommands.discPlayer(sender);
                    }
                }
                else {
                    NodewarCommands.tooFewArguments(sender);
                }
            }
            else {
                NodewarCommands.doesNotHavePermission(playerSender, Commands.COMMAND_ADMIN_PLAYER.getPermission());
            }
        }
        else {
            NodewarCommands.playerOnly(sender);
        }
    }
    
    private void nodewarGUICommand(final CommandSender sender, final String[] args) {
        if (NodewarCommands.isSenderPlayer(sender)) {
            final Player player = (Player)sender;
            if (player.hasPermission(Commands.COMMAND_ADMIN_NODEWAR.getPermission())) {
                WorldsGUIs.initGUI(player, this.plugin);
            }
            else {
                NodewarCommands.doesNotHavePermission(player, Commands.COMMAND_ADMIN_NODEWAR.getPermission());
            }
        }
        else {
            NodewarCommands.playerOnly(sender);
        }
    }
}
