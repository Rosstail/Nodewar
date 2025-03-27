package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.events.playerevents.PlayerInitDeployEvent;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.TeamManager;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import fr.rosstail.nodewar.territory.TerritoryModel;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
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
        NwITeam playerNwITeam;
        Territory territory;
        PlayerData playerData;
        TeamManager teamManager = TeamManager.getManager();
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
        playerNwITeam = teamManager.getPlayerTeam(senderPlayer);

        if (playerNwITeam == null) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_PLAYER_NOT_IN_TEAM)));
            return;
        }

        if (playerData.getLastDeploy() > System.currentTimeMillis() - ConfigData.getConfigData().team.deployCooldown) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_DEPLOY_FAILURE_TIMER).replaceAll("\\[timer]", AdaptMessage.getAdaptMessage().countdownFormatter(playerData.getLastDeploy() + ConfigData.getConfigData().team.deployCooldown - System.currentTimeMillis())));
            return;
        }

        territoryName = args[2];

        if (args.length >= 4) {
            territoryRegionName = args[3];
        }

        List<Territory> teleportTerritoryList = TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream()
                .filter(streamTerritory -> streamTerritory.getOwnerITeam() == playerNwITeam &&
                        streamTerritory.getProtectedRegionList().stream().anyMatch(region ->
                                region.getFlags().containsKey(Flags.TELE_LOC))).collect(Collectors.toList());
        if (!teleportTerritoryList.stream().anyMatch(territory1 -> territory1.getName().equalsIgnoreCase(territoryName))) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_DEPLOY_FAILURE_TERRITORY));
            return;
        }

        territory = teleportTerritoryList.stream().filter(territory1 -> territory1.getName().equalsIgnoreCase(territoryName)).findFirst().get();

        List<ProtectedRegion> teleportTerritoryRegionList = territory.getProtectedRegionList().stream().filter(region ->
                                region.getFlags().containsKey(Flags.TELE_LOC)).collect(Collectors.toList());

        String finalTerritoryRegionName = territoryRegionName;
        ProtectedRegion protectedRegion;
        if (territoryRegionName != null) {
            if (teleportTerritoryRegionList.stream().noneMatch(protectedRegion1 -> protectedRegion1.getId().equalsIgnoreCase(finalTerritoryRegionName))) {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_DEPLOY_FAILURE_REGION));
                return;
            }

            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_DEPLOY_REGION));
            protectedRegion = teleportTerritoryRegionList.stream().filter(protectedRegion1 -> protectedRegion1.getId().equalsIgnoreCase(finalTerritoryRegionName)).findFirst().get();
        } else {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_DEPLOY_TERRITORY));
            protectedRegion = teleportTerritoryRegionList.get((int) (Math.random() * teleportTerritoryRegionList.size()));
        }

        PlayerInitDeployEvent playerInitDeployEvent = new PlayerInitDeployEvent(senderPlayer, playerNwITeam, territory, protectedRegion);
        Bukkit.getPluginManager().callEvent(playerInitDeployEvent);
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        TeamManager teamManager = TeamManager.getManager();
        NwITeam playerNwITeam = teamManager.getPlayerTeam((Player) sender);
        String selectedTerritoryName;
        if (playerNwITeam != null && args.length <= 4) {
            Stream<Territory> territoryStream = TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream()
                    .filter(territory -> territory.getOwnerITeam() == playerNwITeam &&
                            territory.getProtectedRegionList().stream().anyMatch(region ->
                                    region.getFlags().containsKey(Flags.TELE_LOC)));
            if (args.length <= 3) {
                return territoryStream.map(Territory::getName).collect(Collectors.toList());
            } else {
                selectedTerritoryName = args[2];
                Territory selectedTerritory = territoryStream.filter(territory -> (territory.getName().equalsIgnoreCase(selectedTerritoryName))).findFirst().orElse(null);
                if (selectedTerritory != null) {
                    List<ProtectedRegion> teleportTerritoryRegionList = selectedTerritory.getProtectedRegionList().stream().filter(region ->
                            region.getFlags().containsKey(Flags.TELE_LOC)).toList();
                    return teleportTerritoryRegionList.stream().map(ProtectedRegion::getId).collect(Collectors.toList());
                }
            }
        }
        return Collections.emptyList();
    }
}
