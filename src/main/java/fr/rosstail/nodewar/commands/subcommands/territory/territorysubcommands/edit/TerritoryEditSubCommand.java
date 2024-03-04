package fr.rosstail.nodewar.commands.subcommands.territory.territorysubcommands.edit;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.territory.TerritorySubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TerritoryEditSubCommand extends TerritorySubCommand {
    @Override
    public String getName() {
        return "edit";
    }

    @Override
    public String getDescription() {
        return "Edit territory as admin";
    }

    @Override
    public String getSyntax() {
        return "nodewar territory edit <territory>";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.territory.edit";
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
