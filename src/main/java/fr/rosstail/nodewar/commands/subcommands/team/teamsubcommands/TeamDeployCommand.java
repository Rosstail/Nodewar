package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.TerritoryModel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TeamDeployCommand extends TeamSubCommand {

    public TeamDeployCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_DEPLOY_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "deploy";
    }

    @Override
    public String getDescription() {
        return "Desc deploy nodewar team";
    }

    @Override
    public String getSyntax() {
        return "nodewar team deploy <territory> (region)";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.deploy";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String territoryName;
        String territoryRegionName = null;
        Player senderPlayer;
        NwTeam playerNwTeam;
        Territory territory;
        PlayerData playerData;
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
        playerData = PlayerDataManager.getPlayerDataFromMap(senderPlayer);
        playerNwTeam = teamDataManager.getTeamOfPlayer(senderPlayer);

        if (playerNwTeam == null) {
            sender.sendMessage("you are not in a team");
            return;
        }

        if (playerData.getLastDeploy() > System.currentTimeMillis() - (ConfigData.getConfigData().team.deployCooldown * 1000L)) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_DEPLOY_FAILURE_TIMER));
            return;
        }

        territoryName = args[2];

        if (args.length >= 4) {
            territoryRegionName = args[3];
        }

        List<Territory> teleportTerritoryList = TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream()
                .filter(streamTerritory -> streamTerritory.getOwnerTeam().equals(playerNwTeam) &&
                        streamTerritory.getProtectedRegionList().stream().anyMatch(region ->
                                region.getFlags().containsKey(Flags.TELE_LOC))).collect(Collectors.toList());
        if (!teleportTerritoryList.stream().anyMatch(territory1 -> territory1.getModel().getName().equalsIgnoreCase(territoryName))) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_DEPLOY_FAILURE_TERRITORY));
            return;
        }

        territory = teleportTerritoryList.stream().filter(territory1 -> territory1.getModel().getName().equalsIgnoreCase(territoryName)).findFirst().get();

        String finalTerritoryRegionName = territoryRegionName;
        ProtectedRegion protectedRegion;
        if (territoryRegionName != null) {
            if (territory.getProtectedRegionList().stream().noneMatch(protectedRegion1 -> protectedRegion1.getId().equalsIgnoreCase(finalTerritoryRegionName))) {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_DEPLOY_FAILURE_REGION));
                return;
            }

            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_DEPLOY_REGION));
            protectedRegion = territory.getProtectedRegionList().stream().filter(protectedRegion1 -> protectedRegion1.getId().equalsIgnoreCase(finalTerritoryRegionName)).findFirst().get();
        } else {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_DEPLOY_TERRITORY));
            protectedRegion = territory.getProtectedRegionList().get(0);
        }

        playerData.setLastDeploy(System.currentTimeMillis());
        senderPlayer.teleport(BukkitAdapter.adapt(protectedRegion.getFlag(Flags.TELE_LOC)));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        TeamDataManager teamDataManager = TeamDataManager.getTeamDataManager();
        NwTeam playerNwTeam = teamDataManager.getTeamOfPlayer(sender);
        String selectedTerritoryName;
        if (playerNwTeam != null && args.length <= 4) {
            Stream<Territory> territoryStream = TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream()
                    .filter(territory -> territory.getOwnerTeam().equals(playerNwTeam) &&
                            territory.getProtectedRegionList().stream().anyMatch(region ->
                                    region.getFlags().containsKey(Flags.TELE_LOC)));
            if (args.length <= 3) {
                return territoryStream.map(Territory::getModel).map(TerritoryModel::getName).collect(Collectors.toList());
            } else {
                selectedTerritoryName = args[2];
                Territory selectedTerritory = territoryStream.filter(territory -> (territory.getModel().getName().equalsIgnoreCase(selectedTerritoryName))).findFirst().orElse(null);
                if (selectedTerritory != null) {
                    return selectedTerritory.getProtectedRegionList().stream().map(ProtectedRegion::getId).collect(Collectors.toList());
                }
            }
        }
        return Collections.emptyList();
    }
}
