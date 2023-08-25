package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.member;

import fr.rosstail.nodewar.commands.subcommands.admin.team.AdminTeamSubCommand;
import fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.AdminTeamEditSubCommand;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class AdminTeamEditMemberSubCommand extends AdminTeamEditSubCommand {
    @Override
    public String getName() {
        return "member";
    }

    @Override
    public String getDescription() {
        return "Team admin edit command description";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin team edit <team> member";
    }

    @Override
    public abstract List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments);
}
