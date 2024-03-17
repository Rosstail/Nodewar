package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.invites.teaminvitessubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.invites.TeamInvitesSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwTeamInvite;
import fr.rosstail.nodewar.team.TeamDataManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TeamInvitesCheckCommand extends TeamInvitesSubCommand {

    public TeamInvitesCheckCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_INVITES_CHECK_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));

    }

    @Override
    public String getName() {
        return "check";
    }

    @Override
    public String getDescription() {
        return "Blocks any team invitation";
    }

    @Override
    public String getSyntax() {
        return "nodewar team invites check";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.invites.check";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        Player player;
        StringBuilder message = new StringBuilder(LangManager.getMessage(LangMessage.COMMANDS_TEAM_INVITES_CHECK_RESULT_HEADER));
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_BY_PLAYER_ONLY));
            return;
        }

        player = ((Player) sender).getPlayer();

        Set<NwTeamInvite> playerInvites = TeamDataManager.getTeamDataManager().getTeamInviteHashSet().stream().filter(nwTeamInvite -> (
                nwTeamInvite.getReceiver().equals(player)
        )).collect(Collectors.toSet());

        playerInvites.forEach(nwTeamInvite -> {
            message.append("\n").append(AdaptMessage.getAdaptMessage().adaptTeamMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_INVITES_CHECK_RESULT_LINE), nwTeamInvite.getNwTeam()));
        });
        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(message.toString()));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
