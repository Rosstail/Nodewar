package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.relation;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.commands.subcommands.admin.team.AdminTeamSubCommand;
import fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.relation.adminteamrelationsubcommands.*;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminTeamEditRelationCommand extends AdminTeamEditRelationSubCommand {
    public List<AdminTeamEditRelationSubCommand> subCommands = new ArrayList<>();
    public AdminTeamEditRelationCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_RELATION_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
        subCommands.add(new AdminTeamEditRelationCloseCommand());
        subCommands.add(new AdminTeamEditRelationOpenCommand());
        subCommands.add(new AdminTeamEditRelationInvitesCommand());
        subCommands.add(new AdminTeamEditRelationSetCommand());
        subCommands.add(new AdminTeamEditRelationRequestCommand());
    }

    @Override
    public String getDescription() {
        return "Desc relation nodewar team";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin team edit <team> relation <subcommand>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (args.length < 6) {
            sender.sendMessage(getSubCommandHelp());
            return;
        }

        SubCommand subCommand = subCommands.stream()
                .filter(teamSubCommand -> teamSubCommand.getName().equalsIgnoreCase(args[5]))
                .findFirst().orElse(null);

        if (subCommand == null) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_COMMAND)));
            return;
        }

        subCommand.perform(sender, args, arguments);
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        if (args.length <= 6) {
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
