package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.commands.subcommands.admin.team.AdminTeamSubCommand;
import fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.*;
import fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.member.AdminTeamEditMemberCommand;
import fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.relation.AdminTeamEditRelationCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.TeamDataManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminTeamEditCommand extends AdminTeamEditSubCommand {
    public List<AdminTeamEditSubCommand> subCommands = new ArrayList<>();
    public AdminTeamEditCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
        subCommands.add(new AdminTeamEditDisbandCommand());
        subCommands.add(new AdminTeamEditOpenCommand());
        subCommands.add(new AdminTeamEditCloseCommand());
        subCommands.add(new AdminTeamEditInviteCommand());
        subCommands.add(new AdminTeamEditMemberCommand());
        subCommands.add(new AdminTeamEditColorCommand());
        subCommands.add(new AdminTeamEditRelationCommand());
    }

    @Override
    public String getDescription() {
        return "Desc manage nodewar team";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin team edit <team> edit <subcommand>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String teamName;
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (args.length < 5) {
            sender.sendMessage("Help of team manage needed");
            sender.sendMessage(getSubCommandHelp());
            return;
        }

        teamName = args[3];
        if (!TeamDataManager.getTeamDataManager().getStringTeamMap().containsKey(teamName)) {
            sender.sendMessage("this team does not exists: " + teamName);
            return;
        }

        List<String> subCommandsStringList = new ArrayList<>();
        for (AdminTeamSubCommand subCommand : subCommands) {
            subCommandsStringList.add(subCommand.getName());
        }

        if (!subCommandsStringList.contains(args[4])) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_COMMAND)));
            return;
        }

        for (AdminTeamSubCommand subCommand : subCommands) {
            if (subCommand.getName().equalsIgnoreCase(args[4])) {
                subCommand.perform(sender, args, arguments);
            }
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        if (args.length <= 4) {
            return new ArrayList<>(TeamDataManager.getTeamDataManager().getStringTeamMap().keySet());
        } else if (args.length == 5) {
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
