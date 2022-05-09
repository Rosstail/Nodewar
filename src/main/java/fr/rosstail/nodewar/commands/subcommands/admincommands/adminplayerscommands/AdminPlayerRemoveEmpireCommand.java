package fr.rosstail.nodewar.commands.subcommands.admincommands.adminplayerscommands;

import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.datahandlers.PlayerInfo;
import fr.rosstail.nodewar.datahandlers.PlayerInfoManager;
import fr.rosstail.nodewar.empires.EmpireManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminPlayerRemoveEmpireCommand extends SubCommand {
    @Override
    public String getName() {
        return "removeempire";
    }

    @Override
    public String getDescription() {
        return "Force player to leave an empire";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin player leaveempire";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.admin.player.leaveempire";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage("You don't have permission " + getPermission());
            return;
        }

        if (args.length < 3) {
            sender.sendMessage("Not enough arguments !");
            return;
        }

        String playerName = args[2];
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage("The player " + playerName + " is not connected.");
            return;
        }

        PlayerInfo playerInfo = PlayerInfoManager.getPlayerInfoManager().getSet(player);
        if (playerInfo.getEmpire() == null || playerInfo.getEmpire() == EmpireManager.getEmpireManager().getNoEmpire()) {
            sender.sendMessage("Player " + player.getName() + " is already without empire");
        } else {
            playerInfo.leaveEmpire();
            sender.sendMessage("Player " + player.getName() + " left his empire");
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        if (args.length == 3) {
            ArrayList<String> players = new ArrayList<>();
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                players.add(onlinePlayer.getName());
            }
            return players;
        }
        return null;
    }

}
