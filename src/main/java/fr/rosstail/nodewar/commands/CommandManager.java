package fr.rosstail.nodewar.commands;

import fr.rosstail.nodewar.commands.subcommands.AdminCommand;
import fr.rosstail.nodewar.commands.subcommands.EmpireCommand;
import fr.rosstail.nodewar.commands.subcommands.HelpCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final ArrayList<SubCommand> subCommands = new ArrayList<>();

    public CommandManager() {
        subCommands.add(new AdminCommand());
        subCommands.add(new EmpireCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            HelpCommand help = new HelpCommand(this);
            help.perform(sender, args);
        } else {
            for (int index = 0; index < getSubCommands().size(); index++) {
                if (args[0].equalsIgnoreCase(getSubCommands().get(index).getName())) {
                    getSubCommands().get(index).perform(sender, args);
                    return true;
                }
            }
        }

        return true;
    }

    public ArrayList<SubCommand> getSubCommands() {
        return subCommands;
    }

    public List<String> onTabComplete(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length <= 1) {
            ArrayList<String> subCommandArguments = new ArrayList<>();

            for (int i = 0; i < getSubCommands().size(); i++) {
                subCommandArguments.add(getSubCommands().get(i).getName());
            }

            return subCommandArguments;
        } else {
            for (SubCommand subCommand : getSubCommands()) {
                if (subCommand.getName().equalsIgnoreCase(args[0])) {
                    return subCommand.getSubCommandsArguments((Player) sender, args);
                }
            }
        }

        return null;
    }
}
