package fr.rosstail.nodewar.commands.subcommands.empirecommands;

import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.datahandlers.PlayerInfo;
import fr.rosstail.nodewar.datahandlers.PlayerInfoManager;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.empires.EmpireManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class EmpireCreateCommand extends SubCommand {
    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "Create your own empire";
    }

    @Override
    public String getSyntax() {
        return "nodewar empire create";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.empire.create";
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
        PlayerInfo playerInfo = PlayerInfoManager.getPlayerInfoManager().getSet(player);
        Empire playerEmpire = playerInfo.getEmpire();
        EmpireManager empireManager = EmpireManager.getEmpireManager();
        Empire noEmpire = empireManager.getNoEmpire();

        if (playerEmpire != null && !playerEmpire.equals(noEmpire)) {
            player.sendMessage("You are already in the " + playerEmpire.getDisplay() + " empire");
            return;
        }

        if (args.length < 3) {
            player.sendMessage("Not enough arguments !");
            return;
        }

        String empireID = args[2];
        if (empireManager.getEmpires().containsKey(empireID)) {
            player.sendMessage("The empire " + empireID + " already exists");
        } else {
            Empire empire = empireManager.getSet(player, empireID);
            sender.sendMessage("You have founded the empire " + empire.getDisplay() + "!");
            playerInfo.tryJoinEmpire(empire);
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        return null;
    }
}
