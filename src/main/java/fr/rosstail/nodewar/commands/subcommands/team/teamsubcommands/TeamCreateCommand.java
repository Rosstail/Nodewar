package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.Team;
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
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (args.length >= 4) {
            String teamName = args[2];
            String displayName = args[3];
            String ownerUuid = null;
            TeamModel selectTeamModel = StorageManager.getManager().selectTeamModelByName(teamName);
            if (selectTeamModel != null) {
                sender.sendMessage("TeamCreateCommand - This team already exist in storage");
                return;
            }
            TeamModel teamModel = new TeamModel(teamName, displayName);
            if (sender instanceof Player) {
                ownerUuid = ((Player) sender).getUniqueId().toString();

                TeamMemberModel selectTeamMember = StorageManager.getManager().selectTeamMemberModel(ownerUuid);
                if (selectTeamMember != null) {
                    sender.sendMessage("You are already on a team");
                    return;
                }
            } else {
                teamModel.setPermanent(true);
            }

            boolean insertTeam = StorageManager.getManager().insertTeamModel(teamModel);
            if (insertTeam) {
                TeamDataManager.getTeamDataManager().getStringTeamMap().put(teamModel.getName(), new Team(teamModel));
                sender.sendMessage("Team added successfully");
                teamModel.setId(StorageManager.getManager().selectTeamModelByName(teamName).getId());
                Team team = new Team(teamModel);
                TeamDataManager.getTeamDataManager().addNewTeam(team);

                if (ownerUuid != null) {
                    TeamMemberModel teamMemberModel =
                            new TeamMemberModel(teamModel.getId(), ownerUuid, 1, new Timestamp(System.currentTimeMillis()));
                    team.getMemberModelMap().put(ownerUuid, teamMemberModel);
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
