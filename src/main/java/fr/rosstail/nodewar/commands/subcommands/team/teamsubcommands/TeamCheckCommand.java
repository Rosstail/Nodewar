package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.relation.TeamRelation;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TeamCheckCommand extends TeamSubCommand {

    public TeamCheckCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_CHECK_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "check";
    }

    @Override
    public String getDescription() {
        return "Desc check nodewar team";
    }

    @Override
    public String getSyntax() {
        return "nodewar team check (teamName)";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.join";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        Player senderPlayer;
        PlayerData playerData;
        NwTeam team;
        String message = LangManager.getMessage(LangMessage.COMMANDS_TEAM_CHECK_RESULT);
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (args.length < 3 && !(sender instanceof Player)) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TOO_FEW_ARGUMENTS));
            return;
        }

        if (args.length >= 3) {
            team = TeamDataManager.getTeamDataManager().getStringTeamMap().get(args[2]);
            if (team != null) {
                message = LangManager.getMessage(LangMessage.COMMANDS_TEAM_CHECK_RESULT_OTHER);
                sender.sendMessage(AdaptMessage.getAdaptMessage().adaptTeamMessage(message, team, null));
            } else {
                message = LangManager.getMessage(LangMessage.COMMANDS_WRONG_VALUE).replaceAll("\\[value]", args[2]);
                sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(message));
            }
        } else {
            senderPlayer = ((Player) sender).getPlayer();
            playerData = PlayerDataManager.getPlayerDataMap().get(senderPlayer.getName());

            if (playerData.getTeam() == null) {
                sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(LangManager.getMessage(LangMessage.COMMANDS_PLAYER_NOT_IN_TEAM)));
                return;
            }
            team = playerData.getTeam();
            sender.sendMessage(AdaptMessage.getAdaptMessage().adaptTeamMessage(message, team, senderPlayer));
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return new ArrayList<>(TeamDataManager.getTeamDataManager().getStringTeamMap().keySet());
    }
}
