package fr.rosstail.nodewar.commands.subcommands.empirecommands.empireditcommands;

import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.datahandlers.PlayerInfo;
import fr.rosstail.nodewar.datahandlers.PlayerInfoManager;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import org.bukkit.boss.BarColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EmpireEditBossBarColorCommand extends SubCommand {
    @Override
    public String getName() {
        return "bossbar";
    }

    @Override
    public String getDescription() {
        return "Edit your empire display name";
    }

    @Override
    public String getSyntax() {
        return "nodewar empire edit bossbar";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.empire.edit.bossbar";
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

        try {
            playerEmpire.setBarColor(BarColor.valueOf(args[3]));
            sender.sendMessage("Bossbarcolor is now !" + BarColor.valueOf(args[3]));
            playerEmpire.saveConfigFile();
        } catch (Exception e) {
            sender.sendMessage("The barcolor value doesn't exist");
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        if (args.length == 4) {
            ArrayList<String> barColors = new ArrayList<>();
            barColors.add(BarColor.RED.name());
            barColors.add(BarColor.YELLOW.name());
            barColors.add(BarColor.GREEN.name());
            barColors.add(BarColor.BLUE.name());
            barColors.add(BarColor.PINK.name());
            barColors.add(BarColor.PURPLE.name());
            barColors.add(BarColor.WHITE.name());
            return barColors;
        }
        return null;
    }
}
