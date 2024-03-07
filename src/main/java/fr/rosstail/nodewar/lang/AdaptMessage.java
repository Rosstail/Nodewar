package fr.rosstail.nodewar.lang;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.apis.ExpressionCalculator;
import fr.rosstail.nodewar.player.PlayerModel;
import fr.rosstail.nodewar.team.NwTeam;
import fr.rosstail.nodewar.team.TeamModel;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryModel;
import fr.rosstail.nodewar.territory.attackrequirements.AttackRequirements;
import fr.rosstail.nodewar.territory.objective.Objective;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.Bukkit.getLogger;

public class AdaptMessage {

    private static AdaptMessage adaptMessage;
    private final Nodewar plugin;
    private static final Pattern hexPattern = Pattern.compile("\\{(#[a-fA-F0-9]{6})}");
    private static final Pattern calculatePattern = Pattern.compile("\\[eval(\\w+)?_([^%\\s]*)]");

    public enum prints {
        OUT,
        WARNING,
        ERROR
    }

    public AdaptMessage(Nodewar plugin) {
        this.plugin = plugin;
    }


    public static void initAdaptMessage(Nodewar plugin) {
        adaptMessage = new AdaptMessage(plugin);
    }

    public static AdaptMessage getAdaptMessage() {
        return adaptMessage;
    }

    private final Map<Player, Long> coolDown = new HashMap<>();

