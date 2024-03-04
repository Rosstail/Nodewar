package fr.rosstail.nodewar.commands.subcommands.territory.territorysubcommands.edit.territoryeditsubcommands.team.territoryeditteamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.commands.subcommands.territory.territorysubcommands.edit.territoryeditsubcommands.team.TerritoryEditTeamSubCommand;
import fr.rosstail.nodewar.events.territoryevents.TerritoryOwnerChangeEvent;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TerritoryEditTeamSetCommand extends TerritoryEditTeamSubCommand {

    public TerritoryEditTeamSetCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TERRITORY_EDIT_TEAM_SET_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));

    }

    @Override
    public String getName() {
        return "set";
    }

    @Override
    public String getDescription() {
        return "Set territory owner";
    }

    @Override
    public String getSyntax() {
        return "nodewar territory edit <territory> team set <team>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        Territory territory;
        NwTeam team;
        String message = LangManager.getMessage(LangMessage.COMMANDS_TERRITORY_EDIT_TEAM_SET_RESULT);
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (args.length < 6) {
            sender.sendMessage("Too few args");
            return;
        }

        territory = TerritoryManager.getTerritoryManager().getTerritoryMap().get(args[2]);
        team = TeamDataManager.getTeamDataManager().getStringTeamMap().get(args[5]);

        if (team == null) {
            message = "this team does not exist";
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(message));
            return;
        }

        TerritoryOwnerChangeEvent event = new TerritoryOwnerChangeEvent(territory, team, null);
        Bukkit.getPluginManager().callEvent(event);
        message = AdaptMessage.getAdaptMessage().adaptTeamMessage(message, team);
        message = AdaptMessage.getAdaptMessage().adaptTerritoryMessage(message, territory);
        sender.sendMessage(message);
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return new ArrayList<>(TeamDataManager.getTeamDataManager().getStringTeamMap().keySet());
    }
}
