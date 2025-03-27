package fr.rosstail.nodewar.commands.subcommands.admin;

import fr.rosstail.nodewar.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class AdminSubCommand extends SubCommand {
    @Override
    public String getName() {
        return "admin";
    }

    @Override
    public String getDescription() {
        return "Admin command description";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin <subcommand>";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.admin";
    }

    @Override
    public abstract List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments);
}
