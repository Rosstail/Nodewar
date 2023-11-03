package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        NwTeam nwTeam;
        PlayerData playerData;
        TeamMember teamMember;
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Must be sent by player");
            return;
        }
        senderPlayer = ((Player) sender).getPlayer();
        playerData = PlayerDataManager.getPlayerDataMap().get(senderPlayer.getName());
        nwTeam = playerData.getTeam();

        if (nwTeam == null) {
            sender.sendMessage("You are not on a team");
            return;
        }

        sender.sendMessage("TODO leaving " + nwTeam.getModel().getName() + " team.");
        teamMember = nwTeam.getMemberMap().get(playerData.getId());

        if (teamMember.getRank() == TeamRank.OWNER) {
            sender.sendMessage("You cannot leave the team while you are his owner");
            return;
        }

        StorageManager.getManager().deleteTeamMemberModel(teamMember.getModel().getId());
        nwTeam.getMemberMap().put(senderPlayer, teamMember);
        playerData.removeTeam();
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return new ArrayList<>(TeamDataManager.getTeamDataManager().getStringTeamMap().keySet());
    }
}
