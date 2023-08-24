package fr.rosstail.nodewar.commands.subcommands.team;

import fr.rosstail.nodewar.commands.subcommands.TeamSubCommand;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.storage.storagetype.StorageRequest;
import fr.rosstail.nodewar.team.TeamModel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
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
        return "team create <name> <display> <hexcolor>";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.create";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (args.length >= 5) {
            TeamModel teamModel;
            if (sender instanceof Player) {
                teamModel = new TeamModel(((Player) sender).getUniqueId().toString(), args[2], args[3], args[4]);
            } else {
                teamModel = new TeamModel(null, args[2], args[3], args[4]);
            }

            sender.sendMessage("The creation of the team " + teamModel.getName() + " is "
                    + StorageManager.getManager().insertTeamModel(teamModel));
        } else {
            System.out.println("TeamCreateCommand - Not enough args");
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        List<String> list = new ArrayList<>();
        if (args.length > 3) {
            list.add("#FFFFFF");
        }

        return list;
    }
}
