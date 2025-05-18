package fr.rosstail.nodewar.commands.subcommands.admin.territory.adminterritorysubcommands.team;

import fr.rosstail.nodewar.commands.subcommands.admin.territory.AdminTerritorySubCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class AdminTerritoryTeamSubCommand extends AdminTerritorySubCommand {
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
        return "nodewar admin territory <territory> team";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        return List.of();
    }
}
