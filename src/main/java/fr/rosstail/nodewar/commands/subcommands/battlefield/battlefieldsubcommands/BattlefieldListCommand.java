package fr.rosstail.nodewar.commands.subcommands.battlefield.battlefieldsubcommands;

import fr.rosstail.nodewar.battlefield.Battlefield;
import fr.rosstail.nodewar.battlefield.BattlefieldManager;
import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.battlefield.BattlefieldSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.TeamManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BattlefieldListCommand extends BattlefieldSubCommand {

    public BattlefieldListCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_BATTLEFIELD_LIST_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "List of battlefields";
    }

    @Override
    public String getSyntax() {
        return "nodewar battlefield list (page)";
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        return List.of("-c", "-o");
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        StringBuilder message = new StringBuilder(LangManager.getMessage(LangMessage.COMMANDS_BATTLEFIELD_LIST_RESULT_HEADER));
        int page = 0;

        List<Battlefield> battlefieldList = BattlefieldManager.getManager().getBattlefieldList();
        Stream<Battlefield> battlefieldStreamList = battlefieldList.stream();

        if (CommandManager.doesCommandMatchParameter(arguments, "o", "open")) {
            battlefieldStreamList = battlefieldStreamList.filter(battlefield -> battlefield.getModel().isOpen());
        }
        if (CommandManager.doesCommandMatchParameter(arguments, "c", "closed")) {
            battlefieldStreamList = battlefieldStreamList.filter(battlefield -> !battlefield.getModel().isOpen());
        }

        battlefieldList = battlefieldStreamList.toList();
        List<String> strList = battlefieldList.stream().map(battlefield -> battlefield.getModel().getName()).toList();
        int size = strList.size();
        int maxPage = (int) Math.ceil((float) size / 10);
        
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (args.length >= 3) {
            page = Integer.parseInt(args[2]) - 1;
        }

        if (page < 0 || page >= maxPage) {
            if (size == 0) {
                sender.sendMessage("P.0/0");
            }
            return;
        }

        if (size > 0) {
            for (int i = page * 10; i < Math.min(size, page * 10 + 10); i++) {
                String s = strList.get(i);
                Battlefield battlefield = battlefieldList.stream().filter(battlefield1 -> battlefield1.getModel().getName().equals(s)).findFirst().get();
                message.append("\n").append(battlefield.adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_BATTLEFIELD_LIST_RESULT_LINE)));
            }
            message.append("\nP." + (page + 1) + "/" + maxPage);
        } else {
            message.append("\n&rP.0/0");
        }
        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(message.toString()));
    }
}
