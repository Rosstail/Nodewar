package fr.rosstail.nodewar.commands.subcommands.territory;

import fr.rosstail.nodewar.commands.SubCommand;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class TerritorySubCommand extends SubCommand {
    @Override
    public String getName() {
        return "territory";
    }

    @Override
    public String getDescription() {
        return "Territory command description";
    }

    @Override
    public String getSyntax() {
        return "nodewar territory <subcommand>";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.territory";
    }

    @Override
    public abstract List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments);
}
