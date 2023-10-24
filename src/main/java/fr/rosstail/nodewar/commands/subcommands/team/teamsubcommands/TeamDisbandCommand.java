package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.Team;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.TeamModel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class TeamDisbandCommand extends TeamSubCommand {
    @Override
    public String getName() {
        return "disband";
    }

    @Override
    public String getDescription() {
        return "Disband your team";
    }

    @Override
    public String getSyntax() {
        return "nodewar team disband";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.disband";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (sender instanceof Player) {
            Player player = ((Player) sender).getPlayer();
            boolean disband = false;
            Team playerTeam = TeamDataManager.getTeamDataManager().getTeamOfPlayer(player);
            if (playerTeam != null) {
                if (playerTeam.getMemberModelMap().get(PlayerDataManager.getPlayerDataMap().get(player.getName()).getId()).getRank() == 1) {
                    disband = true;
                    StorageManager.getManager().deleteTeamModel(playerTeam.getTeamModel().getId());
                    PlayerDataManager.getPlayerDataMap().values().stream().filter(playerData ->
                            (playerData.getTeam() == playerTeam)).forEach(playerData -> {
                        playerData.setTeam(null);
                    });
                } else {
                    sender.sendMessage("you do not have enough rank on your team");
                }
            } else {
                sender.sendMessage("Your team is null");
            }

            if (!disband) {
                sender.sendMessage("You are not owner of any team");
            } else {
                sender.sendMessage("Disbanded team");
            }
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
