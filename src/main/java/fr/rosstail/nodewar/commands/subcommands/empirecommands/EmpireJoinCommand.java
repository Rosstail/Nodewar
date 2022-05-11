package fr.rosstail.nodewar.commands.subcommands.empirecommands;

import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.datahandlers.PlayerInfo;
import fr.rosstail.nodewar.datahandlers.PlayerInfoManager;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.empires.EmpireManager;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EmpireJoinCommand extends SubCommand {
    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getDescription() {
        return "Joins an empire";
    }

    @Override
    public String getSyntax() {
        return "nodewar empire join";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.empire.join";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(AdaptMessage.adapt(LangManager.getMessage(LangMessage.PERMISSION_DENIED)));
            return;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(AdaptMessage.adapt(LangManager.getMessage(LangMessage.BY_PLAYER_ONLY)));
            return;
        }
        Player player = Objects.requireNonNull(((Player) sender).getPlayer());
        PlayerInfo playerInfo = PlayerInfoManager.getPlayerInfoManager().getSet(player);
        Empire playerEmpire = playerInfo.getEmpire();
        EmpireManager empireManager = EmpireManager.getEmpireManager();

        if (args.length < 3) {
            sender.sendMessage(AdaptMessage.adapt(LangManager.getMessage(LangMessage.TOO_FEW_ARGUMENTS)));
            return;
        }

        String empireName = args[2];
        if (playerEmpire == null || playerEmpire == empireManager.getNoEmpire()) {
            if (empireManager.getEmpires().containsKey(empireName)) {
                Empire newEmpire = EmpireManager.getEmpireManager().getEmpires().get(empireName);
                playerInfo.setEmpire(newEmpire);
                sender.sendMessage("You have successfully joined the empire " + newEmpire.getDisplay());
            } else {
                sender.sendMessage("The empire " + empireName + " does not exist");
            }
        } else {
            sender.sendMessage("You are already in the " + playerInfo.getEmpire().getDisplay() + " empire");
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        if (args.length <= 3) {
            return new ArrayList<>(EmpireManager.getEmpireManager().getEmpires().keySet());
        }
        return null;
    }
}
