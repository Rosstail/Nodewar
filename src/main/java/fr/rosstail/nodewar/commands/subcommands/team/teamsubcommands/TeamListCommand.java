package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.team.TeamManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class TeamListCommand extends TeamSubCommand {

    public TeamListCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_LIST_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));

    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "Desc check nodewar team list";
    }

    @Override
    public String getSyntax() {
        return "nodewar team list";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.list";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        StringBuilder message = new StringBuilder(LangManager.getMessage(LangMessage.COMMANDS_TEAM_LIST_RESULT_HEADER));
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        for (Map.Entry<String, NwITeam> entry : TeamManager.getManager().getStringTeamMap().entrySet()) {
            String s = entry.getKey();
            NwITeam nwTeam = entry.getValue();
            message.append("\n").append(AdaptMessage.getAdaptMessage().adaptTeamMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_LIST_RESULT_LINE), nwTeam));
        }
        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(message.toString()));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
