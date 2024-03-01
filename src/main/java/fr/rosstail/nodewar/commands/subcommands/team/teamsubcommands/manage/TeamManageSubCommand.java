package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage;

import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class TeamManageSubCommand extends TeamSubCommand {
    @Override
    public String getName() {
        return "manage";
    }

    @Override
    public String getDescription() {
        return "Team manage command description";
    }

    @Override
    public String getSyntax() {
        return "nodewar team manage <subcommand>";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.manage";
    }

    @Override
    public abstract List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments);
}
