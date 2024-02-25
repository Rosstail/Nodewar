package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.*;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.rank.TeamRank;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TeamLeaveCommand extends TeamSubCommand {

    public TeamLeaveCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_LEAVE_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }
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
        teamMember = nwTeam.getMemberMap().get(senderPlayer);

        if (teamMember.getRank() == TeamRank.OWNER) {
            sender.sendMessage("You cannot leave the team while you are his owner");
            return;
        }

        StorageManager.getManager().deleteTeamMemberModel(teamMember.getModel().getId());
        nwTeam.getMemberMap().remove(senderPlayer);
        playerData.removeTeam();
        sender.sendMessage("You successfully left " + nwTeam.getModel().getDisplay() + " team");
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return new ArrayList<>(TeamDataManager.getTeamDataManager().getStringTeamMap().keySet());
    }
}
