package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.relation;

import fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.AdminTeamEditSubCommand;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class AdminTeamEditRelationSubCommand extends AdminTeamEditSubCommand {
    @Override
    public String getName() {
        return "relation";
    }

    @Override
    public String getDescription() {
        return "Team admin command description";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin team edit relation";
    }

    @Override
    public abstract List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments);
}
