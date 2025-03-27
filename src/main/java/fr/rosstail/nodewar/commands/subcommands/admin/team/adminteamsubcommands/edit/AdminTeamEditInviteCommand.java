package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.AdminTeamEditSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class AdminTeamEditInviteCommand extends AdminTeamEditSubCommand {

    public AdminTeamEditInviteCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_INVITE_DESC))
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
        return "nodewar admin team edit <team> invite <player>";
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
        NwITeam targetTeam;
        Player targetPlayer;
        PlayerData targetPlayerData;

        if (args.length < 5) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TOO_FEW_ARGUMENTS));
            return;
        }

        targetTeamName = args[3];
        targetPlayerName = args[5];
        targetTeam = TeamManager.getManager().getStringTeamMap().get(targetTeamName);

        if (targetTeam == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_DOES_NOT_EXIST));
            return;
        }

        if (ConfigData.getConfigData().team.maximumMembers > -1 && targetTeam.getMemberMap().size() >= ConfigData.getConfigData().team.maximumMembers) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_FULL));
            return;
        }

        targetPlayer = Bukkit.getPlayer(targetPlayerName);

        if (targetPlayer == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_PLAYER_DOES_NOT_EXIST));
            return;
        }

        targetPlayerData = PlayerDataManager.getPlayerDataFromMap(targetPlayer);


        if (targetPlayerData.getTeam() != null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_PLAYER_ALREADY_IN_TEAM));
            return;
        }


        if (sender instanceof Player) {
            if (targetTeam.canInvitePlayer(((Player) sender).getPlayer(), targetPlayer)) {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_INVITE_RESULT));
                TeamManager.getManager().invitePlayerToTeam(targetTeam, ((Player) sender).getPlayer(), targetPlayer);
            }
        } else {
            if (targetTeam.canInvitePlayer(null, targetPlayer)) {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_INVITE_RESULT));
                TeamManager.getManager().invitePlayerToTeam(targetTeam, null, targetPlayer);
            }
        }
        sender.sendMessage("impossible invitation");
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        return null;
    }
}
