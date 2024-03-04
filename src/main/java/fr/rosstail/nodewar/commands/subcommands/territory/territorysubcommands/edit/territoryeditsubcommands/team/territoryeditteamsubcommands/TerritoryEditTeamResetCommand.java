package fr.rosstail.nodewar.commands.subcommands.territory.territorysubcommands.edit.territoryeditsubcommands.team.territoryeditteamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.territory.territorysubcommands.edit.territoryeditsubcommands.team.TerritoryEditTeamSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import org.bukkit.command.CommandSender;

public class TerritoryEditTeamResetCommand extends TerritoryEditTeamSubCommand {

    public TerritoryEditTeamResetCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TERRITORY_EDIT_TEAM_RESET_DESC))
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
        return "nodewar territory edit <territory> team reset";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        Territory territory;
        String message = LangManager.getMessage(LangMessage.COMMANDS_TERRITORY_EDIT_TEAM_RESET_RESULT);
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        territory = TerritoryManager.getTerritoryManager().getTerritoryMap().get(args[3]);

        territory.setOwnerTeam(null);
        message = AdaptMessage.getAdaptMessage().adaptTerritoryMessage(message, territory);

        sender.sendMessage(message);
    }

}
