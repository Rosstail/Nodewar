package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.relation.adminteamrelationsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.relation.AdminTeamEditRelationSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.team.TeamManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class AdminTeamEditRelationCloseCommand extends AdminTeamEditRelationSubCommand {

    public AdminTeamEditRelationCloseCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_RELATION_CLOSE_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "close";
    }

    @Override
    public String getDescription() {
        return "Disable reception of any relation invitation";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin team edit <team> relation close";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        NwITeam nwTeam;

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        nwTeam = TeamManager.getManager().getStringTeamMap().get(args[3]);

        if (nwTeam == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_DOES_NOT_EXIST));
            return;
        }

        nwTeam.setOpenRelation(false);
        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_RELATION_CLOSE_RESULT)));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
