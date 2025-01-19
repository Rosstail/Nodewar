package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.relation.adminteamrelationsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.relation.AdminTeamEditRelationSubCommand;
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

public class AdminTeamEditRelationOpenCommand extends AdminTeamEditRelationSubCommand {

    public AdminTeamEditRelationOpenCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_RELATION_OPEN_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "open";
    }

    @Override
    public String getDescription() {
        return "Enable reception of relation invitation";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin team edit <team> relation open";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        NwITeam nwITeam;

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        nwITeam = TeamManager.getManager().getTeam(args[3]);

        if (nwITeam == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_DOES_NOT_EXIST));
            return;
        }

        nwITeam.setOpenRelation(true);
        StorageManager.getManager().updateTeamModel(nwITeam);
        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_RELATION_OPEN_RESULT)));
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        return null;
    }
}
