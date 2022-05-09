package fr.rosstail.nodewar.commands.subcommands.admincommands;

import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.commands.subcommands.HelpCommand;
import fr.rosstail.nodewar.commands.subcommands.admincommands.adminterritorycommands.AdminTerritoryVulnerabilityCommand;
import fr.rosstail.nodewar.commands.subcommands.admincommands.adminterritorycommands.AdminTerritoryNeutralizeCommand;
import fr.rosstail.nodewar.commands.subcommands.admincommands.adminterritorycommands.AdminTerritorySetOwnerCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminTerritoryCommand extends SubCommand {

    public AdminTerritoryCommand() {
        getSubCommands().add(new AdminTerritorySetOwnerCommand());
        getSubCommands().add(new AdminTerritoryNeutralizeCommand());
        getSubCommands().add(new AdminTerritoryVulnerabilityCommand());
    }
    @Override
    public String getName() {
        return "territory";
    }

    @Override
    public String getDescription() {
        return "Territory commands";
    }

    @Override
    public String getSyntax() {
        return "nodewar territory";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.territory";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage("You don't have permission " + getPermission());
            return;
        }
        if (args.length <= 3) {
            HelpCommand help = new HelpCommand(this);
            help.perform(sender, args);
        } else {
            for (int index = 0; index < getSubCommands().size(); index++) {
                if (args[2].equalsIgnoreCase(getSubCommands().get(index).getName())) {
                    getSubCommands().get(index).perform(sender, args);
                    return;
                }
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
