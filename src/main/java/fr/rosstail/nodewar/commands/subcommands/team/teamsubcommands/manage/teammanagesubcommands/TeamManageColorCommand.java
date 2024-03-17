package fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.teammanagesubcommands;

import fr.rosstail.nodewar.commands.CommandManager;
import fr.rosstail.nodewar.commands.subcommands.team.teamsubcommands.manage.TeamManageSubCommand;
import fr.rosstail.nodewar.lang.AdaptMessage;
import fr.rosstail.nodewar.lang.LangManager;
import fr.rosstail.nodewar.lang.LangMessage;
import fr.rosstail.nodewar.storage.StorageManager;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamDataManager;
import fr.rosstail.nodewar.team.rank.TeamRank;
import fr.rosstail.nodewar.territory.dynmap.DynmapHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class TeamManageColorCommand extends TeamManageSubCommand {

    private static final Pattern hexPattern = Pattern.compile("#[a-fA-F0-9]{6}");

    public TeamManageColorCommand() {
        help = AdaptMessage.getAdaptMessage().adaptMessage(
                LangManager.getMessage(LangMessage.COMMANDS_HELP_LINE)
                        .replaceAll("\\[desc]", LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_COLOR_DESC))
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
        return "nodewar team manage color <hexcolor>";
    }

    @Override
    public String getHelp() {
        return super.getHelp();
    }

    @Override
    public String getPermission() {
        return "nodewar.command.team.manage.color";
    }

    @Override
    public void perform(CommandSender sender, String[] args, String[] arguments) {
        Player player;
        NwTeam playerNwTeam;
        String colorValue;
        if (!CommandManager.canLaunchCommand(sender, this)) {
            return;
        }
        if (!(sender instanceof Player)) {
            return;
        }

        player = ((Player) sender).getPlayer();
        playerNwTeam = TeamDataManager.getTeamDataManager().getTeamOfPlayer(player);

        if (playerNwTeam == null) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_PART_OF_NO_TEAM));
            return;
        }

        if (!hasSenderTeamRank(((Player) sender).getPlayer(), playerNwTeam, TeamRank.OWNER)) {
            return;
        }

        if (args.length < 4) {
            sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_TOO_FEW_ARGUMENTS));
            return;
        }

        colorValue = args[3].toUpperCase();


        if (colorValue.startsWith("#")) {
            if (hexPattern.matcher(colorValue).find()) {
                if (Integer.parseInt(Bukkit.getVersion().split("\\.")[1]) < 16) {
                    sender.sendMessage("you cannot use HEX values on 1.13 and lower.");
                    return;
                }
            } else {
                sender.sendMessage("the hex color must be in this format: '#RRGGBB'. Ex: '#CA734F'");
            }
        } else {
            try {
                ChatColor.valueOf(colorValue);
            } catch (IllegalArgumentException e) {sender.sendMessage(LangManager.getMessage(LangMessage.COMMANDS_WRONG_VALUE));
                return;
            }
        }
        playerNwTeam.getModel().setTeamColor(colorValue);

        sender.sendMessage(
                AdaptMessage.getAdaptMessage().adaptTeamMessage(LangManager.getMessage(LangMessage.COMMANDS_TEAM_MANAGE_COLOR_RESULT), playerNwTeam, player)
        );

        StorageManager.getManager().updateTeamModel(playerNwTeam.getModel());
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
