package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.admin.team.AdminTeamSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.rank.TeamRank;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class AdminTeamOpenCommand extends AdminTeamSubCommand {

    public AdminTeamOpenCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_OPEN_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }
    @Override
    public String getName() {
        return "open";
    }

    @Override
    public String getDescription() {
        return "Open your team";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin team <team> open";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String targetTeamName = args[2];
        NwTeam targetTeam = TeamDataManager.getTeamDataManager().getStringTeamMap().get(targetTeamName);

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (targetTeam == null) {
            sender.sendMessage("Your team is null");
            return;
        }

        targetTeam.getModel().setOpen(true);

        sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_OPEN_RESULT));
        StorageManager.getManager().updateTeamModel(targetTeam.getModel());
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
