package fr.rosstail.nodewar.commands.subcommands.territory.territorysubcommands.edit;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.commands.subcommands.territory.TerritorySubCommand;
import fr.rosstail.nodewar.commands.subcommands.territory.territorysubcommands.edit.territoryeditsubcommands.team.TerritoryEditTeamCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.territory.TerritoryManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TerritoryEditCommand extends TerritoryEditSubCommand {
    public List<TerritoryEditSubCommand> subCommands = new ArrayList<>();

    public TerritoryEditCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TERRITORY_EDIT_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
        subCommands.add(new TerritoryEditTeamCommand());
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (args.length < 3) {
            sender.sendMessage("Select a territory first");
            return;
        } else if (!TerritoryManager.getTerritoryManager().getTerritoryMap().containsKey(args[2])) {
            sender.sendMessage("The territory " + args[2] + " does not exist");
            return;
        }

        if (args.length < 4) {
            sender.sendMessage(getSubCommandHelp());
            return;
        }

        List<String> subCommandsStringList = new ArrayList<>();
        for (TerritorySubCommand subCommand : subCommands) {
            subCommandsStringList.add(subCommand.getName());
        }

        if (!subCommandsStringList.contains(args[3])) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_COMMAND)));
            return;
        }

        for (TerritorySubCommand subCommand : subCommands) {
            if (subCommand.getName().equalsIgnoreCase(args[3])) {
                subCommand.perform(sender, args, arguments);
            }
        }

    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        if (args.length <= 3) {
            return TerritoryManager.getTerritoryManager().getTerritoryMap().keySet().stream()
                    .filter(s -> (args[2].isEmpty() || s.startsWith(args[2]))).collect(Collectors.toList());
        } else if (args.length == 4) {
            List<String> list = new ArrayList<>();
            for (SubCommand subCommand : subCommands) {
                list.add(subCommand.getName());
            }
            return list;
        } else {
            for (SubCommand subCommand : subCommands) {
                if (subCommand.getName().equalsIgnoreCase(args[3])) {
                    return subCommand.getSubCommandsArguments(sender, args, arguments);
                }
            }
        }
        return null;
    }

    @Override
    public String getSubCommandHelp() {
        StringBuilder subCommandHelp = new StringBuilder(super.getSubCommandHelp());
        for (SubCommand subCommand : subCommands) {
            if (subCommand.getHelp() != null) {
                subCommandHelp.append("\n").append(subCommand.getHelp());
            }
        }
        return subCommandHelp.toString();
    }
}
