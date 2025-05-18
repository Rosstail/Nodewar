package fr.rosstail.nodewar.commands.subcommands.admin.territory;

import fr.rosstail.nodewar.commands.subcommands.admin.AdminSubCommand;

public abstract class AdminTerritorySubCommand extends AdminSubCommand {
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
        return "nodewar admin territory <territory> <subcommand>";
    }
}
