package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TeamOpenInvitesCommand extends TeamSubCommand {

    public TeamOpenInvitesCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_OPENINVITES_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));

    }

    @Override
    public String getName() {
        return "openinvites";
    }

    @Override
    public String getDescription() {
        return "Receive any team invitation";
    }

    @Override
    public String getSyntax() {
        return "nodewar team openinvites";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.openinvites";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        Player player;
        StringBuilder message = new StringBuilder(LangManager.getMessage(LangMessage.COMMANDS_TEAM_OPENINVITES_RESULT));
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("by player only");
            return;
        }

        player = ((Player) sender).getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerDataFromMap(player);

        playerData.setTeamOpen(true);
        StorageManager.getManager().updatePlayerModel(playerData, true);

        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(message.toString()));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
