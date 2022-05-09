package fr.rosstail.nodewar.commands.subcommands.admincommands.adminempirecommands;

import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.commands.subcommands.HelpCommand;
import fr.rosstail.nodewar.commands.subcommands.admincommands.adminempirecommands.adminempireeditcommands.AdminEmpireEditBossBarColorCommand;
import fr.rosstail.nodewar.commands.subcommands.admincommands.adminempirecommands.adminempireeditcommands.AdminEmpireEditColorCommand;
import fr.rosstail.nodewar.commands.subcommands.admincommands.adminempirecommands.adminempireeditcommands.AdminEmpireEditDisplayCommand;
import fr.rosstail.nodewar.commands.subcommands.admincommands.adminempirecommands.adminempireeditcommands.AdminEmpireEditFriendlyFireCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminEmpireEditCommand extends SubCommand {

    public AdminEmpireEditCommand() {
        getSubCommands().add(new AdminEmpireEditBossBarColorCommand());
        getSubCommands().add(new AdminEmpireEditColorCommand());
        getSubCommands().add(new AdminEmpireEditDisplayCommand());
        getSubCommands().add(new AdminEmpireEditFriendlyFireCommand());
    }

    @Override
    public String getName() {
        return "edit";
    }

    @Override
    public String getDescription() {
        return "Edit an existing empire";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin empire edit";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.admin.empire.edit";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length <= 3) {
            HelpCommand help = new HelpCommand(this);
            help.perform(sender, args);
        } else {
            for (int index = 0; index < getSubCommands().size(); index++) {
                if (args[3].equalsIgnoreCase(getSubCommands().get(index).getName())) {
                    getSubCommands().get(index).perform(sender, args);
                    return;
                }
            }
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        if (args.length <= 4) {
            ArrayList<String> subCommands = new ArrayList<>();
            for (SubCommand subCommand : getSubCommands()) {
                subCommands.add(subCommand.getName());
            }
            return subCommands;
        } else {
            for (SubCommand subCommand : getSubCommands()) {
                if (args[3].equalsIgnoreCase(subCommand.getName())) {
                    return subCommand.getSubCommandsArguments(sender, args);
                }
            }
        }
        return null;
    }
}
