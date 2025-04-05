package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.NwTeamInvite;
import fr.rosstail.nodewar.team.TeamManager;
import fr.rosstail.nodewar.team.member.TeamMemberModel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeamJoinCommand extends TeamSubCommand {

    public TeamJoinCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_JOIN_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getDescription() {
        return "Desc join nodewar team";
    }

    @Override
    public String getSyntax() {
        return "nodewar team join <name>";
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
        NwITeam nwTeam;
        PlayerData playerData;
        NwTeamInvite teamInvite = null;
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_BY_PLAYER_ONLY));
            return;
        }
        senderPlayer = ((Player) sender).getPlayer();
        playerData = PlayerDataManager.getPlayerDataMap().get(senderPlayer.getName());

        if (playerData.getTeam() != null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_PLAYER_ALREADY_IN_TEAM));
            return;
        } else if (args.length < 3) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TOO_FEW_ARGUMENTS));
            return;
        }

        teamName = args[2];
        nwTeam = TeamManager.getManager().getStringTeamMap().get(teamName);

        if (nwTeam == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_VALUE));
            return;
        }
        
        if (!nwTeam.isOpenRelation()) {
            teamInvite = TeamManager.getManager().getTeamInviteHashSet().stream()
                    .filter(nwTeamInvite -> nwTeamInvite.getNwTeam() == nwTeam && nwTeamInvite.getReceiver() == sender)
                    .findFirst().orElse(null);
            if (teamInvite == null) {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_JOIN_RESULT_UNINVITED));
                return;
            }
        }

        if (ConfigData.getConfigData().team.maximumMembers > -1 && nwTeam.getMemberMap().size() >= ConfigData.getConfigData().team.maximumMembers) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_FULL));
            return;
        }

        if (teamInvite != null) {
            TeamManager.getManager().getTeamInviteHashSet().remove(teamInvite);
        }

        sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_JOIN_RESULT));
        TeamManager.getManager().createOnlineTeamMember(nwTeam, senderPlayer);
    }

    @Override
    public List<String> getSubCommandsArguments(CommandSender sender, String[] args, String[] arguments) {
        Set<String> teamSet = new HashSet<>();
        TeamManager.getManager().getStringTeamMap().values().stream().filter(NwITeam::isOpen).forEach(nwTeam -> {
            teamSet.add(nwTeam.getName());
        });

        TeamManager.getManager().getTeamInviteHashSet().stream().filter(nwTeamInvite -> (
                nwTeamInvite.getReceiver() == sender
        )).forEach(nwTeamInvite -> {
            teamSet.add(nwTeamInvite.getNwTeam().getModel().getName());
        });
        return new ArrayList<>(teamSet);
    }
}
