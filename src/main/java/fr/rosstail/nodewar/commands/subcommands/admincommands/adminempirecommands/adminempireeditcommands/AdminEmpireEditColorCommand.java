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
import java.util.regex.Pattern;

public class AdminEmpireEditColorCommand extends SubCommand {
    @Override
    public String getName() {
        return "color";
    }

    @Override
    public String getDescription() {
        return "Edit an empire dynmap color";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin empire edit color [empire] [color]";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.admin.empire.edit.color";
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
        String colorString = args[5];
        Pattern hexPattern = Pattern.compile("(#[a-fA-F0-9]{6})");

        if (!colorString.matches(hexPattern.pattern())) {
            sender.sendMessage(AdaptMessage.adapt(LangManager.getMessage(LangMessage.WRONG_VALUE)));
            return;
        }

        empire.setMapColor(colorString);
        sender.sendMessage("Edited color successfully !");
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
