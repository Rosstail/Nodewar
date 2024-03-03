package fr.rosstail.nodewar.commands.subcommands.territory;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TerritorySubCommand extends SubCommand {
    @Override
    public String getName() {
        return "territory";
    }

    @Override
    public String getDescription() {
        return "Territory commands";
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
