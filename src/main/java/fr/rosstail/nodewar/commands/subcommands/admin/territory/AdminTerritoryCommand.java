package fr.rosstail.nodewar.commands.subcommands.admin.territory;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.commands.subcommands.admin.territory.adminterritorysubcommands.AdminTerritoryProtectCommand;
import fr.rosstail.nodewar.commands.subcommands.admin.territory.adminterritorysubcommands.AdminTerritoryReloadRegionsCommand;
import fr.rosstail.nodewar.commands.subcommands.admin.territory.adminterritorysubcommands.AdminTerritoryVulnerableCommand;
import fr.rosstail.nodewar.commands.subcommands.admin.territory.adminterritorysubcommands.team.AdminTerritoryTeamCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.territory.TerritoryManager;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class AdminTerritoryCommand extends AdminTerritorySubCommand {
    public List<AdminTerritorySubCommand> subCommands = new ArrayList<>();
    public AdminTerritoryCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TERRITORY_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
        subCommands.add(new AdminTerritoryReloadRegionsCommand());
        subCommands.add(new AdminTerritoryTeamCommand());
        subCommands.add(new AdminTerritoryProtectCommand());
        subCommands.add(new AdminTerritoryVulnerableCommand());
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String territoryName;
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (args.length < 4) {
            sender.sendMessage(getSubCommandHelp());
            return;
        }

        territoryName = args[2];

        if (!TerritoryManager.getTerritoryManager().getTerritoryMap().containsKey(territoryName)) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_VALUE).replaceAll("\\[value]", territoryName));
            return;
        }

        SubCommand subCommand = subCommands.stream()
                .filter(teamSubCommand -> teamSubCommand.getName().equalsIgnoreCase(args[3]))
                .findFirst().orElse(null);

        if (subCommand == null) {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_COMMAND)));
            return;
        }

        subCommand.perform(sender, args, arguments);
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        if (args.length <= 3) {
            return new ArrayList<>(TerritoryManager.getTerritoryManager().getTerritoryMap().keySet());
        } else if (args.length == 4) {
            return subCommands.stream().map(SubCommand::getName).toList();
        } else {
            SubCommand subCommand = subCommands.stream()
                    .filter(filterSubCommand -> filterSubCommand.getName().equalsIgnoreCase(args[3]))
                    .findFirst().orElse(null);
            
            if (subCommand == null) {
                return null;
            }
            return subCommand.getSubCommandsArguments(sender, args, arguments);
        }
    }

    @Override
    public String getSubCommandHelp() {
        StringBuilder subCommandHelp = new StringBuilder(super.getSubCommandHelp());
        for (SubCommand subCommand : subCommands) {
            if (subCommand.getHelp() != null) {
                subCommandHelp.append("\n").append(subCommand.getHelp());
            }
        }
        return subCommandHelp.toString();
    }
}
