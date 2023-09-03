package fr.rosstail.nodewar.commands.subcommands.edit.territory;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.edit.EditSubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class EditTerritorySubCommand extends EditSubCommand {
    @Override
    public String getName() {
        return "territory";
    }

    @Override
    public String getDescription() {
        return "Edit territory as admin";
    }

    @Override
    public String getSyntax() {
        return "nodewar edit territory <subcommand>";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.edit";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }


    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
