package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.relation.teammanagerelationsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.relation.TeamManageRelationSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.TeamIRelation;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.team.TeamManager;
import fr.rosstail.nodewar.team.rank.NwTeamRank;
import fr.rosstail.nodewar.team.relation.NwTeamRelationRequest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class TeamManageRelationAcceptCommand extends TeamManageRelationSubCommand {

    public TeamManageRelationAcceptCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_ACCEPT_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "accept";
    }

    @Override
    public String getDescription() {
        return "Desc relation nodewar team";
    }

    @Override
    public String getSyntax() {
        return "nodewar team manage relation accept <team>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.manage.relation.accept";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String senderteamName;
        NwITeam playerNwITeam;
        NwITeam senderNwITeam;

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_BY_PLAYER_ONLY));
            return;
        }

        RelationType relationType;
        if (args.length < 5) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TOO_FEW_ARGUMENTS));
            return;
        }

        senderteamName = args[4];
        playerNwITeam = TeamManager.getManager().getPlayerTeam(((Player) sender));
        senderNwITeam = TeamManager.getManager().getStringTeamMap().get(senderteamName);

        if (playerNwITeam == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_PART_OF_NO_TEAM));
            return;
        }
        if (!hasPlayerEnoughClearance(((Player) sender).getPlayer(), playerNwITeam, NwTeamRank.OWNER)) {
            return;
        }
        if (senderNwITeam == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_VALUE).replaceAll("\\[value]", senderteamName));
        }

        NwTeamRelationRequest teamRelationRequest = TeamManager.getManager().getTeamRelationRequest(senderNwITeam, playerNwITeam);

        if (teamRelationRequest == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_ACCEPT_NONE));
            return;
        }

        TeamIRelation currentIRelation = TeamManager.getManager().getTeamRelation(senderNwITeam, playerNwITeam);
        TeamManager.getManager().deleyeRelationRequest(senderNwITeam, playerNwITeam);
        StorageManager.getManager().deleteTeamRelationModel(currentIRelation.getID());
        playerNwITeam.getRelations().remove(senderNwITeam);
        senderNwITeam.getRelations().remove(playerNwITeam);
        createNewRelation(senderNwITeam, playerNwITeam, teamRelationRequest.getRelationType());
        sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_REQUEST_RESULT_EFFECTIVE));

    }

    private void createNewRelation(NwITeam senderTeam, NwITeam targetTeam, RelationType newRelationType) {
        TeamManager.getManager().createRelation(senderTeam, targetTeam, newRelationType);
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        NwITeam playerNwITeam = TeamManager.getManager().getPlayerTeam(sender);
        if (args.length <= 5) {
            return TeamManager.getManager().getTeamRelationRequestSet().stream().filter(nwTeamRelationInvite -> (nwTeamRelationInvite.getTargetTeam() == playerNwITeam)).map(nwTeamRelationInvite -> nwTeamRelationInvite.getSenderTeam().getName()).collect(Collectors.toList());
        }
        return null;
    }
}
