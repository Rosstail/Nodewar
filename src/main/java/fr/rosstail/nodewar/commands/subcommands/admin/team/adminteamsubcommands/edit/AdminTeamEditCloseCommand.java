package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.AdminTeamEditSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.team.TeamManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class AdminTeamEditCloseCommand extends AdminTeamEditSubCommand {

    public AdminTeamEditCloseCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_CLOSE_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }
    @Override
    public String getName() {
        return "close";
    }

    @Override
    public String getDescription() {
        return "Close team";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin team edit <team> close";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String targetTeamName = args[3];
        NwITeam targetTeam = TeamManager.getManager().getStringTeamMap().get(targetTeamName);

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (targetTeam == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_DOES_NOT_EXIST));
            return;
        }

        targetTeam.setOpen(false);
        StorageManager.getManager().updateTeamModel(targetTeam);

        sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_CLOSE_RESULT));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
