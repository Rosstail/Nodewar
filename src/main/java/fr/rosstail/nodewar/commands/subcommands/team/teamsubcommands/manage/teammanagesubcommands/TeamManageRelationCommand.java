package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.TeamManageSubCommand;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.storage.storagetype.SqlStorageRequest;
import fr.rosstail.nodewar.team.*;
import fr.rosstail.nodewar.team.rank.TeamRank;
import fr.rosstail.nodewar.team.relation.TeamRelation;
import fr.rosstail.nodewar.team.relation.TeamRelationManager;
import fr.rosstail.nodewar.team.relation.TeamRelationModel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TeamManageRelationCommand extends TeamManageSubCommand {

    @Override
    public String getName() {
        return "relation";
    }

    @Override
    public String getDescription() {
        return "Desc relation nodewar team";
    }

    @Override
    public String getSyntax() {
        return "nodewar team manage relation <teamname> <relation>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.manage.relation";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String targetTeamName;
        NwTeam playerNwTeam;
        NwTeam targetNwTeam;
        RelationType defaultRelation = ConfigData.getConfigData().team.defaultRelation;
        TeamRelation playerTeamRelation;

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("player only");
            return;
        }

        RelationType relationType;
        if (args.length < 5) {
            sender.sendMessage("not enough args: <relation> <team>");
            return;
        }

        try {
            relationType = RelationType.valueOf(args[3].toUpperCase());
            if (!RelationType.getSelectableRelations().contains(relationType)) {
                sender.sendMessage("this relation type is not selectable: " + args[3].toUpperCase());
                return;
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage("this relation type does not exist: " + args[3].toUpperCase());
            return;
        }

        targetTeamName = args[4];
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

        if (currentRelation == null) { // implicit default relation
            if (newRelationType.getWeight() > defaultRelationWeight) {
                createNewRelation(senderTeam, targetTeam, newRelationType);
                System.out.println("set immediate relation");
            } else if (newRelationType.getWeight() < defaultRelationWeight) {
                System.out.println("Ask other team to relation.");
            } else {
                System.out.println("same relation. No changes.");
            }
        } else { // explicit relation
            if (newRelationType.getWeight() > currentRelation.getRelationType().getWeight()) {
                StorageManager.getManager().deleteTeamRelationModel(currentRelation.getModel().getId());
                senderTeam.getRelations().remove(targetTeam.getModel().getName());
                targetTeam.getRelations().remove(senderTeam.getModel().getName());

                createNewRelation(senderTeam, targetTeam, newRelationType);
                System.out.println("set immediate relation");
            } else if (newRelationType.getWeight() < currentRelation.getRelationType().getWeight()) {
                System.out.println("Ask other team to relation.");
            } else {
                TeamRelationModel teamRelationModelInvite = new TeamRelationModel(senderTeam.getModel().getId(), targetTeam.getModel().getId(), newRelationType.getWeight());
                TeamRelationManager.getRelationInvitesHashSet().add(teamRelationModelInvite);
                System.out.println("same relation. No changes.");
            }
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
        if (args.length <= 4) {
            List<String> relations = new ArrayList<>();

            RelationType.getSelectableRelations().forEach(relationType -> {
                relations.add(relationType.toString().toLowerCase());
            });

            return relations;
        } else if (args.length == 5) {
            List<String> teams = new ArrayList<>(TeamDataManager.getTeamDataManager().getStringTeamMap().keySet());
            if (playerNwTeam != null) {
                teams.remove(playerNwTeam.getModel().getName());
            }
            return teams;
        }
        return null;
    }
}
