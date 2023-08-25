package fr.rosstail.nodewar.commands.subcommands.admin.territory.adminterritorysubcommands.team;

import fr.rosstail.nodewar.commands.subcommands.admin.territory.AdminTerritorySubCommand;

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

}
