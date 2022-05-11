package fr.rosstail.nodewar.commands.subcommands.empirecommands.empireditcommands;

import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.datahandlers.PlayerInfo;
import fr.rosstail.nodewar.datahandlers.PlayerInfoManager;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class EmpireEditDisplayCommand extends SubCommand {
    @Override
    public String getName() {
        return "display";
    }

    @Override
    public String getDescription() {
        return "Edit your empire display name";
    }

    @Override
    public String getSyntax() {
        return "nodewar empire edit display";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.empire.edit.display";
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

        if (args.length < 4) {
            sender.sendMessage(AdaptMessage.adapt(LangManager.getMessage(LangMessage.TOO_FEW_ARGUMENTS)));
            return;
        }

        StringBuilder display = new StringBuilder();
        for (int i = 3; i < args.length; i++) {
            if (i > 3) {
                display.append(" ");
            }
            display.append(args[i]);
        }
        playerEmpire.setDisplay(AdaptMessage.playerMessage(player, display.toString()));
        sender.sendMessage("Your empire is now displayed as " + display);
        playerEmpire.saveConfigFile();
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        return null;
    }
}
