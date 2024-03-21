package fr.rosstail.nodewar.commands.subcommands.admin.territory.adminterritorysubcommands;

import fr.rosstail.nodewar.commands.subcommands.admin.territory.AdminTerritorySubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import org.bukkit.command.CommandSender;

public class AdminTerritoryVulnerableCommand extends AdminTerritorySubCommand {

    public AdminTerritoryVulnerableCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TERRITORY_VULNERABLE_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));

    }

    @Override
    public String getName() {
        return "vulnerable";
    }

    @Override
    public String getDescription() {
        return "Make territory sensible to attacks";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin territory <territory> vulnerable";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        Territory territory;
        String message = LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TERRITORY_VULNERABLE_RESULT);

        territory = TerritoryManager.getTerritoryManager().getTerritoryMap().get(args[2]);
        territory.getModel().setUnderProtection(false);

        message = AdaptMessage.getAdaptMessage().adaptTerritoryMessage(message, territory);
        sender.sendMessage(message);
    }

}
