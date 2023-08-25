package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands;

import fr.rosstail.nodewar.commands.subcommands.admin.team.AdminTeamSubCommand;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class AdminTeamEditSubCommand extends AdminTeamSubCommand {
    @Override
    public String getName() {
        return "edit";
    }

    @Override
    public String getDescription() {
        return "Team admin edit command description";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin team edit <team> <subcommand>";
    }

    @Override
    public abstract List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments);
}
