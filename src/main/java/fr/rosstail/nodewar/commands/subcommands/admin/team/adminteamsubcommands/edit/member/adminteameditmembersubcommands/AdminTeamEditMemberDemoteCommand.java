package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.member.adminteameditmembersubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.member.AdminTeamEditMemberSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.team.TeamManager;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.rank.NwTeamRank;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdminTeamEditMemberDemoteCommand extends AdminTeamEditMemberSubCommand {

    public AdminTeamEditMemberDemoteCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_MEMBER_DEMOTE_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "demote";
    }

    @Override
    public String getDescription() {
        return "Demote a member from his team rank";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin team edit <team> member demote <player>";
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
        TeamMember targetTeamMember;
        TeamMemberModel targetTeamMemberModel;
        int newRank;

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        targetTeamName = args[3];
        targetTeam = TeamManager.getManager().getStringTeamMap().get(targetTeamName);

        if (targetTeam == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_DOES_NOT_EXIST));
            return;
        }

        if (args.length < 7) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TOO_FEW_ARGUMENTS));
            return;
        }

        targetPlayerName = args[6];

        targetTeamMember = targetTeam.getMemberMap().values().stream()
                .filter(teamMemberModel -> teamMemberModel.getModel().getUsername().equalsIgnoreCase(targetPlayerName)).findFirst().orElse(null);

        if (targetTeamMember == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_PLAYER_NOT_IN_TEAM));
            return;
        }

        newRank = targetTeamMember.getModel().getRank() - 1;
        if (newRank == 0) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_MEMBER_DEMOTE_ERROR));
            return;
        }

        //targetTeamMember.setRank(newRank);
        //StorageManager.getManager().updateTeamMemberModel(targetTeamMemberModel);

        targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            targetTeamMember = targetTeam.getOnlineMemberMap().get(targetPlayer);
            targetTeamMember.setRank(Arrays.stream(NwTeamRank.values()).filter(teamRank -> teamRank.getWeight() == newRank).findFirst().get());
        }

        sender.sendMessage(
                AdaptMessage.getAdaptMessage().adaptTeamMessage(LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_MEMBER_DEMOTE_RESULT), targetTeam, targetPlayer)
        );
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        NwITeam nwTeam = TeamManager.getManager().getStringTeamMap().get(args[3]);
        if (nwTeam != null) {
            return nwTeam.getMemberMap().values().stream().map(teamMember -> teamMember.getModel().getUsername()).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
