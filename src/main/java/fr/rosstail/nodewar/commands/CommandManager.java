package fr.rosstail.nodewar.commands;

import fr.rosstail.nodewar.commands.subcommands.HelpCommand;
import fr.rosstail.nodewar.commands.subcommands.admin.AdminCommand;
import fr.rosstail.nodewar.commands.subcommands.battlefield.BattlefieldCommand;
import fr.rosstail.nodewar.commands.subcommands.team.TeamCommand;
import fr.rosstail.nodewar.commands.subcommands.territory.TerritoryCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checking what method/class will be used on command, depending on command Sender and number of args.
 */
public class CommandManager implements CommandExecutor, TabExecutor {

    private final ArrayList<SubCommand> subCommands = new ArrayList<SubCommand>();
    private static final Pattern shortParamPattern = Pattern.compile("^-[A-Za-z]+"); //
    private static final Pattern longParamPattern = Pattern.compile("^--[A-Za-z]+");

    public CommandManager() {
        subCommands.add(new AdminCommand());
        subCommands.add(new BattlefieldCommand());
        subCommands.add(new TeamCommand());
        subCommands.add(new TerritoryCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            HelpCommand help = new HelpCommand(this);
            help.perform(sender, args, null);
            return true;
        }
        String[] arguments = getCommandArguments(args);
        args = removeFoundArgumentsFromCommand(args, arguments);

        @NotNull String[] finalArgs = args;
        subCommands.stream()
                .filter(subCommand -> subCommand.getName().equalsIgnoreCase(finalArgs[0]))
                .findFirst().ifPresent(subCommand -> subCommand.perform(sender, finalArgs, arguments));

        return true;
    }

    public ArrayList<SubCommand> getSubCommands() {
        return subCommands;
    }

    public List<String> onTabComplete(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        String[] arguments = getCommandArguments(args);
        String[] commandArgs = removeFoundArgumentsFromCommand(args, arguments);

        if (args.length <= 1) {
            return subCommands.stream().map(SubCommand::getName).toList();
        } else {
            SubCommand subCommand = subCommands.stream()
                    .filter(filterSubCommand -> filterSubCommand.getName().equalsIgnoreCase(args[5]))
                    .findFirst().orElse(null);

            if (subCommand == null) {
                return null;
            }
            return subCommand.getSubCommandsArguments(sender, args, arguments);
        }
    }

    public static boolean canLaunchCommand(CommandSender sender, SubCommand command) {
        if (!(sender instanceof Player) || sender.hasPermission(command.getPermission())) {
            return true;
        }
        permissionDenied(sender, command);
        return false;
    }

    private static void permissionDenied(CommandSender sender, SubCommand command) {
        AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
        String message = LangManager.getMessage(LangMessage.COMMANDS_PERMISSION_DENIED);
        message = adaptMessage.adaptPlayerMessage((Player) sender, message);
        message = adaptMessage.adaptMessage(message)
                .replaceAll("\\[command]", command.getName())
                .replaceAll("\\[permission]", command.getPermission());
        sender.sendMessage(message);
    }

    /**
     * @param sender
     */
    public static void disconnectedPlayer(CommandSender sender, String playerName) {
        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_EDIT_PLAYER_DISCONNECTED).replaceAll("\\[player]", playerName)));
    }

    public static void errorMessage(CommandSender sender, Exception e) {
        if (e instanceof ArrayIndexOutOfBoundsException) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_VALUE)));
        }
        if (e instanceof NumberFormatException) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_VALUE)));
            e.printStackTrace();
        }
    }


    private static String[] getCommandArguments(String[] commandArg) {
        List<String> foundArguments = new ArrayList<>();
        for (String s : commandArg) {
            Matcher shortMatcher = shortParamPattern.matcher(s);
            Matcher longMatcher = longParamPattern.matcher(s);

            if (shortMatcher.find() || longMatcher.find()) {
                foundArguments.add(s);
            }
        }

        return foundArguments.toArray(new String[0]);
    }

    private static String[] removeFoundArgumentsFromCommand(String[] command, String[] foundArguments) {
        List<String> commandList = Arrays.asList(command);
        List<String> foundArgumentList = Arrays.asList(foundArguments);

        List<String> difference = new ArrayList<>(commandList);
        difference.removeAll(foundArgumentList);

        return difference.toArray(new String[0]);
    }

    public static boolean doesCommandMatchParameter(String[] arguments, String shortParam, String longParam) {
        StringBuilder argumentString = new StringBuilder();
        for (String argument : arguments) {
            argumentString.append(argument).append(" ");
        }
        Matcher shortMatcher = shortParamPattern.matcher(argumentString);
        while (shortMatcher.find()) {
            if (shortMatcher.group().contains(shortParam)) {
                return true;
            }
        }

        Matcher longMatcher = longParamPattern.matcher(argumentString);
        while (longMatcher.find()) {
            if (longMatcher.group().contains(longParam)) {
                return true;
            }
        }

        return false;
    }
}
