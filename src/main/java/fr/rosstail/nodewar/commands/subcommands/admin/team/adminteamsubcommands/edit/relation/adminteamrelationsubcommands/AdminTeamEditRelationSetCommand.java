package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.relation.adminteamrelationsubcommands;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.relation.AdminTeamEditRelationSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.relation.TeamRelation;
import fr.rosstail.nodewar.team.relation.TeamRelationManager;
import fr.rosstail.nodewar.team.relation.TeamRelationModel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminTeamEditRelationSetCommand extends AdminTeamEditRelationSubCommand {

    public AdminTeamEditRelationSetCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_RELATION_SET_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "set";
    }

    @Override
    public String getDescription() {
        return "Desc relation nodewar team";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin team edit <team> relation set <team2> <relation>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String baseTeamName;
        String targetTeamName;
        NwTeam baseTeam;
        NwTeam targetTeam;

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        RelationType relationType;
        if (args.length < 8) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TOO_FEW_ARGUMENTS));
            return;
        }

        baseTeamName = args[3];
        targetTeamName = args[6];
        baseTeam = TeamDataManager.getTeamDataManager().getStringTeamMap().get(baseTeamName);
        targetTeam = TeamDataManager.getTeamDataManager().getStringTeamMap().get(targetTeamName);

        if (baseTeam == null) {
            sender.sendMessage("base team does not exist");
            return;
        }

        if (targetTeam == null) {
            sender.sendMessage("target team does not exist");
            return;
        }

        try {
            relationType = RelationType.valueOf(args[7].toUpperCase());
            if (!RelationType.getSelectableRelations().contains(relationType)) {
                sender.sendMessage("this relation type is not selectable: " + args[7].toUpperCase());
                return;
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage("this relation type does not exist: " + args[7].toUpperCase());
            return;
        }

        if (targetTeam.equals(baseTeam)) {
            sender.sendMessage("Teams are the same.");
            return;
        }

        handleRelationChange(sender, baseTeam, targetTeam, relationType, baseTeam.getRelations().get(targetTeam.getModel().getName()));
    }

    private void handleRelationChange(CommandSender sender, NwTeam baseTeam, NwTeam targetTeam, RelationType newRelationType, TeamRelation currentRelation) {
        int defaultRelationWeight = ConfigData.getConfigData().team.defaultRelation.getWeight();

        if (currentRelation == null) { // implicit default relation
            if (newRelationType.getWeight() != defaultRelationWeight) {
                createNewRelation(baseTeam, targetTeam, newRelationType);
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_INVITE_RESULT_EFFECTIVE));
            } else {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_INVITE_RESULT_UNCHANGED));
            }
        } else { // explicit relation
            if (newRelationType.getWeight() != currentRelation.getRelationType().getWeight()) {
                StorageManager.getManager().deleteTeamRelationModel(currentRelation.getModel().getId());
                baseTeam.getRelations().remove(targetTeam.getModel().getName());
                targetTeam.getRelations().remove(baseTeam.getModel().getName());

                if (newRelationType.getWeight() != defaultRelationWeight) {
                    createNewRelation(baseTeam, targetTeam, newRelationType);
                }
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_INVITE_RESULT_EFFECTIVE));
            } else {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_INVITE_RESULT_UNCHANGED));
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
        NwTeam nwTeam = TeamDataManager.getTeamDataManager().getStringTeamMap().get(args[2]);
        if (args.length <= 7) {
            List<String> teams = new ArrayList<>(TeamDataManager.getTeamDataManager().getStringTeamMap().keySet());
            if (nwTeam != null) {
                teams.remove(nwTeam.getModel().getName());
            }
            return teams;
        } else if (args.length == 8) {
            List<String> relations = new ArrayList<>();

            RelationType.getSelectableRelations().forEach(relationType -> {
                relations.add(relationType.toString().toLowerCase());
            });

            return relations;
        }
        return null;
    }
}
