package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.team.*;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.rank.NwTeamRank;
import fr.rosstail.nodewar.team.type.NwTeam;
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
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_BY_PLAYER_ONLY));
            return;
        }
        senderPlayer = ((Player) sender).getPlayer();
        playerData = PlayerDataManager.getPlayerDataMap().get(senderPlayer.getName());
        nwTeam = (NwTeam) playerData.getTeam();

        if (nwTeam == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_PLAYER_NOT_IN_TEAM));
            return;
        }

        teamMember = nwTeam.getOnlineMemberMap().get(senderPlayer);

        if (teamMember.getRank() == NwTeamRank.OWNER) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_LEAVE_RESULT_FAILURE_OWNER));
            return;
        }

        TeamManager.getManager().deleteOnlineTeamMember(nwTeam, senderPlayer, false);
        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptTeamMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_LEAVE_RESULT), nwTeam, senderPlayer) );
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return new ArrayList<>(TeamManager.getManager().getStringTeamMap().keySet());
    }
}
