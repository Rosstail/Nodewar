package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.relations.teammanagerelationssubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.relations.TeamManageRelationSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.rank.TeamRank;
import fr.rosstail.nodewar.team.relation.NwTeamRelationInvite;
import fr.rosstail.nodewar.team.relation.TeamRelationManager;
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
        Player player;
        NwTeam playerNwTeam;
        StringBuilder message = new StringBuilder(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_INVITES_RESULT_HEADER));
        String receivedInvitationLine = LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_INVITES_RESULT_LINE_RECEIVED);
        String sentInvitationLine = LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_INVITES_RESULT_LINE_SENT);

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("player only");
            return;
        }
        player = ((Player) sender).getPlayer();
        playerNwTeam = TeamDataManager.getTeamDataManager().getTeamOfPlayer(((Player) sender));

        if (playerNwTeam == null) {
            sender.sendMessage("you are part of no team");
            return;
        }
        if (playerNwTeam.getMemberMap().get(player).getRank().getWeight() < TeamRank.CAPTAIN.getWeight()) {
            sender.sendMessage("you are not the high ranked enough");
            return;
        }

        Set<NwTeamRelationInvite> teamRelationInviteSet = TeamRelationManager.getRelationInvitesHashSet().stream().filter(teamRelationModel -> (
                teamRelationModel.getSenderTeam() == playerNwTeam || teamRelationModel.getTargetTeam() == playerNwTeam
        )).collect(Collectors.toSet());


        teamRelationInviteSet.forEach(invite -> {
            message.append("\n").append(AdaptMessage.getAdaptMessage().adaptTeamMessage(
                    LangManager.getMessage(
                                    LangMessage.COMMANDS_TEAM_MANAGE_RELATION_INVITES_RESULT_LINE
                            ).replaceAll("\\[team_line_direction]", invite.getSenderTeam() == playerNwTeam ? sentInvitationLine : receivedInvitationLine)
                            .replaceAll("\\[team_relation]",
                                    TeamRelationManager.getTeamRelationManager().getRelationBetweenTeams(invite.getSenderTeam(), invite.getTargetTeam()).name())
                            .replaceAll("\\[team_relation_invite]", invite.getRelationType().name())
                    , invite.getSenderTeam() == playerNwTeam ? invite.getTargetTeam() : invite.getSenderTeam()));
        });
        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(message.toString()));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
