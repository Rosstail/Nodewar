package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.TeamManageSubCommand;
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

public class TeamManageInviteCommand extends TeamManageSubCommand {

    public TeamManageInviteCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_INVITE_DESC))
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
        return "nodewar team manage invite <player>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.manage.invite";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_BY_PLAYER_ONLY));
            return;
        }

        Player senderPlayer = ((Player) sender).getPlayer();
        NwTeam playerNwTeam = TeamDataManager.getTeamDataManager().getTeamOfPlayer(senderPlayer);
        Player targetPlayer;
        PlayerData targetPlayerData;


        if (playerNwTeam == null) {
            senderPlayer.sendMessage("Your have no team");
            return;
        }

        if (!hasSenderTeamRank(((Player) sender).getPlayer(), playerNwTeam, TeamRank.LIEUTENANT)) {
            return;
        }

        if (args.length < 4) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TOO_FEW_ARGUMENTS));
            return;
        }
        targetPlayer = Bukkit.getPlayer(args[3]);

        if (targetPlayer == null) {
            senderPlayer.sendMessage("Player does not exist or is disconnected");
            return;
        }

        targetPlayerData = PlayerDataManager.getPlayerDataFromMap(targetPlayer);

        if (targetPlayerData.getTeam() != null) {
            sender.sendMessage("player already in a team");
            return;
        }

        if (playerNwTeam.getModel().getTeamMemberModelMap().size() >= ConfigData.getConfigData().team.maximumMembers) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_FULL));
            return;
        }

        if (TeamDataManager.getTeamDataManager().invite(targetPlayer, playerNwTeam)) {
            senderPlayer.sendMessage("Invitation sent");
        } else {
            senderPlayer.sendMessage("impossible invitation");
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
