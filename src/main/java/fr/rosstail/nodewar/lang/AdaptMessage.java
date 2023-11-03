package fr.rosstail.nodewar.lang;

import fr.rosstail.nodewar.ConfigData;
import fr.rosstail.nodewar.Nodewar;
import fr.rosstail.nodewar.apis.ExpressionCalculator;
import fr.rosstail.nodewar.player.PlayerDataManager;
import fr.rosstail.nodewar.player.PlayerModel;
import fr.rosstail.nodewar.territory.Territory;
import fr.rosstail.nodewar.territory.TerritoryModel;
import fr.rosstail.nodewar.territory.attackrequirements.AttackRequirements;
import fr.rosstail.nodewar.territory.objective.Objective;
import fr.rosstail.nodewar.territory.type.TerritoryType;
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
        if (!player.hasMetadata("NPC")) {
            PlayerModel playerModel = PlayerDataManager.getPlayerDataMap().get(player.getName());
            message = adaptMessageToModel(playerModel, message, playerType);
        } else {
            message = message.replaceAll("\\[karma]", decimalFormat(player.getMetadata("Karma").get(0).asFloat(), '.'));
        }
        if (Objects.equals(playerType, PlayerType.PLAYER.getText())) {
            message = ChatColor.translateAlternateColorCodes('&', setPlaceholderMessage(player, message));
        }
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
        if (Integer.parseInt(Bukkit.getVersion().split("\\.")[1].replaceAll("\\)", "")) >= 16) {
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

    public void printTerritory(Territory territory) {
        TerritoryModel territoryModel = territory.getModel();
        Objective objective = territory.getObjective();
        AttackRequirements attackRequirements = territory.getAttackRequirements();
        String message = territoryModel.getName() + " : " +
                "\n * Display: " + territoryModel.getPrefix() + territoryModel.getDisplay() + territoryModel.getSuffix() +
                "\n * World: " + territoryModel.getWorldName() +
                "\n * Type: " + territoryModel.getTypeName() +
                "\n * Protected: " + territoryModel.isUnderProtection();

        StringBuilder objectiveRequirementsMessage = new StringBuilder("\n * Objective: " +
                "\n   > Type: " + territoryModel.getObjectiveTypeName());

        objectiveRequirementsMessage.append(objective.print());

        message = message + objectiveRequirementsMessage;


        StringBuilder attackRequirementsMessage = new StringBuilder("\n * Attack requirements:" +
                "\n   > lattice types:");
        for (Map.Entry<String, TerritoryType> entry : attackRequirements.getLatticeNetwork().entrySet()) {
            String s = entry.getKey();
            TerritoryType territoryType1 = entry.getValue();
            attackRequirementsMessage.append("\n     - ").append(territoryType1.getName());
        }

        attackRequirementsMessage.append("\n   > types amounts:");
        for (Map.Entry<String, Map<TerritoryType, Integer>> e : attackRequirements.getTerritoryTypeAmountMap().entrySet()) {
            String s1 = e.getKey();
            Map<TerritoryType, Integer> territoryTypeIntegerMap = e.getValue();
            attackRequirementsMessage.append("\n    * ").append(s1).append(":");
            for (Map.Entry<TerritoryType, Integer> entry : territoryTypeIntegerMap.entrySet()) {
                TerritoryType territoryType1 = entry.getKey();
                Integer integer = entry.getValue();
                attackRequirementsMessage.append("\n      - ").append(territoryType1.getName()).append(":").append(integer);
            }
        }

        attackRequirementsMessage.append("\n   > required territories:");
        for (Map.Entry<String, List<Territory>> entry : attackRequirements.getTerritoryListMap().entrySet()) {
            String s = entry.getKey();
            List<Territory> requiredTerritoryList = entry.getValue();
            attackRequirementsMessage.append("\n    * ").append(s).append(":");

            for (Territory requiredterritory : requiredTerritoryList) {
                attackRequirementsMessage.append("\n      - ").append(requiredterritory.getModel().getName());
            }
        }

        message = message + attackRequirementsMessage + "\n------------";
        AdaptMessage.print(message, AdaptMessage.prints.OUT);
    }
}
