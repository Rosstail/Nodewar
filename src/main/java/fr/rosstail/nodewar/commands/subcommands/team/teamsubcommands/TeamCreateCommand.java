package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.TeamMemberModel;
import fr.rosstail.nodewar.team.TeamModel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.List;

public class TeamCreateCommand extends TeamSubCommand {
    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "Desc create nodewar team";
    }

    @Override
    public String getSyntax() {
        return "nodewar team create <name> <display> <hexcolor>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.create";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String teamName;
        String displayName;
        Player senderPlayer = null;
        NwTeam playerNwTeam;
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (args.length >= 4) {
            teamName = args[2];
            displayName = args[3];
            int ownerId = 0;

            if (TeamDataManager.getTeamDataManager().getStringTeamMap().get(teamName) != null) {
                sender.sendMessage("TeamCreateCommand - This team already exist in storage");
                return;
            }
            TeamModel teamModel = new TeamModel(teamName, displayName);
            if (sender instanceof Player) {
                senderPlayer = (Player) sender;
                PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(senderPlayer.getName());
                ownerId = playerData.getId();

                playerNwTeam = TeamDataManager.getTeamDataManager().getTeamOfPlayer(senderPlayer);
                if (playerNwTeam != null) {
                    sender.sendMessage("You are already on a team");
                    return;
                }
            } else {
                teamModel.setPermanent(true);
            }

            boolean insertTeam = StorageManager.getManager().insertTeamModel(teamModel);
            if (insertTeam) {
                playerNwTeam = new NwTeam(teamModel);
                TeamDataManager.getTeamDataManager().addNewTeam(playerNwTeam);
                teamModel.setId(StorageManager.getManager().selectTeamModelByName(teamName).getId());

                sender.sendMessage("Team added successfully");

                if (senderPlayer != null) {
                    PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(senderPlayer.getName());
                    playerData.setTeam(playerNwTeam);

                    TeamMemberModel teamMemberModel =
                            new TeamMemberModel(teamModel.getId(), ownerId, 1, new Timestamp(System.currentTimeMillis()));
                    playerNwTeam.getMemberModelMap().put(ownerId, teamMemberModel);
                    StorageManager.getManager().insertTeamMemberModel(teamMemberModel);
                }
            } else {
                sender.sendMessage("Team added unsuccessfully");
            }
        } else {
            sender.sendMessage("TeamCreateCommand - Not enough args");
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
