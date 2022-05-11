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
import java.util.regex.Pattern;

public class EmpireEditColorCommand extends SubCommand {
    @Override
    public String getName() {
        return "color";
    }

    @Override
    public String getDescription() {
        return "Edit your empire display name";
    }

    @Override
    public String getSyntax() {
        return "nodewar empire edit color";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.empire.edit.color";
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

        if (args.length < 4) {
            sender.sendMessage(AdaptMessage.adapt(LangManager.getMessage(LangMessage.TOO_FEW_ARGUMENTS)));
            return;
        }

        Player player = Objects.requireNonNull(((Player) sender).getPlayer());
        PlayerInfo playerInfo = PlayerInfoManager.getPlayerInfoManager().getSet(player);
        Empire playerEmpire = playerInfo.getEmpire();

        String colorString = args[4];
        Pattern hexPattern = Pattern.compile("(#[a-fA-F0-9]{6})");

        if (!colorString.matches(hexPattern.pattern())) {
            sender.sendMessage(AdaptMessage.adapt(LangManager.getMessage(LangMessage.WRONG_VALUE)));
            return;
        }

        playerEmpire.setMapColor(colorString);
        sender.sendMessage("Edited color successfully !");
        playerEmpire.saveConfigFile();
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        return null;
    }
}
