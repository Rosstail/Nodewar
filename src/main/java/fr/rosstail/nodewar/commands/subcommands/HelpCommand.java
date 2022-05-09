package fr.rosstail.nodewar.commands.subcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HelpCommand extends SubCommand {


    public HelpCommand(final CommandManager manager) {
        subCommands = manager.getSubCommands();
    }

    public HelpCommand(final SubCommand subCommand) {
        subCommands = subCommand.getSubCommands();
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Displays a list of commands";
    }

    @Override
    public String getSyntax() {
        return "nodewar help";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.help";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage("You don't have permission " + getPermission());
            return;
        }
        StringBuilder helpCommand = new StringBuilder("=====" + getName().toUpperCase() + "=====");
        for (SubCommand subCommand : subCommands) {
                helpCommand.append("\n").append(" > /").append(subCommand.getSyntax()).append(": ").append(subCommand.getDescription());
        }
        sender.sendMessage(helpCommand.toString());
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        return null;
    }
}
