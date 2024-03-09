package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.member;

import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.TeamManageSubCommand;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class TeamManageMemberSubCommand extends TeamManageSubCommand {
    @Override
    public String getName() {
        return "member";
    }

    @Override
    public String getDescription() {
        return "Team manage command description";
    }

    @Override
    public String getSyntax() {
        return "nodewar team manage member";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.manage.member";
    }

    @Override
    public abstract List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments);
}
