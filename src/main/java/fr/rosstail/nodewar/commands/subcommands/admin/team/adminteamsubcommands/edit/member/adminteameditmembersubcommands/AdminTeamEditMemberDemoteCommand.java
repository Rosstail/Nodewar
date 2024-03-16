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
        NwTeam targetTeam;
        Player targetPlayer;
        TeamMember targetTeamMember;
        TeamMemberModel targetTeamMemberModel;
        int newRank;

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        targetTeamName = args[3];
        targetTeam = TeamDataManager.getTeamDataManager().getStringTeamMap().get(targetTeamName);

        if (targetTeam == null) {
            sender.sendMessage("The team does not exist");
            return;
        }

        if (args.length < 7) {
            sender.sendMessage("not enough arguments");
            return;
        }

        targetPlayerName = args[6];

        targetTeamMemberModel = targetTeam.getModel().getTeamMemberModelMap().values().stream()
                .filter(teamMemberModel -> teamMemberModel.getUsername().equalsIgnoreCase(targetPlayerName)).findFirst().orElse(null);

        if (targetTeamMemberModel == null) {
            sender.sendMessage("the player is not in your team.");
            return;
        }

        newRank = targetTeamMemberModel.getRank() - 1;
        if (newRank == 0) {
            sender.sendMessage("You cannot demote this player.");
            return;
        }

        targetTeamMemberModel.setRank(newRank);
        StorageManager.getManager().updateTeamMemberModel(targetTeamMemberModel);

        targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer != null) {
            targetTeamMember = targetTeam.getMemberMap().get(targetPlayer);
            targetTeamMember.setRank(Arrays.stream(TeamRank.values()).filter(teamRank -> teamRank.getWeight() == newRank).findFirst().get());
        }

        sender.sendMessage(
                AdaptMessage.getAdaptMessage().adaptTeamMessage(LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_MEMBER_DEMOTE_RESULT), targetTeam, targetPlayer)
        );
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
