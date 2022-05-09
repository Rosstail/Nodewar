package fr.rosstail.nodewar.commands.subcommands.empirecommands;

import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.datahandlers.PlayerInfo;
import fr.rosstail.nodewar.datahandlers.PlayerInfoManager;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.empires.EmpireManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EmpireDisbandCommand extends SubCommand {

    public EmpireDisbandCommand() {
    }
    @Override
    public String getName() {
        return "disband";
    }

    @Override
    public String getDescription() {
        return "Disband your empire";
    }

    @Override
    public String getSyntax() {
        return "nodewar empire disband";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.empire.disband";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage("You don't have permission " + getPermission());
            return;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("By player only");
            return;
        }
        Player player = Objects.requireNonNull(((Player) sender).getPlayer());
        Empire playerEmpire = PlayerInfoManager.getPlayerInfoManager().getSet(player).getEmpire();
        String empireOwnerUUID = playerEmpire.getOwnerUUID();
        if (empireOwnerUUID == null || !empireOwnerUUID.equals(player.getUniqueId().toString())) {
            player.sendMessage("You do not own this empire !");
            return;
        }

        EmpireManager empireManager = EmpireManager.getEmpireManager();
        Map<Player, PlayerInfo> playerInfoMap = PlayerInfoManager.getPlayerInfoManager().getPlayerInfoMap();
        empireManager.getEmpires().remove(playerEmpire.getName());
        for (Map.Entry<Player, PlayerInfo> entry : playerInfoMap.entrySet()) {
            PlayerInfo playerInfo = entry.getValue();
            if (playerInfo.getEmpire() == playerEmpire) {
                playerInfo.leaveEmpire();
            }
        }
        playerEmpire.deleteConfig();
        sender.sendMessage("Empire disbanded.");
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        return null;
    }
}
