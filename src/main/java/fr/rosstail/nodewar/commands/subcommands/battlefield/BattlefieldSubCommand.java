package fr.rosstail.nodewar.commands.subcommands.battlefield;

import fr.rosstail.nodewar.commands.SubCommand;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class BattlefieldSubCommand extends SubCommand {
    @Override
    public String getName() {
        return "battlefield";
    }

    @Override
    public String getDescription() {
        return "Battlefield command description";
    }

    @Override
    public String getSyntax() {
        return "nodewar battlefield <subcommand>";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.battlefield";
    }

    @Override
    public abstract List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments);
}
