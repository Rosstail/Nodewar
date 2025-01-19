package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.relation.teammanagerelationsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.relation.TeamManageRelationSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.TeamManager;
import fr.rosstail.nodewar.team.rank.NwTeamRank;
import fr.rosstail.nodewar.team.relation.NwTeamRelationRequest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TeamManageRelationInvitesCommand extends TeamManageRelationSubCommand {

    public TeamManageRelationInvitesCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_INVITES_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "invites";
    }

    @Override
    public String getDescription() {
        return "Desc relation nodewar team";
    }

    @Override
    public String getSyntax() {
        return "nodewar team manage relation invites";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.manage.relation.invites";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        Player senderPlayer;
        NwITeam playerNwITeam;
        StringBuilder message = new StringBuilder(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_INVITES_RESULT_HEADER));
        String receivedInvitationLine = LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_INVITES_RESULT_LINE_RECEIVED);
        String sentInvitationLine = LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_INVITES_RESULT_LINE_SENT);

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_BY_PLAYER_ONLY));
            return;
        }
        senderPlayer = ((Player) sender).getPlayer();
        playerNwITeam = TeamManager.getManager().getPlayerTeam(senderPlayer);

        if (playerNwITeam == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_PART_OF_NO_TEAM));
            return;
        }
        if (!hasPlayerEnoughClearance(((Player) sender).getPlayer(), playerNwITeam, NwTeamRank.CAPTAIN)) {
        }

        Set<NwTeamRelationRequest> teamRelationInviteSet = TeamManager.getManager().getTeamRelationRequestSet().stream().filter(teamRelation -> (
                teamRelation.getSenderTeam() == playerNwITeam || teamRelation.getTargetTeam() == playerNwITeam
        )).collect(Collectors.toSet());


        teamRelationInviteSet.forEach(invite -> {
            message.append("\n").append(AdaptMessage.getAdaptMessage().adaptTeamMessage(
                    LangManager.getMessage(
                                    LangMessage.COMMANDS_TEAM_MANAGE_RELATION_INVITES_RESULT_LINE
                            ).replaceAll("\\[team_line_direction]", invite.getSenderTeam() == playerNwITeam ? sentInvitationLine : receivedInvitationLine)
                            .replaceAll("\\[team_relation]",
                                    invite.getSenderTeam().getIRelation(invite.getTargetTeam()).getType().getDisplay()
                                            .replaceAll("\\[team_relation_invite]", invite.getRelationType().name())
                            ), invite.getSenderTeam() == playerNwITeam ? invite.getTargetTeam() : invite.getSenderTeam()));
        });

        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(message.toString()));
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        return null;
    }
}
