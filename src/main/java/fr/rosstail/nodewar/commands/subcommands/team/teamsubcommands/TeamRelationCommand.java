package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TeamRelationCommand extends TeamSubCommand {

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
        return "nodewar team relation <teamname> <relation>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.relation";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (sender instanceof Player) {
            if (args.length < 4) {
                sender.sendMessage("Not enough args RELATION TEAM");
                return;
            }
            String relationType = args[2];
            String targetTeamName = args[3];
            Team playerTeam = TeamDataManager.getTeamDataManager().getTeamOfPlayer(((Player) sender).getUniqueId().toString());
            Team targetTeam = TeamDataManager.getTeamDataManager().getStringTeamMap().get(targetTeamName);
            if (playerTeam == null) {
                sender.sendMessage("You are part of no team");
                return;
            }
            if (playerTeam.getMemberModelMap().get(((Player) sender).getUniqueId().toString()).getRank() > 1) {
                sender.sendMessage("You are not the owner of the team");
                return;
            }
            if (targetTeam == null) {
                sender.sendMessage("This team does not exist " + targetTeamName);
                return;
            }

            if (targetTeam.equals(playerTeam)) {
                sender.sendMessage("You cannot edit relation with your own team");
                return;
            }

            String defaultRelation = ConfigData.getConfigData().team.defaultRelation;

            if (playerTeam.getRelationModelMap().containsKey(targetTeamName)) {
                if (!relationType.equalsIgnoreCase(defaultRelation)) {
                    List<String> relations = new ArrayList<>();
                    Collections.addAll(relations, ConfigData.getConfigData().bossbar.relations);
                    TeamRelationModel playerTeamRelationModel = playerTeam.getRelationModelMap().get(targetTeamName);
                    playerTeamRelationModel.setRelation(relations.indexOf(relationType));
                } else {
                    if (!relationType.equalsIgnoreCase(defaultRelation) || Arrays.stream(arguments).collect(
                            Collectors.toList()).contains("-e")) {
                        sender.sendMessage("TEST - UPDATE EXPLICITE relation to " + relationType + " with team " + targetTeamName);
                        //StorageManager.getManager().ins; //
                    } else {
                        sender.sendMessage("This is a default implicit relation. Use -e or --explicit to make this explicit");
                        sender.sendMessage("TEST - DELETE relation to " + relationType + " with team " + targetTeamName);
                    }
                }
            } else {
                if (!relationType.equalsIgnoreCase(defaultRelation) || Arrays.stream(arguments).collect(
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
        Team playerTeam = TeamDataManager.getTeamDataManager().getTeamOfPlayer(sender.getUniqueId().toString());
        if (args.length <= 3) {
            List<String> relations = new ArrayList<>();
            Collections.addAll(relations, ConfigData.getConfigData().bossbar.relations);
            return relations;
        } else if (args.length == 4) {
            List<String> teams = new ArrayList<>(TeamDataManager.getTeamDataManager().getStringTeamMap().keySet());
            if (playerTeam != null) {
                teams.remove(playerTeam.getTeamModel().getName());
            }
            return teams;
        }
        return null;
    }
}
