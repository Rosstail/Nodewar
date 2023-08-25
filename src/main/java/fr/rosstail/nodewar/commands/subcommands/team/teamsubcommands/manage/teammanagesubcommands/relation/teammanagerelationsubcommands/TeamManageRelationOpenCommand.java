package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.relation.teammanagerelationsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.relation.TeamManageRelationSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.team.TeamManager;
import fr.rosstail.nodewar.team.rank.NwTeamRank;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TeamManageRelationOpenCommand extends TeamManageRelationSubCommand {

    public TeamManageRelationOpenCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_OPEN_DESC))
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
        return "nodewar team manage relation open";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.manage.relation.open";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        NwITeam playerNwITeam;

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_BY_PLAYER_ONLY));
            return;
        }

        playerNwITeam = TeamManager.getManager().getPlayerTeam(((Player) sender));

        if (playerNwITeam == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_PART_OF_NO_TEAM));
            return;
        }
        if (!hasPlayerEnoughClearance(((Player) sender).getPlayer(), playerNwITeam, NwTeamRank.LIEUTENANT)) {
            return;
        }

        playerNwITeam.setOpenRelation(true);

        StorageManager.getManager().updateTeamModel(playerNwITeam);
        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_OPEN_RESULT)));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
