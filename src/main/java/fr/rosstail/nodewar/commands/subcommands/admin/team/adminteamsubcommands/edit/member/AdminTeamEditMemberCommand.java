package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.member;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.member.adminteameditmembersubcommands.*;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminTeamEditMemberCommand extends AdminTeamEditMemberSubCommand {
    public List<AdminTeamEditMemberSubCommand> subCommands = new ArrayList<>();

    public AdminTeamEditMemberCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_MEMBER_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
        subCommands.add(new AdminTeamEditMemberAddCommand());
        subCommands.add(new AdminTeamEditMemberPromoteCommand());
        subCommands.add(new AdminTeamEditMemberDemoteCommand());
        subCommands.add(new AdminTeamEditMemberRemoveCommand());
        subCommands.add(new AdminTeamEditMemberTransferCommand());
    }

    @Override
    public String getDescription() {
        return "Desc relation nodewar team";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin team edit <team> member <subcommand>";
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

        List<String> subCommandsStringList = new ArrayList<>();
        for (AdminTeamEditMemberSubCommand subCommand : subCommands) {
            subCommandsStringList.add(subCommand.getName());
        }

        if (!subCommandsStringList.contains(args[5])) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_COMMAND)));
            return;
        }

        subCommands.stream()
                .filter(subCommand -> subCommand.getName().equalsIgnoreCase(args[5]))
                .findFirst().ifPresent(subCommand -> subCommand.perform(sender, args, arguments));
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        if (args.length <= 6) {
            List<String> list = new ArrayList<>();
            for (SubCommand subCommand : subCommands) {
                list.add(subCommand.getName());
            }
            return list;
        } else {
            for (SubCommand subCommand : subCommands) {
                if (subCommand.getName().equalsIgnoreCase(args[5])) {
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
