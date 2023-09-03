package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.TeamModel;
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
        sender.sendMessage("disband");
        if (sender instanceof Player) {
            TeamModel teamModel = StorageManager.getManager().selectTeamModelByOwnerUuid(((Player) sender).getUniqueId().toString());
            if (teamModel != null) {
                sender.sendMessage("Disbanded team");
                StorageManager.getManager().deleteTeamModel(teamModel.getId());
            } else {
                sender.sendMessage("You are not owner of any team");
            }
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
