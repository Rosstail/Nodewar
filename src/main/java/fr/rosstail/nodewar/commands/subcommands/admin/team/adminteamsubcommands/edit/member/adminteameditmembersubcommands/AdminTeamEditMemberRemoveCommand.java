package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.member.adminteameditmembersubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.member.AdminTeamEditMemberSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
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
        NwTeam targetTeam;
        Player targetPlayer;
        PlayerData targetData;

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (args.length < 7) {
            sender.sendMessage("not enough arguments");
            return;
        }

        targetTeamName = args[3];
        targetTeam = TeamDataManager.getTeamDataManager().getStringTeamMap().get(targetTeamName);

        if (targetTeam == null) {
            sender.sendMessage("Your team is null");
            return;
        }

        targetPlayerName = args[6];

        if (targetTeam.getModel().getTeamMemberModelMap().values().stream()
                .noneMatch(teamMemberModel -> teamMemberModel.getUsername().equalsIgnoreCase(targetTeamName))) {
            sender.sendMessage("the player is not in your team.");
            return;
        }

        StorageManager.getManager().deleteTeamMemberModel(targetTeam.getModel().getId());

        targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            targetTeam.getMemberMap().remove(targetPlayer);
            targetData = PlayerDataManager.getPlayerDataFromMap(targetPlayer);
            targetData.removeTeam();
        }
        sender.sendMessage(
                AdaptMessage.getAdaptMessage().adaptTeamMessage(LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_MEMBER_REMOVE_RESULT), targetTeam, targetPlayer)
        );

        StorageManager.getManager().updateTeamModel(targetTeam.getModel());
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        NwTeam nwTeam = TeamDataManager.getTeamDataManager().getStringTeamMap().get(args[3]);
        if (nwTeam != null) {
            return nwTeam.getModel().getTeamMemberModelMap().values().stream().map(TeamMemberModel::getUsername).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
