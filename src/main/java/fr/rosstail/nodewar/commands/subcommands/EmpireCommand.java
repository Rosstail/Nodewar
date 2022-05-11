package fr.rosstail.nodewar.commands.subcommands;

import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.commands.subcommands.empirecommands.*;
import fr.rosstail.nodewar.commands.subcommands.empirecommands.EmpireCreateCommand;
import fr.rosstail.nodewar.commands.subcommands.empirecommands.EmpireDisbandCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EmpireCommand extends SubCommand {

    public EmpireCommand() {
        subCommands.add(new EmpireCreateCommand());
        subCommands.add(new EmpireEditCommand());
        subCommands.add(new EmpireJoinCommand());
        subCommands.add(new EmpireLeaveCommand());
        subCommands.add(new EmpireListCommand());
        subCommands.add(new EmpireDisbandCommand());
    }

    @Override
    public String getName() {
        return "empire";
    }

    @Override
    public String getDescription() {
        return "Empire commands";
    }

    @Override
    public String getSyntax() {
        return "nodewar empire";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.empire";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(AdaptMessage.adapt(LangManager.getMessage(LangMessage.PERMISSION_DENIED)));
            return;
        }

        if (args.length <= 1) {
            HelpCommand help = new HelpCommand(this);
            help.perform(sender, args);
        } else {
            for (int index = 0; index < getSubCommands().size(); index++) {
                if (args[1].equalsIgnoreCase(getSubCommands().get(index).getName())) {
                    getSubCommands().get(index).perform(sender, args);
                    return;
                }
            }
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        ArrayList<String> subCommandsArguments = new ArrayList<>();
        if (args.length <= 2) {
            ArrayList<String> subCommands = new ArrayList<>();
            for (SubCommand subCommand : getSubCommands()) {
                subCommands.add(subCommand.getName());
            }
            return subCommands;
        } else {
            for (SubCommand subCommand : getSubCommands()) {
                if (args[1].equalsIgnoreCase(subCommand.getName())) {
                    return subCommand.getSubCommandsArguments(sender, args);
                }
            }
        }
        return subCommandsArguments;
    }
}
