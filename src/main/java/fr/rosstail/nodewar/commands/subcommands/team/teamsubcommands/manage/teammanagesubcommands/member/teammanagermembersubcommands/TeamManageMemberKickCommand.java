package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.member.teammanagermembersubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.TeamManageSubCommand;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.member.TeamManageMemberSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.rank.TeamRank;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TeamManageMemberKickCommand extends TeamManageMemberSubCommand {

    public TeamManageMemberKickCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_MEMBER_KICK_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "kick";
    }

    @Override
    public String getDescription() {
        return "Kick a member of your team";
    }

    @Override
    public String getSyntax() {
        return "nodewar team manage member kick <player>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.manage.member.kick";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String targetName;
        Player targetPlayer;
        PlayerData targetData;
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

            if (!hasSenderTeamRank(((Player) sender).getPlayer(), playerNwTeam, TeamRank.LIEUTENANT)) {
                return;
            }

            if (args.length < 5) {
                sender.sendMessage("not enough arguments");
                return;
            }

            targetName = args[4];

            if (player.getName().equals(targetName)) {
                sender.sendMessage("you can't kick yourself !");
                return;
            }

            if (playerNwTeam.getModel().getTeamMemberModelMap().values().stream()
                    .noneMatch(teamMemberModel -> teamMemberModel.getUsername().equalsIgnoreCase(targetName))) {
                sender.sendMessage("the player is not in your team.");
                return;
            }

            StorageManager.getManager().deleteTeamMemberModel(playerNwTeam.getModel().getId());

            targetPlayer = Bukkit.getPlayer(targetName);
            if (targetPlayer != null) {
                playerNwTeam.getMemberMap().remove(targetPlayer);
                targetData = PlayerDataManager.getPlayerDataFromMap(targetPlayer);
                targetData.removeTeam();
            }

            sender.sendMessage(
                    AdaptMessage.getAdaptMessage().adaptTeamMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_MEMBER_KICK_RESULT), playerNwTeam, player)
            );

            StorageManager.getManager().updateTeamModel(playerNwTeam.getModel());
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
