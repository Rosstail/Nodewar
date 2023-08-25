package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.member.teammanagermembersubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.member.TeamManageMemberSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.TeamManager;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.rank.NwTeamRank;
import fr.rosstail.nodewar.team.type.NwTeam;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TeamManageMemberTransferCommand extends TeamManageMemberSubCommand {

    public TeamManageMemberTransferCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_MEMBER_TRANSFER_DESC))
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
        return "nodewar team manage member transfer <player> <team>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.manage.member.transfer";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String targetName;
        String teamNameConfirmStr;
        Player senderPlayer;
        Player targetPlayer;
        TeamMember senderTeamMember;
        TeamMemberModel senderTeamMemberModel;
        TeamMember targetTeamMember;
        TeamMemberModel targetTeamMemberModel;
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (sender instanceof Player) {
            senderPlayer = ((Player) sender).getPlayer();
            NwITeam senderNwITeam = TeamManager.getManager().getPlayerTeam(senderPlayer);

            if (senderNwITeam == null) {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_PART_OF_NO_TEAM));
                return;
            }

            if (!hasPlayerEnoughClearance(((Player) sender).getPlayer(), senderNwITeam, NwTeamRank.OWNER)) {
                return;
            }

            if (args.length < 5) {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TOO_FEW_ARGUMENTS));
                return;
            }

            targetName = args[4];

            if (senderPlayer.getName().equals(targetName)) {
                sender.sendMessage("You are already owner of " + senderNwITeam.getDisplay());
                return;
            }

            targetTeamMemberModel = senderNwITeam.getMemberMap().values().stream()
                    .filter(teamMember -> teamMember.getModel().getUsername().equalsIgnoreCase(targetName)).findFirst().orElse(null).getModel();

            senderTeamMember = senderNwITeam.getMemberMap().get(senderPlayer);
            senderTeamMemberModel = senderTeamMember.getModel();

            if (targetTeamMemberModel == null) {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_PLAYER_NOT_IN_TEAM));
                return;
            }

            if (args.length < 6) {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RESULT_NAME_CONFIRM));
                return;
            }

            teamNameConfirmStr = args[5];

            if (!senderNwITeam.getName().equalsIgnoreCase(teamNameConfirmStr)) {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_DOES_NOT_EXIST));
                return;
            }

            senderTeamMemberModel.setRank(NwTeamRank.LIEUTENANT.getWeight());
            targetTeamMemberModel.setRank(NwTeamRank.OWNER.getWeight());
            StorageManager.getManager().updateTeamMemberModel(senderTeamMemberModel);
            StorageManager.getManager().updateTeamMemberModel(targetTeamMemberModel);

            senderNwITeam.getMemberMap().get(senderPlayer).setRank(NwTeamRank.LIEUTENANT);
            targetPlayer = Bukkit.getPlayer(targetName);

            if (targetPlayer != null) {
                targetTeamMember = senderNwITeam.getMemberMap().get(targetPlayer);
                targetTeamMember.setRank(NwTeamRank.OWNER);
            }

            sender.sendMessage(
                    AdaptMessage.getAdaptMessage().adaptTeamMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_MEMBER_TRANSFER_RESULT), senderNwITeam, senderPlayer)
            );

            StorageManager.getManager().updateTeamModel(senderNwITeam);
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        NwITeam playerNwITeam = TeamManager.getManager().getPlayerTeam(sender);
        if (playerNwITeam != null) {
            return playerNwITeam.getMemberMap().values().stream().map(teamMember -> teamMember.getModel().getUsername()).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
