package fr.rosstail.nodewar.commands.subcommands.territory.teamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.admin.territory.AdminTerritorySubCommand;
import fr.rosstail.nodewar.commands.subcommands.territory.TerritorySubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TerritoryCheckCommand extends TerritorySubCommand {

    public TerritoryCheckCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TERRITORY_CHECK_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));

    }

    @Override
    public String getName() {
        return "check";
    }

    @Override
    public String getDescription() {
        return "Desc check territory";
    }

    @Override
    public String getSyntax() {
        return "nodewar territory check (territoryName)";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        Player senderPlayer;
        Territory territory;
        String message = LangManager.getMessage(LangMessage.COMMANDS_TERRITORY_CHECK_RESULT);
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (args.length < 3 && !(sender instanceof Player)) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TOO_FEW_ARGUMENTS));
            return;
        }

        if (args.length >= 3) {
            territory = TerritoryManager.getTerritoryManager().getTerritoryMap().get(args[2]);
            if (territory != null) {
                message = LangManager.getMessage(LangMessage.COMMANDS_TERRITORY_CHECK_RESULT_OTHER);
                sender.sendMessage(AdaptMessage.getAdaptMessage().adaptTerritoryMessage(message, territory));
            } else {
                message = "this territory does not exist";
                sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(message));
            }
        } else {
            senderPlayer = ((Player) sender).getPlayer();
            List<Territory> territoryList = TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream().filter(territory1 -> (territory1.getPlayers().contains(senderPlayer))).collect(Collectors.toList());

            if (territoryList.isEmpty()) {
                sender.sendMessage("You are not on a territory");
                return;
            } else if (territoryList.size() > 1) {
                String territoryNames = territoryList.stream().map(territory1 -> territory1.getModel().getName()).collect(Collectors.joining(", "));
                sender.sendMessage("You stand on multiple territories. Please select one of them: " + territoryNames);
                return;
            }

            territory = territoryList.get(0);
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptTerritoryMessage(message, territory));
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return new ArrayList<>(TerritoryManager.getTerritoryManager().getTerritoryMap().keySet());
    }
}
