package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage;

import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.rank.TeamRank;
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

    protected boolean hasSenderTeamRank(Player sender, NwTeam nwTeam, TeamRank requiredRank) {
        boolean value = nwTeam.getMemberMap().get(sender).getRank().getWeight() >= requiredRank.getWeight();
        if (!value) {
            sender.sendMessage("You have not enough clearance to use this command.");
        }
        return value;
    }
}
