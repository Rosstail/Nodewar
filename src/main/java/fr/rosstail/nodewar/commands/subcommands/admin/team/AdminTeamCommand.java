package fr.rosstail.nodewar.commands.subcommands.admin.team;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.*;
import fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.member.AdminMemberTeamCommand;
import fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.relation.AdminRelationTeamCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.TeamDataManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminTeamCommand extends AdminTeamSubCommand {
    public List<AdminTeamSubCommand> subCommands = new ArrayList<>();
    public AdminTeamCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
        subCommands.add(new AdminTeamDisbandCommand());
        subCommands.add(new AdminRelationTeamCommand());
        subCommands.add(new AdminTeamOpenCommand());
        subCommands.add(new AdminTeamCloseCommand());
        subCommands.add(new AdminTeamInviteCommand());
        subCommands.add(new AdminMemberTeamCommand());
        subCommands.add(new AdminTeamColorCommand());
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("Help of team needed");
            sender.sendMessage(getSubCommandHelp());
            return;
        }

        List<String> subCommandsStringList = new ArrayList<>();
        for (AdminTeamSubCommand subCommand : subCommands) {
            subCommandsStringList.add(subCommand.getName());
        }

        if (!subCommandsStringList.contains(args[2])) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_COMMAND)));
            return;
        }

        for (AdminTeamSubCommand subCommand : subCommands) {
            if (subCommand.getName().equalsIgnoreCase(args[2])) {
                subCommand.perform(sender, args, arguments);
            }
        }

    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        if (args.length <= 3) {
            return new ArrayList<>(TeamDataManager.getTeamDataManager().getStringTeamMap().keySet());
        } else if (args.length == 4) {
            List<String> list = new ArrayList<>();
            for (SubCommand subCommand : subCommands) {
                list.add(subCommand.getName());
            }
            return list;
        } else {
            for (SubCommand subCommand : subCommands) {
                if (subCommand.getName().equalsIgnoreCase(args[2])) {
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
