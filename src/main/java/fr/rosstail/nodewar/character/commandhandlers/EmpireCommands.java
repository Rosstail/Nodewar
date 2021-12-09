package fr.rosstail.nodewar.character.commandhandlers;

import fr.rosstail.nodewar.character.datahandlers.PlayerInfo;
import fr.rosstail.nodewar.character.empires.Empire;
import fr.rosstail.nodewar.character.guis.playerguis.EmpiresListGUI;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import fr.rosstail.nodewar.required.lang.LangManager;
import fr.rosstail.nodewar.required.lang.LangMessage;
import fr.rosstail.nodewar.character.commandhandlers.enums.Commands;
import org.bukkit.command.CommandSender;

public class EmpireCommands
{
    private final fr.rosstail.nodewar.Nodewar plugin;
    
    public EmpireCommands(final fr.rosstail.nodewar.Nodewar plugin) {
        this.plugin = plugin;
    }
    
    public void empireCommands(final CommandSender sender, final String command, final String[] args) {
        if (!NodewarCommands.isSenderPlayer(sender) || sender.hasPermission(Commands.COMMAND_EMPIRE.getPermission())) {
            if (command.startsWith(Commands.COMMAND_EMPIRE_LIST.getCommand())) {
                this.listCommand(sender);
            }
            else if (command.startsWith(Commands.COMMAND_EMPIRE_JOIN.getCommand())) {
                this.joinCommand(sender, args);
            }
            else if (command.startsWith(Commands.COMMAND_EMPIRE_LEAVE.getCommand())) {
                this.leaveCommand(sender);
            }
            else {
                LangManager.getListMessage(LangMessage.EMPIRE_HELP).forEach(s -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
            }
        }
        else {
            NodewarCommands.doesNotHavePermission(sender, Commands.COMMAND_EMPIRE.getPermission());
        }
    }
    
    private void listCommand(final CommandSender sender) {
        if (NodewarCommands.isSenderPlayer(sender)) {
            final Player player = (Player)sender;
            if (player.hasPermission(Commands.COMMAND_EMPIRE_LIST.getPermission())) {
                EmpiresListGUI.initGUI(player, this.plugin);
            }
            else {
                NodewarCommands.doesNotHavePermission(player, Commands.COMMAND_EMPIRE_LIST.getPermission());
            }
        }
        else {
            NodewarCommands.playerOnly(sender);
        }
    }
    
    private void joinCommand(final CommandSender sender, final String[] args) {
        if (args.length > 2) {
            if (Empire.getEmpires().containsKey(args[2])) {
                if (NodewarCommands.isSenderPlayer(sender)) {
                    final Player player = (Player)sender;
                    if (player.hasPermission(Commands.COMMAND_EMPIRE_JOIN.getPermission())) {
                        final PlayerInfo playerInfo = PlayerInfo.gets(player);
                        final Empire playerEmpire = playerInfo.getEmpire();
                        final Empire empire = Empire.getEmpires().get(args[2]);
                        if (playerInfo.tryJoinEmpire(empire)) {
                            player.sendMessage("You successfully joined the " + empire.getDisplay() + " empire.");
                        }
                        else if (empire == null) {
                            player.sendMessage("The empire " + args[2] + " doesn't exists");
                        }
                        else if (playerEmpire != null) {
                            player.sendMessage("You already are in the " + playerEmpire.getDisplay() + " empire.");
                        }
                    }
                    else {
                        NodewarCommands.doesNotHavePermission(player, Commands.COMMAND_EMPIRE_JOIN.getPermission());
                    }
                }
                else {
                    NodewarCommands.playerOnly(sender);
                }
            }
        }
        else {
            sender.sendMessage("Display fe class join help");
        }
    }
    
    private void leaveCommand(final CommandSender sender) {
        if (NodewarCommands.isSenderPlayer(sender)) {
            PlayerInfo.gets((Player)sender).leaveEmpire();
        }
        else {
            NodewarCommands.playerOnly(sender);
        }
    }
}
