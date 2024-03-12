package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.member.teammanagermembersubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.member.TeamManageMemberSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.player.PlayerDataManager;
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
            NwTeam playerNwTeam = TeamDataManager.getTeamDataManager().getTeamOfPlayer(player);

            if (playerNwTeam == null) {
                sender.sendMessage("Your team is null");
                return;
            }

            TeamRank playerRank = playerNwTeam.getMemberMap().get(player).getRank();

            if (!hasSenderTeamRank(((Player) sender).getPlayer(), playerNwTeam, TeamRank.LIEUTENANT)) {
                return;
            }

            if (args.length < 5) {
                sender.sendMessage("not enough arguments");
                return;
            }

            targetName = args[4];

            if (player.getName().equals(targetName)) {
                sender.sendMessage("you can't promote yourself !");
                return;
            }

            targetTeamMemberModel = playerNwTeam.getModel().getTeamMemberModelMap().values().stream()
                    .filter(teamMemberModel -> teamMemberModel.getUsername().equalsIgnoreCase(targetName)).findFirst().orElse(null);

            if (targetTeamMemberModel == null) {
                sender.sendMessage("the player is not in your team.");
                return;
            }

            newRank = targetTeamMemberModel.getRank() + 1;
            if (newRank >= playerRank.getWeight()) {
                sender.sendMessage("You cannot promote this player.");
                return;
            }

            targetTeamMemberModel.setRank(newRank);
            StorageManager.getManager().updateTeamMemberModel(targetTeamMemberModel);

            targetPlayer = Bukkit.getPlayer(targetName);
            if (targetPlayer != null) {
                targetTeamMember = playerNwTeam.getMemberMap().get(targetPlayer);
                targetTeamMember.setRank(Arrays.stream(TeamRank.values()).filter(teamRank -> teamRank.getWeight() == newRank).findFirst().get());
            }

            sender.sendMessage(
                    AdaptMessage.getAdaptMessage().adaptTeamMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_MEMBER_PROMOTE_RESULT), playerNwTeam, player)
            );

        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        NwTeam playerNwTeam = TeamDataManager.getTeamDataManager().getTeamOfPlayer(sender);
        if (playerNwTeam != null) {
            return playerNwTeam.getModel().getTeamMemberModelMap().values().stream().map(TeamMemberModel::getUsername).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
