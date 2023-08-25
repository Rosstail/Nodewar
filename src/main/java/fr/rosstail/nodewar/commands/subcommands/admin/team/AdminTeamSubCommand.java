package fr.rosstail.nodewar.commands.subcommands.admin.team;

import fr.rosstail.nodewar.commands.subcommands.admin.AdminSubCommand;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class AdminTeamSubCommand extends AdminSubCommand {
    @Override
    public String getName() {
        return "team";
    }

    @Override
    public String getDescription() {
        return "Admin team command";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin team <subcommand>";
    }

    @Override
    public abstract List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments);
}
