package fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.edit;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.admin.team.AdminTeamSubCommand;
import fr.rosstail.nodewar.commands.subcommands.admin.team.adminteamsubcommands.AdminTeamEditSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.territory.dynmap.DynmapHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class AdminTeamEditColorCommand extends AdminTeamEditSubCommand {

    private static final Pattern hexPattern = Pattern.compile("#[a-fA-F0-9]{6}");

    public AdminTeamEditColorCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_COLOR_DESC))
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
        return "nodewar admin team edit <team> color <hexcolor>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        String targetTeamName;
        NwTeam targetTeam;
        String colorValue;
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }

        if (args.length < 6) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TOO_FEW_ARGUMENTS));
            return;
        }

        targetTeamName = args[3];
        targetTeam = TeamDataManager.getTeamDataManager().getStringTeamMap().get(targetTeamName);

        if (targetTeam == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_DOES_NOT_EXIST));
            return;
        }

        colorValue = args[5].toUpperCase();

        if (colorValue.startsWith("#")) {
            if (hexPattern.matcher(colorValue).find()) {
                if (Integer.parseInt(Bukkit.getVersion().split("\\.")[1]) < 16) {
                    sender.sendMessage("you cannot use HEX values on 1.13 and lower.");
                    return;
                }
            } else {
                sender.sendMessage("the hex color must be in this format: '#RRGGBB'. Ex: '#CA734F'");
                return;
            }
        } else {
            try {
                ChatColor.valueOf(colorValue);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_VALUE).replaceAll("\\[value]", colorValue));
                return;
            }
        }

        targetTeam.getModel().setTeamColor(colorValue);

        StorageManager.getManager().updateTeamModel(targetTeam.getModel());

        sender.sendMessage(
                AdaptMessage.getAdaptMessage().adaptTeamMessage(LangManager.getMessage(LangMessage.COMMANDS_ADMIN_TEAM_EDIT_COLOR_RESULT), targetTeam, null)
        );

        DynmapHandler.getDynmapHandler().resumeRender();
    }

    @Override
    public List<String> getSubCommandsArguments(Player sender, String[] args, String[] arguments) {
        List<String> list = new ArrayList<>();
        if (Integer.parseInt(Bukkit.getVersion().split("\\.")[1]) >= 16) {
            list.add("#");
        }
        Arrays.stream(ChatColor.values()).filter(ChatColor::isColor).forEach(chatColor -> {
            list.add(chatColor.name());
        });
        return list;
    }
}
