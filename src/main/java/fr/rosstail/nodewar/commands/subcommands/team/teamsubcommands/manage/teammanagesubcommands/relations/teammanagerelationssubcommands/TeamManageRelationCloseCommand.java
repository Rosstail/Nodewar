package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.relations.teammanagerelationssubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands.relations.TeamManageRelationSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.RelationType;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.rank.TeamRank;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TeamManageRelationCloseCommand extends TeamManageRelationSubCommand {

    public TeamManageRelationCloseCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_CLOSE_DESC))
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
        return "nodewar team manage relation close";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.manage.relation.close";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        NwTeam playerNwTeam;

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("player only");
            return;
        }

        playerNwTeam = TeamDataManager.getTeamDataManager().getTeamOfPlayer(((Player) sender));

        if (playerNwTeam == null) {
            sender.sendMessage("You are part of no team");
            return;
        }
        if (playerNwTeam.getMemberMap().get(((Player) sender)).getRank().getWeight() < TeamRank.LIEUTENANT.getWeight()) {
            sender.sendMessage("You are not high enough rank of the team");
            return;
        }

        playerNwTeam.getModel().setOpenRelation(false);
        StorageManager.getManager().updateTeamModel(playerNwTeam.getModel());
        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_RELATION_CLOSE_RESULT)));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
