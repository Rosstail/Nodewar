package fr.rosstail.nodewar.datahandlers;

import fr.rosstail.nodewar.Nodewar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerInfoManager {
    private static PlayerInfoManager playerInfoManager;
    private final Nodewar plugin;
    private final Map<Player, PlayerInfo> playerInfoMap = new HashMap<>();
    private final Timer timer = new Timer();

    public PlayerInfoManager(Nodewar plugin) {
        this.plugin = plugin;
    }

    public static PlayerInfoManager init(final Nodewar plugin) {
        if (playerInfoManager == null) {
            playerInfoManager = new PlayerInfoManager(plugin);
        }
        return playerInfoManager;
    }

    public PlayerInfo getSet(final Player player) {
        if (!playerInfoMap.containsKey(player)) {
            playerInfoMap.put(player, new PlayerInfo(player));
        }
        return playerInfoMap.get(player);
    }

    public PlayerInfo getNoSet(final Player player) {
        if (!playerInfoMap.containsKey(player)) {
            return new PlayerInfo(player);
        }
        return playerInfoMap.get(player);
    }

    public static PlayerInfoManager getPlayerInfoManager() {
        return playerInfoManager;
    }

    public Map<Player, PlayerInfo> getPlayerInfoMap() {
        return playerInfoMap;
    }

    public void startTimer() {
        int delay = 1000 * plugin.getCustomConfig().getInt("general.delay-between-database-updates");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                playerInfoMap.forEach((player, playerInfo) -> {
                    playerInfo.updateAll(true);
                });
            }
        }, delay, delay);
    }

    public Timer getTimer() {
        return timer;
    }

    public void stopTimer() {
        timer.cancel();
    }
}
