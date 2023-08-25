package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.relation.teammanagerelationsubcommands;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.relation.TeamManageRelationSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.team.TeamIRelation;
import fr.rosstail.nodewar.team.TeamManager;
import fr.rosstail.nodewar.team.rank.NwTeamRank;
import fr.rosstail.nodewar.team.relation.NwTeamRelationRequest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TeamManageRelationRequestCommand extends TeamManageRelationSubCommand {

    public TeamManageRelationRequestCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE).replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_REQUEST_DESC)).replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "request";
    }

    @Override
    public String getDescription() {
        return "Desc relation nodewar team";
    }

    @Override
    public String getSyntax() {
        return "nodewar team manage relation request <team> <relation>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.manage.relation.request";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String targetTeamName;
        NwITeam playerNwTeam;
        NwITeam targetNwTeam;

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_BY_PLAYER_ONLY));
            return;
        }

        RelationType relationType;
        if (args.length < 6) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TOO_FEW_ARGUMENTS));
            return;
        }

        try {
            relationType = RelationType.valueOf(args[4].toUpperCase());
            if (!RelationType.getSelectableRelations().contains(relationType)) {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_VALUE).replaceAll("\\[value]", args[4].toUpperCase()));
                return;
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_VALUE).replaceAll("\\[value]", args[4].toUpperCase()));
            return;
        }

        targetTeamName = args[5];
        playerNwTeam = TeamManager.getManager().getPlayerTeam(((Player) sender));
        targetNwTeam = TeamManager.getManager().getStringTeamMap().get(targetTeamName);

        if (playerNwTeam == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_PART_OF_NO_TEAM));
            return;
        }
        if (!hasPlayerEnoughClearance(((Player) sender).getPlayer(), playerNwTeam, NwTeamRank.OWNER)) {
            return;
        }
        if (targetNwTeam == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_VALUE).replaceAll("\\[value]", targetTeamName));
            return;
        }
        if (targetNwTeam.equals(playerNwTeam)) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_REQUEST_RESULT_SAME_TEAM));
            return;
        }

        handleRelationChange(sender, playerNwTeam, targetNwTeam, relationType, playerNwTeam.getRelations().get(targetNwTeam));
    }

    private void handleRelationChange(CommandSender sender, NwITeam senderTeam, NwITeam targetTeam, RelationType newRelationType, TeamIRelation currentRelation) {
        int defaultRelationWeight = ConfigData.getConfigData().team.defaultRelation.getWeight();
        /* TODO
        NwTeamRelationRequest teamRelationInvite = TeamRelationManager.getRelationRequestHashSet().stream().filter(nwTeamRelationInvite -> nwTeamRelationInvite.getTargetTeam() == targetTeam).findFirst().orElse(null);

        if (currentRelation == null) { // implicit default relation
            if (newRelationType.getWeight() > defaultRelationWeight) {
                createNewRelation(senderTeam, targetTeam, newRelationType);
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_REQUEST_RESULT_EFFECTIVE));
            } else if (newRelationType.getWeight() < defaultRelationWeight) {
                inviteOrAccept(sender, senderTeam, targetTeam, newRelationType, teamRelationInvite);
            } else {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_REQUEST_RESULT_UNCHANGED));
            }
        } else { // explicit relation
            if (newRelationType.getWeight() != currentRelation.getType().getWeight()) {
                if (newRelationType.getWeight() > currentRelation.getType().getWeight()) {
                    StorageManager.getManager().deleteTeamRelationModel(currentRelation.getID());
                    senderTeam.getRelations().remove(targetTeam);
                    targetTeam.getRelations().remove(senderTeam);

                    createNewRelation(senderTeam, targetTeam, newRelationType);
                    sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_REQUEST_RESULT_EFFECTIVE));
                } else {
                    inviteOrAccept(sender, senderTeam, targetTeam, newRelationType, teamRelationInvite);
                }
            } else {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_REQUEST_RESULT_UNCHANGED));
            }
        }

         */
    }

    private void inviteOrAccept(CommandSender sender, NwITeam senderTeam, NwITeam targetTeam, RelationType newRelationType, NwTeamRelationRequest teamRelationInvite) {
        if (teamRelationInvite != null) {
            if (senderTeam == teamRelationInvite.getTargetTeam()) {
                createNewRelation(teamRelationInvite.getSenderTeam(), teamRelationInvite.getTargetTeam(), teamRelationInvite.getRelationType());
            } else {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_REQUEST_RESULT_ALREADY_SENT));
            }
        } else {
            if (!targetTeam.isOpenRelation()) {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_REQUEST_RESULT_BLOCKED));
                return;
            }
            teamRelationInvite = new NwTeamRelationRequest(senderTeam, targetTeam, newRelationType);
            //TODO
            // TeamRelationManager.getRelationRequestHashSet().add(teamRelationInvite);
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_REQUEST_RESULT_SENT));
        }
    }

    private void createNewRelation(NwITeam senderTeam, NwITeam targetTeam, RelationType newRelationType) {
        TeamManager.getManager().createRelation(senderTeam, targetTeam, newRelationType);
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        NwITeam playerNwTeam = TeamManager.getManager().getPlayerTeam(sender);
        if (args.length <= 5) {
            List<String> relations = new ArrayList<>();

            RelationType.getSelectableRelations().forEach(relationType -> {
                relations.add(relationType.toString().toLowerCase());
            });

            return relations;
        } else if (args.length == 6) {
            List<String> teams = new ArrayList<>(TeamManager.getManager().getStringTeamMap().keySet());
            if (playerNwTeam != null) {
                teams.remove(playerNwTeam.getName());
            }
            return teams;
        }
        return null;
    }
}
