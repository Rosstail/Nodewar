package fr.rosstail.nodewar.lang;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.apis.ExpressionCalculator;
import fr.rosstail.nodewar.player.PlayerModel;
import fr.rosstail.nodewar.team.NwITeam;
import fr.rosstail.nodewar.team.TeamManager;
import fr.rosstail.nodewar.team.rank.NwTeamRank;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryManager;
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
    private static final Pattern colorPattern = Pattern.compile("\\{([A-Za-z_]+)}");
    private static final Pattern calculatePattern = Pattern.compile("\\[eval(\\w+)?_([^%\\s]*)]");
    private static final Pattern territoryPattern = Pattern.compile("(\\[territory)_(\\d+)(_\\w+])");
    private static final Pattern playerTeamPattern = Pattern.compile("(\\[player_team)(_\\w+])");
    private static final Pattern teamPattern = Pattern.compile("(\\[team)_(\\d+)(_\\w+])");

    public enum prints {
        OUT,
        WARNING,
        ERROR,
        DEBUG
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
        if (message == null) {
            return;
        }

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

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                adaptMessage(adaptPlayerMessage(player, message))
        ));
    }

    private void sendTitle(Player player, String title, String subTitle) {
        ConfigData.ConfigLocale configLocale = ConfigData.getConfigData().locale;
        player.sendTitle(adaptPlayerMessage(player, title),
                adaptPlayerMessage(player, subTitle),
                configLocale.titleFadeIn, configLocale.titleStay, configLocale.titleFadeOut);
    }

    public String adaptPlayerMessage(Player player, String message) {
        message = message.replaceAll("\\[player]", player.getName());
        NwITeam playerNwITeam = TeamManager.getManager().getPlayerTeam(player);

        Matcher playerTeamMatcher = playerTeamPattern.matcher(message);

        while (playerTeamMatcher.find()) {
            message = message.replace(playerTeamMatcher.group(), "[team" + playerTeamMatcher.group(2));
            message = adaptTeamMessage(message, playerNwITeam);
        }

        message = ChatColor.translateAlternateColorCodes('&', setPlaceholderMessage(player, message));
        return adaptMessage(message);
    }

    public String adaptTeamMessage(String message, NwITeam team) {
        if (team == null) {
            message = message.replaceAll("\\[team_name]", "NONE-TEST");
            message = message.replaceAll("\\[team_display]", LangManager.getMessage(LangMessage.TEAM_NONE_DISPLAY));
            message = message.replaceAll("\\[team_color_display]", "{" + ConfigData.getConfigData().team.noneColor + "}" + LangManager.getMessage(LangMessage.TEAM_NONE_DISPLAY));
            message = message.replaceAll("\\[team_short]", "");
            message = message.replaceAll("\\[team_color_short]", "{" + ConfigData.getConfigData().team.noneColor + "}");
            message = message.replaceAll("\\[team_color]", ConfigData.getConfigData().team.noneColor);
            message = message.replaceAll("\\[team_(\\w+)]", "");
            message = message.replaceAll("\\[team_open]", "-");
            message = message.replaceAll("\\[team_permanent]", "-");
            message = message.replaceAll("\\[team_creation_date]", "-");
            return adaptMessage(message);
        }
        message = message.replaceAll("\\[team_name]", team.getName());
        message = message.replaceAll("\\[team_id]", String.valueOf(team.getID()));
        message = message.replaceAll("\\[team_display]", team.getDisplay());
        message = message.replaceAll("\\[team_color_display]", "{" + team.getTeamColor() + "}" + team.getDisplay());
        message = message.replaceAll("\\[team_short]", team.getShortName());
        message = message.replaceAll("\\[team_color_short]", "{" + team.getTeamColor() + "}" + team.getShortName());
        message = message.replaceAll("\\[team_color]", team.getTeamColor());
        message = message.replaceAll("\\[team_open]", String.valueOf(team.isOpen()));
        message = message.replaceAll("\\[team_permanent]", String.valueOf(team.isPermanent()));
        message = message.replaceAll("\\[team_creation_date]", String.valueOf(team.getCreationDate()));

        return adaptMessage(message);
    }

    public String adaptTeamMessage(String message, NwITeam nwITeam, Player player) {
        if (message.contains("[team_result_member_line]")) {
            String defaultMemberStringLine = LangManager.getMessage(LangMessage.COMMANDS_TEAM_CHECK_RESULT_MEMBER_LINE);
            List<String> memberStringList = new ArrayList<>();
            nwITeam.getMemberMap().forEach((name, teamMember) -> {
                NwTeamRank playerNwTeamRank = Arrays.stream(NwTeamRank.values()).filter(teamRank -> (teamRank.getWeight() == teamMember.getModel().getRank())).findFirst().get();
                String memberStringLine = defaultMemberStringLine
                        .replaceAll("\\[team_player_connected]", Bukkit.getPlayer(teamMember.getModel().getUsername()) != null ? "&a+" : "&c-")
                        .replaceAll("\\[team_player]", teamMember.getModel().getUsername());
                switch (playerNwTeamRank) {
                    case OWNER:
                        memberStringLine = memberStringLine.replaceAll("\\[team_player_rank]", LangManager.getMessage(LangMessage.TEAM_RANK_OWNER));
                        break;
                    case LIEUTENANT:
                        memberStringLine = memberStringLine.replaceAll("\\[team_player_rank]", LangManager.getMessage(LangMessage.TEAM_RANK_LIEUTENANT));
                        break;
                    case CAPTAIN:
                        memberStringLine = memberStringLine.replaceAll("\\[team_player_rank]", LangManager.getMessage(LangMessage.TEAM_RANK_CAPTAIN));
                        break;
                    case MEMBER:
                        memberStringLine = memberStringLine.replaceAll("\\[team_player_rank]", LangManager.getMessage(LangMessage.TEAM_RANK_MEMBER));
                        break;
                    default:
                        memberStringLine = memberStringLine.replaceAll("\\[team_player_rank]", LangManager.getMessage(LangMessage.TEAM_RANK_RECRUIT));
                }
                memberStringList.add(memberStringLine);
            });
            message = message.replaceAll("\\[team_result_member_line]", String.join("\n", memberStringList));
        }

        if (message.contains("[team_result_relation_line]")) {
            String relationStringLine = LangManager.getMessage(LangMessage.COMMANDS_TEAM_CHECK_RESULT_RELATION_LINE);
            List<String> relationStringList = new ArrayList<>();

            nwITeam.getRelations().forEach((s, teamRelation) -> {
                relationStringList.add(
                        adaptTeamMessage(
                                relationStringLine.replaceAll("\\[team_relation]", teamRelation.getType().getDisplay())
                                , teamRelation.getOrigin() == nwITeam ? teamRelation.getTarget() : teamRelation.getOrigin())
                );
            });
            message = message.replaceAll("\\[team_result_relation_line]", String.join("\n", relationStringList));
        }

        message = message.replaceAll("\\[team]", nwITeam.getName());
        message = message.replaceAll("\\[team_display]", nwITeam.getDisplay());
        message = message.replaceAll("\\[team_color_display]", "{" + nwITeam.getTeamColor() + "}" + nwITeam.getDisplay());
        message = message.replaceAll("\\[team_short]", nwITeam.getShortName());
        message = message.replaceAll("\\[team_color_short]", "{" + nwITeam.getTeamColor() + "}" + nwITeam.getShortName());
        message = message.replaceAll("\\[team_color]", nwITeam.getTeamColor());
        message = message.replaceAll("\\[team_open]", LangManager.getMessage(nwITeam.isOpen() ? LangMessage.TEAM_OPEN : LangMessage.TEAM_CLOSE));
        message = message.replaceAll("\\[team_maximum_members]", ConfigData.getConfigData().team.maximumMembers != -1 ? String.valueOf(ConfigData.getConfigData().team.maximumMembers) : "∞");
        message = message.replaceAll("\\[team_online_member]", nwITeam.getOnlineMemberAmount() + " / " + nwITeam.getMemberAmount());
        message = message.replaceAll("\\[team_relation_default]", ConfigData.getConfigData().team.defaultRelation.toString());

        if (player != null && nwITeam.getMemberMap().containsKey(player)) {
            message = message.replaceAll("\\[team_player_rank]", nwITeam.getMemberMap().get(player).getRank().toString());
        }

        return adaptMessage(message);
    }

    public String adaptTerritoryMessage(String message, Territory territory) {
        message = territory.adaptMessage(message);

        return adaptMessage(message);
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
        if (getVersionNumbers().get(1) >= 16) {
            message = colorFormat(hexPattern, message);
        }
        message = colorFormat(colorPattern, message);

        Matcher territoryMatcher = territoryPattern.matcher(message);

        while (territoryMatcher.find()) {
            long territoryId = Long.parseLong(territoryMatcher.group(2));

            Territory territory = TerritoryManager.getTerritoryManager().getTerritoryMap().values().stream().filter(territory1 -> (territory1.getModel().getId() == territoryId)).findFirst().orElse(null);
            message = message.replace(territoryMatcher.group(), territoryMatcher.group(1) + territoryMatcher.group(3));
            if (territory != null) {
                message = adaptTerritoryMessage(message, territory);
            }
        }


        Matcher teamMatcher = teamPattern.matcher(message);

        while (teamMatcher.find()) {
            long teamId = Long.parseLong(teamMatcher.group(2));

            NwITeam nwTeam = TeamManager.getManager().getStringTeamMap().values().stream().filter(team -> (team.getID() == teamId)).findFirst().orElse(null);
            message = message.replace(teamMatcher.group(), teamMatcher.group(1) + teamMatcher.group(3));
            if (nwTeam != null) {
                message = adaptTeamMessage(message, nwTeam);
            }
        }


        Matcher calculationMatcher = calculatePattern.matcher(message);

        StringBuffer buffer = new StringBuffer();
        while (calculationMatcher.find()) {
            String type = calculationMatcher.group(1);
            String expression = calculationMatcher.group(2);
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
            calculationMatcher.appendReplacement(buffer, replacement);
        }
        calculationMatcher.appendTail(buffer);

        return buffer.toString();
    }

    public String[] listMessage(Player player, List<String> messages) {
        ArrayList<String> newMessages = new ArrayList<>();
        messages.forEach(s -> {
            newMessages.add(adaptMessage(adaptPlayerMessage(player, s)));
        });
        return newMessages.toArray(new String[0]);
    }

    public String colorFormat(Pattern pattern, String message) {
        Matcher matcher = pattern.matcher(message);
        String matched;
        String color;
        while (matcher.find()) {
            try {
                matched = matcher.group(0);
                color = matcher.group(1);
                message = message.replace(matched, String.valueOf(ChatColor.of(color)));
            } catch (Exception e) {
                // e.printStackTrace(); // Not great but conflicts with hour display
            }
        }

        return message;
    }

    public String decimalFormat(float value, char replacement) {
        ConfigData configData = ConfigData.getConfigData();
        int decimal = configData.locale.decNumber;
        return String.format("%." + decimal + "f", value).replaceAll(",", String.valueOf(replacement));
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
            if (print.equals(prints.DEBUG)) {
                getLogger().info("[NW-DEBUG] " + string);
            } else {
                getLogger().info(string);
            }
        }
    }

    /**
     * Calculate from a formatted string
     *
     * @param expression        String, add time with parameters suchs as Xh for x hours (s, m, h, d)
     * @return the calculated duration in ms (Long)
     */
    public static long evalDuration(String expression) {
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

        return totalTimeMs;
    }

    public void alertITeam(NwITeam iTeam, String message, Territory territory, boolean serverWide) {
        if (iTeam == null || message == null) {
            return;
        }

        message = AdaptMessage.getAdaptMessage().adaptTeamMessage(message, iTeam);
        message = adaptTerritoryMessage(message, territory);
        message = AdaptMessage.getAdaptMessage().adaptMessage(message);
        String finalMessage = message;

        if (serverWide) {
            iTeam.getOnlineMemberMap().forEach((player, teamMember) -> {
                player.sendMessage(finalMessage);
            });
        } else {
            Set<Player> teamMemberInTerritory = territory.getNwITeamEffectivePlayerAmountOnTerritory().get(iTeam);
            if (teamMemberInTerritory != null) {
                teamMemberInTerritory.forEach(player -> {
                    player.sendMessage(finalMessage);
                });
            }
        }
    }

    public List<Integer> getVersionNumbers() {
        ArrayList<Integer> versionNumbers = new ArrayList<>();
        Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)");
        String version = Bukkit.getVersion();
        Matcher matcher = pattern.matcher(version);
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                versionNumbers.add(Integer.valueOf(matcher.group(i)));
            }
        }
        return versionNumbers;
    }

    public String getChatColoHexValue(String teamColor) {
        switch (teamColor) {
            case "BLACK":
                return "#000000";
            case "DARK_BLUE":
                return "#0000AA";
            case "DARK_GREEN":
                return "#00AA00";
            case "DARK_AQUA":
                return "#00AAAA";
            case "DARK_RED":
                return "#AA0000";
            case "DARK_PURPLE":
                return "#AA00AA";
            case "GOLD":
                return "#FFAA00";
            case "GRAY":
                return "#AAAAAA";
            case "DARK_GRAY":
                return "#555555";
            case "BLUE":
                return "#5555FF";
            case "GREEN":
                return "#55FF55";
            case "AQUA":
                return "#55FFFF";
            case "RED":
                return "#FF5555";
            case "LIGHT_PURPLE":
                return "#FF55FF";
            case "YELLOW":
                return "#FFFF55";
            case "WHITE":
                return "#FFFFFF";
            default:
                return teamColor;
        }
    }
}
