package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.relations.teammanagerelationssubcommands;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.TeamManageSubCommand;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.relations.TeamManageRelationSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.rank.TeamRank;
import fr.rosstail.nodewar.team.relation.NwTeamRelationInvite;
import fr.rosstail.nodewar.team.relation.TeamRelation;
import fr.rosstail.nodewar.team.relation.TeamRelationManager;
import fr.rosstail.nodewar.team.relation.TeamRelationModel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TeamManageRelationEditCommand extends TeamManageRelationSubCommand {

    public TeamManageRelationEditCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_EDIT_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "edit";
    }

    @Override
    public String getDescription() {
        return "Desc relation nodewar team";
    }

    @Override
    public String getSyntax() {
        return "nodewar team manage relation edit <teamname> <relation>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.manage.relation.edit";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String targetTeamName;
        NwTeam playerNwTeam;
        NwTeam targetNwTeam;

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("player only");
            return;
        }

        RelationType relationType;
        if (args.length < 6) {
            sender.sendMessage("not enough args: <relation> <team>");
            return;
        }

        try {
            relationType = RelationType.valueOf(args[4].toUpperCase());
            if (!RelationType.getSelectableRelations().contains(relationType)) {
                sender.sendMessage("this relation type is not selectable: " + args[4].toUpperCase());
                return;
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage("this relation type does not exist: " + args[4].toUpperCase());
            return;
        }

        targetTeamName = args[5];
        playerNwTeam = TeamDataManager.getTeamDataManager().getTeamOfPlayer(((Player) sender));
        targetNwTeam = TeamDataManager.getTeamDataManager().getStringTeamMap().get(targetTeamName);

        if (playerNwTeam == null) {
            sender.sendMessage("You are part of no team");
            return;
        }
        if (playerNwTeam.getMemberMap().get(((Player) sender)).getRank() != TeamRank.OWNER) {
            sender.sendMessage("You are not the owner of the team");
            return;
        }
        if (targetNwTeam == null) {
            sender.sendMessage("This team does not exist " + targetTeamName);
            return;
        }
        if (targetNwTeam.equals(playerNwTeam)) {
            sender.sendMessage("You cannot edit relation with your own team");
            return;
        }

        handleRelationChange(playerNwTeam, targetNwTeam, relationType, playerNwTeam.getRelations().get(targetNwTeam.getModel().getName()));
    }

    private void handleRelationChange(NwTeam senderTeam, NwTeam targetTeam, RelationType newRelationType, TeamRelation currentRelation) {
        int defaultRelationWeight = ConfigData.getConfigData().team.defaultRelation.getWeight();
        NwTeamRelationInvite teamRelationInvite = TeamRelationManager.getRelationInvitesHashSet().stream().filter(nwTeamRelationInvite -> nwTeamRelationInvite.getTargetTeam() == targetTeam).findFirst().orElse(null);

        if (currentRelation == null) { // implicit default relation
            if (newRelationType.getWeight() > defaultRelationWeight) {
                createNewRelation(senderTeam, targetTeam, newRelationType);
                System.out.println("set immediate relation");
            } else if (newRelationType.getWeight() < defaultRelationWeight) {
                inviteOrAccept(senderTeam, targetTeam, newRelationType, teamRelationInvite);
            } else {
                System.out.println("same relation. No changes.");
            }
        } else { // explicit relation
            if (newRelationType.getWeight() != currentRelation.getRelationType().getWeight()) {
                if (newRelationType.getWeight() > currentRelation.getRelationType().getWeight()) {
                    StorageManager.getManager().deleteTeamRelationModel(currentRelation.getModel().getId());
                    senderTeam.getRelations().remove(targetTeam.getModel().getName());
                    targetTeam.getRelations().remove(senderTeam.getModel().getName());

                    createNewRelation(senderTeam, targetTeam, newRelationType);
                    System.out.println("set immediate relation");
                } else {
                    inviteOrAccept(senderTeam, targetTeam, newRelationType, teamRelationInvite);
                }
            } else {
                System.out.println("same relation. No changes.");
            }
        }
    }

    private void inviteOrAccept(NwTeam senderTeam, NwTeam targetTeam, RelationType newRelationType, NwTeamRelationInvite teamRelationInvite) {
        if (teamRelationInvite != null) {
            if (senderTeam == teamRelationInvite.getTargetTeam()) {
                createNewRelation(teamRelationInvite.getSenderTeam(), teamRelationInvite.getTargetTeam(), teamRelationInvite.getRelationType());
            } else {
                System.out.println("already sent the invitation.");
            }
        } else {
            if (!targetTeam.getModel().isOpenRelation()) {
                System.out.println("team is closed to relation invites");
                return;
            }
            teamRelationInvite = new NwTeamRelationInvite(senderTeam, targetTeam, newRelationType);
            TeamRelationManager.getRelationInvitesHashSet().add(teamRelationInvite);
            System.out.println("Ask other team to relation.");
        }
    }

    private void createNewRelation(NwTeam senderTeam, NwTeam targetTeam, RelationType newRelationType) {
        TeamRelationModel teamRelationModel = new TeamRelationModel(senderTeam.getModel().getId(), targetTeam.getModel().getId(), newRelationType.getWeight());
        StorageManager.getManager().insertTeamRelationModel(teamRelationModel);
        TeamRelation newTeamRelation = new TeamRelation(senderTeam, targetTeam, newRelationType, teamRelationModel);

        TeamRelationManager.getRelationArrayList().add(newTeamRelation);
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        NwTeam playerNwTeam = TeamDataManager.getTeamDataManager().getTeamOfPlayer(sender);
        if (args.length <= 5) {
            List<String> relations = new ArrayList<>();

            RelationType.getSelectableRelations().forEach(relationType -> {
                relations.add(relationType.toString().toLowerCase());
            });

            return relations;
        } else if (args.length == 6) {
            List<String> teams = new ArrayList<>(TeamDataManager.getTeamDataManager().getStringTeamMap().keySet());
            if (playerNwTeam != null) {
                teams.remove(playerNwTeam.getModel().getName());
            }
            return teams;
        }
        return null;
    }
}
