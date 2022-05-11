package fr.rosstail.nodewar.commands.subcommands.admincommands.adminplayerscommands;

import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.datahandlers.PlayerInfo;
import fr.rosstail.nodewar.datahandlers.PlayerInfoManager;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.empires.EmpireManager;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminPlayerSetEmpireCommand extends SubCommand {
    @Override
    public String getName() {
        return "setempire";
    }

    @Override
    public String getDescription() {
        return "Force player to join an empire";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin player setempire [player]";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.admin.player.setempire";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(AdaptMessage.adapt(LangManager.getMessage(LangMessage.PERMISSION_DENIED)));
            return;
        }

        if (args.length < 5) {
            sender.sendMessage(AdaptMessage.adapt(LangManager.getMessage(LangMessage.TOO_FEW_ARGUMENTS)));
            return;
        }

        String playerName = args[3];
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage("The player " + playerName + " is not connected.");
            return;
        }

        EmpireManager empireManager = EmpireManager.getEmpireManager();
        String empireID = args[4];
        PlayerInfo playerInfo = PlayerInfoManager.getPlayerInfoManager().getSet(player);
        if (empireManager.getEmpires().containsKey(empireID)) {
            Empire empire = empireManager.getEmpires().get(empireID);
            playerInfo.setEmpire(empire);
            sender.sendMessage(player.getName() + " joined the empire " + empire.getDisplay());
        } else {
            sender.sendMessage("The empire " + empireID + " does not exist");
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
        } else if (args.length == 4) {
            return new ArrayList<>(EmpireManager.getEmpireManager().getEmpires().keySet());
        }

        return null;
    }

}
