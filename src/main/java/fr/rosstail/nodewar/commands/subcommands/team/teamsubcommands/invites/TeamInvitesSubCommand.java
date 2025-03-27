package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.invites;

import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class TeamInvitesSubCommand extends TeamSubCommand {
    @Override
    public String getName() {
        return "invites";
    }

    @Override
    public String getDescription() {
        return "Team invites command description";
    }

    @Override
    public String getSyntax() {
        return "nodewar team invites <subcommand>";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.invites";
    }

    @Override
    public abstract List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments);
}
