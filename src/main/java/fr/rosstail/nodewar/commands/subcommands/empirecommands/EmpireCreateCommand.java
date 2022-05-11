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
        return "nodewar empire create [name]";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.empire.create";
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
        Empire noEmpire = empireManager.getNoEmpire();

        if (playerEmpire != null && !playerEmpire.equals(noEmpire)) {
            sender.sendMessage(AdaptMessage.adapt(LangManager.getMessage(LangMessage.EMPIRE_PLAYER_ALREADY_JOINED)));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(AdaptMessage.adapt(LangManager.getMessage(LangMessage.TOO_FEW_ARGUMENTS)));
            return;
        }

        String empireID = args[2];
        if (empireManager.getEmpires().containsKey(empireID)) {
            sender.sendMessage("The empire " + empireID + " already exists");
        } else {
            Empire empire = empireManager.getSet(player, empireID);
            sender.sendMessage(LangManager.getMessage(LangMessage.EMPIRE_CREATE));
            playerInfo.tryJoinEmpire(empire);
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        return null;
    }
}
