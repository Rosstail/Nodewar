package fr.rosstail.conquest.character.commandhandlers;

import fr.rosstail.conquest.character.datahandlers.PlayerInfo;
import fr.rosstail.conquest.character.empires.Empire;
import fr.rosstail.conquest.character.guis.playerguis.EmpiresListGUI;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import fr.rosstail.conquest.required.lang.LangManager;
import fr.rosstail.conquest.required.lang.LangMessage;
import fr.rosstail.conquest.character.commandhandlers.enums.Commands;
import org.bukkit.command.CommandSender;
import fr.rosstail.conquest.Conquest;

public class EmpireCommands
{
    private final Conquest plugin;
    
    public EmpireCommands(final Conquest plugin) {
        this.plugin = plugin;
    }
    
    public void empireCommands(final CommandSender sender, final String command, final String[] args) {
        if (!ConquestCommands.isSenderPlayer(sender) || sender.hasPermission(Commands.COMMAND_EMPIRE.getPermission())) {
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
            ConquestCommands.doesNotHavePermission(sender, Commands.COMMAND_EMPIRE.getPermission());
        }
    }
    
    private void listCommand(final CommandSender sender) {
        if (ConquestCommands.isSenderPlayer(sender)) {
            final Player player = (Player)sender;
            if (player.hasPermission(Commands.COMMAND_EMPIRE_LIST.getPermission())) {
                EmpiresListGUI.initGUI(player, this.plugin);
            }
            else {
                ConquestCommands.doesNotHavePermission(player, Commands.COMMAND_EMPIRE_LIST.getPermission());
            }
        }
        else {
            ConquestCommands.playerOnly(sender);
        }
    }
    
    private void joinCommand(final CommandSender sender, final String[] args) {
        if (args.length > 2) {
            if (Empire.getEmpires().containsKey(args[2])) {
                if (ConquestCommands.isSenderPlayer(sender)) {
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
                        ConquestCommands.doesNotHavePermission(player, Commands.COMMAND_EMPIRE_JOIN.getPermission());
                    }
                }
                else {
                    ConquestCommands.playerOnly(sender);
                }
            }
        }
        else {
            sender.sendMessage("Display fe class join help");
        }
    }
    
    private void leaveCommand(final CommandSender sender) {
        if (ConquestCommands.isSenderPlayer(sender)) {
            PlayerInfo.gets((Player)sender).leaveEmpire();
        }
        else {
            ConquestCommands.playerOnly(sender);
        }
    }
}
