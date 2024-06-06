package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands;

import fr.rosstail.nodewar.ConfigData;
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

import java.text.Normalizer;
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
        return "nodewar admin team create <team> <short>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String teamName;
        String displayTeamName;
        String shortName;
        TeamModel teamModel;
        NwTeam team;
        TeamDataManager teamDataManager = TeamDataManager.getTeamDataManager();

        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (args.length < 5) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TOO_FEW_ARGUMENTS));
            return;
        }

        teamName = Normalizer.normalize(ChatColor.stripColor(args[3]).toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");;

        displayTeamName = args[3];
        shortName = args[4];

        if (shortName.length() > ConfigData.getConfigData().team.minimumNameLength && shortName.length() > ConfigData.getConfigData().team.maximumNameLength) {
            String message = LangManager.getMessage(teamName.length() > 40 ? LangMessage.COMMANDS_TEAM_CREATE_TOO_LONG : LangMessage.COMMANDS_TEAM_CREATE_TOO_SHORT);
            sender.sendMessage(message.replaceAll("\\[name]", teamName));
            return;
        }

        if (shortName.length() > ConfigData.getConfigData().team.minimumShortnameLength && shortName.length() > ConfigData.getConfigData().team.maximumShortNameLength) {
            String message = LangManager.getMessage(shortName.length() > 40 ? LangMessage.COMMANDS_TEAM_CREATE_TOO_LONG : LangMessage.COMMANDS_TEAM_CREATE_TOO_SHORT);
            sender.sendMessage(message.replaceAll("\\[name]", shortName));
            return;
        }

        if (teamDataManager.getStringTeamMap().containsKey(teamName)) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_ALREADY_EXIST).replaceAll("\\[name]", teamName));
            return;
        }
        if (teamDataManager.getStringTeamMap().values().stream().anyMatch(team1 -> team1.getModel().getShortName().equalsIgnoreCase(shortName))) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_ALREADY_EXIST).replaceAll("\\[name]", shortName));
            return;
        }

        teamModel = new TeamModel(teamName, displayTeamName, shortName, teamDataManager.generateRandomColor());
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
