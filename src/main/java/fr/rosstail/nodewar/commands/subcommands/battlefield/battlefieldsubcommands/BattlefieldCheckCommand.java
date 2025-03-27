package fr.rosstail.nodewar.commands.subcommands.battlefield.battlefieldsubcommands;

import fr.rosstail.nodewar.battlefield.Battlefield;
import fr.rosstail.nodewar.battlefield.BattlefieldManager;
import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.battlefield.BattlefieldSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.TeamManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BattlefieldCheckCommand extends BattlefieldSubCommand {

    public BattlefieldCheckCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_BATTLEFIELD_CHECK_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "check";
    }

    @Override
    public String getDescription() {
        return "Display info of a battlefield";
    }

    @Override
    public String getSyntax() {
        return "nodewar battlefield check <battlefield>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        Battlefield battlefield;
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TOO_FEW_ARGUMENTS));
            return;
        }

        battlefield = BattlefieldManager.getManager().getBattlefieldList().stream()
                .filter(battlefield1 -> battlefield1.getModel().getName().equalsIgnoreCase(args[2])).findFirst().orElse(null);

        if (battlefield != null) {
            sender.sendMessage(battlefield.adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_BATTLEFIELD_CHECK_RESULT)));
        } else {
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(
                    LangManager.getMessage(LangMessage.COMMANDS_WRONG_VALUE).replaceAll("\\[value]", args[2])
            ));
        }
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        return BattlefieldManager.getManager().getBattlefieldList().stream().map(battlefield -> battlefield.getModel().getName()).toList();
    }
}
