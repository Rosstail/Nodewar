package fr.rosstail.nodewar.commands.subcommands.admincommands.adminempirecommands.adminempireeditcommands;

import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.datahandlers.PlayerInfo;
import fr.rosstail.nodewar.datahandlers.PlayerInfoManager;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.empires.EmpireManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminEmpireEditFriendlyFireCommand extends SubCommand {
    @Override
    public String getName() {
        return "friendlyfire";
    }

    @Override
    public String getDescription() {
        return "Edit the targeted empire display name";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin empire edit display";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.admin.empire.edit.friendlyfire";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage("You don't have permission " + getPermission());
            return;
        }

        if (args.length < 5) {
            sender.sendMessage("Not enough arguments !");
            return;
        }
        Empire empire = EmpireManager.getEmpireManager().getEmpires().get(args[4]);


        empire.setFriendlyFire(args[3].equalsIgnoreCase("true"));
        sender.sendMessage("Friendlyfire for " + empire.getDisplay() + " is now " + (args[3].equalsIgnoreCase("true") ? "enabled" : "disabled"));
        empire.saveConfigFile();
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        if (args.length == 5){
            return new ArrayList<>(EmpireManager.getEmpireManager().getEmpires().keySet());
        } else if (args.length == 6) {
            ArrayList<String> bools = new ArrayList<>();
            bools.add("True");
            bools.add("False");
            return bools;
        }
        return null;
    }
}
