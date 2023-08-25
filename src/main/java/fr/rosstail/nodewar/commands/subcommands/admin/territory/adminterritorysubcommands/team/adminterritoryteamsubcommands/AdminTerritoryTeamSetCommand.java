package fr.rosstail.nodewar.commands.subcommands.admin.territory.adminterritorysubcommands.team.adminterritoryteamsubcommands;

import fr.rosstail.nodewar.commands.subcommands.admin.territory.adminterritorysubcommands.team.AdminTerritoryTeamSubCommand;
import fr.rosstail.nodewar.events.territoryevents.TerritoryOwnerChangeEvent;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.team.TeamManager;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminTerritoryTeamSetCommand extends AdminTerritoryTeamSubCommand {

    public AdminTerritoryTeamSetCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TERRITORY_TEAM_SET_DESC))
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
        return "nodewar admin territory <territory> team set <team>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        Territory territory;
        NwITeam team;
        String message = LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TERRITORY_TEAM_SET_RESULT);

        if (args.length < 6) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TOO_FEW_ARGUMENTS));
            return;
        }

        territory = TerritoryManager.getTerritoryManager().getTerritoryMap().get(args[2]);
        team = TeamManager.getManager().getStringTeamMap().get(args[5]);

        if (team == null) {
            message = LangManager.getMessage(LangMessage.COMMANDS_WRONG_VALUE).replaceAll("\\[value]", args[5]);
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
        return new ArrayList<>(TeamManager.getManager().getStringTeamMap().keySet());
    }
}
