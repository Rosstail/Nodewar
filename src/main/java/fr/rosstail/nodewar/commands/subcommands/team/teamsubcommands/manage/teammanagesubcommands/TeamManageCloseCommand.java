package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.TeamManageSubCommand;
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

public class TeamManageCloseCommand extends TeamManageSubCommand {

    public TeamManageCloseCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_CLOSE_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }
    @Override
    public String getName() {
        return "close";
    }

    @Override
    public String getDescription() {
        return "Close your team";
    }

    @Override
    public String getSyntax() {
        return "nodewar team manage close";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.manage.close";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        Player senderPlayer;
        NwITeam playerNwITeam;
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_BY_PLAYER_ONLY));
            return;
        }
        senderPlayer = ((Player) sender).getPlayer();
        playerNwITeam = TeamManager.getManager().getPlayerTeam(senderPlayer);

        if (playerNwITeam == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_PART_OF_NO_TEAM));
            return;
        }

        if (!playerNwITeam.hasPlayerEnoughClearance(senderPlayer)) {
            return;
        }

        if (!hasPlayerEnoughClearance(((Player) sender).getPlayer(), playerNwITeam, NwTeamRank.OWNER)) {
            return;
        }

        playerNwITeam.setOpen(false);

        sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_CLOSE_RESULT));

        StorageManager.getManager().updateTeamModel(playerNwITeam);
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
