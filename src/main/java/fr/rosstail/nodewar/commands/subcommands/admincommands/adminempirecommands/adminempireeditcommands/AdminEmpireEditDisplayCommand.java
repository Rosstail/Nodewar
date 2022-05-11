package fr.rosstail.nodewar.commands.subcommands.admincommands.adminempirecommands.adminempireeditcommands;

import fr.rosstail.nodewar.commands.SubCommand;
import fr.rosstail.nodewar.datahandlers.PlayerInfo;
import fr.rosstail.nodewar.datahandlers.PlayerInfoManager;
import fr.rosstail.nodewar.empires.Empire;
import fr.rosstail.nodewar.empires.EmpireManager;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminEmpireEditDisplayCommand extends SubCommand {
    @Override
    public String getName() {
        return "display";
    }

    @Override
    public String getDescription() {
        return "Edit an empire display name";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin empire edit display [empire] [display1] <displayX>";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.admin.empire.edit.display";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(AdaptMessage.adapt(LangManager.getMessage(LangMessage.PERMISSION_DENIED)));
            return;
        }

        if (args.length < 6) {
            sender.sendMessage(AdaptMessage.adapt(LangManager.getMessage(LangMessage.TOO_FEW_ARGUMENTS)));
            return;
        }
        Empire empire = EmpireManager.getEmpireManager().getEmpires().get(args[4]);

        StringBuilder display = new StringBuilder();
        for (int i = 5; i < args.length; i++) {
            if (i > 5) {
                display.append(" ");
            }
            display.append(args[i]);
        }
        empire.setDisplay(AdaptMessage.empireMessage(empire, String.valueOf(display)));
        sender.sendMessage("The empire " + empire.getName() + "  is now displayed as " + display);
        empire.saveConfigFile();
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args) {
        if (args.length == 5){
            return new ArrayList<>(EmpireManager.getEmpireManager().getEmpires().keySet());
        }
        return null;
    }
}
