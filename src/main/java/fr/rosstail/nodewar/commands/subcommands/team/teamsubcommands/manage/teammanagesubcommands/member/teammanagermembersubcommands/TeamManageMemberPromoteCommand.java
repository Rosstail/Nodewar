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
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TeamManageMemberPromoteCommand extends TeamManageMemberSubCommand {

    public TeamManageMemberPromoteCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_MEMBER_PROMOTE_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "promote";
    }

    @Override
    public String getDescription() {
        return "Kick a member of your team";
    }

    @Override
    public String getSyntax() {
        return "nodewar team manage member promote <player>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.manage.member.promote";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String targetName;
        Player targetPlayer;
        TeamMember targetTeamMember;
        TeamMemberModel targetTeamMemberModel;
        int newRank;

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (sender instanceof Player) {
            Player player = ((Player) sender).getPlayer();
            NwITeam playerNwITeam = TeamManager.getManager().getPlayerTeam(player);

            if (playerNwITeam == null) {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_PART_OF_NO_TEAM));
                return;
            }

            NwTeamRank playerRank = playerNwITeam.getMemberMap().get(player).getRank();

            if (!hasPlayerEnoughClearance(((Player) sender).getPlayer(), playerNwITeam, NwTeamRank.LIEUTENANT)) {
                return;
            }

            if (args.length < 5) {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TOO_FEW_ARGUMENTS));
                return;
            }

            targetName = args[4];

            if (player.getName().equals(targetName)) {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_MEMBER_CANNOT_TARGET_SELF));
                return;
            }

            targetTeamMemberModel = playerNwITeam.getMemberMap().values().stream()
                    .filter(teamMember -> teamMember.getModel().getUsername().equalsIgnoreCase(targetName)).findFirst().orElse(null).getModel();

            if (targetTeamMemberModel == null) {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_PLAYER_NOT_IN_TEAM));
                return;
            }

            newRank = targetTeamMemberModel.getRank() + 1;
            if (newRank >= playerRank.getWeight()) {
                sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_MEMBER_PROMOTE_ERROR)));
                return;
            }

            targetTeamMemberModel.setRank(newRank);
            StorageManager.getManager().updateTeamMemberModel(targetTeamMemberModel);

            targetPlayer = Bukkit.getPlayer(targetName);
            if (targetPlayer != null) {
                targetTeamMember = playerNwITeam.getMemberMap().get(targetPlayer);
                targetTeamMember.setRank(Arrays.stream(NwTeamRank.values()).filter(teamRank -> teamRank.getWeight() == newRank).findFirst().get());
            }

            sender.sendMessage(
                    AdaptMessage.getAdaptMessage().adaptTeamMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_MEMBER_PROMOTE_RESULT), playerNwITeam, player)
            );

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
