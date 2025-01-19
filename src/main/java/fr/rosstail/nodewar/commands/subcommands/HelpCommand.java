package fr.rosstail.nodewar.commands.subcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HelpCommand extends SubCommand {

    public HelpCommand(final CommandManager manager) {
        subCommands = manager.getSubCommands();
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_HEADER)
                        .replaceAll("\\[syntax]", getSyntax())
                        .replaceAll("\\[permission]", getPermission()));
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
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        StringBuilder helpCommand = new StringBuilder(getHelp());
        for (SubCommand subCommand : subCommands) {
            if (subCommand.getHelp() != null) {
                helpCommand.append("\n").append(subCommand.getHelp());
            }
        }
        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(helpCommand.toString()));
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        return null;
    }
}
