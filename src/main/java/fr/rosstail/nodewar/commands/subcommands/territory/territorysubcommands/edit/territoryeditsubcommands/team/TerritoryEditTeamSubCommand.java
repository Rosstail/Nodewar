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
        return "nodewar territory edit team <subcommand>";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
