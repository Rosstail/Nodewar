package fr.rosstail.nodewar.commands.subcommands.admin.territory.adminterritorysubcommands.team;

import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.commands.subcommands.admin.territory.AdminTerritorySubCommand;
import fr.rosstail.nodewar.commands.subcommands.admin.territory.adminterritorysubcommands.team.adminterritoryteamsubcommands.AdminTerritoryTeamResetCommand;
import fr.rosstail.nodewar.commands.subcommands.admin.territory.adminterritorysubcommands.team.adminterritoryteamsubcommands.AdminTerritoryTeamSetCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminTerritoryTeamCommand extends AdminTerritoryTeamSubCommand {
    public List<AdminTerritorySubCommand> subCommands = new ArrayList<>();

    public AdminTerritoryTeamCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TERRITORY_TEAM_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
        subCommands.add(new AdminTerritoryTeamSetCommand());
        subCommands.add(new AdminTerritoryTeamResetCommand());
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (args.length < 5) {
            sender.sendMessage(getSubCommandHelp());
            return;
        }

        List<String> subCommandsStringList = new ArrayList<>();
        for (AdminTerritorySubCommand subCommand : subCommands) {
            subCommandsStringList.add(subCommand.getName());
        }

        if (!subCommandsStringList.contains(args[4])) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_COMMAND)));
            return;
        }

        for (AdminTerritorySubCommand subCommand : subCommands) {
            if (subCommand.getName().equalsIgnoreCase(args[4])) {
                subCommand.perform(sender, args, arguments);
            }
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        if (args.length <= 5) {
            List<String> list = new ArrayList<>();
            for (SubCommand subCommand : subCommands) {
                list.add(subCommand.getName());
            }
            return list;
        } else {
            for (SubCommand subCommand : subCommands) {
                if (subCommand.getName().equalsIgnoreCase(args[4])) {
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
