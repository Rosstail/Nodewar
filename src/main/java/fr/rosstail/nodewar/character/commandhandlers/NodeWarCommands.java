package fr.rosstail.nodewar.character.commandhandlers;

import fr.rosstail.nodewar.character.commandhandlers.enums.Commands;
import fr.rosstail.nodewar.character.empires.Empire;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.required.lang.AdaptMessage;
import fr.rosstail.nodewar.required.lang.LangManager;
import fr.rosstail.nodewar.required.lang.LangMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class ConquestCommands implements CommandExecutor, TabExecutor
{
    private final EmpireCommands empireCommands;
    private final AdminCommands adminCommands;

    public ConquestCommands(final Nodewar plugin) {
        this.empireCommands = new EmpireCommands(plugin);
        this.adminCommands = new AdminCommands(plugin);
    }
    
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!isSenderPlayer(sender) || sender.hasPermission(Commands.COMMAND_FE.getPermission())) {
            final String string = String.join(" ", args);
            if (string.startsWith(Commands.COMMAND_EMPIRE.getCommand())) {
                this.empireCommands.empireCommands(sender, string, args);
            }
            else if (string.startsWith(Commands.COMMAND_ADMIN.getCommand())) {
                this.adminCommands.adminCommands(sender, string, args);
            }
            else {
                LangManager.getListMessage(LangMessage.HELP).forEach(s -> sender.sendMessage(AdaptMessage.playerMessage(null, s)));
            }
        }
        else {
            doesNotHavePermission(sender, Commands.COMMAND_FE.getPermission());
        }
        return true;
    }
    
    public List<String> onTabComplete(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        final List<String> completions = new ArrayList<String>();
        final List<String> commands = new ArrayList<String>();
        final String string = String.join(" ", args);
        if (args.length <= 1) {
            commands.add("admin");
            commands.add("empire");
            commands.add("help");
            StringUtil.copyPartialMatches(args[0], commands, (Collection<String>)completions);
        }
        else if (args.length == 2) {
            if (string.startsWith(Commands.COMMAND_EMPIRE.getCommand())) {
                commands.add("join");
                commands.add("leave");
                commands.add("list");
            }
            else if (string.startsWith(Commands.COMMAND_ADMIN.getCommand())) {
                commands.add("empire");
                commands.add("conquest");
                commands.add("player");
            }
            StringUtil.copyPartialMatches(args[1], commands, completions);
        }
        else if (args.length == 3) {
            if (string.startsWith(Commands.COMMAND_EMPIRE_JOIN.getCommand())) {
                final ArrayList<String> empiresName = new ArrayList<String>();
                Empire.getEmpires().forEach((s, empire) -> empiresName.add(s));
                empiresName.sort(Comparator.comparing(String::toString));
                commands.addAll(empiresName);
            }
            else if (string.startsWith(Commands.COMMAND_ADMIN_EMPIRE.getCommand())) {
                commands.add("set");
                commands.add("remove");
            }
            else if (string.startsWith(Commands.COMMAND_ADMIN_PLAYER.getCommand())) {
                final ArrayList<String> playersName = new ArrayList<String>();
                Bukkit.getOnlinePlayers().forEach(player -> playersName.add(player.getName()));
                playersName.sort(Comparator.comparing(String::toString));
                commands.addAll(playersName);
            }
            StringUtil.copyPartialMatches(args[2], commands, completions);
        }
        else if (args.length == 4) {
            if (string.startsWith(Commands.COMMAND_ADMIN_EMPIRE_PLAYER_SET.getCommand()) || string.startsWith(Commands.COMMAND_ADMIN_EMPIRE_PLAYER_REMOVE.getCommand())) {
                final ArrayList<String> playersName = new ArrayList<String>();
                Bukkit.getOnlinePlayers().forEach(player -> playersName.add(player.getName()));
                playersName.sort(Comparator.comparing(String::toString));
                commands.addAll(playersName);
            }
            StringUtil.copyPartialMatches(args[3], commands, (Collection<String>)completions);
        }
        else if (args.length == 5) {
            if (string.startsWith(Commands.COMMAND_ADMIN_EMPIRE_PLAYER_SET.getCommand())) {
                final ArrayList<String> empiresName = new ArrayList<String>();
                Empire.getEmpires().forEach((s, empire) -> empiresName.add(s));
                empiresName.sort(Comparator.comparing(String::toString));
                commands.addAll(empiresName);
            }
            StringUtil.copyPartialMatches(args[4], commands, completions);
        }
        Collections.sort(completions);
        return completions;
    }
    
    public static boolean isSenderPlayer(final CommandSender sender) {
        return sender instanceof Player;
    }
    
    public static void discPlayer(final CommandSender sender) {
        sender.sendMessage(AdaptMessage.playerMessage(null, LangManager.getMessage(LangMessage.DISCONNECTED_PLAYER)));
    }
    
    public static void playerOnly(final CommandSender sender) {
        sender.sendMessage(AdaptMessage.playerMessage(null, LangManager.getMessage(LangMessage.BY_PLAYER_ONLY)));
    }
    
    public static void wrongArg(final CommandSender sender) {
        sender.sendMessage(AdaptMessage.playerMessage(null, LangManager.getMessage(LangMessage.WRONG_VALUE)));
    }
    
    public static void tooFewArguments(final CommandSender sender) {
        sender.sendMessage(AdaptMessage.playerMessage(null, LangManager.getMessage(LangMessage.TOO_FEW_ARGUMENTS)));
    }
    
    public static void doesNotHavePermission(final CommandSender sender, final String permission) {
        sender.sendMessage("You don't have this permission : " + permission);
    }
}
