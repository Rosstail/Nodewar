package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.member.adminteameditmembersubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.member.AdminTeamEditMemberSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.rank.TeamRank;
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
            sender.sendMessage("not enough arguments");
            return;
        }

        targetTeamName = args[3];
        targetPlayerName = args[6];

        if (targetTeamName == null) {
            sender.sendMessage("team does not exist");
            return;
        }

        targetTeam = TeamDataManager.getTeamDataManager().getStringTeamMap().get(targetTeamName);


        targetTeamMemberModel = targetTeam.getModel().getTeamMemberModelMap().values().stream()
                .filter(teamMemberModel -> teamMemberModel.getUsername().equalsIgnoreCase(targetPlayerName)).findFirst().orElse(null);

        ownerTeamMemberModel = targetTeam.getModel().getTeamMemberModelMap().values().stream()
                .filter(teamMemberModel -> teamMemberModel.getRank() == TeamRank.OWNER.getWeight()).findFirst().orElse(null);


        if (targetTeamMemberModel == null) {
            sender.sendMessage("the player is not in the team.");
            return;
        }

        if (targetTeamMemberModel == ownerTeamMemberModel) {
            sender.sendMessage("target player is already the owner");
            return;
        }

        if (args.length < 8) {
            sender.sendMessage("add the team name to the command to confirm");
            return;
        }

        teamNameConfirmStr = args[7];

        if (!targetTeam.getModel().getName().equalsIgnoreCase(teamNameConfirmStr)) {
            sender.sendMessage("wrong team name");
            return;
        }

        if (ownerTeamMemberModel != null) {
            ownerTeamMemberModel.setRank(TeamRank.LIEUTENANT.getWeight());
            StorageManager.getManager().updateTeamMemberModel(ownerTeamMemberModel);
            ownerPlayer = Bukkit.getPlayer(ownerTeamMemberModel.getUsername());
            if (ownerPlayer != null) {
                targetTeam.getMemberMap().get(ownerPlayer).setRank(TeamRank.LIEUTENANT);
            }
        }

        targetTeamMemberModel.setRank(TeamRank.OWNER.getWeight());
        StorageManager.getManager().updateTeamMemberModel(targetTeamMemberModel);

        targetPlayer = Bukkit.getPlayer(targetTeamName);
        if (targetPlayer != null) {
            targetTeamMember = targetTeam.getMemberMap().get(targetPlayer);
            targetTeamMember.setRank(TeamRank.OWNER);
        }

        sender.sendMessage(
                AdaptMessage.getAdaptMessage().adaptTeamMessage(LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_MEMBER_TRANSFER_RESULT), targetTeam, null)
        );
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        NwTeam targetTeam = TeamDataManager.getTeamDataManager().getStringTeamMap().get(args[3]);
        if (targetTeam != null) {
            return targetTeam.getModel().getTeamMemberModelMap().values().stream().map(TeamMemberModel::getUsername).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
