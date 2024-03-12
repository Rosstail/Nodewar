package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.member;

import fr.rosstail.nodewar.commands.subcommands.admin.team.AdminTeamSubCommand;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class AdminTeamMemberSubCommand extends AdminTeamSubCommand {
    @Override
    public String getName() {
        return "member";
    }

    @Override
    public String getDescription() {
        return "Team admin admin command description";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin team <team> member";
    }

    @Override
    public abstract List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments);
}
