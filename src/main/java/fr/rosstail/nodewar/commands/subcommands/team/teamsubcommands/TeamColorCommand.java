package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.TeamSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.player.PlayerData;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.rank.TeamRank;
import fr.rosstail.nodewar.territory.dynmap.DynmapHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TeamColorCommand extends TeamSubCommand {

    private static final Pattern hexPattern = Pattern.compile("#[a-fA-F0-9]{6}");

    public TeamColorCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_COLOR_DESC))
                        .replaceAll("\\[syntax]", getSyntax()));
    }

    @Override
    public String getName() {
        return "color";
    }

    @Override
    public String getDescription() {
        return "Change your team color";
    }

    @Override
    public String getSyntax() {
        return "nodewar team color <hexcolor>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.color";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String value;
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (sender instanceof Player) {
            Player player = ((Player) sender).getPlayer();
            NwTeam playerNwTeam = TeamDataManager.getTeamDataManager().getTeamOfPlayer(player);

            if (playerNwTeam == null) {
                sender.sendMessage("Your team is null");
                return;
            }

            if (playerNwTeam.getMemberMap().get(player).getRank() != TeamRank.OWNER) {
                sender.sendMessage("you do not have enough rank on your team");
                return;
            }

            if (args.length < 3) {
                sender.sendMessage("Not enough arguments");
                return;
            }

            value = args[2];

            if (!hexPattern.matcher(value).find()) {
                sender.sendMessage("Wrong argument ex: #CD9F16");
                return;
            }
            playerNwTeam.getModel().setHexColor(value);

            sender.sendMessage(
                    AdaptMessage.getAdaptMessage().adaptTeamMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_COLOR_RESULT), playerNwTeam, player)
            );

            StorageManager.getManager().updateTeamModel(playerNwTeam.getModel());
            DynmapHandler.getDynmapHandler().resumeRender();
        }
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        List<String> list = new ArrayList<>();
        list.add("#");
        return list;
    }
}
