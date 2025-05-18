package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.invites;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.invites.teaminvitessubcommands.TeamInvitesCloseCommand;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.invites.teaminvitessubcommands.TeamInvitesCheckCommand;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.invites.teaminvitessubcommands.TeamInvitesOpenCommand;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.TeamManageSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class TeamInvitesCommand extends TeamInvitesSubCommand {
    public List<TeamInvitesSubCommand> subCommands = new ArrayList<>();

    public TeamInvitesCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_INVITES_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
        subCommands.add(new TeamInvitesOpenCommand());
        subCommands.add(new TeamInvitesCloseCommand());
        subCommands.add(new TeamInvitesCheckCommand());
    }

    @Override
    public String getDescription() {
        return "Desc check nodewar team invites";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        Player player;
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_BY_PLAYER_ONLY));
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
