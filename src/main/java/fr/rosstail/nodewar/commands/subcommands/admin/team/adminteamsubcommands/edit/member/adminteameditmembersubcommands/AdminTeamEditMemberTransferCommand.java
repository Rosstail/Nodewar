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
import java.util.List;
import java.util.stream.Collectors;

public class AdminTeamEditMemberTransferCommand extends AdminTeamEditMemberSubCommand {

    public AdminTeamEditMemberTransferCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_MEMBER_TRANSFER_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "transfer";
    }

    @Override
    public String getDescription() {
        return "Transfer ownership of the team";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin team edit <team> member transfer <player> <team>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String targetTeamName;
        String targetPlayerName;
        String teamNameConfirmStr;
        Player targetPlayer;
        Player ownerPlayer;
        NwTeam targetTeam;
        TeamMember targetTeamMember;
        TeamMember ownerTeamMember;
        TeamMemberModel targetTeamMemberModel;
        TeamMemberModel ownerTeamMemberModel;

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (args.length < 7) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TOO_FEW_ARGUMENTS));
            return;
        }

        targetTeamName = args[3];
        targetPlayerName = args[6];

        if (targetTeamName == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_DOES_NOT_EXIST));
            return;
        }

        targetTeam = (NwTeam) TeamManager.getManager().getStringTeamMap().get(targetTeamName);


        targetTeamMemberModel = targetTeam.getModel().getTeamMemberModelMap().values().stream()
                .filter(teamMemberModel -> teamMemberModel.getUsername().equalsIgnoreCase(targetPlayerName)).findFirst().orElse(null);

        ownerTeamMemberModel = targetTeam.getModel().getTeamMemberModelMap().values().stream()
                .filter(teamMemberModel -> teamMemberModel.getNumRank() == NwTeamRank.OWNER.getWeight()).findFirst().orElse(null);


        if (targetTeamMemberModel == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_PLAYER_NOT_IN_TEAM));
            return;
        }

        if (targetTeamMemberModel == ownerTeamMemberModel) {
            sender.sendMessage("target player is already the owner");
            return;
        }

        if (args.length < 8) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RESULT_NAME_CONFIRM));
            return;
        }

        teamNameConfirmStr = args[7];

        if (!targetTeam.getModel().getName().equalsIgnoreCase(teamNameConfirmStr)) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_VALUE));
            return;
        }

        if (ownerTeamMemberModel != null) {
            ownerTeamMemberModel.setNumRank(NwTeamRank.LIEUTENANT.getWeight());
            StorageManager.getManager().updateTeamMemberModel(ownerTeamMemberModel);
            ownerPlayer = Bukkit.getPlayer(ownerTeamMemberModel.getUsername());
            if (ownerPlayer != null) {
                targetTeam.getOnlineMemberMap().get(ownerPlayer).setRank(NwTeamRank.LIEUTENANT);
            }
        }

        targetTeamMemberModel.setNumRank(NwTeamRank.OWNER.getWeight());
        StorageManager.getManager().updateTeamMemberModel(targetTeamMemberModel);

        targetPlayer = Bukkit.getPlayer(targetTeamName);
        if (targetPlayer != null) {
            targetTeamMember = targetTeam.getOnlineMemberMap().get(targetPlayer);
            targetTeamMember.setRank(NwTeamRank.OWNER);
        }

        sender.sendMessage(
                AdaptMessage.getAdaptMessage().adaptTeamMessage(LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_MEMBER_TRANSFER_RESULT), targetTeam, null)
        );
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        NwITeam targetTeam = TeamManager.getManager().getStringTeamMap().get(args[3]);
        if (targetTeam != null) {
            return targetTeam.getMemberMap().values().stream().map(teamMember -> teamMember.getUsername()).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
