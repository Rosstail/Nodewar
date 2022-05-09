package fr.rosstail.nodewar.commands.subcommands.empirecommands.empireditcommands;

import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.datahandlers.PlayerInfo;
import fr.rosstail.nodewar.datahandlers.PlayerInfoManager;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.lang.AdaptMessage;
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
            sender.sendMessage("You don't have permission " + getPermission());
            return;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("By player only");
            return;
        }

        if (args.length < 4) {
            sender.sendMessage("Not enough arguments !");
            return;
        }

        Player player = Objects.requireNonNull(((Player) sender).getPlayer());
        PlayerInfo playerInfo = PlayerInfoManager.getPlayerInfoManager().getSet(player);
        Empire playerEmpire = playerInfo.getEmpire();

        String colorString = args[4];
        Pattern hexPattern = Pattern.compile("(#[a-fA-F0-9]{6})");

        if (!colorString.matches(hexPattern.pattern())) {
            sender.sendMessage(colorString + " does not match the #RRGGBB regex !");
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
