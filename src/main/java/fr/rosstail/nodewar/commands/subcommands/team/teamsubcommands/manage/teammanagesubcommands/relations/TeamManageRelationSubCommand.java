package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.relations;

import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.TeamManageSubCommand;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class TeamManageRelationSubCommand extends TeamManageSubCommand {
    @Override
    public String getName() {
        return "relation";
    }

    @Override
    public String getDescription() {
        return "Team manage command description";
    }

    @Override
    public String getSyntax() {
        return "nodewar team manage relation";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.manage.relation";
    }

    @Override
    public abstract List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments);
}
