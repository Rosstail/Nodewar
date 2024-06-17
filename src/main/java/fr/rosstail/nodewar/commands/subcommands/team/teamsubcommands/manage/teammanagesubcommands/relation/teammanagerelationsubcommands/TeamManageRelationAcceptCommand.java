package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.relation.teammanagerelationsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.relation.TeamManageRelationSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.rank.TeamRank;
import fr.rosstail.nodewar.team.relation.NwTeamRelationRequest;
import fr.rosstail.nodewar.team.relation.TeamRelation;
import fr.rosstail.nodewar.team.relation.TeamRelationManager;
import fr.rosstail.nodewar.team.relation.TeamRelationModel;
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
        NwTeam playerNwTeam;
        NwTeam senderNwTeam;

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
        playerNwTeam = TeamDataManager.getTeamDataManager().getPlayerTeam(((Player) sender));
        senderNwTeam = TeamDataManager.getTeamDataManager().getStringTeamMap().get(senderteamName);

        if (playerNwTeam == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_PART_OF_NO_TEAM));
            return;
        }
        if (!hasSenderTeamRank(((Player) sender).getPlayer(), playerNwTeam, TeamRank.OWNER)) {
            return;
        }
        if (senderNwTeam == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_VALUE).replaceAll("\\[value]", senderteamName));
            return;
        }

        NwTeamRelationRequest teamRelationInvite = TeamRelationManager.getRelationRequestHashSet().stream().filter(nwTeamRelationInvite -> (nwTeamRelationInvite.getSenderTeam() == senderNwTeam && nwTeamRelationInvite.getTargetTeam() == playerNwTeam)).findFirst().orElse(null);

        if (teamRelationInvite == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_ACCEPT_NONE));
            return;
        }

        TeamRelation currentRelation = TeamRelationManager.getTeamRelationManager().getRelationBetweenTeams(senderNwTeam, playerNwTeam);
        TeamRelationManager.getRelationRequestHashSet().remove(teamRelationInvite);
        StorageManager.getManager().deleteTeamRelationModel(currentRelation.getModel().getId());
        playerNwTeam.getRelations().remove(senderNwTeam.getModel().getName());
        senderNwTeam.getRelations().remove(playerNwTeam.getModel().getName());
        createNewRelation(senderNwTeam, playerNwTeam, teamRelationInvite.getRelationType());
        sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_REQUEST_RESULT_EFFECTIVE));
    }

    private void createNewRelation(NwTeam senderTeam, NwTeam targetTeam, RelationType newRelationType) {
        TeamRelationModel teamRelationModel = new TeamRelationModel(senderTeam.getModel().getId(), targetTeam.getModel().getId(), newRelationType.getWeight());
        StorageManager.getManager().insertTeamRelationModel(teamRelationModel);
        TeamRelation newTeamRelation = new TeamRelation(senderTeam, targetTeam, newRelationType, teamRelationModel);

        TeamRelationManager.getRelationArrayList().add(newTeamRelation);
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        NwTeam playerNwTeam = TeamDataManager.getTeamDataManager().getPlayerTeam(sender);
        if (args.length <= 5) {
            return TeamRelationManager.getRelationRequestHashSet().stream().filter(nwTeamRelationInvite -> (nwTeamRelationInvite.getTargetTeam() == playerNwTeam)).map(nwTeamRelationInvite -> nwTeamRelationInvite.getSenderTeam().getModel().getName()).collect(Collectors.toList());
        }
        return null;
    }
}
