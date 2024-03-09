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

public class TeamManageMemberDemoteCommand extends TeamManageMemberSubCommand {

    public TeamManageMemberDemoteCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_MEMBER_DEMOTE_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "demote";
    }

    @Override
    public String getDescription() {
        return "Kick a member of your team";
    }

    @Override
    public String getSyntax() {
        return "nodewar team manage member demote <player>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.manage.member.demote";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String targetName;
        Player targetPlayer;
        TeamMember targetTeamMember = null;
        TeamMemberModel targetTeamMemberModel;
        String targetUUID;

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

            if (playerRank.getWeight() < TeamRank.LIEUTENANT.getWeight()) {
                sender.sendMessage("you do not have enough rank on your team");
                return;
            }

            if (args.length < 5) {
                sender.sendMessage("not enough arguments");
                return;
            }

            targetName = args[4];

            if (player.getName().equals(targetName)) {
                sender.sendMessage("you can't demote yourself !");
                return;
            }

            targetPlayer = Bukkit.getPlayer(targetName);

            if (targetPlayer != null) {
                targetTeamMember = playerNwTeam.getMemberMap().get(targetPlayer);
                if (targetTeamMember == null) {
                    sender.sendMessage("This player is not in your team");
                    return;
                }
                targetTeamMemberModel = targetTeamMember.getModel();
            } else {
                targetUUID = PlayerDataManager.getPlayerUUIDFromName(targetName);
                targetTeamMemberModel = StorageManager.getManager().selectTeamMemberModelByUUID(targetUUID);

                if (targetTeamMemberModel == null) {
                    sender.sendMessage("This player is not in your team");
                    return;
                }
            }

            if (targetTeamMemberModel.getRank() >= playerRank.getWeight() || targetTeamMemberModel.getRank() == 1) {
                sender.sendMessage("You cannot demote this player anymore.");
                return;
            }

            targetTeamMemberModel.setRank(targetTeamMemberModel.getRank() - 1);
            StorageManager.getManager().updateTeamMemberModel(targetTeamMemberModel);

            if (targetTeamMember != null) {
                TeamMember finalTargetTeamMember = targetTeamMember;
                targetTeamMember.setRank(Arrays.stream(TeamRank.values()).filter(teamRank -> teamRank.getWeight() == finalTargetTeamMember.getRank().getWeight() + 1).findFirst().get());
            }

            sender.sendMessage(
                    AdaptMessage.getAdaptMessage().adaptTeamMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_MEMBER_DEMOTE_RESULT), playerNwTeam, player)
            );

        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        NwTeam playerNwTeam = TeamDataManager.getTeamDataManager().getTeamOfPlayer(sender);
        List<String> memberStringList = new ArrayList<>();

        if (playerNwTeam != null) {
            memberStringList.addAll(StorageManager.getManager().selectAllTeamMemberModel(playerNwTeam.getModel().getName()).keySet());
        }

        return memberStringList;
    }
}
