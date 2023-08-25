package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage;

import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.type.NwTeam;
import fr.rosstail.nodewar.team.rank.NwTeamRank;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class TeamManageSubCommand extends TeamSubCommand {
    @Override
    public String getName() {
        return "manage";
    }

    @Override
    public String getDescription() {
        return "Team manage command description";
    }

    @Override
    public String getSyntax() {
        return "nodewar team manage <subcommand>";
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.manage";
    }

    @Override
    public abstract List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments);

    protected boolean hasPlayerEnoughClearance(Player sender, NwITeam nwITeam, NwTeamRank requiredRank) {
        boolean value = false;
        if (nwITeam instanceof NwTeam) {
            value = nwITeam.getOnlineMemberMap().get(sender).getRank().getWeight() >= requiredRank.getWeight();
        } else {
            // TODO
        }
        if (!value) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_ERROR_CLEARANCE));
        }
        return value;
    }
}
