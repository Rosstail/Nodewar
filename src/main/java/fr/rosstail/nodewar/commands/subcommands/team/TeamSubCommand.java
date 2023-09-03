package fr.rosstail.nodewar.commands.subcommands.team;

import fr.rosstail.nodewar.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class TeamSubCommand extends SubCommand {
    @Override
    public String getName() {
        return "team";
    }

    @Override
    public String getDescription() {
        return "Team command description";
    }

    @Override
    public String getSyntax() {
        return "nodewar team <subcommand>";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {

    }

    @Override
    public abstract List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments);
}
