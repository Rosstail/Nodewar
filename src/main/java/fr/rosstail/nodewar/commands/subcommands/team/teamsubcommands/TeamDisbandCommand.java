package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.Team;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.TeamModel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class TeamDisbandCommand extends TeamSubCommand {
    @Override
    public String getName() {
        return "disband";
    }

    @Override
    public String getDescription() {
        return "Disband your team";
    }

    @Override
    public String getSyntax() {
        return "nodewar team disband";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.disband";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (sender instanceof Player) {
            boolean disband = false;
            List<Team> teamList = TeamDataManager.getTeamDataManager().getTeamOfPlayer(((Player) sender).getUniqueId().toString());
            if (!teamList.isEmpty()) {
                Team team = teamList.get(0);
                if (team.getMemberModelMap().get(((Player) sender).getUniqueId().toString()).getRank() == 1) {
                    disband = true;
                    StorageManager.getManager().deleteTeamModel(team.getTeamModel().getId());
                    TeamDataManager.getTeamDataManager().removeDeletedTeam(team.getTeamModel().getName());
                }
            }

            if (!disband){
                sender.sendMessage("You are not owner of any team");
            } else {
                sender.sendMessage("Disbanded team");
            }
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
