package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.relation.adminteamrelationsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit.relation.AdminTeamEditRelationSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamDataManager;
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
        NwTeam nwTeam;

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        nwTeam = TeamDataManager.getTeamDataManager().getStringTeamMap().get(args[3]);

        if (nwTeam == null) {
            sender.sendMessage("this team does not exist");
            return;
        }

        nwTeam.getModel().setOpenRelation(true);
        StorageManager.getManager().updateTeamModel(nwTeam.getModel());
        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_RELATION_OPEN_RESULT)));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
