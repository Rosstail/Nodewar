package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.rank.TeamRank;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

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
            Player player = ((Player) sender).getPlayer();
            boolean disband = false;
            NwTeam playerNwTeam = TeamDataManager.getTeamDataManager().getTeamOfPlayer(player);
            if (playerNwTeam != null) {
                if (playerNwTeam.getMemberMap().get(player).getRank() == TeamRank.OWNER) {
                    disband = true;
                    TeamDataManager.getTeamDataManager().deleteTeam(playerNwTeam.getModel().getName());
                } else {
                    sender.sendMessage("you do not have enough rank on your team");
                }
            } else {
                sender.sendMessage("Your team is null");
            }

            if (!disband) {
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
