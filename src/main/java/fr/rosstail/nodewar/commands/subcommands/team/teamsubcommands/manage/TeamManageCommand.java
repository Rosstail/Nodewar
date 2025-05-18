package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.*;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.member.TeamManageMemberCommand;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.member.teammanagermembersubcommands.TeamManageMemberKickCommand;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.relation.TeamManageRelationCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TeamManageCommand extends TeamManageSubCommand {
    public List<TeamManageSubCommand> subCommands = new ArrayList<>();
    public TeamManageCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
        subCommands.add(new TeamManageDisbandCommand());
        subCommands.add(new TeamManageRelationCommand());
        subCommands.add(new TeamManageOpenCommand());
        subCommands.add(new TeamManageCloseCommand());
        subCommands.add(new TeamManageInviteCommand());
        subCommands.add(new TeamManageMemberCommand());
        subCommands.add(new TeamManageColorCommand());
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

        SubCommand subCommand = subCommands.stream()
                .filter(teamSubCommand -> teamSubCommand.getName().equalsIgnoreCase(args[2]))
                .findFirst().orElse(null);

        if (subCommand == null) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_COMMAND)));
            return;
        }

        subCommand.perform(sender, args, arguments);
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        if (args.length <= 3) {
            return subCommands.stream().map(SubCommand::getName).toList();
        } else {
            SubCommand subCommand = subCommands.stream()
                    .filter(filterSubCommand -> filterSubCommand.getName().equalsIgnoreCase(args[2]))
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
