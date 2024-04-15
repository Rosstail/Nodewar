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
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.List;

public class AdminTeamEditMemberAddCommand extends AdminTeamEditMemberSubCommand {

    public AdminTeamEditMemberAddCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_MEMBER_ADD_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return "Add a member to the team";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin team edit <team> member add <player>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String targetTeamName;
        String targetPlayerName;
        NwTeam targetTeam;
        Player targetPlayer;
        PlayerData targetData;
        int playerDataId;

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (args.length < 7) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TOO_FEW_ARGUMENTS));
            return;
        }

        targetTeamName = args[3];
        targetTeam = TeamDataManager.getTeamDataManager().getStringTeamMap().get(targetTeamName);

        if (targetTeam == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_DOES_NOT_EXIST));
            return;
        }

        targetPlayerName = args[6];

        if (targetTeam.getModel().getTeamMemberModelMap().values().stream()
                .anyMatch(teamMemberModel -> teamMemberModel.getUsername().equalsIgnoreCase(targetTeamName))) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_PLAYER_ALREADY_IN_TEAM));
            return;
        }

        targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            playerDataId = PlayerDataManager.getPlayerDataFromMap(targetPlayer).getId();
        } else {
            PlayerModel playerModel = StorageManager.getManager().selectPlayerModel(PlayerDataManager.getPlayerUUIDFromName(targetPlayerName));
            if (playerModel == null) {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_PLAYER_DOES_NOT_EXIST));
                return;
            }
            playerDataId = playerModel.getId();
        }

        TeamMemberModel teamMemberModel =
                new TeamMemberModel(targetTeam.getModel().getId(), playerDataId, 1, new Timestamp(System.currentTimeMillis()), targetPlayerName);

        StorageManager.getManager().insertTeamMemberModel(teamMemberModel);

        if (targetPlayer != null) {
            TeamMember teamMember = new TeamMember(targetPlayer, targetTeam, teamMemberModel);
            targetTeam.getMemberMap().put(targetPlayer, teamMember);
            targetData = PlayerDataManager.getPlayerDataFromMap(targetPlayer);
            targetData.removeTeam();
        }
        sender.sendMessage(
                AdaptMessage.getAdaptMessage().adaptTeamMessage(LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_MEMBER_ADD_RESULT), targetTeam, targetPlayer)
        );

        StorageManager.getManager().updateTeamModel(targetTeam.getModel());
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
