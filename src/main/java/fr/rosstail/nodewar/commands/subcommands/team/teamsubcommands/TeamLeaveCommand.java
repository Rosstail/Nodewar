package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.Team;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.TeamMemberModel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TeamLeaveCommand extends TeamSubCommand {
    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getDescription() {
        return "Desc leave nodewar team";
    }

    @Override
    public String getSyntax() {
        return "nodewar team leave";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.leave";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        Player senderPlayer;
        Team team;
        PlayerData playerData;
        TeamMemberModel teamMemberModel;
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Must be sent by player");
            return;
        }
        senderPlayer = ((Player) sender).getPlayer();
        playerData = PlayerDataManager.getPlayerDataMap().get(senderPlayer.getName());
        team = playerData.getTeam();

        if (team == null) {
            sender.sendMessage("You are not on a team");
            return;
        }

        sender.sendMessage("TODO leaving " + team.getTeamModel().getName() + " team.");
        teamMemberModel = team.getMemberModelMap().get(playerData.getId());

        if (teamMemberModel.getRank() == 1 ) {
            sender.sendMessage("You cannot leave the team while you are his owner");
            return;
        }

        StorageManager.getManager().deleteTeamMemberModel(teamMemberModel.getId());
        team.getMemberModelMap().put(playerData.getId(), teamMemberModel);
        playerData.removeTeam();
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return new ArrayList<>(TeamDataManager.getTeamDataManager().getStringTeamMap().keySet());
    }
}
