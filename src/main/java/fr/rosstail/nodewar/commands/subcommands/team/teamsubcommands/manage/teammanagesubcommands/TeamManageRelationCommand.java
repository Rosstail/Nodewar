package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.TeamManageSubCommand;
import fr.rosstail.nodewar.team.*;
import fr.rosstail.nodewar.team.rank.TeamRank;
import fr.rosstail.nodewar.team.relation.TeamRelation;
import fr.rosstail.nodewar.team.relation.TeamRelationManager;
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
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (sender instanceof Player) {
            RelationType relationType;
            if (args.length < 5) {
                sender.sendMessage("Not enough args RELATION TEAM");
                return;
            }
            try {
                relationType = RelationType.valueOf(args[3].toUpperCase());
                if (!RelationType.getSelectableRelations().contains(relationType)) {
                    sender.sendMessage("This relation type is not selectable: " + args[3].toUpperCase());
                    return;
                }
            } catch (IllegalArgumentException e) {
                sender.sendMessage("This relation type does not exist: " + args[3].toUpperCase());
                return;
            }
            String targetTeamName = args[4];
            NwTeam playerNwTeam = TeamDataManager.getTeamDataManager().getTeamOfPlayer(((Player) sender));
            NwTeam targetNwTeam = TeamDataManager.getTeamDataManager().getStringTeamMap().get(targetTeamName);
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

            RelationType defaultRelation = ConfigData.getConfigData().team.defaultRelation;


            if (playerNwTeam.getRelations().containsKey(targetTeamName)) {
                if (!relationType.equals(defaultRelation)) {
                    TeamRelation playerTeamRelation = playerNwTeam.getRelations().get(targetTeamName);
                    playerTeamRelation.setRelationType(relationType);
                } else {
                    if (Arrays.stream(arguments).collect(Collectors.toList()).contains("-e")) {
                        sender.sendMessage("TEST - UPDATE EXPLICIT relation to " + relationType + " with team " + targetTeamName);
                    } else {
                        sender.sendMessage("This is a default implicit relation. Use -e or --explicit to make this explicit");
                        sender.sendMessage("TEST - DELETE relation to " + relationType + " with team " + targetTeamName);
                    }
                }
            } else {
                if (!relationType.equals(defaultRelation) || Arrays.stream(arguments).collect(
                        Collectors.toList()).contains("-e")) {
                    sender.sendMessage("TEST - INSERT relation to " + relationType + " with team " + targetTeamName);
                    //StorageManager.getManager().ins; //
                } else {
                    sender.sendMessage("This is a default implicit relation. Use -e or --explicit to make this explicit");
                }
            }
        }
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