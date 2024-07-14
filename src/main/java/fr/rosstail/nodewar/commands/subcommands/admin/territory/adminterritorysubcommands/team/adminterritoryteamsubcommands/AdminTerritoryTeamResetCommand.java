package fr.rosstail.nodewar.commands.subcommands.admin.territory.adminterritorysubcommands.team.adminterritoryteamsubcommands;

import fr.rosstail.nodewar.commands.subcommands.admin.territory.adminterritorysubcommands.team.AdminTerritoryTeamSubCommand;
import fr.rosstail.nodewar.events.territoryevents.TerritoryOwnerNeutralizeEvent;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class AdminTerritoryTeamResetCommand extends AdminTerritoryTeamSubCommand {

    public AdminTerritoryTeamResetCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TERRITORY_TEAM_RESET_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));

    }

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public String getDescription() {
        return "Reset territory owner";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin territory <territory> team reset";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        Territory territory;
        String message = LangManager.getMessage(LangMessage.COMMANDS_TERRITORY_TEAM_RESET_RESULT);

        territory = TerritoryManager.getTerritoryManager().getTerritoryMap().get(args[2]);

        TerritoryOwnerNeutralizeEvent event = new TerritoryOwnerNeutralizeEvent(territory, null);
        Bukkit.getPluginManager().callEvent(event);

        message = AdaptMessage.getAdaptMessage().adaptTerritoryMessage(message, territory);

        sender.sendMessage(message);
    }

}
