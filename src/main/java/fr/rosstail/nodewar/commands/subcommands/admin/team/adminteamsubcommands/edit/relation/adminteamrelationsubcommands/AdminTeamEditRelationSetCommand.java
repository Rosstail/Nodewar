package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.relation.adminteamrelationsubcommands;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.relation.AdminTeamEditRelationSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.TeamIRelation;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.team.TeamManager;
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
        NwITeam baseTeam;
        NwITeam targetTeam;

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
        baseTeam = TeamManager.getManager().getStringTeamMap().get(baseTeamName);
        targetTeam = TeamManager.getManager().getStringTeamMap().get(targetTeamName);

        if (baseTeam == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_VALUE).replaceAll("\\[value]", baseTeamName));
            return;
        }

        if (targetTeam == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_VALUE).replaceAll("\\[value]", targetTeamName));
            return;
        }

        try {
            relationType = RelationType.valueOf(args[7].toUpperCase());
            if (!RelationType.getSelectableRelations().contains(relationType)) {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_VALUE).replaceAll("\\[value]", args[7].toUpperCase()));
                return;
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_VALUE).replaceAll("\\[value]", args[7].toUpperCase()));
            return;
        }

        if (targetTeam.equals(baseTeam)) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_REQUEST_RESULT_SAME_TEAM));
            return;
        }

        handleRelationChange(sender, baseTeam, targetTeam, relationType, baseTeam.getIRelation(targetTeam));
    }

    private void handleRelationChange(CommandSender sender, NwITeam baseTeam, NwITeam targetTeam, RelationType newRelationType, TeamIRelation currentRelation) {
        int defaultRelationWeight = ConfigData.getConfigData().team.defaultRelation.getWeight();

        if (currentRelation == null) { // implicit default relation
            if (newRelationType.getWeight() != defaultRelationWeight) {
                createNewRelation(baseTeam, targetTeam, newRelationType);
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_RELATION_SET_RESULT));
            } else {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_MANAGE_RELATION_SET_RESULT_UNCHANGED));
            }
        } else { // explicit relation
            if (newRelationType.getWeight() != currentRelation.getType().getWeight()) {
                StorageManager.getManager().deleteTeamRelationModel(currentRelation.getID());
                baseTeam.getRelations().remove(targetTeam);
                targetTeam.getRelations().remove(baseTeam);

                if (newRelationType.getWeight() != defaultRelationWeight) {
                    createNewRelation(baseTeam, targetTeam, newRelationType);
                }
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_RELATION_SET_RESULT));
            } else {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_MANAGE_RELATION_SET_RESULT_UNCHANGED));
            }
        }
    }

    private void createNewRelation(NwITeam senderTeam, NwITeam targetTeam, RelationType newRelationType) {
        TeamManager.getManager().createRelation(senderTeam, targetTeam, newRelationType);
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        NwITeam nwTeam = TeamManager.getManager().getStringTeamMap().get(args[3]);
        if (args.length <= 7) {
            List<String> teams = new ArrayList<>(TeamManager.getManager().getStringTeamMap().keySet());
            if (nwTeam != null) {
                teams.remove(nwTeam.getName());
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
