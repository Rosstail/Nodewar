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
import java.util.stream.Collectors;

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
        return "nodewar team list (page)";
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
        int page = 0;
        Map<String, NwITeam> stringNwITeamMap = TeamManager.getManager().getStringTeamMap();
        List<String> strList = TeamManager.getManager().getStringTeamMap().keySet().stream().sorted().collect(Collectors.toList());
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
                NwITeam nwTeam = stringNwITeamMap.get(s);
                message.append("\n").append(AdaptMessage.getAdaptMessage().adaptTeamMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_LIST_RESULT_LINE), nwTeam));
            }
            message.append("\nP." + (page + 1) + "/" + maxPage);
        } else {
            message.append("\n&rP.0/0");
        }
        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(message.toString()));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
