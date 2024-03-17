package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.invites.teaminvitessubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.invites.TeamInvitesSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TeamInvitesOpenCommand extends TeamInvitesSubCommand {

    public TeamInvitesOpenCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_INVITES_OPEN_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));

    }

    @Override
    public String getName() {
        return "open";
    }

    @Override
    public String getDescription() {
        return "Receive any team invitation";
    }

    @Override
    public String getSyntax() {
        return "nodewar team invites open";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.invites.open";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        Player player;
        String message = LangManager.getMessage(LangMessage.COMMANDS_TEAM_INVITES_OPEN_RESULT);
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_BY_PLAYER_ONLY));
            return;
        }

        player = ((Player) sender).getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerDataFromMap(player);

        playerData.setTeamOpen(true);
        StorageManager.getManager().updatePlayerModel(playerData, true);

        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(message));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
