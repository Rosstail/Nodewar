package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.member.adminteameditmembersubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.member.AdminTeamEditMemberSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.player.PlayerModel;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminTeamEditMemberRemoveCommand extends AdminTeamEditMemberSubCommand {

    public AdminTeamEditMemberRemoveCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_MEMBER_REMOVE_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Kick a member of your team";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin team edit <team> member remove <player>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String targetTeamName;
        String targetPlayerName;
        NwITeam targetTeam;
        Player targetPlayer;
        PlayerData targetData;

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (args.length < 7) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TOO_FEW_ARGUMENTS));
            return;
        }

        targetTeamName = args[3];
        targetTeam = TeamManager.getManager().getStringTeamMap().get(targetTeamName);

        if (targetTeam == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_DOES_NOT_EXIST));
            return;
        }

        targetPlayerName = args[6];

        if (targetTeam.getMemberMap().values().stream()
                .noneMatch(teamMember -> teamMember.getUsername().equalsIgnoreCase(targetPlayerName))) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_PLAYER_NOT_IN_TEAM));
            return;
        }

        targetPlayer = Bukkit.getPlayer(targetPlayerName);

        if (targetPlayer == null) {
            PlayerModel playerModel = StorageManager.getManager().selectPlayerModel(PlayerDataManager.getPlayerUUIDFromName(targetPlayerName));
            if (playerModel == null) {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_PLAYER_DOES_NOT_EXIST));
                return;
            }
            TeamManager.getManager().deleteTeamMember(targetTeam, targetPlayerName, false);
        } else {
            TeamManager.getManager().deleteOnlineTeamMember(targetTeam, targetPlayer, false);
        }

        sender.sendMessage(
                AdaptMessage.getAdaptMessage().adaptTeamMessage(LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_MEMBER_REMOVE_RESULT), targetTeam, targetPlayer)
        );
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        NwITeam nwTeam = TeamManager.getManager().getStringTeamMap().get(args[3]);
        if (nwTeam != null) {
            return nwTeam.getMemberMap().values().stream().map(teamMember -> teamMember.getUsername()).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
