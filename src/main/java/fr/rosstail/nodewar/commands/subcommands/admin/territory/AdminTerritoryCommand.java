package fr.rosstail.nodewar.commands.subcommands.admin.territory;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.commands.subcommands.admin.territory.adminterritorysubcommands.AdminTerritoryReloadRegionsCommand;
import fr.rosstail.nodewar.commands.subcommands.admin.territory.adminterritorysubcommands.team.AdminTerritoryTeamCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminTerritoryCommand extends AdminTerritorySubCommand {
    public List<AdminTerritorySubCommand> subCommands = new ArrayList<>();
    public AdminTerritoryCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TERRITORY_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
        subCommands.add(new AdminTerritoryReloadRegionsCommand());
        subCommands.add(new AdminTerritoryTeamCommand());
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(getSubCommandHelp());
            return;
        }

        List<String> subCommandsStringList = new ArrayList<>();
        for (AdminTerritorySubCommand subCommand : subCommands) {
            subCommandsStringList.add(subCommand.getName());
        }

        if (!subCommandsStringList.contains(args[2])) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_COMMAND)));
            return;
        }

        for (AdminTerritorySubCommand subCommand : subCommands) {
            if (subCommand.getName().equalsIgnoreCase(args[2])) {
                subCommand.perform(sender, args, arguments);
            }
        }

    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        if (args.length <= 3) {
            List<String> list = new ArrayList<>();
            for (SubCommand subCommand : subCommands) {
                list.add(subCommand.getName());
            }
            return list;
        } else {
            for (SubCommand subCommand : subCommands) {
                if (subCommand.getName().equalsIgnoreCase(args[1])) {
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
