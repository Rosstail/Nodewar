package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagecommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.TeamManageSubCommand;
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

public class TeamManageOpenCommand extends TeamManageSubCommand {

    public TeamManageOpenCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_OPEN_DESC))
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
        return "nodewar team manage open";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.manage.open";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        boolean value;
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (sender instanceof Player) {
            Player player = ((Player) sender).getPlayer();
            NwTeam playerNwTeam = TeamDataManager.getTeamDataManager().getTeamOfPlayer(player);

            if (playerNwTeam == null) {
                sender.sendMessage("Your team is null");
                return;
            }

            if (playerNwTeam.getMemberMap().get(player).getRank() != TeamRank.OWNER) {
                sender.sendMessage("you do not have enough rank on your team");
                return;
            }

            playerNwTeam.getModel().setOpen(true);

            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_OPEN_RESULT));
            StorageManager.getManager().updateTeamModel(playerNwTeam.getModel());
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
