package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.relation;

import fr.rosstail.nodewar.commands.subcommands.admin.team.AdminTeamSubCommand;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class AdminTeamRelationSubCommand extends AdminTeamSubCommand {
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
        return "nodewar admin team relation";
    }

    @Override
    public abstract List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments);
}
