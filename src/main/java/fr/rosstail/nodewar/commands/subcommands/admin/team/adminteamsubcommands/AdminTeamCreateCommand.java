package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.admin.team.AdminTeamSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.TeamModel;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class AdminTeamCreateCommand extends AdminTeamSubCommand {

    public AdminTeamCreateCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_CREATE_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "Create new team";
    }

    @Override
    public String getSyntax() {
        return "nodewar admin team create <team>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String teamName;
        String displayTeamName;
        TeamModel teamModel;
        NwTeam team;
        TeamDataManager teamDataManager = TeamDataManager.getTeamDataManager();

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (args.length < 4) {
            sender.sendMessage("too few args");
            return;
        }

        teamName = ChatColor.stripColor(args[3].toLowerCase());
        displayTeamName = args[3];

        if (TeamDataManager.getTeamDataManager().getStringTeamMap().containsKey(teamName)) {
            sender.sendMessage("this team already exist");
            return;
        }

        teamModel = new TeamModel(teamName, displayTeamName, teamDataManager.generateRandomColor());
        teamModel.setOpenRelation(false);
        teamModel.setPermanent(true);
        StorageManager.getManager().insertTeamModel(teamModel);

        team = new NwTeam(teamModel);
        teamDataManager.addNewTeam(team);

        sender.sendMessage(
                AdaptMessage.getAdaptMessage().adaptMessage(
                        AdaptMessage.getAdaptMessage().adaptTeamMessage(LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_CREATE_RESULT), team))
        );
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        return null;
    }
}
