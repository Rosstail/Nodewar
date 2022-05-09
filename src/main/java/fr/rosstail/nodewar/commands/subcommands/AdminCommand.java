package fr.rosstail.nodewar.commands.subcommands;

import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.commands.subcommands.admincommands.AdminEmpireCommand;
import fr.rosstail.nodewar.commands.subcommands.admincommands.AdminPlayerCommand;
import fr.rosstail.nodewar.commands.subcommands.admincommands.AdminTerritoryCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminCommand extends SubCommand {

    public AdminCommand() {
        subCommands.add(new AdminPlayerCommand());
        subCommands.add(new AdminEmpireCommand());
        subCommands.add(new AdminTerritoryCommand());
    }

    @Override
    public String getName() {
        return "admin";
    }

    @Override
    public String getDescription() {
        return "Admin commands";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.admin";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage("You don't have permission " + getPermission());
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

        return null;
    }
}
