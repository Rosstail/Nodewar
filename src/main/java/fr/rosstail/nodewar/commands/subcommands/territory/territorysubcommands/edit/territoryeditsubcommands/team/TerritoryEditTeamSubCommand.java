package fr.rosstail.nodewar.commands.subcommands.territory.territorysubcommands.edit.territoryeditsubcommands.team;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.territory.territorysubcommands.edit.TerritoryEditSubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TerritoryEditTeamSubCommand extends TerritoryEditSubCommand {
    @Override
    public String getName() {
        return "team";
    }

    @Override
    public String getDescription() {
        return "Edit territory owner";
    }

    @Override
    public String getSyntax() {
        return "nodewar territory edit <territory> team";
    }

}
