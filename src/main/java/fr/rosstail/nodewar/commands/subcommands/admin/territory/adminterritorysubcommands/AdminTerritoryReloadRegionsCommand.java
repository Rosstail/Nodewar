package fr.rosstail.nodewar.commands.subcommands.admin.territory.adminterritorysubcommands;

import fr.rosstail.nodewar.commands.subcommands.admin.territory.AdminTerritorySubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import org.bukkit.command.CommandSender;

public class AdminTerritoryReloadRegionsCommand extends AdminTerritorySubCommand {

    public AdminTerritoryReloadRegionsCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TERRITORY_RELOAD_REGIONS_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));

    }

    @Override
    public String getName() {
        return "reloadregions";
    }

    @Override
    public String getDescription() {
        return "Reload regions of territory";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin territory edit <territory> reloadregions";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        Territory territory;
        String message = LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TERRITORY_RELOAD_REGIONS_RESULT);

        territory = TerritoryManager.getTerritoryManager().getTerritoryMap().get(args[2]);
        territory.updateRegionList();

        message = AdaptMessage.getAdaptMessage().adaptTerritoryMessage(message, territory);
        sender.sendMessage(message);
    }

}
