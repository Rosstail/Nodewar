package fr.rosstail.nodewar.commands.subcommands.team;

import fr.rosstail.nodewar.commands.subcommands.TeamSubCommand;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.TeamModel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
    public String getPermission() {
        return "nodewar.command.team.create";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (args.length >= 4) {
            String teamName = args[2];
            String displayName = args[3];
            TeamModel selectTeamModel = StorageManager.getManager().selectTeamModel(teamName);
            if (selectTeamModel != null) {
                sender.sendMessage("TeamCreateCommand - This team already exist in storage");
                return;
            }
            TeamModel teamModel;
            if (sender instanceof Player) {
                teamModel = new TeamModel(teamName, displayName, ((Player) sender).getUniqueId().toString());
            } else {
                teamModel = new TeamModel(teamName, displayName, null);
            }
            sender.sendMessage("TeamCreateCommand - team " + teamModel.getName() + " created "
                    + (StorageManager.getManager().insertTeamModel(teamModel) ? "successfully" : "unsuccessfully"));
        } else {
            System.out.println("TeamCreateCommand - Not enough args");
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
