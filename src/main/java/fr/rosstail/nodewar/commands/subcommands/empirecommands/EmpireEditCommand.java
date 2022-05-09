package fr.rosstail.nodewar.commands.subcommands.empirecommands;

import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.commands.subcommands.HelpCommand;
import fr.rosstail.nodewar.commands.subcommands.empirecommands.empireditcommands.*;
import fr.rosstail.nodewar.datahandlers.PlayerInfoManager;
import fr.rosstail.nodewar.empires.Empire;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EmpireEditCommand extends SubCommand {

    public EmpireEditCommand() {
        getSubCommands().add(new EmpireEditBossBarColorCommand());
        getSubCommands().add(new EmpireEditColorCommand());
        getSubCommands().add(new EmpireEditDisplayCommand());
        getSubCommands().add(new EmpireEditFriendlyFireCommand());
    }
    @Override
    public String getName() {
        return "edit";
    }

    @Override
    public String getDescription() {
        return "Edit your empire";
    }

    @Override
    public String getSyntax() {
        return "nodewar empire edit";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.empire.edit";
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
        Empire playerEmpire = PlayerInfoManager.getPlayerInfoManager().getSet(player).getEmpire();
        String empireOwnerUUID = playerEmpire.getOwnerUUID();
        if (empireOwnerUUID == null || !empireOwnerUUID.equals(player.getUniqueId().toString())) {
            player.sendMessage("You do not own this empire !");
            return;
        }

        if (args.length < 3) {
            HelpCommand help = new HelpCommand(this);
            help.perform(sender, args);
            return;
        }

        for (int index = 0; index < getSubCommands().size(); index++) {
            if (args[2].equalsIgnoreCase(getSubCommands().get(index).getName())) {
                getSubCommands().get(index).perform(sender, args);
                return;
            }
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        if (args.length <= 3) {
            ArrayList<String> subCommands = new ArrayList<>();
            for (SubCommand subCommand : getSubCommands()) {
                subCommands.add(subCommand.getName());
            }
            return subCommands;
        } else {
            for (SubCommand subCommand : getSubCommands()) {
                if (args[2].equalsIgnoreCase(subCommand.getName())) {
                    return subCommand.getSubCommandsArguments(sender, args);
                }
            }
        }
        return null;
    }
}
