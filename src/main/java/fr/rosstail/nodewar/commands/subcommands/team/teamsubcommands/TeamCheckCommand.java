package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.member.TeamMember;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import fr.rosstail.nodewar.team.relation.TeamRelation;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        return "nodewar team check";
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
        String teamName;
        NwTeam nwTeam;
        PlayerData playerData;
        TeamMemberModel teamMemberModel;
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Must be sent by player");
            return;
        }
        senderPlayer = ((Player) sender).getPlayer();
        playerData = PlayerDataManager.getPlayerDataMap().get(senderPlayer.getName());

        if (playerData.getTeam() == null) {
            sender.sendMessage("You are not on a team");
            return;
        }

        NwTeam playerTeam = playerData.getTeam();
        String message = LangManager.getMessage(LangMessage.COMMANDS_TEAM_CHECK_RESULT);
        message = message.replaceAll("\\[team]", playerTeam.getModel().getName());
        message = message.replaceAll("\\[team_display]", playerTeam.getModel().getDisplay());
        message = message.replaceAll("\\[team_player_rank]", playerTeam.getMemberMap().get(senderPlayer).getRank().toString());
        message = message.replaceAll("\\[team_member_count]", playerTeam.getMemberMap().size() + " / " + playerTeam.getModel().getTeamMemberModelMap().size());

        /*message.append("\n > Connected:");
        for (Map.Entry<Player, TeamMember> entry : playerTeam.getMemberMap().entrySet()) {
            Player player = entry.getKey();
            TeamMember teamMember = entry.getValue();
            message.append("\n    - ").append(player.getName());
        }
        message.append("\n > Relations:");
        for (Map.Entry<String, TeamRelation> entry : playerTeam.getRelations().entrySet()) {
            String s = entry.getKey();
            TeamRelation teamRelation = entry.getValue();
            message.append("\n    - ").append(s).append(" ").append(teamRelation.getRelationType().toString());
        }*/
        sender.sendMessage(AdaptMessage.getAdaptMessage().adaptMessage(message));
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
