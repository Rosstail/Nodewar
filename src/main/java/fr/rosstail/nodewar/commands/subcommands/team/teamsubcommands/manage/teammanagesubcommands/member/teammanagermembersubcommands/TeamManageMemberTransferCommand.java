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
        return "nodewar team manage member transfer <player> <teamname>";
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
        Player targetPlayer;
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

            if (!hasSenderTeamRank(((Player) sender).getPlayer(), playerNwTeam, TeamRank.OWNER)) {
                return;
            }

            if (args.length < 5) {
                sender.sendMessage("not enough arguments");
                return;
            }

            targetName = args[4];

            if (player.getName().equals(targetName)) {
                sender.sendMessage("you are already owner of " + playerNwTeam.getModel().getDisplay());
                return;
            }

            if (playerNwTeam.getModel().getTeamMemberModelMap().values().stream()
                    .noneMatch(teamMemberModel -> teamMemberModel.getUsername().equalsIgnoreCase(targetName))) {
                sender.sendMessage("the player is not in your team.");
                return;
            }

            if (args.length < 6) {
                sender.sendMessage("Add the team name to the command to confirm");
                return;
            }

            teamNameConfirmStr = args[5];

            if (!playerNwTeam.getModel().getName().equalsIgnoreCase(teamNameConfirmStr)) {
                sender.sendMessage("Wrong team name");
                return;
            }


            playerNwTeam.getModel().getTeamMemberModelMap();

            targetPlayer = Bukkit.getPlayer(targetName);
            if (targetPlayer != null) {
                playerNwTeam.getMemberMap().get(targetPlayer).setRank(TeamRank.OWNER);
            }

            sender.sendMessage(
                    AdaptMessage.getAdaptMessage().adaptTeamMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_MEMBER_TRANSFER_RESULT), playerNwTeam, player)
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
