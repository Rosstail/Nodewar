package fr.rosstail.nodewar.commands.subcommands.admincommands.adminempirecommands.adminempireeditcommands;

import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.empires.EmpireManager;
import org.bukkit.boss.BarColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminEmpireEditBossBarColorCommand extends SubCommand {
    @Override
    public String getName() {
        return "bossbar";
    }

    @Override
    public String getDescription() {
        return "Edit an empire bossbar color";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin empire edit bossbar";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.admin.empire.edit.bossbar";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage("You don't have permission " + getPermission());
            return;
        }

        if (args.length < 6) {
            sender.sendMessage("Not enough arguments !");
            return;
        }
        Empire empire = EmpireManager.getEmpireManager().getEmpires().get(args[3]);
        if (empire == null) {
            sender.sendMessage("Empire " + args[4] + " does not exist");
            return;
        }

        try {
            empire.setBarColor(BarColor.valueOf(args[5]));
            sender.sendMessage("Bossbarcolor is now !" + BarColor.valueOf(args[5]));
            empire.saveConfigFile();
        } catch (Exception e) {
            sender.sendMessage("The barcolor value doesn't exist");
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        if (args.length == 5){
            return new ArrayList<>(EmpireManager.getEmpireManager().getEmpires().keySet());
        } else if (args.length == 6) {
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
