package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.Team;
import fr.rosstail.nodewar.team.TeamDataManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TeamOpenCommand extends TeamSubCommand {
    @Override
    public String getName() {
        return "open";
    }

    @Override
    public String getDescription() {
        return "Open your team";
    }

    @Override
    public String getSyntax() {
        return "nodewar team open false/true";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.open";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        boolean value;
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (sender instanceof Player) {
            Player player = ((Player) sender).getPlayer();
            Team playerTeam = TeamDataManager.getTeamDataManager().getTeamOfPlayer(player);
            PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player.getName());

            if (playerTeam == null) {
                sender.sendMessage("Your team is null");
                return;
            }

            if (playerTeam.getMemberModelMap().get(playerData.getId()).getRank() != 1) {
                sender.sendMessage("you do not have enough rank on your team");
                return;
            }

            if (args.length < 3) {
                sender.sendMessage("Not enough arguments");
                return;
            }

            value = Boolean.parseBoolean(args[2]);
            playerTeam.getTeamModel().setOpen(value);

            sender.sendMessage("Your team is now " + (value ? "open" : "closed"));
            StorageManager.getManager().updateTeamModel(playerTeam.getTeamModel());
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        List<String> list = new ArrayList<>();
        list.add("false");
        list.add("true");
        return list;
    }
}
