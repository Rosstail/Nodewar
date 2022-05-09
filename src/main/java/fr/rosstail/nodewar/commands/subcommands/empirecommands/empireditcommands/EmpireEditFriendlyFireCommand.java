package fr.rosstail.nodewar.commands.subcommands.empirecommands.empireditcommands;

import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.datahandlers.PlayerInfo;
import fr.rosstail.nodewar.datahandlers.PlayerInfoManager;
import fr.rosstail.nodewar.empires.Empire;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EmpireEditFriendlyFireCommand extends SubCommand {
    @Override
    public String getName() {
        return "friendlyfire";
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
        return "nodewar.command.empire.edit.friendlyfire";
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

        if (args.length < 4) {
            player.sendMessage("Not enough arguments !");
            return;
        }


        playerEmpire.setFriendlyFire(args[3].equalsIgnoreCase("true"));
        sender.sendMessage("Friendlyfire for your empire is now " + (args[3].equalsIgnoreCase("true") ? "enabled" : "disabled"));
        playerEmpire.saveConfigFile();
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        ArrayList<String> arguments = new ArrayList<>();
        arguments.add("True");
        arguments.add("False");
        return arguments;
    }
}
