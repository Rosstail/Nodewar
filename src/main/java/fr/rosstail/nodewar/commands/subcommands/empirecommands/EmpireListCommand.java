package fr.rosstail.nodewar.commands.subcommands.empirecommands;

import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.guis.playerguis.EmpiresListGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EmpireListCommand extends SubCommand {
    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "list the empires";
    }

    @Override
    public String getSyntax() {
        return "nodewar empire list";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.empire.list";
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
        EmpiresListGUI.initGUI(((Player) sender).getPlayer(), Nodewar.getInstance());
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        return null;
    }
}
