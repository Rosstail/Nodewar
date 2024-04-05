package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.TeamModel;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.dynmap.DynmapHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TeamRedeployCommand extends TeamSubCommand {

    public TeamRedeployCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_CREATE_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "redeploy";
    }

    @Override
    public String getDescription() {
        return "Desc redeploy nodewar team";
    }

    @Override
    public String getSyntax() {
        return "nodewar team redeploy <territory> (region)";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.redeploy";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String territoryName;
        String territoryRegionName = null;
        Player senderPlayer;
        NwTeam playerNwTeam;
        Territory territory;
        TeamDataManager teamDataManager = TeamDataManager.getTeamDataManager();
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_BY_PLAYER_ONLY));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TOO_FEW_ARGUMENTS));
            return;
        }
        senderPlayer = (Player) sender;
        playerNwTeam = teamDataManager.getTeamOfPlayer(senderPlayer);

        if (playerNwTeam == null) {
            sender.sendMessage("You are not in a team");
            return;
        }

        territoryName = args[2];

        if (args.length >= 4) {
            territoryRegionName = args[3];
        }

        if (!TerritoryManager.getTerritoryManager().getTerritoryMap().containsKey(territoryName)) {
            sender.sendMessage("territory not exist");
            return;
        }

        territory = TerritoryManager.getTerritoryManager().getTerritoryMap().get(territoryName);

        if (!territory.getProtectedRegionList().isEmpty()) {
            if (territoryRegionName != null) {
                String finalTerritoryRegionName = territoryRegionName;
                if (territory.getProtectedRegionList().stream().anyMatch(protectedRegion -> (protectedRegion.getId().equalsIgnoreCase(finalTerritoryRegionName)))) {
                    sender.sendMessage("YES");
                }

            } else {
                System.out.println("NOPE");
            }
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        TeamDataManager teamDataManager = TeamDataManager.getTeamDataManager();
        NwTeam playerNwTeam = teamDataManager.getTeamOfPlayer(sender);
        String selectedTerritoryName;
        System.out.println("SX");
        if (playerNwTeam != null && args.length <= 4) {
            Stream<Territory> territoryStream = TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream()
                    .filter(territory -> territory.getOwnerTeam().equals(playerNwTeam) && !territory.getProtectedRegionList().isEmpty());

            if (args.length <= 3) {
                return territoryStream.map(Territory::getModel).map(territoryModel -> getName()).collect(Collectors.toList());
            } else {
                return Collections.singletonList("NOPE");
                //selectedTerritoryName = args[3];
                /*return territoryStream.filter(territory -> (territory.getModel().getName().equalsIgnoreCase(selectedTerritoryName)))
                        .map(Territory::getProtectedRegionList).map()

                 */
            }
        }
        return Collections.singletonList("NAH");
    }
}
