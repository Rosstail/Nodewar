package fr.rosstail.nodewar.commands.subcommands.admincommands;

import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.commands.subcommands.HelpCommand;
import fr.rosstail.nodewar.commands.subcommands.admincommands.adminempirecommands.AdminEmpireCreateCommand;
import fr.rosstail.nodewar.commands.subcommands.admincommands.adminempirecommands.AdminEmpireEditCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminEmpireCommand extends SubCommand {

    public AdminEmpireCommand() {
        getSubCommands().add(new AdminEmpireCreateCommand());
        getSubCommands().add(new AdminEmpireEditCommand());
    }
    @Override
    public String getName() {
        return "empire";
    }

    @Override
    public String getDescription() {
        return "Admin commands for empires";
    }

    @Override
    public String getSyntax() {
        return "nodewar nodewar admin empire";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.admin.empire";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {

        if (args.length <= 2) {
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
