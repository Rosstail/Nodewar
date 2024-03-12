package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.admin.team.AdminTeamSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.rank.TeamRank;
import fr.rosstail.nodewar.territory.dynmap.DynmapHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class AdminTeamDisbandCommand extends AdminTeamSubCommand {

    public AdminTeamDisbandCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_DISBAND_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }
    @Override
    public String getName() {
        return "disband";
    }

    @Override
    public String getDescription() {
        return "Disband your team";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin team <team> disband <team>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String targetTeamName;
        NwTeam targetTeam;
        String teamNameConfirmStr;
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        
        if (args.length < 4) {
            sender.sendMessage("Add the team name to the command to confirm");
            return;
        }
        
        targetTeamName = args[2];
        targetTeam = TeamDataManager.getTeamDataManager().getStringTeamMap().get(targetTeamName);


        if (targetTeam == null) {
            sender.sendMessage("team does not exist");
            return;
        }

        teamNameConfirmStr = args[4];

        if (!targetTeamName.equals(teamNameConfirmStr)) {
            sender.sendMessage("wrong team name");
            return;
        }

        TeamDataManager.getTeamDataManager().deleteTeam(targetTeam.getModel().getName());

        sender.sendMessage("Disbanded team");
        DynmapHandler.getDynmapHandler().resumeRender();
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
