package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.admin.team.AdminTeamSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.rank.TeamRank;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class AdminTeamInviteCommand extends AdminTeamSubCommand {

    public AdminTeamInviteCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_INVITE_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public String getDescription() {
        return "Invite a player in your team";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin team <team> invite <player>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        String targetTeamName;
        String targetPlayerName;
        NwTeam targetTeam;
        Player targetPlayer;
        PlayerData targetPlayerData;

        if (args.length < 4) {
            sender.sendMessage("not enough args");
            return;
        }

        targetTeamName = args[2];
        targetPlayerName = args[4];
        targetTeam = TeamDataManager.getTeamDataManager().getStringTeamMap().get(targetTeamName);

        if (targetTeam == null) {
            sender.sendMessage("this team does not exist");
            return;
        }

        targetPlayer = Bukkit.getPlayer(targetPlayerName);

        if (targetPlayer == null) {
            sender.sendMessage("player does not exist or is disconnected");
            return;
        }

        targetPlayerData = PlayerDataManager.getPlayerDataFromMap(targetPlayer);

        if (targetPlayerData.getTeam() != null) {
            sender.sendMessage("player already in a team");
            return;
        }


        if (TeamDataManager.getTeamDataManager().invite(targetPlayer, targetTeam)) {
            sender.sendMessage("Invitation sent");
        } else {
            sender.sendMessage("impossible invitation");
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