    public void sendToPlayer(Player player, String message) {
        if (message != null) {
            if (message.startsWith("[msg-title]")) {
                message = message.replace("[msg-title]", "").trim();
                String title;
                String subTitle = null;
                String[] titles = message.split("\\[msg-subtitle]");
                title = titles[0];
                if (titles.length > 1) {
                    subTitle = titles[1];
                }
                sendTitle(player, title.trim(), subTitle != null ? subTitle.trim() : null);
            } else if (message.startsWith("[msg-actionbar]")) {
                sendActionBar(player, message.replace("[msg-actionbar]", "").trim());
            } else if (message.startsWith("[msg]")) {
                player.sendMessage(message.replace("[msg]", "").trim());
            } else {
                player.sendMessage(message);
            }
        }
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                adaptMessage(adaptPlayerMessage(player, message, PlayerType.PLAYER.getText()))
        ));
    }

    private void sendTitle(Player player, String title, String subTitle) {
        ConfigData.ConfigLocale configLocale = ConfigData.getConfigData().locale;
        player.sendTitle(adaptPlayerMessage(player, title, PlayerType.PLAYER.getText()),
                adaptPlayerMessage(player, subTitle, PlayerType.PLAYER.getText()),
                configLocale.titleFadeIn, configLocale.titleStay, configLocale.titleFadeOut);
    }

    public String adaptPvpMessage(Player attacker, Player victim, String message) {
        message = adaptPlayerMessage(attacker, message, PlayerType.ATTACKER.getText());
        message = adaptPlayerMessage(victim, message, PlayerType.VICTIM.getText());

        message = setPlaceholderMessage(attacker, message);
        message = setPlaceholderMessage(victim, message);
        return ChatColor.translateAlternateColorCodes('&', adaptMessage(message));
    }

    public String adaptPlayerMessage(Player player, String message, String playerType) {
        message = message.replaceAll("\\[" + playerType + "]", player.getName());
        if (Objects.equals(playerType, PlayerType.PLAYER.getText())) {
            message = ChatColor.translateAlternateColorCodes('&', setPlaceholderMessage(player, message));
        }
        return adaptMessage(message);
    }

    public String adaptTeamMessage(String message, NwTeam team) {
        message = message.replaceAll("\\[team_name]", team.getModel().getName());
        message = message.replaceAll("\\[team_id]", String.valueOf(team.getModel().getId()));
        message = message.replaceAll("\\[team_display]", team.getModel().getDisplay());
        message = message.replaceAll("\\[team_hexcolor]", team.getModel().getHexColor());
        message = message.replaceAll("\\[team_open]", String.valueOf(team.getModel().isOpen()));
        message = message.replaceAll("\\[team_permanent]", String.valueOf(team.getModel().isPermanent()));
        message = message.replaceAll("\\[team_creation_date]", String.valueOf(team.getModel().getCreationDate()));

        return message;
    }

    public String adaptTerritoryMessage(Territory territory, String message) {
        message = message.replaceAll("\\[territory_name]", territory.getModel().getName());
        message = message.replaceAll("\\[territory_display]", territory.getModel().getDisplay());
        message = message.replaceAll("\\[territory_world]", territory.getModel().getWorldName());
        message = message.replaceAll("\\[territory_prefix]", territory.getModel().getPrefix());
        message = message.replaceAll("\\[territory_suffix]", territory.getModel().getSuffix());
        message = message.replaceAll("\\[territory_team_name]", territory.getOwnerTeam() != null ? territory.getOwnerTeam().getModel().getName() : "None");
        message = message.replaceAll("\\[territory_objective_name]", territory.getModel().getObjectiveTypeName());

        return message;
    }

    public String adaptMessageToModel(PlayerModel playerModel, String message, String playerType) {
        Player player = Bukkit.getPlayer(playerModel.getUsername());
        boolean isPlayerOnline = player != null && player.isOnline();

        String starter = "\\[" + playerType;
        String ender = "]";

        message = message.replaceAll(starter + ender, playerModel.getUsername());
        message = message.replaceAll(starter + "_uuid" + ender, playerModel.getUuid());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(LangManager.getMessage(LangMessage.FORMAT_DATETIME));

        message = message.replaceAll(starter + "_status" + ender,
                LangManager.getMessage(isPlayerOnline ? LangMessage.PLAYER_ONLINE : LangMessage.PLAYER_OFFLINE));

        message = message.replaceAll(starter + "_last_update" + ender, playerModel.getLastUpdate() > 0L ? simpleDateFormat.format(playerModel.getLastUpdate()) : LangManager.getMessage(LangMessage.FORMAT_DATETIME_NEVER));
        return adaptMessage(message);
    }

    public String adaptMessage(String message) {
        if (message == null) {
            return null;
        }

        message = message.replaceAll("\\[timestamp]", String.valueOf(System.currentTimeMillis()));
        message = message.replaceAll("\\[now]", String.valueOf(System.currentTimeMillis()));

        message = ChatColor.translateAlternateColorCodes('&', setPlaceholderMessage(null, message));
        if (Integer.parseInt(Bukkit.getVersion().split("\\.")[1].replaceAll("\\)", "")) >= 16) {
            message = colorFormat(message);
        }

        Matcher matcher = calculatePattern.matcher(message);

        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String type = matcher.group(1);
            String expression = matcher.group(2);
            double result = ExpressionCalculator.eval(expression);
            String replacement;
            if (type != null) {
                switch (type) {
                    case "int":
                        replacement = String.valueOf((int) result);
                        break;
                    case "float":
                        replacement = String.valueOf((float) result);
                        break;
                    case "format":
                        replacement = decimalFormat((float) result, '.');
                        break;
                    default:
                        replacement = String.valueOf(result);
                        break;
                }
            } else {
                replacement = String.valueOf(result);
            }
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }

    public String[] listMessage(Player player, List<String> messages) {
        ArrayList<String> newMessages = new ArrayList<>();
        messages.forEach(s -> {
            newMessages.add(adaptMessage(adaptPlayerMessage(player, s, PlayerType.PLAYER.getText())));
        });
        return newMessages.toArray(new String[0]);
    }

    public String colorFormat(String message) {
        Matcher hexMatcher = hexPattern.matcher(message);
        while (hexMatcher.find()) {
            try {
                String matched = hexMatcher.group(0);
                String color = hexMatcher.group(1);
                message = message.replace(matched, String.valueOf(ChatColor.of(color)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return message;
    }

    public String decimalFormat(float value, char replacement) {
        ConfigData configData = ConfigData.getConfigData();
        int decimal = configData.locale.decNumber;
        return String.format("%." +  decimal + "f", value).replaceAll(",", String.valueOf(replacement));
    }

    /**
     * Format the given value to a formatted String depending on the config.
     *
     * @param diff
     * @return
     */
    public String countdownFormatter(long diff) {
        String format = LangManager.getMessage(LangMessage.FORMAT_COUNTDOWN);
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        long hoursInDay = TimeUnit.MILLISECONDS.toHours(diff) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(diff));
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long minutesInHour = TimeUnit.MILLISECONDS.toMinutes(diff)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diff));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        long secondsInMinute = TimeUnit.MILLISECONDS.toSeconds(diff)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diff));

        format = format.replaceAll("\\{dd}", (days / 10 == 0 ? "0" : "") + days);
        format = format.replaceAll("\\{d}", String.valueOf(days));

        format = format.replaceAll("\\{HH}", (hoursInDay / 10 == 0 ? "0" : "") + hoursInDay);
        format = format.replaceAll("\\{H}", String.valueOf(hoursInDay));
        format = format.replaceAll("\\{hh}", (hours / 10 == 0 ? "0" : "") + hours);
        format = format.replaceAll("\\{h}", String.valueOf(hours));

        format = format.replaceAll("\\{mm}", (minutesInHour / 10 == 0 ? "0" : "") + minutesInHour);
        format = format.replaceAll("\\{m}", String.valueOf(minutesInHour));
        format = format.replaceAll("\\{MM}", (minutes / 10 == 0 ? "0" : "") + minutes);
        format = format.replaceAll("\\{M}", String.valueOf(minutes));

        format = format.replaceAll("\\{ss}", (secondsInMinute / 10 == 0 ? "0" : "") + secondsInMinute);
        format = format.replaceAll("\\{s}", String.valueOf(secondsInMinute));
        format = format.replaceAll("\\{SS}", (seconds / 10 == 0 ? "0" : "") + seconds);
        format = format.replaceAll("\\{S}", String.valueOf(seconds));

        return format;
    }

    /**
     * Apply placeholder of every plugin into the message
     *
     * @param player
     * @param message
     * @return
     */
    private String setPlaceholderMessage(Player player, String message) {
        if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return PlaceholderAPI.setPlaceholders(player, message);
        }
        return message;
    }

    /**
     * A simple print command
     *
     * @param string The string to print
     * @param print  The format
     */
    public static void print(String string, prints print) {
        if (print.equals(prints.ERROR)) {
            getLogger().severe(string);
        } else if (print.equals(prints.WARNING)) {
            getLogger().warning(string);
        } else {
            getLogger().info(string);
        }
    }

    /**
     * Calculate from an expression and optional current wanted time of a player
     *
     * @param currentWantedTime Long, Current wanted time of player.
     * @param expression        String, add time with parameters suchs as Xh for x hours (s, m, h, d)
     * @return the calculated duration in ms (Long)
     */
    public static long evalDuration(Long currentWantedTime, String expression) {
        List<String> matches = Arrays.asList("(\\d+)s", "(\\d+)m", "(\\d+)h", "(\\d+)d");
        List<Integer> ints = Arrays.asList(1000, 60, 60, 24);

        int multiplier = 1;
        long totalTimeMs = 0;
        for (int i = 0; i < matches.size(); i++) {
            Pattern pattern = Pattern.compile(matches.get(i));
            multiplier *= ints.get(i);
            Matcher matcher = pattern.matcher(expression.replaceAll(" ", ""));
            if (matcher.find()) {
                totalTimeMs += (long) Integer.parseInt(String.valueOf(matcher.group(1))) * multiplier;
            }
        }

        if (expression.contains("[now]") || expression.contains("[timestamp]")) {
            totalTimeMs += System.currentTimeMillis();
        }
        if (expression.contains("[player_wanted_time]")) {
            totalTimeMs += Math.max(System.currentTimeMillis(), currentWantedTime);
        }

        return totalTimeMs;
    }

    public String adaptTerritoryMessage(String message, Territory territory) {
        TerritoryModel territoryModel = territory.getModel();
        Objective objective = territory.getObjective();
        AttackRequirements attackRequirements = territory.getAttackRequirements();
        message = message.replaceAll("\\[territory_prefix]", territoryModel.getPrefix());
        message = message.replaceAll("\\[territory_suffix]", territoryModel.getSuffix());
        message = message.replaceAll("\\[territory_name]", territoryModel.getName());
        message = message.replaceAll("\\[territory_display]", territoryModel.getDisplay());
        message = message.replaceAll("\\[territory_world]", territoryModel.getWorldName());
        message = message.replaceAll("\\[territory_type]", territoryModel.getTypeName());
        message = message.replaceAll("\\[territory_protected]", territoryModel.isUnderProtection() ? "protected" : "vulnerable");
        message = message.replaceAll("\\[territory_owner]", territory.getOwnerTeam() != null ? territory.getOwnerTeam().getModel().getDisplay() : "unoccupied");

        return message;
    }

    public String adaptTeamMessage(String message, NwTeam nwTeam, Player player) {
        TeamModel teamModel = nwTeam.getModel();

        if (message.contains("[team_result_member_line]")) {
            String memberStringLine = LangManager.getMessage(LangMessage.COMMANDS_TEAM_CHECK_RESULT_MEMBER_LINE);
            List<String> memberStringList = new ArrayList<>();
            nwTeam.getMemberMap().forEach((member, teamMember) -> {
                memberStringList.add(memberStringLine
                        .replaceAll("\\[team_player]", member.getName())
                        .replaceAll("\\[team_player_rank]", teamMember.getRank().toString())
                );
            });
            message = message.replaceAll("\\[team_result_member_line]", String.join("\n", memberStringList));
        }

        if (message.contains("[team_result_relation_line]")) {
            String relationStringLine = LangManager.getMessage(LangMessage.COMMANDS_TEAM_CHECK_RESULT_RELATION_LINE);
            List<String> relationStringList = new ArrayList<>();
            nwTeam.getRelations().forEach((s, teamRelation) -> {
                relationStringList.add(relationStringLine
                        .replaceAll("\\[team]", s)
                        .replaceAll("\\[team_relation]", teamRelation.toString())
                );
            });
            message = message.replaceAll("\\[team_result_relation_line]", String.join("\n", relationStringList));
        }

        message = message.replaceAll("\\[team]", teamModel.getName());
        message = message.replaceAll("\\[team_display]", teamModel.getDisplay());
        message = message.replaceAll("\\[team_hexcolor]", teamModel.getHexColor());
        message = message.replaceAll("\\[team_open]", nwTeam.getModel().isOpen() ? "cpen" : "close");
        message = message.replaceAll("\\[team_online_member]", nwTeam.getMemberMap().size() + " / " + teamModel.getTeamMemberModelMap().size());
        message = message.replaceAll("\\[team_relation_default]", ConfigData.getConfigData().team.defaultRelation.toString());

        if (player != null && nwTeam.getMemberMap().containsKey(player)) {
            message = message.replaceAll("\\[team_player_rank]", nwTeam.getMemberMap().get(player).getRank().toString());
        }

        return adaptMessage(message);
    }
}
